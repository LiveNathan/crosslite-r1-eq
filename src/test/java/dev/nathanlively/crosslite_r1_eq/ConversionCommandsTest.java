package dev.nathanlively.crosslite_r1_eq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConversionCommandsTest {

    private ConversionCommands commands;
    private TestFileConversionService testService;

    @BeforeEach
    void setUp() {
        testService = new TestFileConversionService();
        commands = new ConversionCommands(testService);
    }

    @Test
    void convertFile_shouldHandleMangledWindowsPath(@TempDir Path tempDir) throws Exception {
        // Create a real file that represents what the user intended
        Path actualFile = tempDir.resolve("Users")
                .resolve("wW.PoundThisHine.c0m")
                .resolve("Downloads")
                .resolve("mEq.txt");
        Files.createDirectories(actualFile.getParent());
        Files.writeString(actualFile, "Frequency=1000Hz Gain=3dB Qbp=1.5");

        // Simulate what Spring Shell does - strips backslashes
        String mangledPath = actualFile.toString().replace("\\", "");

        String result = commands.convertFile(mangledPath, null);

        assertThat(result).contains("Successfully converted");
        assertThat(testService.lastInputPath).isEqualTo(actualFile.toString());
    }

    @Test
    void convertFile_shouldReturnErrorForNonExistentFile() {
        String nonExistentPath = "C:NonExistentPathdoesnotexist.txt";

        String result = commands.convertFile(nonExistentPath, null);

        assertThat(result).startsWith("Error: Input file does not exist:");
        assertThat(testService.lastInputPath).isNull(); // Should not have called service
    }

    @Test
    void convertFile_shouldHandleNormalPathsWithoutMangling(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Frequency=1000Hz Gain=3dB Qbp=1.5");

        String result = commands.convertFile(testFile.toString(), null);

        assertThat(result).contains("Successfully converted");
        assertThat(testService.lastInputPath).isEqualTo(testFile.toString());
    }

    @Test
    void convertDirectory_shouldHandleMangledWindowsPath(@TempDir Path tempDir) throws Exception {
        // Create directory structure that could be mangled
        Path actualDir = tempDir.resolve("Users")
                .resolve("test")
                .resolve("Documents");
        Files.createDirectories(actualDir);

        // Simulate mangled directory path
        String mangledPath = actualDir.toString().replace("\\", "");

        String result = commands.convertDirectory(mangledPath, null);

        assertThat(result).contains("Successfully converted all .txt files");
        assertThat(testService.lastInputDir).isEqualTo(actualDir.toString());
    }

    @Test
    void convertDirectory_shouldReturnErrorForNonExistentDirectory() {
        String nonExistentDir = "C:NonExistentDirdoesnotexist";

        String result = commands.convertDirectory(nonExistentDir, null);

        assertThat(result).startsWith("Error:");
        assertThat(testService.lastInputDir).isNull();
    }

    /**
     * Test stub for FileConversionService that records method calls.
     */
    static class TestFileConversionService extends FileConversionService {
        String lastInputPath;
        String lastOutputPath;
        String lastInputDir;
        String lastOutputDir;

        public TestFileConversionService() {
            super(null, null, null); // We won't actually use these
        }

        @Override
        public void convertFile(String inputPath, String outputPath) throws IOException {
            this.lastInputPath = inputPath;
            this.lastOutputPath = outputPath;
            // Don't actually do any file operations
        }

        @Override
        public void convertDirectory(String inputDir, String outputDir) throws IOException {
            this.lastInputDir = inputDir;
            this.lastOutputDir = outputDir;
            // Don't actually do any file operations
        }
    }
}