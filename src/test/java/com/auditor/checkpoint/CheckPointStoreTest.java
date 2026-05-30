package com.auditor.checkpoint;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CheckPointStoreTest {

    @Test
    void shouldReturnFalseWhenIdNotProcessed() throws IOException {
        Path tempFile = Files.createTempFile("checkpoint", ".txt");
        Files.delete(tempFile); // make sure it doesn't exist

        CheckPointStore checkpoint = new CheckPointStore(tempFile);
        assertFalse(checkpoint.isProcessed("123"));
    }

    @Test
    void shouldReturnTrueForProcessedId() throws IOException {
        Path tempFile = Files.createTempFile("checkpoint", ".txt");

        CheckPointStore checkpoint = new CheckPointStore(tempFile);
        checkpoint.markProcessed("1");
        checkpoint.markProcessed("2");

        assertTrue(checkpoint.isProcessed("1"));
    }
    @Test
    void shouldLoadExistingCheckpointFromDisk() throws IOException {
        Path tempFile = Files.createTempFile("checkpoint", ".txt");

        CheckPointStore first = new CheckPointStore(tempFile);
        first.markProcessed("123");

        // Simulating a new run by creating a fresh instance on the same file
        CheckPointStore second = new CheckPointStore(tempFile);
        assertTrue(second.isProcessed("123"));
    }
}