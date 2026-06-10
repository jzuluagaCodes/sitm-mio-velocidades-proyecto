package edu.icesi.sitmmio.app;

import edu.icesi.sitmmio.core.ResultWriter;
import edu.icesi.sitmmio.csv.ActiveRoutesReader;
import edu.icesi.sitmmio.csv.CsvReader;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;
import edu.icesi.sitmmio.ice.MasterIceCalculator;
import edu.icesi.sitmmio.ice.MasterIceCalculator.WorkerAddress;

import java.nio.file.Path;
import java.util.*;

/**
 * Punto de entrada del nodo Master.
 *
 * Argumentos:
 *   --lines      <ruta>         CSV de rutas activas
 *   --datagrams  <ruta>         CSV de datagramas
 *   --output     <ruta>         CSV de salida
 *   --workers    host:port,...  Lista de workers separada por comas
 *
 * Ejemplo con 3 workers:
 *   java -jar sitm-mio-distribuido-ice-1.0.0.jar \
 *        --lines data/sample/lines-241-ActiveGT.csv \
 *        --datagrams data/sample/datagrams-MiniPilot.csv \
 *        --output output/distribuido.csv \
 *        --workers localhost:10001,localhost:10002,localhost:10003
 */
public final class MainMaster {

    public static void main(String[] args) throws Exception {
        Path linesPath      = Path.of("data/sample/lines-241-ActiveGT.csv");
        Path datagramsPath  = Path.of("data/sample/datagrams-MiniPilot.csv");
        Path outputPath     = Path.of("output/distribuido.csv");
        List<WorkerAddress> workers = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--lines":      linesPath     = Path.of(args[++i]); break;
                case "--datagrams":  datagramsPath = Path.of(args[++i]); break;
                case "--output":     outputPath    = Path.of(args[++i]); break;
                case "--workers":
                    for (String spec : args[++i].split(","))
                        workers.add(WorkerAddress.parse(spec.trim()));
                    break;
                default:
                    System.err.println("[Master] Argumento no reconocido: " + args[i]);
            }
        }

        if (workers.isEmpty()) {
            System.err.println("[Master] ERROR: Debe especificar al menos un worker con --workers host:port");
            System.exit(1);
        }

        System.out.println("[Master] Leyendo rutas activas desde: " + linesPath);
        Set<String> activeRoutes = new ActiveRoutesReader().readActiveRoutes(linesPath);
        System.out.println("[Master] Rutas activas cargadas: " + activeRoutes.size());

        System.out.println("[Master] Leyendo datagramas desde: " + datagramsPath);
        char sep = CsvReader.detectSeparator(datagramsPath);
        List<Map<String, String>> allRows = new CsvReader(sep).readAll(datagramsPath);
        System.out.println("[Master] Filas cargadas: " + allRows.size());

        long start = System.currentTimeMillis();
        MasterIceCalculator master = new MasterIceCalculator(workers);
        Map<RouteMonthKey, AggregationResult> results = master.calculate(allRows, activeRoutes);
        long elapsed = System.currentTimeMillis() - start;

        ResultWriter.writeCsv(results, outputPath);
        System.out.println("[Master] Resultado escrito en: " + outputPath);
        System.out.println("[Master] Tiempo total de procesamiento distribuido: " + elapsed + " ms");
        System.out.println("[Master] Entradas en resultado: " + results.size());
    }
}
