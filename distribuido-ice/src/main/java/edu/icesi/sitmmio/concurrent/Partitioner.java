package edu.icesi.sitmmio.concurrent;

import java.util.ArrayList;
import java.util.List;

public final class Partitioner {

    public <T> List<List<T>> split(List<T> rows, int partitions) {
        List<List<T>> result = new ArrayList<>();

        int safePartitions = Math.max(1, partitions);
        int size = rows.size();
        int chunkSize = (int) Math.ceil(size / (double) safePartitions);

        for (int start = 0; start < size; start += chunkSize) {
            result.add(new ArrayList<>(rows.subList(start, Math.min(size, start + chunkSize))));
        }

        if (result.isEmpty()) {
            result.add(new ArrayList<>());
        }

        return result;
    }
}