package com.auditor.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.auditor.checkpoint.CheckPointStore;
import com.auditor.gemini.EvaluationResult;

public class CsvWriter {

    private final Path outputPath;
    private final Path checkpointPath;

    //constructor
    public CsvWriter(Path outputPath, Path checkpointPath) {
        this.outputPath = outputPath;
        this.checkpointPath = checkpointPath;
    }

    //take evaluation results and writes elements to a csv at outputpath, then mark tweet as checked so it doesn't get checked again
    public void writeToCsv(List<EvaluationResult> results) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()));

        writer.write("tweet_url,reason,deleted");
        writer.newLine();
        
        for (EvaluationResult result : results) {
            if (result.isFlagged()) {
                writer.write(result.getTweetUrl() + "," + result.getReason() + ",false");
                writer.newLine();

                CheckPointStore checkPointStore = new CheckPointStore(checkpointPath);
                //mark tweet as checked so it doesn't get checked again
                checkPointStore.markProcessed(result.getTweetId());
            }
        }

        writer.close();

    }
}
