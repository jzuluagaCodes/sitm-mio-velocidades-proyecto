package edu.icesi.sitmmio.ice;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import edu.icesi.sitmmio.concurrent.Partitioner;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public final class MasterIceCalculator {

    public static final class WorkerAddress {
        private final String host;
        private final int port;

        public WorkerAddress(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String host() { return host; }
        public int port()    { return port; }

        public static WorkerAddress parse(String spec) {
            String[] parts = spec.split(":");
            if (parts.length != 2)
                throw new IllegalArgumentException(
                    "Formato esperado host:puerto, recibido: " + spec);
            return new WorkerAddress(parts[0], Integer.parseInt(parts[1]));
        }
    }

    private final List<WorkerAddress> workers;

    public MasterIceCalculator(List<WorkerAddress> workers) {
        this.workers = new ArrayList<>(workers);
    }

    // CAMBIO: ahora recibe List<String> (líneas crudas) en vez de List<Map<String,String>>
    public Map<RouteMonthKey, AggregationResult> calculate(
            List<String> allRows,
            Set<String> activeRoutes) {

        if (allRows.isEmpty() || workers.isEmpty()) return new TreeMap<>();

        Partitioner partitioner = new Partitioner();
        List<List<String>> partitions = partitioner.split(allRows, workers.size());

        Map<RouteMonthKey, AggregationResult> consolidated = new ConcurrentHashMap<>();
        List<String> routeList = new ArrayList<>(activeRoutes);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        String[] iceArgs = { "--Ice.MessageSizeMax=131072" };

        try (Communicator communicator = Util.initialize(iceArgs)) {
            for (int i = 0; i < partitions.size(); i++) {
                List<String> partition = partitions.get(i);
                if (partition.isEmpty()) continue;

                WorkerAddress addr = workers.get(i % workers.size());
                String proxyStr = "Calculadora:tcp -h " + addr.host() + " -p " + addr.port();
                ObjectPrx base = communicator.stringToProxy(proxyStr);
                sitmmio.CalculadoraPrx proxy = sitmmio.CalculadoraPrx.checkedCast(base);

                if (proxy == null) {
                    System.err.println("[Master] No se pudo conectar al Worker "
                            + addr.host() + ":" + addr.port());
                    continue;
                }

                System.out.println("[Master] Enviando asíncronamente " + partition.size()
                        + " filas al Worker " + addr.host() + ":" + addr.port());

                // CAMBIO: String[] en vez de Map<String,String>[]
                String[] partitionArray = partition.toArray(new String[0]);
                String[] routeArray     = routeList.toArray(new String[0]);

                CompletableFuture<String> iceFuture = proxy.calcularAsync(partitionArray, routeArray);

                CompletableFuture<Void> javaFuture = iceFuture.thenAccept(resultCsv -> {
                    Map<RouteMonthKey, AggregationResult> partial = ResultSerializer.deserialize(resultCsv);
                    synchronized (consolidated) {
                        partial.forEach((k, v) ->
                            consolidated.computeIfAbsent(k, ignored -> new AggregationResult()).merge(v));
                    }
                    System.out.println("[Master] Respuesta procesada de "
                            + addr.host() + ":" + addr.port());
                }).exceptionally(ex -> {
                    System.err.println("[Master] Error en nodo remoto "
                            + addr.host() + ": " + ex.getMessage());
                    return null;
                });

                futures.add(javaFuture);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        return new TreeMap<>(consolidated);
    }
}