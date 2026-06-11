package edu.icesi.sitmmio.domain;

import java.time.LocalDateTime;

public final class DatagramPoint {

    private final String busId;
    private final String routeId;
    private final double latitude;
    private final double longitude;
    private final LocalDateTime timestamp;

    public DatagramPoint(String busId, String routeId, double latitude, double longitude, LocalDateTime timestamp) {
        this.busId = busId;
        this.routeId = routeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getBusId() {
        return busId;
    }

    public String getRouteId() {
        return routeId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}