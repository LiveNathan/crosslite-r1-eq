package dev.nathanlively.crosslite_r1_eq.converter;

import dev.nathanlively.crosslite_r1_eq.domain.CrossLiteSettings;
import dev.nathanlively.crosslite_r1_eq.domain.EqBand;
import dev.nathanlively.crosslite_r1_eq.domain.R1Filter;
import dev.nathanlively.crosslite_r1_eq.domain.R1Settings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts CrossLite EQ settings to R1 format with appropriate value clamping.
 */
@Component
public class EqConverter {

    // R1 constraints based on example3 analysis
    private static final double MIN_GAIN = -18.0;
    private static final double MAX_GAIN = 12.0;
    private static final double MIN_Q = 0.1;
    private static final double MAX_Q = 25.0;

    public R1Settings convert(CrossLiteSettings crossLiteSettings) {
        List<R1Filter> filters = new ArrayList<>();

        // Convert active EQ bands
        for (int i = 0; i < crossLiteSettings.eqBands().size() && i < R1Settings.MAX_FILTERS; i++) {
            EqBand band = crossLiteSettings.eqBands().get(i);
            filters.add(R1Filter.enabled(
                    band.frequency(),
                    clampQ(band.qFactor()),
                    clampGain(band.gain())
            ));
        }

        // Fill remaining slots with disabled filters
        while (filters.size() < R1Settings.MAX_FILTERS) {
            filters.add(R1Filter.disabled());
        }

        return new R1Settings(filters);
    }

    private double clampGain(double gain) {
        return Math.clamp(gain, MIN_GAIN, MAX_GAIN);
    }

    private double clampQ(double q) {
        return Math.clamp(q, MIN_Q, MAX_Q);
    }
}