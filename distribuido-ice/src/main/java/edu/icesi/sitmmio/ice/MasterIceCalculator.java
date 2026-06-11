package edu.icesi.sitmmio.ice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

public final class MasterIceCalculator {

    private static final int CHUNK_SIZE = 500_000;

    public static final class WorkerAddress {
        private final String host;
        private final int port;

        public WorkerAddress(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String host() {
            return host;
        }

        public int port() {
            return port;
        }

        public static WorkerAddress parse(String spec) {
            String[] parts = spec.split(":");

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Formato esperado host:puerto, recibido: " + spec
                );
            }

            return new WorkerAddress(parts[0], Integer.parseInt(parts[1]));
        }
    }

    private static final class WorkerProxy {
        private final WorkerAddress address;
        private final sitmmio.CalculadoraPrx proxy;

        private WorkerProxy(WorkerAddress address, sitmmio.CalculadoraPrx proxy) {
            this.address = address;
            this.proxy = proxy;
        }
    }

    private final List<WorkerAddress> workers;

    public MasterIceCalculator(List<WorkerAddress> workers) {
        this.workers = new ArrayList<>(workers);
    }

    public Map<RouteMonthKey, AggregationResult> calculate(
            List<String> allRows,
            Set<String> activeRoutes
    ) {
        if (allRows == null || allRows.isEmpty() || workers.isEmpty()) {
            return new TreeMap<>();
        }

        Map<RouteMonthKey, AggregationResult> consolidated = new ConcurrentHashMap<>();

        List<String> routeList = new ArrayList<>(activeRoutes);
        String[] routeArray = routeList.toArray(new String[0]);

        List<List<String>> chunks = splitByChunkSize(allRows, CHUNK_SIZE);

        System.out.println("[Master] Total filas a procesar: " + allRows.size());
        System.out.println("[Master] Tamaño de chunk: " + CHUNK_SIZE);
        System.out.println("[Master] Total chunks generados: " + chunks.size());
        System.out.println("[Master] Workers configurados: " + workers.size());

        String[] iceArgs = {
                "--Ice.MessageSizeMax=1048576"
        };

        try (Communicator communicator = Util.initialize(iceArgs)) {
            List<WorkerProxy> availableWorkers = createAvailableWorkerProxies(communicator);

            if (availableWorkers.isEmpty()) {
                System.err.println("[Master] No hay workers disponibles. No se puede distribuir el procesamiento.");
                return new TreeMap<>();
            }

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                List<String> chunk = chunks.get(i);

                if (chunk.isEmpty()) {
                    continue;
                }

                WorkerProxy worker = availableWorkers.get(i % availableWorkers.size());

                System.out.println("[Master] Enviando chunk " + (i + 1) + "/" + chunks.size()
                        + " con " + chunk.size()
                        + " filas al Worker "
                        + worker.address.host() + ":" + worker.address.port());

                String[] partitionArray = chunk.toArray(new String[0]);

                CompletableFuture<String> iceFuture =
                        worker.proxy.calcularAsync(partitionArray, routeArray);

                CompletableFuture<Void> javaFuture = iceFuture
                        .thenAccept(resultCsv -> {
                            Map<RouteMonthKey, AggregationResult> partial =
                                    ResultSerializer.deserialize(resultCsv);

                            synchronized (consolidated) {
                                partial.forEach((key, value) ->
                                        consolidated
                                                .computeIfAbsent(key, ignored -> new AggregationResult())
                                                .merge(value)
                                );
                            }

                            System.out.println("[Master] Respuesta procesada desde Worker "
                                    + worker.address.host() + ":" + worker.address.port()
                                    + " con " + partial.size() + " entradas parciales.");
                        })
                        .exceptionally(ex -> {
                            System.err.println("[Master] Error en nodo remoto "
                                    + worker.address.host() + ":" + worker.address.port()
                                    + ": " + ex.getMessage());
                            return null;
                        });

                futures.add(javaFuture);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        System.out.println("[Master] Entradas consolidadas: " + consolidated.size());

        return new TreeMap<>(consolidated);
    }

    private List<WorkerProxy> createAvailableWorkerProxies(Communicator communicator) {
        List<WorkerProxy> availableWorkers = new ArrayList<>();

        for (WorkerAddress address : workers) {
            try {
                String proxyStr = "Calculadora:tcp -h " + address.host() + " -p " + address.port();

                ObjectPrx base = communicator.stringToProxy(proxyStr);
                sitmmio.CalculadoraPrx proxy = sitmmio.CalculadoraPrx.checkedCast(base);

                if (proxy == null) {
                    System.err.println("[Master] No se pudo conectar al Worker "
                            + address.host() + ":" + address.port());
                    continue;
                }

                availableWorkers.add(new WorkerProxy(address, proxy));

                System.out.println("[Master] Worker disponible: "
                        + address.host() + ":" + address.port());
            } catch (Exception ex) {
                System.err.println("[Master] Worker no disponible "
                        + address.host() + ":" + address.port()
                        + " -> " + ex.getMessage());
            }
        }

        return availableWorkers;
    }

    private List<List<String>> splitByChunkSize(List<String> rows, int chunkSize) {
        List<List<String>> chunks = new ArrayList<>();

        int safeChunkSize = Math.max(1, chunkSize);

        for (int start = 0; start < rows.size(); start += safeChunkSize) {
            int end = Math.min(start + safeChunkSize, rows.size());
            chunks.add(new ArrayList<>(rows.subList(start, end)));
        }

        return chunks;
    }
}