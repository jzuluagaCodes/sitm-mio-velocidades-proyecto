package edu.icesi.sitmmio.core;

import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;
import edu.icesi.sitmmio.domain.SpeedRecord;

import java.util.Map;
import java.util.TreeMap;

public final class SpeedAggregator {
    public void add(Map<RouteMonthKey, AggregationResult> result, SpeedRecord record) {
        RouteMonthKey key = new RouteMonthKey(record.getRouteId(), record.getYearMonth());
        result.computeIfAbsent(key, ignored -> new AggregationResult()).add(record.getSpeed());
    }

    public Map<RouteMonthKey, AggregationResult> newResultMap() {
        return new TreeMap<>();
    }

    public Map<RouteMonthKey, AggregationResult> merge(Map<RouteMonthKey, AggregationResult> left,
                                                        Map<RouteMonthKey, AggregationResult> right) {
        for (Map.Entry<RouteMonthKey, AggregationResult> entry : right.entrySet()) {
            left.computeIfAbsent(entry.getKey(), ignored -> new AggregationResult()).merge(entry.getValue());
        }
        return left;
    }
}
