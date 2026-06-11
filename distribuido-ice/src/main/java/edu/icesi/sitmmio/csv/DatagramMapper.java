package edu.icesi.sitmmio.csv;

import edu.icesi.sitmmio.domain.SpeedRecord;

import java.util.*;

/**
 * Mapea filas del datagrama real (sin encabezado, 12 columnas por índice).
 *
 * Índices relevantes:
 *   col[2]  = busId        (identificador del bus)
 *   col[4]  = latitud escalada  (/1e7 → grados)
 *   col[5]  = longitud escalada (/1e7 → grados)
 *   col[7]  = routeId / LINEID  (coincide con LINEID de lines-241-ActiveGT.csv)
 *   col[10] = timestamp GPS     ("2019-05-27 20:14:43")
 *
 * La velocidad se calcula con Haversine entre datagramas consecutivos
 * del mismo bus, ordenados por timestamp.
 */
public final class DatagramMapper {

    // Estado interno: último punto por busId
    private final Map<String, long[]>   lastTimeByBus = new HashMap<>();
    private final Map<String, double[]> lastPosByBus  = new HashMap<>();

    /**
     * Procesa una fila raw del CSV y retorna un SpeedRecord si es válido.
     * Debe llamarse en orden de timestamp (el CsvReader los entrega en orden de archivo).
     */
    public Optional<SpeedRecord> map(String rawLine, Set<String> activeRoutes) {
        if (rawLine == null || rawLine.isBlank()) return Optional.empty();

        String[] cols = rawLine.split(",", -1);
        if (cols.length < 11) return Optional.empty();

        String routeId  = cols[7].trim();
        String busId    = cols[2].trim();
        String tsRaw    = cols[10].trim();

        // filtrar por ruta activa
        if (routeId.isBlank() || busId.isBlank()) return Optional.empty();
        if (!activeRoutes.isEmpty() && !activeRoutes.contains(routeId)) return Optional.empty();

        // parsear coordenadas escaladas
        double lat, lon;
        try {
            lat = Long.parseLong(cols[4].trim()) / 1e7;
            lon = Long.parseLong(cols[5].trim()) / 1e7;
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        // coordenadas inválidas (bus sin señal GPS)
        if (lat == 0.0 && lon == 0.0) return Optional.empty();

        // parsear timestamp
        long tsMillis = parseTimestamp(tsRaw);
        if (tsMillis < 0) return Optional.empty();

        // extraer año-mes para el resultado
        String yearMonth = tsRaw.length() >= 7 ? tsRaw.substring(0, 7) : null;
        if (yearMonth == null) return Optional.empty();

        String busKey = busId + ":" + routeId;

        Optional<SpeedRecord> result = Optional.empty();

        if (lastPosByBus.containsKey(busKey)) {
            double[] prevPos  = lastPosByBus.get(busKey);
            long     prevTime = lastTimeByBus.get(busKey)[0];

            double distKm  = haversineKm(prevPos[0], prevPos[1], lat, lon);
            double deltaHr = (tsMillis - prevTime) / 3_600_000.0;

            if (deltaHr > 0 && deltaHr < 2.0) { // máx 2 h entre datagramas del mismo bus
                double speed = distKm / deltaHr;
                if (speed >= 0.5 && speed <= 120.0) {
                    result = Optional.of(new SpeedRecord(routeId, yearMonth, speed));
                }
            }
        }

        // actualizar estado para el siguiente datagrama de este bus
        lastPosByBus.put(busKey, new double[]{lat, lon});
        lastTimeByBus.put(busKey, new long[]{tsMillis});

        return result;
    }

    // ---------------------------------------------------------------

    private long parseTimestamp(String ts) {
        // formato esperado: "2019-05-27 20:14:43"
        try {
            ts = ts.replace("\"", "").trim();
            String[] parts = ts.split("[\\s]");
            if (parts.length < 2) return -1;
            String[] date = parts[0].split("-");
            String[] time = parts[1].split(":");
            if (date.length < 3 || time.length < 3) return -1;

            int y  = Integer.parseInt(date[0]);
            int mo = Integer.parseInt(date[1]);
            int d  = Integer.parseInt(date[2]);
            int h  = Integer.parseInt(time[0]);
            int mi = Integer.parseInt(time[1]);
            int s  = Integer.parseInt(time[2]);

            // milisegundos aproximados (sin usar java.time para no depender del locale)
            Calendar cal = new GregorianCalendar(y, mo - 1, d, h, mi, s);
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    /** Fórmula de Haversine — devuelve distancia en kilómetros */
    private static double haversineKm(double lat1, double lon1,
                                       double lat2, double lon2) {
        final double R = 6371.0; // radio de la Tierra en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}