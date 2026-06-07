package edu.icesi.sitmmio.csv;

import edu.icesi.sitmmio.domain.SpeedRecord;
import edu.icesi.sitmmio.util.HeaderFinder;
import edu.icesi.sitmmio.util.MonthExtractor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DatagramMapper {
    private static final List<String> ROUTE_COLUMNS = List.of(
            "route_id", "routeid", "line_id", "lineid", "route", "ruta", "line", "linea", "linename", "nombre_ruta"
    );
    private static final List<String> DATE_COLUMNS = List.of(
            "timestamp", "time", "date", "fecha", "fecha_hora", "datetime", "created_at", "gps_time", "event_time"
    );
    private static final List<String> SPEED_COLUMNS = List.of(
            "speed", "velocidad", "velocity", "kmh", "km_h", "speed_kmh", "velocidad_kmh", "velocidadpromedio"
    );
    private static final List<String> DISTANCE_COLUMNS = List.of(
            "distance", "distancia", "distance_km", "distancia_km", "meters", "metros"
    );
    private static final List<String> DURATION_COLUMNS = List.of(
            "duration", "duracion", "elapsed", "tiempo", "duration_seconds", "segundos", "seconds"
    );

    public Optional<SpeedRecord> map(Map<String, String> row, Set<String> activeRoutes) {
        String routeId = HeaderFinder.find(row, ROUTE_COLUMNS);
        if (routeId == null || routeId.isBlank()) return Optional.empty();
        routeId = routeId.trim();
        if (!activeRoutes.isEmpty() && !activeRoutes.contains(routeId)) return Optional.empty();

        String timestamp = HeaderFinder.find(row, DATE_COLUMNS);
        String yearMonth = MonthExtractor.toYearMonth(timestamp);
        if (yearMonth == null) return Optional.empty();

        Double speed = parseDouble(HeaderFinder.find(row, SPEED_COLUMNS));
        if (speed == null) {
            speed = calculateSpeedFromDistanceAndDuration(row);
        }
        if (speed == null || speed.isNaN() || speed.isInfinite() || speed < 0) return Optional.empty();
        return Optional.of(new SpeedRecord(routeId, yearMonth, speed));
    }

    private Double calculateSpeedFromDistanceAndDuration(Map<String, String> row) {
    Double distance = parseDouble(HeaderFinder.find(row, DISTANCE_COLUMNS));
    Double duration = parseDouble(HeaderFinder.find(row, DURATION_COLUMNS));
    if (distance == null || duration == null || duration <= 0) return null;
    // distancia en metros → km; duración en segundos → horas
    double distanceKm = distance / 1000.0;
    double durationHours = duration / 3600.0;
    if (durationHours <= 0) return null;
    double speed = distanceKm / durationHours;
    // Velocidad razonable para bus urbano: entre 1 y 120 km/h
    if (speed < 1.0 || speed > 120.0) return null;
    return speed;
    }

    private Double parseDouble(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return null;
        String clean = rawValue.trim().replace(",", ".");
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
