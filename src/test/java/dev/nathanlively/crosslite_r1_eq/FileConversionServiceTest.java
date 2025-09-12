package dev.nathanlively.crosslite_r1_eq;

import dev.nathanlively.crosslite_r1_eq.converter.EqConverter;
import dev.nathanlively.crosslite_r1_eq.parser.CrossLiteParser;
import dev.nathanlively.crosslite_r1_eq.writer.R1Writer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileConversionServiceTest {
    
    @Autowired
    private CrossLiteParser parser;
    
    @Autowired
    private EqConverter converter;
    
    @Autowired
    private R1Writer writer;
    
    @TempDir
    Path tempDir;
    
    @Test
    void shouldConvertExample1File() throws IOException {
        FileConversionService service = new FileConversionService(parser, converter, writer);
        
        // Copy example file to temp directory
        ClassPathResource resource = new ClassPathResource("example1.txt");
        Path inputFile = tempDir.resolve("example1.txt");
        Files.copy(resource.getInputStream(), inputFile);
        
        Path outputFile = tempDir.resolve("example1.rcp");
        
        service.convertFile(inputFile.toString(), outputFile.toString());
        
        assertTrue(Files.exists(outputFile));
        String content = Files.readString(outputFile);
        
        // Verify basic structure
        assertTrue(content.startsWith("<R1EQSETTINGS_20>"));
        assertTrue(content.endsWith("</R1EQSETTINGS_20>"));
        
        // Verify first filter is enabled with correct values
        assertTrue(content.contains("<F_1>1001.000000</F_1>"));
        assertTrue(content.contains("<Q_1>0.750000</Q_1>"));
        assertTrue(content.contains("<G_1>-6.000000</G_1>"));
        assertTrue(content.contains("<E_1>1.000000</E_1>"));
        
        // Verify second filter is disabled
        assertTrue(content.contains("<E_2>0.000000</E_2>"));
    }
    
    @Test
    void shouldConvertExample2File() throws IOException {
        FileConversionService service = new FileConversionService(parser, converter, writer);
        
        ClassPathResource resource = new ClassPathResource("example2.txt");
        Path inputFile = tempDir.resolve("example2.txt");
        Files.copy(resource.getInputStream(), inputFile);
        
        Path outputFile = tempDir.resolve("example2.rcp");
        
        service.convertFile(inputFile.toString(), outputFile.toString());
        
        assertTrue(Files.exists(outputFile));
        String content = Files.readString(outputFile);
        
        // Verify two filters are enabled
        assertTrue(content.contains("<E_1>1.000000</E_1>"));
        assertTrue(content.contains("<E_2>1.000000</E_2>"));
        assertTrue(content.contains("<E_3>0.000000</E_3>"));
        
        // Verify values
        assertTrue(content.contains("<F_1>1001.000000</F_1>"));
        assertTrue(content.contains("<F_2>102.000000</F_2>"));
        assertTrue(content.contains("<G_2>4.300000</G_2>"));
    }
    
    @Test
    void shouldConvertDirectoryWithMultipleFiles() throws IOException {
        FileConversionService service = new FileConversionService(parser, converter, writer);
        
        // Create input directory with test files
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(inputDir);
        
        // Copy example files
        ClassPathResource example1 = new ClassPathResource("example1.txt");
        ClassPathResource example2 = new ClassPathResource("example2.txt");
        
        Files.copy(example1.getInputStream(), inputDir.resolve("example1.txt"));
        Files.copy(example2.getInputStream(), inputDir.resolve("example2.txt"));
        
        service.convertDirectory(inputDir.toString(), outputDir.toString());
        
        // Verify output files exist
        assertTrue(Files.exists(outputDir.resolve("example1.rcp")));
        assertTrue(Files.exists(outputDir.resolve("example2.rcp")));
        
        // Verify content of one file
        String content = Files.readString(outputDir.resolve("example1.rcp"));
        assertTrue(content.contains("<F_1>1001.000000</F_1>"));
    }
    
    @Test
    void shouldThrowExceptionForNonExistentFile() {
        FileConversionService service = new FileConversionService(parser, converter, writer);
        
        assertThrows(IOException.class, () -> 
            service.convertFile("nonexistent.txt", "output.rcp"));
    }
    
    @Test
    void shouldThrowExceptionForNonExistentDirectory() {
        FileConversionService service = new FileConversionService(parser, converter, writer);
        
        assertThrows(IllegalArgumentException.class, () -> 
            service.convertDirectory("nonexistent", "output"));
    }
}