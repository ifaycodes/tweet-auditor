package com.auditor.gemini;

import com.auditor.config.AppConfig;
import com.auditor.model.Tweet;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiClientTest {

    @Test
    void shouldBuildPromptContainingCriteriaAndTweetTextAndParseFlaggedTweetFromGeminiResponse() {
        AppConfig config = new AppConfig();
        config.setApiKey("736fg76ef783g");
        config.setUsername("tester");
        config.setCriteria(List.of("contains profanity"));

        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);

        GeminiClient client = new GeminiClient(config, mockHttpClient);

        String id = "";
        String text = "";
        String created = "";
        String url = "";

        Tweet tweet = new Tweet(id, text, created, url);
        tweet.setId("123");
        tweet.setText("this is a bad tweet");
        tweet.setCreatedAt("2024-01-01");
        tweet.setUrl("https://x.com/testuser/status/123");

        String fakeResponse = """
        [{"id": "123", "flagged": true, "reason": "contains profanity"}]
        """;

        //you need to remove the private on the methods buildPrompt and parseResponse for this work
        String prompt = client.buildPrompt(List.of(tweet));
        System.out.println(prompt);

        List<EvaluationResult> results = client.parseResponse(fakeResponse, List.of(tweet));


        assertEquals(1, results.size());
        assertTrue(results.getFirst().isFlagged());
        assertEquals("contains profanity", results.getFirst().getReason());
        assertTrue(prompt.contains("contains profanity"));
        assertTrue(prompt.contains("this is a bad tweet"));
    }

    @Test
    void shouldReturnEvaluationResultsFromEvaluateAll() throws Exception {
        AppConfig config = new AppConfig();
        config.setApiKey("nd98uyg4v874gr");
        config.setUsername("tester");
        config.setCriteria(List.of("contains profanity"));

        // Mock the HttpClient and HttpResponse
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse mockResponse = Mockito.mock(HttpResponse.class);

        String fakeGeminiResponse = """
        {
          "candidates": [{
            "content": {
              "parts": [{"text": "[{\\"id\\": \\"123\\", \\"flagged\\": true, \\"reason\\": \\"contains profanity\\"}]"}]
            }
          }]
        }
        """;

        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        Mockito.when(mockResponse.body()).thenReturn(fakeGeminiResponse);
        Mockito.when(mockHttpClient.send(Mockito.any(), Mockito.any())).thenReturn(mockResponse);

        // Inject the mock HttpClient into GeminiClient
        GeminiClient client = new GeminiClient(config, mockHttpClient);

        String id = "";
        String text = "";
        String created = "";
        String url = "";

        Tweet tweet = new Tweet(id, text, created, url);
        tweet.setId("123");
        tweet.setText("bad tweet");
        tweet.setCreatedAt("2024-01-01");
        tweet.setUrl("https://x.com/testuser/status/123");

        List<EvaluationResult> results = client.evaluateAll(List.of(List.of(tweet)), batchResults -> {});

        assertEquals(1, results.size());
        assertTrue(results.getFirst().isFlagged());
    }
}