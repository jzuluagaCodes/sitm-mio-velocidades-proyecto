package edu.icesi.sitmmio.csv;

import edu.icesi.sitmmio.domain.SpeedRecord;
import edu.icesi.sitmmio.util.HeaderFinder;
import edu.icesi.sitmmio.util.MonthExtractor;

import java.util.*;

public final class DatagramMapper {
    private static final List<String> ROUTE_COLS   = List.of("route_id","routeid","line_id","lineid","route","ruta","line","linea","linename","nombre_ruta");
    private static final List<String> DATE_COLS    = List.of("timestamp","time","date","fecha","fecha_hora","datetime","created_at","gps_time","event_time");
    private static final List<String> SPEED_COLS   = List.of("speed","velocidad","velocity","kmh","km_h","speed_kmh","velocidad_kmh","velocidadpromedio");
    private static final List<String> DIST_COLS    = List.of("distance","distancia","distance_km","distancia_km","meters","metros");
    private static final List<String> DUR_COLS     = List.of("duration","duracion","elapsed","tiempo","duration_seconds","segundos","seconds");

    public Optional<SpeedRecord> map(Map<String, String> row, Set<String> activeRoutes) {
        String routeId = HeaderFinder.find(row, ROUTE_COLS);
        if (routeId == null || routeId.isBlank()) return Optional.empty();
        routeId = routeId.trim();
        if (!activeRoutes.isEmpty() && !activeRoutes.contains(routeId)) return Optional.empty();

        String yearMonth = MonthExtractor.toYearMonth(HeaderFinder.find(row, DATE_COLS));
        if (yearMonth == null) return Optional.empty();

        Double speed = parseDouble(HeaderFinder.find(row, SPEED_COLS));
        if (speed == null) speed = calcFromDistDur(row);
        if (speed == null || speed.isNaN() || speed.isInfinite() || speed < 0) return Optional.empty();

        return Optional.of(new SpeedRecord(routeId, yearMonth, speed));
    }

    private Double calcFromDistDur(Map<String, String> row) {
        Double dist = parseDouble(HeaderFinder.find(row, DIST_COLS));
        Double dur  = parseDouble(HeaderFinder.find(row, DUR_COLS));
        if (dist == null || dur == null || dur <= 0) return null;
        double speed = (dist / 1000.0) / (dur / 3600.0);
        return (speed < 1.0 || speed > 120.0) ? null : speed;
    }

    private Double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return Double.parseDouble(raw.trim().replace(",",".")); }
        catch (NumberFormatException e) { return null; }
    }
}
