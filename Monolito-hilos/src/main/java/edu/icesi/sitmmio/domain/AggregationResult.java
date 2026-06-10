package edu.icesi.sitmmio.domain;

import java.io.Serializable;

public final class AggregationResult implements Serializable {
    private long count;
    private double speedSum;

    public void add(double speed) {
        speedSum += speed;
        count++;
    }

    public void merge(AggregationResult other) {
        this.speedSum += other.speedSum;
        this.count += other.count;
    }

    public long getCount() {
        return count;
    }

    public double getAverageSpeed() {
        if (count == 0) return 0.0;
        return speedSum / count;
    }
}
