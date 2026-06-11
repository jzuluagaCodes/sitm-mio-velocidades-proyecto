package edu.icesi.sitmmio.core;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import edu.icesi.sitmmio.concurrent.ConcurrentCalculator;
import edu.icesi.sitmmio.csv.ActiveRoutesReader;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

public final class CalculatorsComparisonTest {

    private static final Path LINES_PATH = Path.of("data/sample/lines-241-ActiveGT.csv");
    private static final Path DATAGRAMS_PATH = Path.of("data/sample/datagrams-MiniPilot.csv");

    @Test
    public void testMonolithicAndConcurrentProduceIdenticalResults() throws Exception {
        ActiveRoutesReader routesReader = new ActiveRoutesReader();
        Set<String> activeRoutes = routesReader.readActiveRoutes(LINES_PATH);

        MonolithicCalculator monolithic = new MonolithicCalculator();
        Map<RouteMonthKey, AggregationResult> monoResults =
                monolithic.calculate(DATAGRAMS_PATH, activeRoutes);

        ConcurrentCalculator concurrent = new ConcurrentCalculator();
        Map<RouteMonthKey, AggregationResult> threadResults =
                concurrent.calculate(DATAGRAMS_PATH, activeRoutes, 4);

        assertEquals(
                monoResults.size(),
                threadResults.size(),
                "Tamaño monolito vs hilos difiere"
        );

        for (RouteMonthKey key : monoResults.keySet()) {
            AggregationResult monoRes = monoResults.get(key);
            AggregationResult threadRes = threadResults.get(key);

            assertNotNull(
                    threadRes,
                    "Clave no encontrada en hilos: "
                            + key.getRouteId()
                            + " "
                            + key.getMonth()
            );

            assertEquals(
                    monoRes.getCount(),
                    threadRes.getCount(),
                    "Cantidad de registros difiere en hilos para " + key.getRouteId()
            );

            assertEquals(
                    monoRes.getAverageSpeed(),
                    threadRes.getAverageSpeed(),
                    0.000001,
                    "Velocidad promedio difiere en hilos"
            );
        }
    }
}