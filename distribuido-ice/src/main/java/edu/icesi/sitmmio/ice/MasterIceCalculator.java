package edu.icesi.sitmmio.ice;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import edu.icesi.sitmmio.concurrent.Partitioner;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.util.*;

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
        return new WorkerAddress(parts[0].trim(), Integer.parseInt(parts[1].trim()));
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}

    private final List<WorkerAddress> workers;

    public MasterIceCalculator(List<WorkerAddress> workers) {
        if (workers == null || workers.isEmpty())
            throw new IllegalArgumentException("Debe haber al menos un Worker configurado.");
        this.workers = List.copyOf(workers);
    }

    /**
     * Distribuye el dataset entre los Workers, recopila resultados parciales
     * y los consolida en un único mapa ordenado.
     *
     * @param allRows      filas completas del CSV de datagramas
     * @param activeRoutes rutas activas del piloto
     * @return mapa consolidado de velocidades promedio por ruta y mes
     */
    public Map<RouteMonthKey, AggregationResult> calculate(
            List<Map<String, String>> allRows,
            Set<String> activeRoutes) {

        Partitioner partitioner = new Partitioner();
        List<List<Map<String, String>>> partitions = partitioner.split(allRows, workers.size());
        List<String> routeList = new ArrayList<>(activeRoutes);

        String[] iceArgs = { "--Ice.MessageSizeMax=131072" };

        Map<RouteMonthKey, AggregationResult> consolidated = new TreeMap<>();

        try (Communicator communicator = Util.initialize(iceArgs)) {
            for (int i = 0; i < workers.size(); i++) {
                WorkerAddress addr   = workers.get(i);
                List<Map<String, String>> partition = i < partitions.size()
                        ? partitions.get(i)
                        : Collections.emptyList();

                String proxyStr = "Calculadora:tcp -h " + addr.host() + " -p " + addr.port();
                ObjectPrx base  = communicator.stringToProxy(proxyStr);

                // En ICE 3.7 Java no existe PrxHelper — el cast es directo
                sitmmio.CalculadoraPrx proxy = sitmmio.CalculadoraPrx.checkedCast(base);

                if (proxy == null) {
                    System.err.println("[Master] No se pudo conectar al Worker "
                            + addr.host() + ":" + addr.port());
                    continue;
                }

                System.out.println("[Master] Enviando " + partition.size()
                        + " filas al Worker " + addr.host() + ":" + addr.port());

                // ICE espera arrays, no List — convertimos
                @SuppressWarnings("unchecked")
                Map<String, String>[] partitionArray = partition.toArray(new Map[0]);
                String[] routeArray = routeList.toArray(new String[0]);

                String resultCsv = proxy.calcular(partitionArray, routeArray);

                Map<RouteMonthKey, AggregationResult> partial = ResultSerializer.deserialize(resultCsv);
                partial.forEach((k, v) ->
                        consolidated.computeIfAbsent(k, ignored -> new AggregationResult()).merge(v));

                System.out.println("[Master] Worker " + addr.host() + ":" + addr.port()
                        + " devolvió " + partial.size() + " entradas.");
            }
        }

        return consolidated;
    }
}
