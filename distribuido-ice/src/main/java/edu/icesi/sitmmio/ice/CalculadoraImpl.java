package edu.icesi.sitmmio.ice;

import com.zeroc.Ice.Current;
import edu.icesi.sitmmio.concurrent.AggregationTask;
import edu.icesi.sitmmio.concurrent.Partitioner;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.util.*;
import java.util.concurrent.*;

public final class CalculadoraImpl implements sitmmio.Calculadora {

    private final int threads;

    public CalculadoraImpl(int threads) {
        this.threads = Math.max(1, threads);
    }

    @Override
    public String calcular(Map<String, String>[] partition,
                           String[] activeRoutes,
                           Current current) {

        System.out.println("\n[Worker] ¡Paquete recibido!");

        List<Map<String, String>> rows = Arrays.asList(partition);
        Set<String> routeSet = new HashSet<>(Arrays.asList(activeRoutes));

        // Si solo hay 1 hilo, procesa directo sin overhead de ThreadPool
        if (threads == 1) {
            return processSingle(rows, routeSet);
        }

        // Varios hilos: subdivide la partición recibida
        Partitioner partitioner = new Partitioner();
        List<List<Map<String, String>>> subPartitions = partitioner.split(rows, threads);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Map<RouteMonthKey, AggregationResult>>> futures = new ArrayList<>();

        for (List<Map<String, String>> sub : subPartitions)
            futures.add(pool.submit(new AggregationTask(sub, routeSet)));

        pool.shutdown();

        Map<RouteMonthKey, AggregationResult> consolidated = new TreeMap<>();
        for (Future<Map<RouteMonthKey, AggregationResult>> f : futures) {
            try {
                f.get().forEach((k, v) ->
                    consolidated.computeIfAbsent(k, ignored -> new AggregationResult()).merge(v));
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                System.err.println("[Worker] Error en hilo interno: " + e.getMessage());
            }
        }

        return ResultSerializer.serialize(consolidated);
    }

    private String processSingle(List<Map<String, String>> rows, Set<String> routeSet) {
        edu.icesi.sitmmio.csv.DatagramMapper mapper = new edu.icesi.sitmmio.csv.DatagramMapper();
        edu.icesi.sitmmio.core.SpeedAggregator aggregator = new edu.icesi.sitmmio.core.SpeedAggregator();
        Map<RouteMonthKey, AggregationResult> results = aggregator.newResultMap();
        for (Map<String, String> row : rows)
            mapper.map(row, routeSet).ifPresent(r -> aggregator.add(results, r));
        return ResultSerializer.serialize(results);
    }
}