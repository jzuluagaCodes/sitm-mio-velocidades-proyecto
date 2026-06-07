package edu.icesi.sitmmio.domain;

public final class SpeedRecord {
    private final String routeId;
    private final String yearMonth;
    private final double speed;

    public SpeedRecord(String routeId, String yearMonth, double speed) {
        this.routeId = routeId;
        this.yearMonth = yearMonth;
        this.speed = speed;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public double getSpeed() {
        return speed;
    }
}
