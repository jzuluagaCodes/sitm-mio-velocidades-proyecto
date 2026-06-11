package edu.icesi.sitmmio.ice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.zeroc.Ice.Current;

import edu.icesi.sitmmio.concurrent.AggregationTask;
import edu.icesi.sitmmio.concurrent.Partitioner;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

public final class CalculadoraImpl implements sitmmio.Calculadora {

    private final int threads;

    public CalculadoraImpl(int threads) {
        this.threads = Math.max(1, threads);
    }

    @Override
    public String calcular(String[] partition, String[] activeRoutes, Current current) {
        System.out.println("\n[Worker] Paquete recibido con " + partition.length + " filas.");

        List<String> rows = Arrays.asList(partition);
        Set<String> routeSet = new HashSet<>(Arrays.asList(activeRoutes));

        if (threads == 1 || rows.size() <= 1) {
            Map<RouteMonthKey, AggregationResult> results =
                    new AggregationTask(rows, routeSet).call();

            System.out.println("[Worker] Resultados parciales generados: " + results.size());
            return ResultSerializer.serialize(results);
        }

        Partitioner partitioner = new Partitioner();
        List<List<String>> subPartitions = partitioner.split(rows, threads);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Map<RouteMonthKey, AggregationResult>>> futures = new ArrayList<>();

        for (List<String> subPartition : subPartitions) {
            if (!subPartition.isEmpty()) {
                futures.add(pool.submit(new AggregationTask(subPartition, routeSet)));
            }
        }

        pool.shutdown();

        Map<RouteMonthKey, AggregationResult> consolidated = new TreeMap<>();

        for (Future<Map<RouteMonthKey, AggregationResult>> future : futures) {
            try {
                Map<RouteMonthKey, AggregationResult> partial = future.get();

                partial.forEach((key, value) ->
                        consolidated
                                .computeIfAbsent(key, ignored -> new AggregationResult())
                                .merge(value)
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[Worker] Procesamiento interrumpido: " + e.getMessage());
            } catch (ExecutionException e) {
                System.err.println("[Worker] Error en hilo interno: " + e.getMessage());
            }
        }

        System.out.println("[Worker] Resultados parciales generados: " + consolidated.size());

        return ResultSerializer.serialize(consolidated);
    }
}