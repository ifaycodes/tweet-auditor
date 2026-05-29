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

    private final ObjectMapper mapper = new ObjectMapper();
    private final String username;

    //constructor
    public TweetParser(String username) {
        this.username = username;
    }

    //parsing tweets and retaining only id, text, date created and url
    public List<Tweet> parse(Path tweetsJsPath) throws IOException {
        //reads the entire file as one string
        String raw = Files.readString(tweetsJsPath);

        int jsonStart = raw.indexOf('[');

        if (jsonStart == -1) {
            throw new IllegalArgumentException("No JSON array found in tweets.js - unexpected file format");
        }

        //cut off everything before index"[", not inclusive, pass whatever is let to json
        String json = raw.substring(jsonStart);

        JsonNode root = mapper.readTree(json);
        if (!root.isArray()) {
            throw new IllegalArgumentException("Expected a JSON array at the root of tweets.js");
        }

        List<Tweet> tweets = new ArrayList<>();

        //iterate through every node element on the tree made from 'json'
        for (JsonNode element : root) {
            JsonNode tweetNode = element.get("tweet");

            if (tweetNode == null) {
                continue;
            }

            String idStr = tweetNode.get("id_str").asText();
            String fullText = tweetNode.get("full_text").asText();
            String createdAt = tweetNode.get("created_at").asText();

            //check if it's a retweet and drop it if yes
            if (fullText.startsWith("RT @")) {
                continue;
            }

            String tweetUrl = "https://x.com/" + username + "/status/" + idStr;

            tweets.add(new Tweet(idStr, fullText, createdAt, tweetUrl));
        }

        return tweets;
    }
}
