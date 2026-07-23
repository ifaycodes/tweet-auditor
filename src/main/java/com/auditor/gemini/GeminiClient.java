package com.auditor.gemini;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.auditor.config.AppConfig;
import com.auditor.errors.DailyQuotaExceededException;
import com.auditor.errors.RateLimitException;
import com.auditor.model.Tweet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class GeminiClient {

  //using semaphore to rate limit across threads
  private static final Semaphore RATE_LIMITER = new Semaphore(1);
  private static final ScheduledExecutorService RATE_LIMITER_SCHEDULER = Executors.newSingleThreadScheduledExecutor();

  private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";

  //variables
  private final String apiKey;
  private final List<String> criteria;
  private final HttpClient httpClient;
  private final ObjectMapper mapper;
  private final ExecutorService threadPool;
  private static final int MAX_RETRIES = 3;
  private static final long RETRY_DELAY_MS = 2000;

  //constructor
  public GeminiClient(AppConfig config) {
    this.apiKey = config.getApiKey();
    this.criteria = config.getCriteria();
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    this.mapper = new ObjectMapper();
    this.threadPool = Executors.newFixedThreadPool(5);
  }

  //constructor for mock
  public GeminiClient(AppConfig config, HttpClient httpClient) {
      this.apiKey = config.getApiKey();
      this.criteria = config.getCriteria();
      this.httpClient = httpClient; // uses the one you pass in
      this.mapper = new ObjectMapper();
      this.threadPool = Executors.newFixedThreadPool(5);
  }

  //functional interface so callers can persist each batch's results as soon as it completes
  @FunctionalInterface
  public interface BatchResultHandler {
    void onBatchComplete(List<EvaluationResult> batchResults) throws Exception;
  }

  //take all batches and calls evaluateBatch, passing one batch after another.
  //invokes onBatchComplete as soon as each batch's results are ready, so progress isn't lost if a later batch fails or the run is interrupted
  public List<EvaluationResult> evaluateAll(List<List<Tweet>> batches, BatchResultHandler onBatchComplete) {
        List<Future<List<EvaluationResult>>> futures = new ArrayList<>();

        for (List<Tweet> batch : batches) {
            Future<List<EvaluationResult>> future = threadPool.submit(() -> evaluateBatch(batch));
            futures.add(future);
        }

        List<EvaluationResult> allResults = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                List<EvaluationResult> batchResults = futures.get(i).get(); // blocks until that batch is done
                allResults.addAll(batchResults);
                onBatchComplete.onBatchComplete(batchResults);
            } catch (Exception e) {
                System.err.println("Batch failed: " + e.getMessage());

                if (e.getCause() instanceof DailyQuotaExceededException) {
                    int remaining = futures.size() - i - 1;
                    System.err.println("Daily quota exceeded — stopping run, skipping " + remaining + " remaining batch(es).");
                    for (int j = i + 1; j < futures.size(); j++) {
                        futures.get(j).cancel(false);
                    }
                    break;
                }
            }
        }

        threadPool.shutdownNow();
        return allResults;
    }

  //evaluate by batch
  private List<EvaluationResult> evaluateBatch(List<Tweet> batch) throws Exception {
    String prompt = buildPrompt(batch);
    String responseText = callGeminiWithRetry(prompt);
    return parseResponse(responseText, batch);
  }

  //build prompt with criteria and batch of tweet list and ask for json response back
  private String buildPrompt(List<Tweet> batch) {
    StringBuilder promptStringBuilder = new StringBuilder();

    promptStringBuilder.append("You are reviewing tweets to flag ones that violate the following criteria:\n");
    for (int i=0; i < criteria.size(); i++) {
      promptStringBuilder.append((i+1)).append(". ").append(criteria.get(i)).append("\n");
    }

    promptStringBuilder.append("\nEvaluate the tweet below and respond ONLY with a JSON array. \nNo explanation or markdown, just a JSON array. ");
    promptStringBuilder.append("Format:\n");
    promptStringBuilder.append("[{\"id\": \"tweet_id\", \"flagged\": true, \"reason\": \"reason here\"}, ...]\n\n");
    promptStringBuilder.append("Tweets:\n");

    for (Tweet tweet : batch) {
      promptStringBuilder.append("ID: ").append(tweet.getId());
      promptStringBuilder.append("\nText: ").append(tweet.getText()).append("\n");
    }

    return promptStringBuilder.toString();
  }


  //set rate limit for how calling gemini
  private String callGeminiWithRetry(String prompt) throws Exception {
    int attempts = 0;

    while (attempts < MAX_RETRIES) {
      //block until there is a slot
      RATE_LIMITER.acquire();
      try {
          return callGemini(prompt);

      } catch (DailyQuotaExceededException e) {
        //retrying within seconds can't help a daily cap, so give up on this batch right away
        throw e;

      } catch (RateLimitException e) {
        attempts++;
        System.err.printf("Rate limited, retrying in " + RETRY_DELAY_MS + "ms (attempt %d)", attempts);

        Thread.sleep(RETRY_DELAY_MS * attempts); // to keep off longer with each retry

      } catch (Exception e) {
        attempts++;
        System.err.printf("Request failed: " + e.getMessage() + " (attempt %d)", attempts);

        if (attempts >= MAX_RETRIES) throw e;
        Thread.sleep(RETRY_DELAY_MS);
      } finally {
        // release after 1 second to maintain Gemini 60 requests/min intended rate limit,
        // regardless of whether the call succeeded or failed
        RATE_LIMITER_SCHEDULER.schedule(() -> RATE_LIMITER.release(), 1100, TimeUnit.MILLISECONDS);
      }
    }
    
    throw new RuntimeException("Max retries exceeded");
  }

  //Http request to gemini with request build that gemini would expect
  private String callGemini(String prompt) throws IOException, InterruptedException {
    
    String requestBody = """
        {
          "contents": [{
            "parts": [{"text": "%s"}]
          }]
        }
        """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL + apiKey))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 429) {
          String body = response.body();
          //Gemini's daily-quota exceptions include a quotaId like "...PerDayPerProjectPerModel..."
          //a per-minute throttle does not, so this substring is how we tell the two apart
          if (body != null && body.toLowerCase().contains("perday")) {
            throw new DailyQuotaExceededException("Gemini daily quota exceeded: " + extractErrorSummary(body));
          }
          throw new RateLimitException("Gemini rate limit hit");
        }
        if (response.statusCode() != 200) {
          throw new RuntimeException("Gemini error: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        return root
            .path("candidates").get(0)
            .path("content")
            .path("parts").get(0)
            .path("text")
            .asText();
  }

  //pulls just status + message out of a Gemini error body instead of dumping the whole JSON
  private String extractErrorSummary(String body) {
    try {
      JsonNode error = mapper.readTree(body).path("error");
      return "[" + error.path("status").asText() + "] " + error.path("message").asText();
    } catch (Exception e) {
      return body;
    }
  }

  //adding gemini json response into EvaluationResult object
  private List<EvaluationResult> parseResponse(String responseText, List<Tweet> batch) {
        List<EvaluationResult> results = new ArrayList<>();
        Map<String, String> urlById = new HashMap<>();
        for (Tweet tweet : batch) {
          urlById.put(tweet.getId(), tweet.getUrl());
        }
        
        try {
            // Strip all mark down code fences if Gemini ignored our instructions
            String cleanText = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim();
 
            JsonNode array = mapper.readTree(cleanText);
            for (JsonNode node : array) {
                String id = node.get("id").asText();
                boolean flagged = node.get("flagged").asBoolean();
                String reason = node.has("reason")
                    ? node.get("reason").asText() 
                    : "none";
                String tweetUrl = urlById.get(id);
                results.add(new EvaluationResult(id, flagged, reason, tweetUrl));
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response: " + e.getMessage());
            System.err.println("Raw response: " + responseText);
        }
 
        return results;
    }


}