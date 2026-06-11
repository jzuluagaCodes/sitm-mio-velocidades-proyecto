package edu.icesi.sitmmio.domain;

import java.time.YearMonth;
import java.util.Objects;

public final class RouteMonthKey implements Comparable<RouteMonthKey> {

    private final String routeId;
    private final YearMonth month;

    public RouteMonthKey(String routeId, YearMonth month) {
        this.routeId = routeId;
        this.month = month;
    }

    public String getRouteId() {
        return routeId;
    }

    public YearMonth getMonth() {
        return month;
    }

    public String getYearMonth() {
        return month.toString();
    }

    @Override
    public int compareTo(RouteMonthKey other) {
        int routeComparison = this.routeId.compareTo(other.routeId);

        if (routeComparison != 0) {
            return routeComparison;
        }

        return this.month.compareTo(other.month);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteMonthKey)) return false;
        RouteMonthKey that = (RouteMonthKey) o;
        return Objects.equals(routeId, that.routeId)
                && Objects.equals(month, that.month);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId, month);
    }
}