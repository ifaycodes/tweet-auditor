package com.auditor.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.auditor.model.Tweet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TweetParser {

    private ObjectMapper mapper = new ObjectMapper();
    private String username;

    //constructor
    public TweetParser(String username) {
        this.username = username;
    }

    public List<Tweet> parse(Path tweetsJsPath) throws IOException {
        String raw = Files.readString(tweetsJsPath);

        int jsonstart = raw.indexOf('[');

        if (jsonstart == -1) {
            throw new IllegalArgumentException("No JSON array found in twwets.js - unexpected file format");
        }

        String json = raw.substring(jsonstart);

        JsonNode root = mapper.readTree(json);
        if (!root.isArray()) {
            throw new IllegalArgumentException("Expected a JSON array at the root of tweets.js");
        }

        List<Tweet> tweets = new ArrayList<>();

        for (JsonNode element : root) {
            JsonNode tweetNode = element.get("tweet");

            if (tweetNode == null) {
                continue;
            }

            String idStr = tweetNode.get("id_str").asText();
            String fullText = tweetNode.get("full_text").asText();
            String createdAt = tweetNode.get("created_at").asText();

            if (fullText.startsWith("RT @")) {
                continue;
            }

            String tweetUrl = "https://x.com/" + username + "/status/" + idStr;

            tweets.add(new Tweet(idStr, fullText, createdAt, tweetUrl));
        }

        return tweets;
    }
}
