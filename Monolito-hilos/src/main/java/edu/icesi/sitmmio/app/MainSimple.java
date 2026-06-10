package edu.icesi.sitmmio.app;

import edu.icesi.sitmmio.core.MonolithicCalculator;
import edu.icesi.sitmmio.core.ResultWriter;
import edu.icesi.sitmmio.csv.ActiveRoutesReader;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public final class MainSimple {
    private MainSimple() { }

    public static void main(String[] args) throws Exception {
        ExecutionArguments arguments = ExecutionArguments.parse(args);
        ActiveRoutesReader routesReader = new ActiveRoutesReader();
        Set<String> activeRoutes = routesReader.readActiveRoutes(arguments.getLinesPath());

        MonolithicCalculator calculator = new MonolithicCalculator();
        Instant start = Instant.now();
        Map<RouteMonthKey, AggregationResult> results = calculator.calculate(arguments.getDatagramsPath(), activeRoutes);
        Duration duration = Duration.between(start, Instant.now());

        new ResultWriter().write(arguments.getOutputPath(), results);
        System.out.println("Modo: monolito simple");
        System.out.println("Rutas activas cargadas: " + activeRoutes.size());
        System.out.println("Resultados generados: " + results.size());
        System.out.println("Archivo de salida: " + arguments.getOutputPath().toAbsolutePath());
        System.out.println("Tiempo ms: " + duration.toMillis());
    }
}
