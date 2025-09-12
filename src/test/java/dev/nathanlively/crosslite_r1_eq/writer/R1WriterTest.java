package dev.nathanlively.crosslite_r1_eq.writer;

import dev.nathanlively.crosslite_r1_eq.domain.R1Filter;
import dev.nathanlively.crosslite_r1_eq.domain.R1Settings;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class R1WriterTest {
    
    private final R1Writer writer = new R1Writer();
    
    @Test
    void shouldWriteBasicXmlStructure() {
        List<R1Filter> filters = new ArrayList<>();
        filters.add(R1Filter.enabled(1001.0, 0.750, -6.0));
        // Fill with disabled filters
        for (int i = 1; i < 16; i++) {
            filters.add(R1Filter.disabled());
        }
        
        R1Settings settings = new R1Settings(filters);
        String xml = writer.writeToXml(settings);
        
        assertTrue(xml.startsWith("<R1EQSETTINGS_20><EQ><REMARKS></REMARKS><EQx_ON>1.0</EQx_ON>"));
        assertTrue(xml.endsWith("</EQ></R1EQSETTINGS_20>"));
        assertTrue(xml.contains("<F_1>1001.000000</F_1>"));
        assertTrue(xml.contains("<Q_1>0.750000</Q_1>"));
        assertTrue(xml.contains("<G_1>-6.000000</G_1>"));
        assertTrue(xml.contains("<E_1>1.000000</E_1>"));
    }
    
    @Test
    void shouldWriteDisabledFilters() {
        List<R1Filter> filters = new ArrayList<>();
        // All disabled filters
        for (int i = 0; i < 16; i++) {
            filters.add(R1Filter.disabled());
        }
        
        R1Settings settings = new R1Settings(filters);
        String xml = writer.writeToXml(settings);
        
        // Check that all filters are disabled
        for (int i = 1; i <= 16; i++) {
            assertTrue(xml.contains("<E_" + i + ">0.000000</E_" + i + ">"));
            assertTrue(xml.contains("<F_" + i + ">1000.000000</F_" + i + ">"));
            assertTrue(xml.contains("<Q_" + i + ">0.700000</Q_" + i + ">"));
            assertTrue(xml.contains("<G_" + i + ">0.000000</G_" + i + ">"));
        }
    }
    
    @Test
    void shouldFormatNumbersCorrectly() {
        List<R1Filter> filters = new ArrayList<>();
        filters.add(R1Filter.enabled(102.0, 1.2, 4.3));
        // Fill with disabled filters
        for (int i = 1; i < 16; i++) {
            filters.add(R1Filter.disabled());
        }
        
        R1Settings settings = new R1Settings(filters);
        String xml = writer.writeToXml(settings);
        
        assertTrue(xml.contains("<F_1>102.000000</F_1>"));
        assertTrue(xml.contains("<Q_1>1.200000</Q_1>"));
        assertTrue(xml.contains("<G_1>4.300000</G_1>"));
    }
    
    @Test
    void shouldWriteAllSixteenFilters() {
        List<R1Filter> filters = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            filters.add(R1Filter.disabled());
        }
        
        R1Settings settings = new R1Settings(filters);
        String xml = writer.writeToXml(settings);
        
        // Verify all 16 filters are present
        for (int i = 1; i <= 16; i++) {
            assertTrue(xml.contains("<FILTER_" + i + ">"));
            assertTrue(xml.contains("<F_" + i + ">"));
            assertTrue(xml.contains("<Q_" + i + ">"));
            assertTrue(xml.contains("<G_" + i + ">"));
            assertTrue(xml.contains("<E_" + i + ">"));
        }
    }
}