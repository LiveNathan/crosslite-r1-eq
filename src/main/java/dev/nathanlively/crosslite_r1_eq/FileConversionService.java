package dev.nathanlively.crosslite_r1_eq;

import dev.nathanlively.crosslite_r1_eq.converter.EqConverter;
import dev.nathanlively.crosslite_r1_eq.domain.CrossLiteSettings;
import dev.nathanlively.crosslite_r1_eq.domain.R1Settings;
import dev.nathanlively.crosslite_r1_eq.parser.CrossLiteParser;
import dev.nathanlively.crosslite_r1_eq.writer.R1Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class FileConversionService {

    private static final Logger log = LoggerFactory.getLogger(FileConversionService.class);

    private final CrossLiteParser parser;
    private final EqConverter converter;
    private final R1Writer writer;

    public FileConversionService(CrossLiteParser parser, EqConverter converter, R1Writer writer) {
        this.parser = parser;
        this.converter = converter;
        this.writer = writer;
    }

    /**
     * Convert a file, automatically detecting single vs multi-channel.
     * For single-channel or single-output files, uses outputPath as full file path.
     * For multi-channel files, uses outputPath as directory.
     */
    public void convertFile(String inputPath, String outputPath) throws IOException {
        String content = Files.readString(Paths.get(inputPath));
        Map<String, CrossLiteSettings> channels = parser.parseMultiChannel(content);

        if (channels.isEmpty()) {
            log.warn("No EQ bands found in file: {}", inputPath);
            return;
        }

        // Check if this should be treated as a single-channel file
        if (isSingleChannelOutput(channels)) {
            // Single channel file - use outputPath as full file path
            CrossLiteSettings settings = channels.values().iterator().next();
            convertSingleChannel(settings, outputPath);
        } else {
            // Multi-channel file - use outputPath as directory
            convertMultipleChannels(channels, inputPath, outputPath);
        }
    }

    private boolean isSingleChannelOutput(Map<String, CrossLiteSettings> channels) {
        // Treat as single-channel if:
        // 1. Only one channel AND it's named "default"
        // 2. Only one channel AND it looks like metadata (backward compatibility)
        if (channels.size() != 1) {
            return false;
        }

        String channelName = channels.keySet().iterator().next();
        return "default".equals(channelName);
    }

    private void convertSingleChannel(CrossLiteSettings settings, String outputPath) throws IOException {
        R1Settings r1Settings = converter.convert(settings);
        String xmlOutput = writer.writeToXml(r1Settings);

        Path output = Paths.get(outputPath);
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        Files.writeString(output, xmlOutput);
        log.info("Converted single channel to: {}", outputPath);
    }

    private void convertMultipleChannels(Map<String, CrossLiteSettings> channels,
                                         String inputPath, String outputPath) throws IOException {
        Path outputDir;

        // Determine output directory
        if (outputPath.endsWith(".rcp")) {
            // User provided a file path, use its parent directory
            outputDir = Paths.get(outputPath).getParent();
            if (outputDir == null) {
                outputDir = Paths.get(".");
            }
            log.info("Multiple channels detected, using directory: {}", outputDir);
        } else {
            // Use as directory
            outputDir = Paths.get(outputPath);
        }

        Files.createDirectories(outputDir);

        int converted = 0;
        for (Map.Entry<String, CrossLiteSettings> entry : channels.entrySet()) {
            String channelName = sanitizeFilename(entry.getKey());
            CrossLiteSettings settings = entry.getValue();

            // Skip channels with no EQ bands (this should already be filtered by parser)
            if (settings.eqBands().isEmpty()) {
                log.debug("Skipping channel '{}' with no EQ bands", channelName);
                continue;
            }

            R1Settings r1Settings = converter.convert(settings);
            String xmlOutput = writer.writeToXml(r1Settings);

            Path channelOutput = outputDir.resolve(channelName + ".rcp");
            Files.writeString(channelOutput, xmlOutput);
            log.info("Converted channel '{}' to: {}", entry.getKey(), channelOutput);
            converted++;
        }

        log.info("Converted {} channels from {} to directory {}", converted, inputPath, outputDir);
    }

    private String sanitizeFilename(String name) {
        // Replace problematic characters for filenames
        return name.replaceAll("[<>:\"/\\\\|?*]", "_");
    }

    public void convertDirectory(String inputDir, String outputDir) throws IOException {
        Path inputPath = Paths.get(inputDir);
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
                String baseName = relativePath.replaceAll("\\.txt$", "");

                // First check if it's a multi-channel file
                String content = Files.readString(txtFile);
                Map<String, CrossLiteSettings> channels = parser.parseMultiChannel(content);

                if (isSingleChannelOutput(channels)) {
                    // Single channel - output directly to the output directory
                    String outputFileName = baseName + ".rcp";
                    Path outputFile = outputPath.resolve(outputFileName);
                    convertFile(txtFile.toString(), outputFile.toString());
                } else {
                    // Multi-channel - create subdirectory
                    Path fileOutputDir = outputPath.resolve(baseName);
                    convertFile(txtFile.toString(), fileOutputDir.toString());
                }
            }
        }
    }
}