package dev.nathanlively.crosslite_r1_eq;

import dev.nathanlively.crosslite_r1_eq.converter.EqConverter;
import dev.nathanlively.crosslite_r1_eq.domain.CrossLiteSettings;
import dev.nathanlively.crosslite_r1_eq.domain.R1Settings;
import dev.nathanlively.crosslite_r1_eq.parser.CrossLiteParser;
import dev.nathanlively.crosslite_r1_eq.writer.R1Writer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service for converting CrossLite files to R1 format.
 */
@Service
public class FileConversionService {

    private final CrossLiteParser parser;
    private final EqConverter converter;
    private final R1Writer writer;

    public FileConversionService(CrossLiteParser parser, EqConverter converter, R1Writer writer) {
        this.parser = parser;
        this.converter = converter;
        this.writer = writer;
    }

    public void convertFile(String inputPath, String outputPath) throws IOException {
        String content = Files.readString(PathUtils.resolvePath(inputPath));
        CrossLiteSettings crossLiteSettings = parser.parse(content);
        R1Settings r1Settings = converter.convert(crossLiteSettings);
        String xmlOutput = writer.writeToXml(r1Settings);

        Path output = Paths.get(outputPath);
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        Files.writeString(output, xmlOutput);
    }

    public void convertDirectory(String inputDir, String outputDir) throws IOException {
        Path inputPath = PathUtils.resolvePath(inputDir);
        Path outputPath = Paths.get(outputDir);

        if (!Files.exists(inputPath) || !Files.isDirectory(inputPath)) {
            throw new IllegalArgumentException("Input directory does not exist: " + inputDir);
        }

        Files.createDirectories(outputPath);

        try (Stream<Path> files = Files.walk(inputPath)) {
            List<Path> txtFiles = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                    .toList();

            for (Path txtFile : txtFiles) {
                String relativePath = inputPath.relativize(txtFile).toString();
                String outputFileName = relativePath.replaceAll("\\.txt$", ".rcp");
                Path outputFile = outputPath.resolve(outputFileName);

                convertFile(txtFile.toString(), outputFile.toString());
            }
        }
    }

    private String generateOutputPath(String inputPath) {
        return inputPath.replaceAll("\\.txt$", ".rcp");
    }
}
