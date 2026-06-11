package edu.icesi.sitmmio.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.icesi.sitmmio.csv.CsvReader;
import edu.icesi.sitmmio.csv.GpsDatagramMapper;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.DatagramPoint;
import edu.icesi.sitmmio.domain.RouteMonthKey;

public final class MonolithicCalculator {

    private static final double MAX_REASONABLE_SPEED_KMH = 120.0;

    private final GpsDatagramMapper mapper;
    private final GeoDistanceCalculator distanceCalculator;

    public MonolithicCalculator() {
        this.mapper = new GpsDatagramMapper();
        this.distanceCalculator = new GeoDistanceCalculator();
    }

    public Map<RouteMonthKey, AggregationResult> calculate(Path datagramsPath, Set<String> activeRoutes) throws IOException {
        char separator = CsvReader.detectSeparator(datagramsPath);

        Map<String, List<DatagramPoint>> pointsByRouteAndBus = new HashMap<>();

        long rowsRead = 0;
        long validPoints = 0;

        try (BufferedReader reader = Files.newBufferedReader(datagramsPath, StandardCharsets.UTF_8)) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                rowsRead++;

                String[] row = parseLine(line, separator);

                Optional<DatagramPoint> optionalPoint = mapper.map(row, activeRoutes);

                if (optionalPoint.isPresent()) {
                    DatagramPoint point = optionalPoint.get();
                    validPoints++;

                    String key = point.getRouteId() + "|" + point.getBusId();
                    pointsByRouteAndBus
                            .computeIfAbsent(key, ignored -> new ArrayList<>())
                            .add(point);
                }
            }
        }

        Map<RouteMonthKey, AggregationResult> results = new HashMap<>();

        long calculatedSpeeds = 0;
        long discardedSpeeds = 0;

        for (List<DatagramPoint> trajectory : pointsByRouteAndBus.values()) {
            if (trajectory.size() < 2) {
                continue;
            }

            trajectory.sort(Comparator.comparing(DatagramPoint::getTimestamp));

            for (int i = 1; i < trajectory.size(); i++) {
                DatagramPoint previous = trajectory.get(i - 1);
                DatagramPoint current = trajectory.get(i);

                long seconds = Duration.between(previous.getTimestamp(), current.getTimestamp()).getSeconds();

                if (seconds <= 0) {
                    discardedSpeeds++;
                    continue;
                }

                double distanceKm = distanceCalculator.distanceInKm(
                        previous.getLatitude(),
                        previous.getLongitude(),
                        current.getLatitude(),
                        current.getLongitude()
                );

                double hours = seconds / 3600.0;
                double speedKmh = distanceKm / hours;

                if (Double.isNaN(speedKmh)
                        || Double.isInfinite(speedKmh)
                        || speedKmh < 0
                        || speedKmh > MAX_REASONABLE_SPEED_KMH) {
                    discardedSpeeds++;
                    continue;
                }

                YearMonth month = YearMonth.from(current.getTimestamp());
                RouteMonthKey resultKey = new RouteMonthKey(current.getRouteId(), month);

                results.computeIfAbsent(resultKey, ignored -> new AggregationResult())
                        .add(speedKmh);

                calculatedSpeeds++;
            }
        }

        System.out.println("Filas leídas: " + rowsRead);
        System.out.println("Puntos GPS válidos: " + validPoints);
        System.out.println("Grupos ruta-bus: " + pointsByRouteAndBus.size());
        System.out.println("Velocidades calculadas: " + calculatedSpeeds);
        System.out.println("Velocidades descartadas: " + discardedSpeeds);
        System.out.println("Resultados agregados: " + results.size());

        return results;
    }

    private String[] parseLine(String line, char separator) {
        List<String> values = new ArrayList<>();
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
}