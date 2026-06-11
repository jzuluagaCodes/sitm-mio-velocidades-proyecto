package edu.icesi.sitmmio.concurrent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import edu.icesi.sitmmio.core.SpeedAggregator;
import edu.icesi.sitmmio.csv.DatagramMapper;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

public final class AggregationTask implements Callable<Map<RouteMonthKey, AggregationResult>> {
    private final List<String> rows;
    private final Set<String> activeRoutes;

    public AggregationTask(List<String> rows, Set<String> activeRoutes) {
        this.rows = rows;
        this.activeRoutes = activeRoutes;
    }

    @Override
    public Map<RouteMonthKey, AggregationResult> call() {
        DatagramMapper mapper = new DatagramMapper();
        SpeedAggregator aggregator = new SpeedAggregator();
        Map<RouteMonthKey, AggregationResult> results = aggregator.newResultMap();

        for (String row : rows) {
            mapper.map(row, activeRoutes).ifPresent(record -> aggregator.add(results, record));
        }

        return results;
    }
}