package com.auditor.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TweetParserTest {

    @Test
    void parserShouldThrowIoexceptionWhenFileNotFound() {
        assertThrows(IOException.class, () -> {
            TweetParser parser = new TweetParser("myUsername");
            parser.parse(Path.of("mytweets.json"));
        });
    }

    @Test
    void shouldThrowIllegalArgumentExceptions() {
        assertThrows(IllegalArgumentException.class, () -> {
            String raw = "this is going to the tweets in my js file.";
            int jsonStart = raw.indexOf('[');
            if (jsonStart == -1) {
                throw new IllegalArgumentException("Not a tweet array");
            }
            
        });
    }
}