package dev.nathanlively.crosslite_r1_eq.domain;

import java.util.List;

/**
 * Represents the EQ settings from a CrossLite file.
 */
public record CrossLiteSettings(List<EqBand> eqBands) {
    public CrossLiteSettings {
        if (eqBands == null) {
            throw new IllegalArgumentException("EQ bands cannot be null");
        }
    }
}