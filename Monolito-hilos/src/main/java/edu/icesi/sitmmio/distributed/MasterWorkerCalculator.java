package edu.icesi.sitmmio.distributed;

import edu.icesi.sitmmio.concurrent.Partitioner;
import edu.icesi.sitmmio.core.SpeedAggregator;
import edu.icesi.sitmmio.csv.CsvReader;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MasterWorkerCalculator {
    private final Partitioner partitioner;
    private final SpeedAggregator aggregator;

    public MasterWorkerCalculator() {
        this.partitioner = new Partitioner();
        this.aggregator = new SpeedAggregator();
    }

    public Map<RouteMonthKey, AggregationResult> calculate(Path datagramsPath,
                                                            Set<String> activeRoutes,
                                                            List<WorkerAddress> workers) throws Exception {
        if (workers.isEmpty()) throw new IllegalArgumentException("Debe registrar al menos un worker");
        char separator = CsvReader.detectSeparator(datagramsPath);
        CsvReader reader = new CsvReader(separator);
        List<Map<String, String>> rows = reader.readAll(datagramsPath);
        List<List<Map<String, String>>> partitions = partitioner.split(rows, workers.size());
        List<Thread> threads = new ArrayList<>();
        List<Map<RouteMonthKey, AggregationResult>> partialResults = new ArrayList<>();
        List<Exception> errors = new ArrayList<>();

        for (int i = 0; i < partitions.size(); i++) {
            WorkerAddress worker = workers.get(i % workers.size());
            List<Map<String, String>> partition = partitions.get(i);
            Thread thread = new Thread(() -> {
                try {
                    Map<RouteMonthKey, AggregationResult> result = sendWork(worker, partition, activeRoutes);
                    synchronized (partialResults) {
                        partialResults.add(result);
                    }
                } catch (Exception exception) {
                    synchronized (errors) {
                        errors.add(exception);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) thread.join();
        if (!errors.isEmpty()) throw errors.get(0);
        Map<RouteMonthKey, AggregationResult> finalResult = aggregator.newResultMap();
        for (Map<RouteMonthKey, AggregationResult> partial : partialResults) {
            aggregator.merge(finalResult, partial);
        }
        return finalResult;
    }

    private Map<RouteMonthKey, AggregationResult> sendWork(WorkerAddress worker,
                                                            List<Map<String, String>> rows,
                                                            Set<String> activeRoutes) throws Exception {
        try (Socket socket = new Socket(worker.getHost(), worker.getPort());
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            output.writeObject(new WorkRequest(rows, activeRoutes));
            output.flush();
            Object response = input.readObject();
            if (!(response instanceof WorkResponse)) {
                throw new IllegalStateException("Respuesta inválida del worker");
            }
            WorkResponse workResponse = (WorkResponse) response;
            return workResponse.getPartialResult();
        }
    }
}
