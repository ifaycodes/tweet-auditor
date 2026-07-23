package com.auditor.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.auditor.checkpoint.CheckPointStore;
import com.auditor.gemini.EvaluationResult;

public class CsvWriter {

    private final Path outputPath;
    private final CheckPointStore checkpointStore;

    //constructor
    public CsvWriter(Path outputPath, Path checkpointPath) throws IOException {
        this.outputPath = outputPath;
        this.checkpointStore = new CheckPointStore(checkpointPath);
    }

    //take evaluation results and writes elements to a csv at outputPath, then mark tweet as checked so it doesn't get checked again
    public void writeToCsv(List<EvaluationResult> results) throws IOException {

        boolean isNewFile = !Files.exists(outputPath) || Files.size(outputPath) == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile(), true))) {
            if (isNewFile) {
                writer.write("tweet_url,reason,deleted");
                writer.newLine();
            }

            for (EvaluationResult result : results) {
                if (result.isFlagged()) {
                    writer.write(result.getTweetUrl() + "," + result.getReason() + ",false");
                    writer.newLine();
                }

                //mark tweet as checked so it doesn't get checked again, flagged or not
                checkpointStore.markProcessed(result.getTweetId());
            }
        }
    }
}
