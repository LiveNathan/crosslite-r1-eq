package dev.nathanlively.crosslite_r1_eq.converter;

import dev.nathanlively.crosslite_r1_eq.domain.CrossLiteSettings;
import dev.nathanlively.crosslite_r1_eq.domain.EqBand;
import dev.nathanlively.crosslite_r1_eq.domain.R1Filter;
import dev.nathanlively.crosslite_r1_eq.domain.R1Settings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EqConverterTest {

    private final EqConverter converter = new EqConverter();

    @Test
    void shouldConvertSingleEqBand() {
        CrossLiteSettings input = new CrossLiteSettings(List.of(
            new EqBand(1001.0, -6.0, 0.750)
        ));

        R1Settings result = converter.convert(input);

        assertThat(result.filters()).hasSize(16);

        R1Filter firstFilter = result.filters().get(0);
        assertThat(firstFilter.enabled()).isTrue();
        assertThat(firstFilter.frequency()).isEqualTo(1001.0);
        assertThat(firstFilter.gain()).isEqualTo(-6.0);
        assertThat(firstFilter.qFactor()).isEqualTo(0.750);

        // Remaining filters should be disabled
        for (int i = 1; i < 16; i++) {
            assertThat(result.filters().get(i).enabled()).isFalse();
        }
    }

    @Test
    void shouldConvertMultipleEqBands() {
        CrossLiteSettings input = new CrossLiteSettings(List.of(
            new EqBand(1001.0, -6.0, 0.750),
            new EqBand(102.0, 4.3, 1.200)
        ));

        R1Settings result = converter.convert(input);

        R1Filter filter1 = result.filters().get(0);
        assertThat(filter1.enabled()).isTrue();
        assertThat(filter1.frequency()).isEqualTo(1001.0);
        assertThat(filter1.gain()).isEqualTo(-6.0);
        assertThat(filter1.qFactor()).isEqualTo(0.750);

        R1Filter filter2 = result.filters().get(1);
        assertThat(filter2.enabled()).isTrue();
        assertThat(filter2.frequency()).isEqualTo(102.0);
        assertThat(filter2.gain()).isEqualTo(4.3);
        assertThat(filter2.qFactor()).isEqualTo(1.200);

        // Remaining filters should be disabled
        for (int i = 2; i < 16; i++) {
            assertThat(result.filters().get(i).enabled()).isFalse();
        }
    }

    @Test
    void shouldClampExtremeGainValues() {
        CrossLiteSettings input = new CrossLiteSettings(List.of(
            new EqBand(1001.0, -40.0, 0.100),  // Gain too low
            new EqBand(102.0, 40.0, 128.0)     // Gain too high
        ));

        R1Settings result = converter.convert(input);

        R1Filter filter1 = result.filters().get(0);
        assertThat(filter1.gain()).isEqualTo(-18.0); // Clamped to minimum

        R1Filter filter2 = result.filters().get(1);
        assertThat(filter2.gain()).isEqualTo(12.0); // Clamped to maximum
    }

    @Test
    void shouldClampExtremeQValues() {
        CrossLiteSettings input = new CrossLiteSettings(List.of(
            new EqBand(1001.0, -6.0, 0.05),   // Q too low
            new EqBand(102.0, 4.0, 200.0)     // Q too high
        ));

        R1Settings result = converter.convert(input);

        R1Filter filter1 = result.filters().get(0);
        assertThat(filter1.qFactor()).isEqualTo(0.1); // Clamped to minimum

        R1Filter filter2 = result.filters().get(1);
        assertThat(filter2.qFactor()).isEqualTo(25.0); // Clamped to maximum
    }

    @Test
    void shouldHandleEmptyInput() {
        CrossLiteSettings input = new CrossLiteSettings(List.of());

        R1Settings result = converter.convert(input);

        assertThat(result.filters()).hasSize(16);
        // All filters should be disabled
        for (R1Filter filter : result.filters()) {
            assertThat(filter.enabled()).isFalse();
        }
    }

    @Test
    void shouldLimitToMaxFilters() {
        // Create 20 EQ bands (more than R1's 16 limit)
        List<EqBand> bands = List.of(
            new EqBand(100, 1, 1), new EqBand(200, 1, 1), new EqBand(300, 1, 1), new EqBand(400, 1, 1),
            new EqBand(500, 1, 1), new EqBand(600, 1, 1), new EqBand(700, 1, 1), new EqBand(800, 1, 1),
            new EqBand(900, 1, 1), new EqBand(1000, 1, 1), new EqBand(1100, 1, 1), new EqBand(1200, 1, 1),
            new EqBand(1300, 1, 1), new EqBand(1400, 1, 1), new EqBand(1500, 1, 1), new EqBand(1600, 1, 1),
            new EqBand(1700, 1, 1), new EqBand(1800, 1, 1), new EqBand(1900, 1, 1), new EqBand(2000, 1, 1)
        );

        CrossLiteSettings input = new CrossLiteSettings(bands);

        R1Settings result = converter.convert(input);

        assertThat(result.filters()).hasSize(16);
        // First 16 should be enabled
        for (int i = 0; i < 16; i++) {
            assertThat(result.filters().get(i).enabled()).isTrue();
        }
    }
}