package edu.icesi.sitmmio.csv;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import edu.icesi.sitmmio.domain.DatagramPoint;

public final class GpsDatagramMapper {

    private static final int BUS_ID_COLUMN = 2;
    private static final int LATITUDE_COLUMN = 4;
    private static final int LONGITUDE_COLUMN = 5;
    private static final int ROUTE_ID_COLUMN = 7;
    private static final int TIMESTAMP_COLUMN = 10;

    private static final double COORDINATE_SCALE = 10_000_000.0;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Optional<DatagramPoint> map(String[] row, Set<String> activeRoutes) {
        try {
            if (row.length <= TIMESTAMP_COLUMN) {
                return Optional.empty();
            }

            String busId = row[BUS_ID_COLUMN].trim();
            String routeId = row[ROUTE_ID_COLUMN].trim();

            if (!activeRoutes.contains(routeId)) {
                return Optional.empty();
            }

            double latitude = Long.parseLong(row[LATITUDE_COLUMN].trim()) / COORDINATE_SCALE;
            double longitude = Long.parseLong(row[LONGITUDE_COLUMN].trim()) / COORDINATE_SCALE;

            LocalDateTime timestamp = LocalDateTime.parse(
                    row[TIMESTAMP_COLUMN].trim(),
                    TIMESTAMP_FORMATTER
            );

            return Optional.of(new DatagramPoint(
                    busId,
                    routeId,
                    latitude,
                    longitude,
                    timestamp
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}