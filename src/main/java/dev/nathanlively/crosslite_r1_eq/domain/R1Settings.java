package dev.nathanlively.crosslite_r1_eq.domain;

import java.util.List;

/**
 * Represents the complete R1 EQ settings with 16 filters.
 */
public record R1Settings(List<R1Filter> filters) {
    public static final int MAX_FILTERS = 16;
    
    public R1Settings {
        if (filters == null) {
            throw new IllegalArgumentException("Filters cannot be null");
        }
        if (filters.size() != MAX_FILTERS) {
            throw new IllegalArgumentException("R1 settings must have exactly " + MAX_FILTERS + " filters");
        }
    }
}