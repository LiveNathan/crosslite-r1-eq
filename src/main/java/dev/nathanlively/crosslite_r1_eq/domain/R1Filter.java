package dev.nathanlively.crosslite_r1_eq.domain;

/**
 * Represents a single filter in the R1 format.
 */
public record R1Filter(
    double frequency,
    double qFactor,
    double gain,
    boolean enabled
) {
    public static R1Filter disabled() {
        return new R1Filter(1000.0, 0.7, 0.0, false);
    }
    
    public static R1Filter enabled(double frequency, double qFactor, double gain) {
        return new R1Filter(frequency, qFactor, gain, true);
    }
}