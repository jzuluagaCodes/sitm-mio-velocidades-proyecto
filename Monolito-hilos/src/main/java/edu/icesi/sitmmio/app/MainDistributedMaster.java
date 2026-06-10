package edu.icesi.sitmmio.app;

import edu.icesi.sitmmio.core.ResultWriter;
import edu.icesi.sitmmio.csv.ActiveRoutesReader;
import edu.icesi.sitmmio.distributed.MasterWorkerCalculator;
import edu.icesi.sitmmio.distributed.WorkerAddress;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MainDistributedMaster {
    private MainDistributedMaster() { }

    public static void main(String[] args) throws Exception {
        ExecutionArguments arguments = ExecutionArguments.parse(removeWorkerArgs(args));
        List<WorkerAddress> workers = parseWorkers(args);
        ActiveRoutesReader routesReader = new ActiveRoutesReader();
        Set<String> activeRoutes = routesReader.readActiveRoutes(arguments.getLinesPath());

        MasterWorkerCalculator calculator = new MasterWorkerCalculator();
        Instant start = Instant.now();
        Map<RouteMonthKey, AggregationResult> results = calculator.calculate(arguments.getDatagramsPath(), activeRoutes, workers);
        Duration duration = Duration.between(start, Instant.now());

        new ResultWriter().write(arguments.getOutputPath(), results);
        System.out.println("Modo: distribuido Master-Worker");
        System.out.println("Workers usados: " + workers.size());
        System.out.println("Rutas activas cargadas: " + activeRoutes.size());
        System.out.println("Resultados generados: " + results.size());
        System.out.println("Archivo de salida: " + arguments.getOutputPath().toAbsolutePath());
        System.out.println("Tiempo ms: " + duration.toMillis());
    }

    private static List<WorkerAddress> parseWorkers(String[] args) {
        List<WorkerAddress> workers = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if ("--workers".equals(args[i])) {
                String[] values = args[++i].split(",");
                for (String value : values) {
                    workers.add(WorkerAddress.parse(value.trim()));
                }
            }
        }
        if (workers.isEmpty()) workers.add(new WorkerAddress("localhost", 9090));
        return workers;
    }

    private static String[] removeWorkerArgs(String[] args) {
        List<String> filtered = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if ("--workers".equals(args[i])) {
                i++;
            } else {
                filtered.add(args[i]);
            }
        }
        return filtered.toArray(new String[0]);
    }
}
