package edu.icesi.sitmmio.csv;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.icesi.sitmmio.domain.SpeedRecord;

public final class DatagramMapper {

    private static final int BUS_ID_INDEX = 2;
    private static final int LATITUDE_INDEX = 4;
    private static final int LONGITUDE_INDEX = 5;
    private static final int ROUTE_ID_INDEX = 7;
    private static final int TIMESTAMP_INDEX = 10;

    private static final double GPS_SCALE = 10_000_000.0;
    private static final double MAX_REASONABLE_SPEED_KMH = 120.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<String, PreviousPoint> previousPointByRouteAndBus = new HashMap<>();

    public Optional<SpeedRecord> map(String rawLine, Set<String> activeRoutes) {
        if (rawLine == null || rawLine.isBlank()) {
            return Optional.empty();
        }

        String[] row = parseLine(rawLine, ',');

        if (row.length <= TIMESTAMP_INDEX) {
            return Optional.empty();
        }

        try {
            String busId = row[BUS_ID_INDEX].trim();
            String routeId = row[ROUTE_ID_INDEX].trim();

            if (busId.isEmpty() || routeId.isEmpty()) {
                return Optional.empty();
            }

            if (activeRoutes != null && !activeRoutes.isEmpty() && !activeRoutes.contains(routeId)) {
                return Optional.empty();
            }

            double latitude = Double.parseDouble(row[LATITUDE_INDEX].trim()) / GPS_SCALE;
            double longitude = Double.parseDouble(row[LONGITUDE_INDEX].trim()) / GPS_SCALE;

            if (latitude == 0.0 && longitude == 0.0) {
                return Optional.empty();
            }

            LocalDateTime timestamp = LocalDateTime.parse(
                    row[TIMESTAMP_INDEX].trim(),
                    TIMESTAMP_FORMATTER
            );

            String trajectoryKey = routeId + "|" + busId;
            PreviousPoint previous = previousPointByRouteAndBus.get(trajectoryKey);

            previousPointByRouteAndBus.put(
                    trajectoryKey,
                    new PreviousPoint(latitude, longitude, timestamp)
            );

            if (previous == null) {
                return Optional.empty();
            }

            long seconds = Duration.between(previous.timestamp, timestamp).getSeconds();

            if (seconds <= 0) {
                return Optional.empty();
            }

            double distanceKm = haversineKm(
                    previous.latitude,
                    previous.longitude,
                    latitude,
                    longitude
            );

            double hours = seconds / 3600.0;
            double speedKmh = distanceKm / hours;

            if (Double.isNaN(speedKmh)
                    || Double.isInfinite(speedKmh)
                    || speedKmh < 0
                    || speedKmh > MAX_REASONABLE_SPEED_KMH) {
                return Optional.empty();
            }

            String yearMonth = YearMonth.from(timestamp).toString();

            return Optional.of(new SpeedRecord(routeId, yearMonth, speedKmh));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private String[] parseLine(String line, char separator) {
        String[] quickSplit = line.split(String.valueOf(separator), -1);

        if (!line.contains("\"")) {
            for (int i = 0; i < quickSplit.length; i++) {
                quickSplit[i] = quickSplit[i].trim();
            }
            return quickSplit;
        }

        java.util.List<String> values = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (character == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (character == separator && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        values.add(current.toString().trim());

        return values.toArray(new String[0]);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    private static final class PreviousPoint {
        private final double latitude;
        private final double longitude;
        private final LocalDateTime timestamp;

        private PreviousPoint(double latitude, double longitude, LocalDateTime timestamp) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }
    }
}