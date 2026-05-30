package com.auditor.output;

import org.junit.jupiter.api.Test;

import com.auditor.gemini.EvaluationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvWriterTest {

    @Test
    void shouldCreateCsvWithHeader() throws IOException {
        Path tempCsv = Files.createTempFile("flagged", ".csv");
        Path tempCp = Files.createTempFile("cp", ".csv");
        CsvWriter writer = new CsvWriter(tempCsv, tempCp);
        writer.writeToCsv(List.of());

        List<String> lines = Files.readAllLines(tempCsv);
        assertEquals("tweet_url,reason,deleted", lines.get(0));
    }

    @Test
    void shouldWriteFlaggedTweetsOnly() throws IOException {
        Path tempCsv = Files.createTempFile("flagged", ".csv");
        Path tempCp = Files.createTempFile("cp", ".csv");
        CsvWriter writer = new CsvWriter(tempCsv, tempCp);

        List<EvaluationResult> results = List.of(
                new EvaluationResult("1", true, "offensive", "https://x.com/user/status/1"),
                new EvaluationResult("2", false, "none", "https://x.com/user/status/2")
        );

        writer.writeToCsv(results);

        List<String> lines = Files.readAllLines(tempCsv);
        assertEquals(2, lines.size()); // header + 1 flagged
        assertEquals("https://x.com/user/status/1,offensive,false", lines.get(1));
    }
}