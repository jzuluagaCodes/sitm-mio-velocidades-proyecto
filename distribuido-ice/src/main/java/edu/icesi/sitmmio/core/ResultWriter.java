package edu.icesi.sitmmio.core;

import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class ResultWriter {

    private ResultWriter() {}

    public static void writeCsv(Map<RouteMonthKey, AggregationResult> results, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent() != null ? outputPath.getParent() : Path.of("."));
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("route_id,year_month,avg_speed_kmh,record_count");
            writer.newLine();
            for (Map.Entry<RouteMonthKey, AggregationResult> entry : results.entrySet()) {
                RouteMonthKey    key = entry.getKey();
                AggregationResult ar = entry.getValue();
                writer.write(String.format("%s,%s,%.4f,%d",
                        key.getRouteId(),
                        key.getYearMonth(),
                        ar.getAverageSpeed(),
                        ar.getCount()));
                writer.newLine();
            }
        }
    }
}
