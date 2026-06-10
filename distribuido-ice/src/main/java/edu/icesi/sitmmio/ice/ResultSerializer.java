package edu.icesi.sitmmio.ice;

import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.util.Map;
import java.util.TreeMap;

/**
 * Convierte Map<RouteMonthKey, AggregationResult> a/desde un String CSV
 * con formato: routeId|yearMonth|count|speedSum  (una línea por entrada).
 *
 * Se usa para transportar resultados parciales a través de ICE sin
 * depender de serialización Java binaria.
 */
public final class ResultSerializer {

    private ResultSerializer() {}

    /** Serializa el mapa a String CSV separado por pipes y saltos de línea. */
    public static String serialize(Map<RouteMonthKey, AggregationResult> results) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<RouteMonthKey, AggregationResult> entry : results.entrySet()) {
            AggregationResult ar = entry.getValue();
            sb.append(entry.getKey().getRouteId())
              .append('|').append(entry.getKey().getYearMonth())
              .append('|').append(ar.getCount())
              .append('|').append(ar.getSpeedSum())
              .append('\n');
        }
        return sb.toString();
    }

    /** Deserializa el String CSV de vuelta a Map<RouteMonthKey, AggregationResult>. */
    public static Map<RouteMonthKey, AggregationResult> deserialize(String data) {
        Map<RouteMonthKey, AggregationResult> results = new TreeMap<>();
        if (data == null || data.isBlank()) return results;
        for (String line : data.split("\n")) {
            if (line.isBlank()) continue;
            String[] parts = line.split("\\|");
            if (parts.length != 4) continue;
            try {
                RouteMonthKey key = new RouteMonthKey(parts[0], parts[1]);
                long count       = Long.parseLong(parts[2]);
                double speedSum  = Double.parseDouble(parts[3]);
                AggregationResult ar = new AggregationResult();
                // Reconstruimos sumando speedSum/count individualmente no es posible
                // sin acceso directo a los campos, así que AggregationResult expone
                // un constructor de reconstitución vía mergeRaw.
                ar.mergeRaw(count, speedSum);
                results.put(key, ar);
            } catch (NumberFormatException ignored) {}
        }
        return results;
    }
}
