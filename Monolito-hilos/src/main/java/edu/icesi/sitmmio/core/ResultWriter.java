package edu.icesi.sitmmio.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

public final class ResultWriter {
    public void write(Path outputPath, Map<RouteMonthKey, AggregationResult> results) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) Files.createDirectories(parent);
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("ruta,mes,velocidad_promedio,cantidad_registros");
            writer.newLine();
            for (Map.Entry<RouteMonthKey, AggregationResult> entry : results.entrySet()) {
                RouteMonthKey key = entry.getKey();
                AggregationResult value = entry.getValue();
                writer.write(escape(key.getRouteId()));
                writer.write(',');
                writer.write(escape(key.getMonth().toString()));
                writer.write(',');
                writer.write(String.format(java.util.Locale.US, "%.6f", value.getAverageSpeed()));
                writer.write(',');
                writer.write(Long.toString(value.getCount()));
                writer.newLine();
            }
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
