package com.auditor.batching;

import java.util.ArrayList;
import java.util.List;

public class BatchBuilder {

    public static <T> List<List<T>> partition(List<T> tweets, int size) {

        List<List<T>> partitions = new ArrayList<>();
        
        for (int i=0; i < tweets.size(); i+=size) {
            partitions.add(tweets.subList(i, Math.min(i + size, tweets.size())));
        }

        return partitions;
    }
}
