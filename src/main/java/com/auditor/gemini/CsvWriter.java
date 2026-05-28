package com.auditor.gemini;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CsvWriter {

    private final Path outputPath;

    public CsvWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    
    public void writeToCsv(List<EvaluationResult> results) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()));

        writer.write("tweet_url,reason,deleted");
        writer.newLine();
        
        for (EvaluationResult result : results) {
            if (result.isFlagged()) {
                writer.write(result.getTweetUrl() + result.getReason() + ",false");
                writer.newLine();
            }
        }

        writer.close();

    }
}
