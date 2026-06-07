package edu.icesi.sitmmio.concurrent;

import edu.icesi.sitmmio.core.SpeedAggregator;
import edu.icesi.sitmmio.csv.DatagramMapper;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;
import edu.icesi.sitmmio.domain.SpeedRecord;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

public final class AggregationTask implements Callable<Map<RouteMonthKey, AggregationResult>> {
    private final List<Map<String, String>> rows;
    private final Set<String> activeRoutes;

    public AggregationTask(List<Map<String, String>> rows, Set<String> activeRoutes) {
        this.rows = rows;
        this.activeRoutes = activeRoutes;
    }

    @Override
    public Map<RouteMonthKey, AggregationResult> call() {
        DatagramMapper mapper = new DatagramMapper();
        SpeedAggregator aggregator = new SpeedAggregator();
        Map<RouteMonthKey, AggregationResult> results = aggregator.newResultMap();
        for (Map<String, String> row : rows) {
            Optional<SpeedRecord> record = mapper.map(row, activeRoutes);
            record.ifPresent(speedRecord -> aggregator.add(results, speedRecord));
        }
        return results;
    }
}
