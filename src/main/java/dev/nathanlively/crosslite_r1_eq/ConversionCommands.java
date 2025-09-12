package dev.nathanlively.crosslite_r1_eq;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command line interface for CrossLite to R1 EQ conversion.
 */
@Command(group = "Conversion")
public class ConversionCommands {
    
    private final FileConversionService fileConversionService;

    public ConversionCommands(FileConversionService fileConversionService) {
        this.fileConversionService = fileConversionService;
    }
    
    @Command(command = "convert-file", description = "Convert a single CrossLite file to R1 format")
    public String convertFile(
            @Option(longNames = "input", shortNames = 'i', description = "Input CrossLite .txt file", required = true) String inputPath,
            @Option(longNames = "output", shortNames = 'o', description = "Output R1 .rcp file (optional)") String outputPath) {
        
        try {
            Path input = Paths.get(inputPath);
            if (!Files.exists(input)) {
                return "Error: Input file does not exist: " + inputPath;
            }
            
            String actualOutputPath = outputPath != null ? outputPath : generateOutputPath(inputPath);
            fileConversionService.convertFile(inputPath, actualOutputPath);
            
            return String.format("Successfully converted '%s' to '%s'", inputPath, actualOutputPath);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Unexpected error: " + e.getMessage();
        }
    }
    
    @Command(command = "convert-directory", description = "Convert all .txt files in a directory to R1 format")
    public String convertDirectory(
            @Option(longNames = "input", shortNames = 'i', description = "Input directory containing .txt files", required = true) String inputDir,
            @Option(longNames = "output", shortNames = 'o', description = "Output directory (optional, defaults to input directory)") String outputDir) {
        
        try {
            String actualOutputDir = outputDir != null ? outputDir : inputDir;
            fileConversionService.convertDirectory(inputDir, actualOutputDir);
            
            return String.format("Successfully converted all .txt files from '%s' to '%s'", inputDir, actualOutputDir);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Unexpected error: " + e.getMessage();
        }
    }
    
    @Command(command = "help-conversion", description = "Show detailed help for conversion commands")
    public String helpConversion() {
        return """
                CrossLite to R1 EQ Converter
                ============================
                
                This tool converts CrossLite EQ settings (.txt files) to d&b R1 format (.rcp files).
                
                Commands:
                
                convert-file --input <file.txt> [--output <file.rcp>]
                    Convert a single file. If output is not specified, creates file.rcp in the same directory.
                    
                convert-directory --input <directory> [--output <directory>]
                    Convert all .txt files in a directory. If output directory is not specified, 
                    creates .rcp files in the same directory as the input files.
                
                Examples:
                    convert-file -i example1.txt
                    convert-file -i example1.txt -o converted/example1.rcp
                    convert-directory -i ./crosslite-files
                    convert-directory -i ./crosslite-files -o ./r1-files
                
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
