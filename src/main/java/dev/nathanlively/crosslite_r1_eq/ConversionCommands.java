package dev.nathanlively.crosslite_r1_eq;

import org.jspecify.annotations.Nullable;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.component.PathInput;
import org.springframework.shell.component.PathInput.PathInputContext;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.StringInput.StringInputContext;
import org.springframework.shell.standard.AbstractShellComponent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Command(group = "Conversion")
public class ConversionCommands extends AbstractShellComponent {

    private final FileConversionService fileConversionService;

    public ConversionCommands(FileConversionService fileConversionService) {
        this.fileConversionService = fileConversionService;
    }

    @Command(command = "convert-file", description = "Convert a single CrossLite file to R1 format")
    public String convertFile(
            @Nullable @Option(longNames = "input", shortNames = 'i', description = "Input CrossLite .txt file") String inputPath,
            @Nullable @Option(longNames = "output", shortNames = 'o', description = "Output R1 .rcp file (optional)") String outputPath) {

        // If no input provided, fall back to interactive mode
        if (inputPath == null) {
            try {
                return convertFileInteractive();
            } catch (Exception e) {
                return "❌ Interactive mode not available. Use: convert-file -i \"filename.txt\"";
            }
        }

        try {
            Path input = Paths.get(inputPath);
            if (!Files.exists(input)) {
                return "Error: Input file does not exist: " + inputPath;
            }

            String actualOutputPath = outputPath != null ? outputPath : generateOutputPath(inputPath);
            fileConversionService.convertFile(inputPath, actualOutputPath);

            return String.format("✅ Successfully converted '%s' to '%s'", inputPath, actualOutputPath);
        } catch (IOException e) {
            return "❌ Error: " + e.getMessage();
        } catch (Exception e) {
            return "❌ Unexpected error: " + e.getMessage();
        }
    }

    @Command(command = "convert-directory", description = "Convert all .txt files in a directory to R1 format")
    public String convertDirectory(
            @Nullable @Option(longNames = "input", shortNames = 'i', description = "Input directory containing .txt files") String inputDir,
            @Nullable @Option(longNames = "output", shortNames = 'o', description = "Output directory (optional, defaults to input directory)") String outputDir) {

        // If no input provided, fall back to interactive mode
        if (inputDir == null) {
            try {
                return convertDirectoryInteractive();
            } catch (Exception e) {
                return "❌ Interactive mode not available. Use: convert-directory -i \".\"";
            }
        }

        try {
            Path inputPath = Paths.get(inputDir);
            if (!Files.exists(inputPath) || !Files.isDirectory(inputPath)) {
                return "Error: Input directory does not exist or is not a directory: " + inputDir;
            }

            String actualOutputDir = outputDir != null ? outputDir : inputDir;
            fileConversionService.convertDirectory(inputDir, actualOutputDir);

            return String.format("✅ Successfully converted all .txt files from '%s' to '%s'", inputDir, actualOutputDir);
        } catch (IOException e) {
            return "❌ Error: " + e.getMessage();
        } catch (Exception e) {
            return "❌ Unexpected error: " + e.getMessage();
        }
    }

    @Command(command = "list", description = "List .txt files in current directory")
    public String listFiles() {
        try {
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            try (var paths = Files.walk(currentDir, 1)) {
                var txtFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                        .map(p -> p.getFileName().toString())
                        .sorted()
                        .toList();

                if (txtFiles.isEmpty()) {
                    return "No .txt files found in current directory:\n  " + currentDir;
                }

                return "CrossLite files in current directory:\n" +
                       txtFiles.stream()
                               .map(f -> "  " + f)
                               .collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            return "❌ Error listing files: " + e.getMessage();
        }
    }

    @Command(command = "convert-interactive", description = "Interactive file converter with file browser")
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

    @Command(command = "convert-directory-interactive", description = "Interactive directory converter with file browser")
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

                Quick Commands:
                ---------------
                list                          - Show .txt files in current directory
                convert-file                  - Convert single file (interactive if no args)
                convert-directory             - Convert directory (interactive if no args)

                Direct Usage (Windows-friendly):
                ---------------------------------
                convert-file -i "filename.txt" [-o "output.rcp"]
                convert-directory -i "." [-o "output_dir"]

                Interactive Commands (Mac/Linux):
                ---------------------------------
                convert-interactive           - Interactive file converter with file browser
                convert-directory-interactive - Interactive directory converter with file browser

                Windows Users - Important:
                --------------------------
                Due to path handling limitations, for direct commands:
                1. Navigate to your files directory: cd C:\\MyFiles
                2. Use filenames only: convert-file -i "settings.txt"
                3. Use "." for current directory: convert-directory -i "."

                Examples:
                ---------
                list
                convert-file -i "eq_settings.txt"
                convert-directory -i "." -o "converted"

                Notes:
                ------
                • Gain values clamped to R1 limits (-18dB to +12dB)
                • Q factor values clamped to R1 limits (0.1 to 25.0)
                • R1 format supports up to 16 EQ bands
                • Output files get .rcp extension automatically
                """;
    }

    private String generateOutputPath(String inputPath) {
        return inputPath.replaceAll("\\.txt$", ".rcp");
    }
}