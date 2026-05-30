package com.auditor.batching;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchBuilderTest {

    @Test
    void shouldPartitionIntoCorrectBatchSize() {
        List<String> items = List.of("a", "b", "c", "d");
        List<List<String>> batches = BatchBuilder.partition(items, 2);

        assertEquals(2, batches.size());
        assertEquals(2, batches.get(0).size());
        assertEquals(2, batches.get(1).size());
    }

    @Test
    void shouldReturnEmptyListIfInputIsEmpty() {
        List<List<String>> batches = BatchBuilder.partition(List.of(), 10);
        assertTrue(batches.isEmpty());
    }

    @Test
    void shouldReturnOneBatchWhenListSmallerThanBatchSize() {
        List<String> items = List.of("a", "b");
        List<List<String>> batches = BatchBuilder.partition(items, 10);

        assertEquals(1, batches.size());
        assertEquals(2, batches.get(0).size());
    }
}