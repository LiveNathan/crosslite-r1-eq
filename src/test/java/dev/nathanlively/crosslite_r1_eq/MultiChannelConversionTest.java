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

class MultiChannelConversionTest {

    @TempDir
    Path tempDir;

    private FileConversionService service;

    @BeforeEach
    void setUp() {
        service = new FileConversionService(
                new CrossLiteParser(),
                new EqConverter(),
                new R1Writer()
        );
    }

    @Test
    void shouldConvertMultiChannelFile() throws IOException {
        Path inputFile = tempDir.resolve("multi.txt");
        String content = """
            Layer 1
            IIR Bypassed.ml
            1) 1Parametric EQ
            Frequency= 126.0Hz Gain= -2.0dB Qbp= 6.463
            
            c
            IIR Crossover HPF: Bypassed.
            1) 1Parametric EQ  
            Frequency= 818.0Hz Gain= 1.0dB Qbp= 3.688
            
            mr
            IIR Crossover HPF: Bypassed.
            1) 1Parametric EQ
            Frequency= 638.0Hz Gain= 0.8dB Qbp= 3.595
            """;

        Files.writeString(inputFile, content);
        Path outputDir = tempDir.resolve("output");

        service.convertFile(inputFile.toString(), outputDir.toString());

        // Verify output files created
        assertThat(Files.exists(outputDir.resolve("ml.rcp"))).isTrue();
        assertThat(Files.exists(outputDir.resolve("c.rcp"))).isTrue();
        assertThat(Files.exists(outputDir.resolve("mr.rcp"))).isTrue();

        // Verify content of one file
        String mlContent = Files.readString(outputDir.resolve("ml.rcp"));
        assertThat(mlContent).contains("<F_1>126.000000</F_1>");
        assertThat(mlContent).contains("<G_1>-2.000000</G_1>");
    }

    @Test
    void shouldSkipChannelsWithNoEqBands() throws IOException {
        Path inputFile = tempDir.resolve("sparse.txt");
        String content = """
        IIR Bypassed.hasEq
        IIR Crossover HPF: Bypassed.
        Frequency= 100.0Hz Gain= 1.0dB Qbp= 1.0
        
        emptyChannel
        IIR Crossover HPF: Bypassed.
        IIR Crossover LPF: Bypassed.
        
        anotherChannel
        IIR Crossover HPF: Bypassed.
        Frequency= 200.0Hz Gain= 2.0dB Qbp= 2.0
        """;

        Files.writeString(inputFile, content);
        Path outputDir = tempDir.resolve("output");

        service.convertFile(inputFile.toString(), outputDir.toString());

        assertThat(Files.exists(outputDir.resolve("hasEq.rcp"))).isTrue();
        assertThat(Files.exists(outputDir.resolve("anotherChannel.rcp"))).isTrue();
        assertThat(Files.exists(outputDir.resolve("emptyChannel.rcp"))).isFalse();
    }
}