package dev.nathanlively.crosslite_r1_eq.converter;

import dev.nathanlively.crosslite_r1_eq.domain.CrossLiteSettings;
import dev.nathanlively.crosslite_r1_eq.domain.EqBand;
import dev.nathanlively.crosslite_r1_eq.domain.R1Filter;
import dev.nathanlively.crosslite_r1_eq.domain.R1Settings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EqConverterTest {
    
    private final EqConverter converter = new EqConverter();
    
    @Test
    void shouldConvertSingleEqBand() {
        CrossLiteSettings input = new CrossLiteSettings(List.of(
            new EqBand(1001.0, -6.0, 0.750)
        ));
        
        R1Settings result = converter.convert(input);
        
        assertEquals(16, result.filters().size());
        
        R1Filter firstFilter = result.filters().get(0);
        assertTrue(firstFilter.enabled());
        assertEquals(1001.0, firstFilter.frequency());
        assertEquals(-6.0, firstFilter.gain());
        assertEquals(0.750, firstFilter.qFactor());
        
        // Remaining filters should be disabled
        for (int i = 1; i < 16; i++) {
            assertFalse(result.filters().get(i).enabled());
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
        assertTrue(filter1.enabled());
        assertEquals(1001.0, filter1.frequency());
        assertEquals(-6.0, filter1.gain());
        assertEquals(0.750, filter1.qFactor());
        
        R1Filter filter2 = result.filters().get(1);
        assertTrue(filter2.enabled());
        assertEquals(102.0, filter2.frequency());
        assertEquals(4.3, filter2.gain());
        assertEquals(1.200, filter2.qFactor());
        
        // Remaining filters should be disabled
        for (int i = 2; i < 16; i++) {
            assertFalse(result.filters().get(i).enabled());
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
        assertEquals(-18.0, filter1.gain()); // Clamped to minimum
        
        R1Filter filter2 = result.filters().get(1);
        assertEquals(12.0, filter2.gain()); // Clamped to maximum
    }
    
    @Test
    void shouldClampExtremeQValues() {
        CrossLiteSettings input = new CrossLiteSettings(List.of(
            new EqBand(1001.0, -6.0, 0.05),   // Q too low
            new EqBand(102.0, 4.0, 200.0)     // Q too high
        ));
        
        R1Settings result = converter.convert(input);
        
        R1Filter filter1 = result.filters().get(0);
        assertEquals(0.1, filter1.qFactor()); // Clamped to minimum
        
        R1Filter filter2 = result.filters().get(1);
        assertEquals(25.0, filter2.qFactor()); // Clamped to maximum
    }
    
    @Test
    void shouldHandleEmptyInput() {
        CrossLiteSettings input = new CrossLiteSettings(List.of());
        
        R1Settings result = converter.convert(input);
        
        assertEquals(16, result.filters().size());
        // All filters should be disabled
        for (R1Filter filter : result.filters()) {
            assertFalse(filter.enabled());
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
        
        assertEquals(16, result.filters().size());
        // First 16 should be enabled
        for (int i = 0; i < 16; i++) {
            assertTrue(result.filters().get(i).enabled());
        }
    }
}