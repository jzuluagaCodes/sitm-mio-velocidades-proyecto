package edu.icesi.sitmmio.core;

import edu.icesi.sitmmio.csv.CsvReader;
import edu.icesi.sitmmio.csv.DatagramMapper;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;
import edu.icesi.sitmmio.domain.SpeedRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MonolithicCalculator {
    private final DatagramMapper mapper;
    private final SpeedAggregator aggregator;

    public MonolithicCalculator() {
        this.mapper = new DatagramMapper();
        this.aggregator = new SpeedAggregator();
    }

    public Map<RouteMonthKey, AggregationResult> calculate(Path datagramsPath, Set<String> activeRoutes) throws IOException {
        char separator = CsvReader.detectSeparator(datagramsPath);
        CsvReader reader = new CsvReader(separator);
        List<Map<String, String>> rows = reader.readAll(datagramsPath);
        Map<RouteMonthKey, AggregationResult> results = aggregator.newResultMap();
        for (Map<String, String> row : rows) {
            Optional<SpeedRecord> record = mapper.map(row, activeRoutes);
            record.ifPresent(speedRecord -> aggregator.add(results, speedRecord));
        }
        return results;
    }
}
