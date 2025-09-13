package dev.nathanlively.crosslite_r1_eq;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.PathInput;
import org.springframework.shell.component.PathInput.PathInputContext;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.StringInput.StringInputContext;
import org.springframework.shell.standard.AbstractShellComponent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Command(group = "Conversion")
public class ConversionCommands extends AbstractShellComponent {

    private final FileConversionService fileConversionService;

    public ConversionCommands(FileConversionService fileConversionService) {
        this.fileConversionService = fileConversionService;
    }

    @Command(command = "convert", description = "Select file to convert")
    public String convertFileInteractive() {
        // Get input file
        PathInput inputComponent = new PathInput(getTerminal(), "Enter CrossLite file path (.txt):");
        inputComponent.setResourceLoader(getResourceLoader());
        inputComponent.setTemplateExecutor(getTemplateExecutor());
        PathInputContext inputContext = inputComponent.run(PathInputContext.empty());

        Path inputPath = inputContext.getResultValue();
        if (inputPath == null) {
            return "No input file selected. Conversion cancelled.";
        }
        if (!Files.exists(inputPath)) {
            return "Error: File does not exist: " + inputPath;
        }
        if (!inputPath.toString().toLowerCase().endsWith(".txt")) {
            StringInput confirmComponent = new StringInput(getTerminal(),
                    "File doesn't have .txt extension. Continue anyway? (yes/no):",
                    "no");
            confirmComponent.setResourceLoader(getResourceLoader());
            confirmComponent.setTemplateExecutor(getTemplateExecutor());
            StringInputContext confirmContext = confirmComponent.run(StringInputContext.empty());

            if (!"yes".equalsIgnoreCase(confirmContext.getResultValue())) {
                return "Conversion cancelled.";
            }
        }

        // Ask if user wants custom output path
        StringInput customOutputComponent = new StringInput(getTerminal(),
                "Use custom output path? (yes/no):",
                "no");
        customOutputComponent.setResourceLoader(getResourceLoader());
        customOutputComponent.setTemplateExecutor(getTemplateExecutor());
        StringInputContext customOutputContext = customOutputComponent.run(StringInputContext.empty());

        String outputPath;
        if ("yes".equalsIgnoreCase(customOutputContext.getResultValue())) {
            // Get custom output path
            PathInput outputComponent = new PathInput(getTerminal(),
                    "Enter output file path (.rcp):");
            outputComponent.setResourceLoader(getResourceLoader());
            outputComponent.setTemplateExecutor(getTemplateExecutor());
            PathInputContext outputContext = outputComponent.run(PathInputContext.empty());

            Path customPath = outputContext.getResultValue();
            if (customPath == null) {
                // Use default
                outputPath = generateOutputPath(inputPath.toString());
                System.out.println("Using default output path: " + outputPath);
            } else {
                outputPath = customPath.toString();
                // Ensure .rcp extension
                if (!outputPath.toLowerCase().endsWith(".rcp")) {
                    outputPath = outputPath + ".rcp";
                }
            }
        } else {
            outputPath = generateOutputPath(inputPath.toString());
        }

        // Perform conversion
        try {
            fileConversionService.convertFile(inputPath.toString(), outputPath);
            return String.format("✅ Successfully converted:\n  From: %s\n  To:   %s",
                    inputPath, outputPath);
        } catch (IOException e) {
            return "❌ Error during conversion: " + e.getMessage();
        }
    }

    @Command(command = "convert-directory", description = "Select directory to convert")
    public String convertDirectoryInteractive() {
        // Get input directory
        PathInput inputComponent = new PathInput(getTerminal(),
                "Enter directory containing CrossLite files:");
        inputComponent.setResourceLoader(getResourceLoader());
        inputComponent.setTemplateExecutor(getTemplateExecutor());
        PathInputContext inputContext = inputComponent.run(PathInputContext.empty());

        Path inputDir = inputContext.getResultValue();
        if (inputDir == null) {
            return "No directory selected. Conversion cancelled.";
        }

        // Validate directory
        if (!Files.exists(inputDir)) {
            return "Error: Directory does not exist: " + inputDir;
        }
        if (!Files.isDirectory(inputDir)) {
            return "Error: Path is not a directory: " + inputDir;
        }

        // Count .txt files
        long txtFileCount;
        try {
            try (var paths = Files.walk(inputDir)) {
                txtFileCount = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                        .count();
            }
        } catch (IOException e) {
            return "Error scanning directory: " + e.getMessage();
        }

        if (txtFileCount == 0) {
            return "No .txt files found in directory: " + inputDir;
        }

        // Show count and ask for confirmation
        StringInput confirmComponent = new StringInput(getTerminal(),
                String.format("Found %d .txt file(s). Convert all? (yes/no):", txtFileCount),
                "yes");
        confirmComponent.setResourceLoader(getResourceLoader());
        confirmComponent.setTemplateExecutor(getTemplateExecutor());
        StringInputContext confirmContext = confirmComponent.run(StringInputContext.empty());

        if (!"yes".equalsIgnoreCase(confirmContext.getResultValue())) {
            return "Conversion cancelled.";
        }

        // Ask for output directory
        StringInput customOutputComponent = new StringInput(getTerminal(),
                "Use custom output directory? (yes/no):",
                "no");
        customOutputComponent.setResourceLoader(getResourceLoader());
        customOutputComponent.setTemplateExecutor(getTemplateExecutor());
        StringInputContext customOutputContext = customOutputComponent.run(StringInputContext.empty());

        String outputDir;
        if ("yes".equalsIgnoreCase(customOutputContext.getResultValue())) {
            PathInput outputComponent = new PathInput(getTerminal(),
                    "Enter output directory:");
            outputComponent.setResourceLoader(getResourceLoader());
            outputComponent.setTemplateExecutor(getTemplateExecutor());
            PathInputContext outputContext = outputComponent.run(PathInputContext.empty());

            Path customPath = outputContext.getResultValue();
            if (customPath == null) {
                outputDir = inputDir.toString(); // Same as input
                System.out.println("Using input directory as output: " + outputDir);
            } else {
                outputDir = customPath.toString();
            }
        } else {
            outputDir = inputDir.toString(); // Same directory as input
        }

        // Perform conversion
        try {
            fileConversionService.convertDirectory(inputDir.toString(), outputDir);
            return String.format("✅ Successfully converted %d file(s) from:\n  %s\nto:\n  %s",
                    txtFileCount, inputDir, outputDir);
        } catch (IOException e) {
            return "❌ Error during conversion: " + e.getMessage();
        }
    }

    @Command(command = "help", description = "Show help for conversion commands")
    public String helpConversion() {
        return """
                CrossLite to R1 EQ Converter
                ============================

                Interactive Commands (Recommended for Windows users):
                ------------------------------------------------------
                convert                       - Main interactive converter with menu
                convert-directory             - Interactive directory conversion

                Direct Commands:
                ----------------
                convert -i <file> [-o <output>]
                convert-directory -i <dir> [-o <output-dir>]

                Notes:
                - Gain values are clamped to R1 limits (-18dB to +12dB)
                - Q factor values are clamped to R1 limits (0.1 to 25.0)
                - R1 format supports up to 16 EQ bands
                """;
    }

    private String generateOutputPath(String inputPath) {
        return inputPath.replaceAll("\\.txt$", ".rcp");
    }
}