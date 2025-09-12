package dev.nathanlively.crosslite_r1_eq.parser;

import dev.nathanlively.crosslite_r1_eq.domain.CrossLiteSettings;
import dev.nathanlively.crosslite_r1_eq.domain.EqBand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrossLiteParserTest {
    
    private final CrossLiteParser parser = new CrossLiteParser();
    
    @Test
    void shouldParseSingleEqBand() {
        String content = "Frequency= 1001.0Hz Gain= -6.0dB Qbp= 0.750";
        
        CrossLiteSettings result = parser.parse(content);
        
        assertEquals(1, result.eqBands().size());
        EqBand band = result.eqBands().get(0);
        assertEquals(1001.0, band.frequency());
        assertEquals(-6.0, band.gain());
        assertEquals(0.750, band.qFactor());
    }
    
    @Test
    void shouldParseMultipleEqBands() {
        String content = """
            Frequency= 1001.0Hz Gain= -6.0dB Qbp= 0.750
            Frequency= 102.0Hz Gain= 4.3dB Qbp= 1.200
            """;
        
        CrossLiteSettings result = parser.parse(content);
        
        assertEquals(2, result.eqBands().size());
        
        EqBand band1 = result.eqBands().get(0);
        assertEquals(1001.0, band1.frequency());
        assertEquals(-6.0, band1.gain());
        assertEquals(0.750, band1.qFactor());
        
        EqBand band2 = result.eqBands().get(1);
        assertEquals(102.0, band2.frequency());
        assertEquals(4.3, band2.gain());
        assertEquals(1.200, band2.qFactor());
    }
    
    @Test
    void shouldParseExtremeValues() {
        String content = """
            Frequency= 1001.0Hz Gain= -40.0dB Qbp= 0.100
            Frequency= 102.0Hz Gain= 40.0dB Qbp= 128.000
            """;
        
        CrossLiteSettings result = parser.parse(content);
        
        assertEquals(2, result.eqBands().size());
        
        EqBand band1 = result.eqBands().get(0);
        assertEquals(1001.0, band1.frequency());
        assertEquals(-40.0, band1.gain());
        assertEquals(0.100, band1.qFactor());
        
        EqBand band2 = result.eqBands().get(1);
        assertEquals(102.0, band2.frequency());
        assertEquals(40.0, band2.gain());
        assertEquals(128.000, band2.qFactor());
    }
    
    @Test
    void shouldReturnEmptyListWhenNoEqBandsFound() {
        String content = "Some random content without EQ bands";
        
        CrossLiteSettings result = parser.parse(content);
        
        assertTrue(result.eqBands().isEmpty());
    }
    
    @Test
    void shouldIgnoreIrrelevantContent() {
        String content = """
            Layer 1
            IIR Bypassed.L 1 Ch 1
            1) 1Parametric EQ
            Frequency= 1001.0Hz Gain= -6.0dB Qbp= 0.750
            Magnitude Mode Normal, Phase Mode Normal.
            biquad1,
            b0=0.926144439211431525,
            Layer 2
            IIR Bypassed.
            """;
        
        CrossLiteSettings result = parser.parse(content);
        
        assertEquals(1, result.eqBands().size());
        EqBand band = result.eqBands().get(0);
        assertEquals(1001.0, band.frequency());
        assertEquals(-6.0, band.gain());
        assertEquals(0.750, band.qFactor());
    }
}