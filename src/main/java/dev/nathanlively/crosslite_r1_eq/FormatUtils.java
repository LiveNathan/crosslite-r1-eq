package dev.nathanlively.crosslite_r1_eq;

/**
 * Formatting utilities for numeric values.
 */
public final class FormatUtils {

    private FormatUtils() {
        // Utility class
    }

    /**
     * Format a double with 6 decimal places.
     */
    public static String f6(double value) {
        return String.format("%.6f", value);
    }

    /**
     * Format a double with 3 decimal places.
     */
    public static String f3(double value) {
        return String.format("%.3f", value);
    }
}