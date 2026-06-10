package edu.icesi.sitmmio.distributed;

import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.io.Serializable;
import java.util.Map;

public final class WorkResponse implements Serializable {
    private final Map<RouteMonthKey, AggregationResult> partialResult;

    public WorkResponse(Map<RouteMonthKey, AggregationResult> partialResult) {
        this.partialResult = partialResult;
    }

    public Map<RouteMonthKey, AggregationResult> getPartialResult() {
        return partialResult;
    }
}
