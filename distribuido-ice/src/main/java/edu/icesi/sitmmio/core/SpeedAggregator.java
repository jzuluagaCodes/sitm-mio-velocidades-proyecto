package edu.icesi.sitmmio.core;

import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;
import edu.icesi.sitmmio.domain.SpeedRecord;

import java.util.Map;
import java.util.TreeMap;

public final class SpeedAggregator {
    public void add(Map<RouteMonthKey, AggregationResult> result, SpeedRecord record) {
        result.computeIfAbsent(
                new RouteMonthKey(record.getRouteId(), record.getYearMonth()),
                k -> new AggregationResult()
        ).add(record.getSpeed());
    }

    public Map<RouteMonthKey, AggregationResult> newResultMap() { return new TreeMap<>(); }

    public Map<RouteMonthKey, AggregationResult> merge(
            Map<RouteMonthKey, AggregationResult> left,
            Map<RouteMonthKey, AggregationResult> right) {
        right.forEach((k, v) ->
                left.computeIfAbsent(k, ignored -> new AggregationResult()).merge(v));
        return left;
    }
}
