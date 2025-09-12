package dev.nathanlively.crosslite_r1_eq;

import dev.nathanlively.crosslite_r1_eq.converter.EqConverter;
import dev.nathanlively.crosslite_r1_eq.parser.CrossLiteParser;
import dev.nathanlively.crosslite_r1_eq.writer.R1Writer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ManualConversionTest {

    @TempDir
    Path tempDir;
    private FileConversionService service;

    @BeforeEach
    void setUp() {
        CrossLiteParser parser = new CrossLiteParser();
        EqConverter converter = new EqConverter();
        R1Writer writer = new R1Writer();
        service = new FileConversionService(parser, converter, writer);
    }

    @Test
    void shouldPerformEndToEndConversion() throws IOException {
        

        // Create a test file
        Path testFile = tempDir.resolve("test_conversion.txt");
        String testContent = """
            Layer 1
            IIR Bypassed.L 1 Ch 1
            1) 1Parametric EQ
            Frequency= 1001.0Hz Gain= -6.0dB Qbp= 0.750
            Magnitude Mode Normal, Phase Mode Normal.
            """;

        Files.writeString(testFile, testContent);

        Path outputFile = tempDir.resolve("test_conversion.rcp");

        // Perform conversion
        service.convertFile(testFile.toString(), outputFile.toString());

        // Verify output exists and has correct content
        assertThat(Files.exists(outputFile)).isTrue();
        String output = Files.readString(outputFile);

        assertThat(output).startsWith("<R1EQSETTINGS_20>");
        assertThat(output).contains("<F_1>1001.000000</F_1>");
        assertThat(output).contains("<G_1>-6.000000</G_1>");
        assertThat(output).contains("<Q_1>0.750000</Q_1>");
        assertThat(output).contains("<E_1>1.000000</E_1>");
        assertThat(output).endsWith("</R1EQSETTINGS_20>");

        

        System.out.println("✅ End-to-end conversion test passed!");
        System.out.println("Generated XML: " + output.substring(0, Math.min(200, output.length())) + "...");
    }
}