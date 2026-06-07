package edu.icesi.sitmmio.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Partitioner {
    public List<List<Map<String, String>>> split(List<Map<String, String>> rows, int partitions) {
        List<List<Map<String, String>>> result = new ArrayList<>();
        int safePartitions = Math.max(1, partitions);
        int size = rows.size();
        int chunkSize = (int) Math.ceil(size / (double) safePartitions);
        for (int start = 0; start < size; start += chunkSize) {
            int end = Math.min(size, start + chunkSize);
            result.add(new ArrayList<>(rows.subList(start, end)));
        }
        if (result.isEmpty()) result.add(new ArrayList<>());
        return result;
    }
}
