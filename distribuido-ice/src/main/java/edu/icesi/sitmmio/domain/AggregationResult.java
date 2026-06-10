package edu.icesi.sitmmio.domain;

public final class AggregationResult {
    private long count;
    private double speedSum;

    public void add(double speed)              { speedSum += speed; count++; }
    public void merge(AggregationResult other) { speedSum += other.speedSum; count += other.count; }

    /** Reconstituye un resultado a partir de valores ya acumulados (uso exclusivo de ResultSerializer). */
    public void mergeRaw(long rawCount, double rawSpeedSum) {
        this.count    += rawCount;
        this.speedSum += rawSpeedSum;
    }

    public long   getCount()        { return count; }
    public double getSpeedSum()     { return speedSum; }
    public double getAverageSpeed() { return count == 0 ? 0.0 : speedSum / count; }
}
