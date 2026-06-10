package edu.icesi.sitmmio.domain;

import java.util.Objects;

public final class RouteMonthKey implements Comparable<RouteMonthKey> {
    private final String routeId;
    private final String yearMonth;

    public RouteMonthKey(String routeId, String yearMonth) {
        this.routeId = routeId;
        this.yearMonth = yearMonth;
    }

    public String getRouteId()   { return routeId; }
    public String getYearMonth() { return yearMonth; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RouteMonthKey)) return false;
        RouteMonthKey other = (RouteMonthKey) obj;
        return Objects.equals(routeId, other.routeId) && Objects.equals(yearMonth, other.yearMonth);
    }

    @Override
    public int hashCode() { return Objects.hash(routeId, yearMonth); }

    @Override
    public int compareTo(RouteMonthKey other) {
        int c = routeId.compareTo(other.routeId);
        return c != 0 ? c : yearMonth.compareTo(other.yearMonth);
    }
}
