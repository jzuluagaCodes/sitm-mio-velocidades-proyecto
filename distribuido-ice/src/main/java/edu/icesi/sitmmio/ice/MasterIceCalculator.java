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

/**
 * Orquestador Master del patrón Master-Worker con ICE.
 *
 * Responsabilidades:
 *  1. Crear proxies ICE hacia cada Worker registrado.
 *  2. Particionar el dataset en tantas partes como Workers haya.
 *  3. Enviar cada partición al Worker correspondiente (llamada síncrona ICE).
 *  4. Consolidar todos los resultados parciales en un único mapa final.
 *
 * Corresponde al componente "CalculadoraPrx + ParticionadorCSV" del
 * dispositivo de procesamiento central en el diagrama de deployment.
 */
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

    /**
     * Distribuye el dataset entre los Workers, recopila resultados parciales
     * y los consolida en un único mapa ordenado.
     *
     * @param allRows      filas completas del CSV de datagramas
     * @param activeRoutes rutas activas del piloto
     * @return mapa consolidado de velocidades promedio por ruta y mes
     */

    public Map<RouteMonthKey, AggregationResult> calculate(List<Map<String, String>> allRows, Set<String> activeRoutes) {
        if (allRows.isEmpty() || workers.isEmpty()) {
            return new TreeMap<>();
        }

        // 1. Particionar el dataset entre la cantidad de Workers
        Partitioner partitioner = new Partitioner();
        List<List<Map<String, String>>> partitions = partitioner.split(allRows, workers.size());

        // 2. Mapa Concurrente para consolidación (Garantiza CORRECTITUD en llamadas paralelas)
        Map<RouteMonthKey, AggregationResult> consolidated = new ConcurrentHashMap<>();
        List<String> routeList = new ArrayList<>(activeRoutes);

        // Lista para monitorear los futuros de Java
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        String[] iceArgs = { "--Ice.MessageSizeMax=131072" }; // 128 MB máximo por mensaje

        try (Communicator communicator = Util.initialize(iceArgs)) {
            for (int i = 0; i < partitions.size(); i++) {
                List<Map<String, String>> partition = partitions.get(i);
                if (partition.isEmpty()) continue;

                WorkerAddress addr = workers.get(i % workers.size());
                String proxyStr = "Calculadora:tcp -h " + addr.host() + " -p " + addr.port();
                ObjectPrx base = communicator.stringToProxy(proxyStr);
                
                // Conversión directa al Proxy generado por Slice
                sitmmio.CalculadoraPrx proxy = sitmmio.CalculadoraPrx.checkedCast(base);

                if (proxy == null) {
                    System.err.println("[Master] No se pudo conectar al Worker " + addr.host() + ":" + addr.port());
                    continue;
                }

                System.out.println("[Master] Enviando asíncronamente " + partition.size() 
                        + " filas al Worker " + addr.host() + ":" + addr.port());

                @SuppressWarnings("unchecked")
                Map<String, String>[] partitionArray = partition.toArray(new Map[0]);
                String[] routeArray = routeList.toArray(new String[0]);

                // 🔥 CAMBIO CRÍTICO: LLAMADA ASÍNCRONA (AMI)
                // No bloquea el hilo principal; el ciclo 'for' continúa de inmediato enviando datos a los demás workers.
                com.zeroc.Ice.CompletableFuture<String> iceFuture = proxy.calcularAsync(partitionArray, routeArray);

                // Configurar la acción (Callback) para cuando el Worker termine su cómputo en la red
                CompletableFuture<Void> javaFuture = iceFuture.whenComplete((resultCsv, ex) -> {
                    if (ex == null) {
                        Map<RouteMonthKey, AggregationResult> partial = ResultSerializer.deserialize(resultCsv);
                        
                        // Bloque sincronizado para evitar condiciones de carrera (Race Conditions) en el mapa global
                        synchronized (consolidated) {
                            partial.forEach((k, v) ->
                                    consolidated.computeIfAbsent(k, ignored -> new AggregationResult()).merge(v));
                        }
                        System.out.println("[Master] Respuesta procesada de " + addr.host() + ":" + addr.port());
                    } else {
                        System.err.println("[Master] Error en nodo remoto " + addr.host() + ": " + ex.getMessage());
                    }
                });

                futures.add(javaFuture);
            }

            // 🛑 BARRERA GLOBAL: Esperamos a que TODOS los Workers terminen de procesar en la red del laboratorio
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // Retornamos un TreeMap ordenado para que el escritor de archivos guarde el reporte final alfabéticamente
        return new TreeMap<>(consolidated);
    }
}
