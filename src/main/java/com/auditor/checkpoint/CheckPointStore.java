package com.auditor.checkpoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class CheckPointStore {
    private final Path checkpointPath;
    private final Set<String> processedIds;

    //constructor
    public CheckPointStore(Path checkpointPath) throws IOException {
        this.checkpointPath = checkpointPath;
        this.processedIds = new HashSet<>();

        if (Files.exists(checkpointPath)) {
            processedIds.addAll(Files.readAllLines(checkpointPath));
        }
    }

    //to check if tweet is already processed
    public boolean isProcessed(String id) {
        return processedIds.contains(id);
    }

    //to mark tweet as processed
    public void markProcessed(String id) throws IOException {
        processedIds.add(id);
        Files.writeString(checkpointPath, id + "\n",
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}