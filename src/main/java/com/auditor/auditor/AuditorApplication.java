package com.auditor.auditor;

import java.nio.file.Path;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.auditor.batching.BatchBuilder;
import com.auditor.checkpoint.CheckPointStore;
import com.auditor.config.AppConfig;
import com.auditor.gemini.EvaluationResult;
import com.auditor.gemini.GeminiClient;
import com.auditor.model.Tweet;
import com.auditor.output.CsvWriter;
import com.auditor.parser.TweetParser;

@SpringBootApplication
public class AuditorApplication {

	public static void main(String[] args)  throws Exception {
		SpringApplication.run(AuditorApplication.class, args);

		//load config file
		AppConfig config = AppConfig.loadConfig(Path.of("config.json"));

		System.out.println("Config loaded");


		//parse tweets from archive
		TweetParser parser = new TweetParser(config.getUsername());
		List<Tweet> tweets = parser.parse(Path.of("tweets.js")).subList(0, 100);

		System.out.println("Created " + tweets.size() + " tweets.");


		//filter out already processed tweets by id
		Path checkpointPath = Path.of("checkpoint.txt");
		CheckPointStore checkpoint = new CheckPointStore(checkpointPath);

		tweets.stream().filter(t -> !checkpoint.isProcessed(t.getId())).toList();


		//batch tweets into chunks of 50
		List<List<Tweet>> batches = BatchBuilder.partition(tweets, 25);

		System.out.println("Created " + batches.size() + " batches.");


		//send batches to gemini for evaluation
		GeminiClient geminiClient = new GeminiClient(config);
		List<EvaluationResult> results = geminiClient.evaluateAll(batches);

		System.out.println("Evaluation complete. " + results.size() + " results returned.");


		//write flagged tweets to csv
		CsvWriter csvWriter = new CsvWriter(Path.of("flaggedtweets.csv"), checkpointPath);
		csvWriter.writeToCsv(results);
		long flaggedCount = results.stream().filter(EvaluationResult::isFlagged).count();

		System.out.println("Done. " + flaggedCount + " tweets flagged and written to flaggedtweets.csv.");
	}

}
