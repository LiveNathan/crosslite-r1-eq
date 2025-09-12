package dev.nathanlively.crosslite_r1_eq.domain;

/**
 * Represents a single EQ band with frequency, gain, and Q factor.
 */
public record EqBand(
    double frequency,
    double gain,
    double qFactor
) {
    public EqBand {
        if (frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be positive");
        }
        if (qFactor <= 0) {
            throw new IllegalArgumentException("Q factor must be positive");
        }
    }
}