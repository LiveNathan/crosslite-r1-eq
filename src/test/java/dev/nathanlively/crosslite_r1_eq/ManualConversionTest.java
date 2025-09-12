package dev.nathanlively.crosslite_r1_eq;

import dev.nathanlively.crosslite_r1_eq.converter.EqConverter;
import dev.nathanlively.crosslite_r1_eq.parser.CrossLiteParser;
import dev.nathanlively.crosslite_r1_eq.writer.R1Writer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ManualConversionTest {
    
    @Autowired
    private CrossLiteParser parser;
    
    @Autowired
    private EqConverter converter;
    
    @Autowired
    private R1Writer writer;
    
    @Test
    void shouldPerformEndToEndConversion() throws IOException {
        FileConversionService service = new FileConversionService(parser, converter, writer);
        
        // Create a test file
        Path testFile = Path.of("test_conversion.txt");
        String testContent = """
            Layer 1
            IIR Bypassed.L 1 Ch 1
            1) 1Parametric EQ
            Frequency= 1001.0Hz Gain= -6.0dB Qbp= 0.750
            Magnitude Mode Normal, Phase Mode Normal.
            """;
        
        Files.writeString(testFile, testContent);
        
        Path outputFile = Path.of("test_conversion.rcp");
        
        // Perform conversion
        service.convertFile(testFile.toString(), outputFile.toString());
        
        // Verify output exists and has correct content
        assertTrue(Files.exists(outputFile));
        String output = Files.readString(outputFile);
        
        assertTrue(output.startsWith("<R1EQSETTINGS_20>"));
        assertTrue(output.contains("<F_1>1001.000000</F_1>"));
        assertTrue(output.contains("<G_1>-6.000000</G_1>"));
        assertTrue(output.contains("<Q_1>0.750000</Q_1>"));
        assertTrue(output.contains("<E_1>1.000000</E_1>"));
        assertTrue(output.endsWith("</R1EQSETTINGS_20>"));
        
        // Cleanup
        Files.deleteIfExists(testFile);
        Files.deleteIfExists(outputFile);
        
        System.out.println("✅ End-to-end conversion test passed!");
        System.out.println("Generated XML: " + output.substring(0, Math.min(200, output.length())) + "...");
    }
}