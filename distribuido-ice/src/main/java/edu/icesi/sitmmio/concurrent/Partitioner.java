package edu.icesi.sitmmio.concurrent;

import java.util.*;

public final class Partitioner {
    public List<List<Map<String, String>>> split(List<Map<String, String>> rows, int partitions) {
        List<List<Map<String, String>>> result = new ArrayList<>();
        int safe = Math.max(1, partitions);
        int size = rows.size();
        int chunk = (int) Math.ceil(size / (double) safe);
        for (int start = 0; start < size; start += chunk)
            result.add(new ArrayList<>(rows.subList(start, Math.min(size, start + chunk))));
        if (result.isEmpty()) result.add(new ArrayList<>());
        return result;
    }
}
