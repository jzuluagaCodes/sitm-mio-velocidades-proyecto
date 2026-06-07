package edu.icesi.sitmmio.concurrent;

import edu.icesi.sitmmio.core.SpeedAggregator;
import edu.icesi.sitmmio.csv.CsvReader;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ConcurrentCalculator {
    private final Partitioner partitioner;
    private final SpeedAggregator aggregator;

    public ConcurrentCalculator() {
        this.partitioner = new Partitioner();
        this.aggregator = new SpeedAggregator();
    }

    public Map<RouteMonthKey, AggregationResult> calculate(Path datagramsPath, Set<String> activeRoutes, int threads)
            throws IOException, InterruptedException, ExecutionException {
        char separator = CsvReader.detectSeparator(datagramsPath);
        CsvReader reader = new CsvReader(separator);
        List<Map<String, String>> rows = reader.readAll(datagramsPath);
        int safeThreads = Math.max(1, threads);
        List<List<Map<String, String>>> partitions = partitioner.split(rows, safeThreads);
        ExecutorService executor = Executors.newFixedThreadPool(safeThreads);
        try {
            List<Future<Map<RouteMonthKey, AggregationResult>>> futures = new ArrayList<>();
            for (List<Map<String, String>> partition : partitions) {
                futures.add(executor.submit(new AggregationTask(partition, activeRoutes)));
            }
            Map<RouteMonthKey, AggregationResult> finalResults = aggregator.newResultMap();
            for (Future<Map<RouteMonthKey, AggregationResult>> future : futures) {
                aggregator.merge(finalResults, future.get());
            }
            return finalResults;
        } finally {
            executor.shutdown();
        }
    }
}
