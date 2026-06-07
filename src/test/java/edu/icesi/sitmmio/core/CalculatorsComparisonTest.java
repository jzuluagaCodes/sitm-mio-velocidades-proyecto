package edu.icesi.sitmmio.core;

import edu.icesi.sitmmio.concurrent.ConcurrentCalculator;
import edu.icesi.sitmmio.csv.ActiveRoutesReader;
import edu.icesi.sitmmio.distributed.MasterWorkerCalculator;
import edu.icesi.sitmmio.distributed.WorkerAddress;
import edu.icesi.sitmmio.distributed.WorkerServer;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public final class CalculatorsComparisonTest {

    private static final Path LINES_PATH = Path.of("data/sample/lines-241-ActiveGT.csv");
    private static final Path DATAGRAMS_PATH = Path.of("data/sample/datagrams-MiniPilot.csv");
    private static int workerPort;
    private static Thread workerThread;
    private static WorkerServer workerServer;

    @BeforeAll
    public static void startLocalWorker() throws Exception {
        // Encontrar un puerto libre dinámicamente
        try (ServerSocket socket = new ServerSocket(0)) {
            workerPort = socket.getLocalPort();
        }
        workerServer = new WorkerServer(workerPort);
        workerThread = new Thread(() -> {
            try {
                workerServer.start();
            } catch (Exception ignored) {
                // Se detiene al cerrar socket
            }
        });
        workerThread.setDaemon(true);
        workerThread.start();
        // Esperar un momento a que el servidor del worker inicie
        Thread.sleep(200);
    }

    @Test
    public void testCalculatorsProduceIdenticalResults() throws Exception {
        ActiveRoutesReader routesReader = new ActiveRoutesReader();
        Set<String> activeRoutes = routesReader.readActiveRoutes(LINES_PATH);

        // 1. Monolito simple
        MonolithicCalculator monolithic = new MonolithicCalculator();
        Map<RouteMonthKey, AggregationResult> monoResults = monolithic.calculate(DATAGRAMS_PATH, activeRoutes);

        // 2. Monolito concurrente (4 hilos)
        ConcurrentCalculator concurrent = new ConcurrentCalculator();
        Map<RouteMonthKey, AggregationResult> threadResults = concurrent.calculate(DATAGRAMS_PATH, activeRoutes, 4);

        // 3. Distribuido Master-Worker
        MasterWorkerCalculator distributed = new MasterWorkerCalculator();
        List<WorkerAddress> workers = List.of(new WorkerAddress("localhost", workerPort));
        Map<RouteMonthKey, AggregationResult> distResults = distributed.calculate(DATAGRAMS_PATH, activeRoutes, workers);

        // Validaciones
        assertEquals(monoResults.size(), threadResults.size(), "Tamaño monolito vs hilos difiere");
        assertEquals(monoResults.size(), distResults.size(), "Tamaño monolito vs distribuido difiere");

        for (RouteMonthKey key : monoResults.keySet()) {
            AggregationResult monoRes = monoResults.get(key);
            AggregationResult threadRes = threadResults.get(key);
            AggregationResult distRes = distResults.get(key);

            assertNotNull(threadRes, "Clave no encontrada en hilos: " + key.getRouteId() + " " + key.getYearMonth());
            assertNotNull(distRes, "Clave no encontrada en distribuido: " + key.getRouteId() + " " + key.getYearMonth());

            assertEquals(monoRes.getCount(), threadRes.getCount(), "Cantidad de registros difiere en hilos para " + key.getRouteId());
            assertEquals(monoRes.getCount(), distRes.getCount(), "Cantidad de registros difiere en distribuido para " + key.getRouteId());

            assertEquals(monoRes.getAverageSpeed(), threadRes.getAverageSpeed(), 0.000001, "Velocidad promedio difiere en hilos");
            assertEquals(monoRes.getAverageSpeed(), distRes.getAverageSpeed(), 0.000001, "Velocidad promedio difiere en distribuido");
        }
    }
}
