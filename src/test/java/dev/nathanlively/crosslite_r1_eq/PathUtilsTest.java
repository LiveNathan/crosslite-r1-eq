package dev.nathanlively.crosslite_r1_eq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PathUtilsTest {

    @Test
    void reconstructWindowsPath_shouldHandleBasicMangledPath() {
        String mangled = "C:UserswW.PoundThisHine.c0mDownloadsmEq.txt";
        String expected = "C:\\Users\\wW.PoundThisHine.c0m\\Downloads\\mEq.txt";

        String result = PathUtils.reconstructWindowsPath(mangled);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void reconstructWindowsPath_shouldHandleSimpleMangledPath() {
        String mangled = "C:UsersjohnDownloadsfile.txt";
        String expected = "C:\\Users\\john\\Downloads\\file.txt";

        String result = PathUtils.reconstructWindowsPath(mangled);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void reconstructWindowsPath_shouldHandleDocumentsFolder() {
        String mangled = "C:UsersAdminDocumentstest.txt";
        String expected = "C:\\Users\\Admin\\Documents\\test.txt";

        String result = PathUtils.reconstructWindowsPath(mangled);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void reconstructWindowsPath_shouldHandleDesktopFolder() {
        String mangled = "C:UsersjaneDesktopproject.txt";
        String expected = "C:\\Users\\jane\\Desktop\\project.txt";

        String result = PathUtils.reconstructWindowsPath(mangled);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void reconstructWindowsPath_shouldReturnOriginalIfNotMangledWindowsPath() {
        String normal = "/home/user/file.txt";

        String result = PathUtils.reconstructWindowsPath(normal);

        assertThat(result).isEqualTo(normal);
    }

    @Test
    void reconstructWindowsPath_shouldReturnOriginalIfAlreadyCorrect() {
        String correct = "C:\\Users\\test\\file.txt";

        String result = PathUtils.reconstructWindowsPath(correct);

        assertThat(result).isEqualTo(correct);
    }

    @Test
    void resolvePath_shouldReturnExistingPath(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);

        Path result = PathUtils.resolvePath(testFile.toString());

        assertThat(result).isEqualTo(testFile);
        assertThat(Files.exists(result)).isTrue();
    }

    @Test
    void resolvePath_shouldTryReconstructionOnWindows(@TempDir Path tempDir) throws Exception {
        // Skip this test on non-Windows systems
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }

        // Create a file with a path that could be mangled
        Path actualFile = tempDir.resolve("Users").resolve("test").resolve("file.txt");
        Files.createDirectories(actualFile.getParent());
        Files.createFile(actualFile);

        // Simulate mangled path (backslashes stripped)
        String mangledPath = actualFile.toString().replace("\\", "");

        Path result = PathUtils.resolvePath(mangledPath);

        assertThat(Files.exists(result)).isTrue();
    }

    @Test
    void resolvePath_shouldReturnOriginalPathIfReconstructionFails() {
        String nonExistentPath = "C:NonExistentPathdoesnotexist.txt";

        Path result = PathUtils.resolvePath(nonExistentPath);

        assertThat(result.toString()).contains(nonExistentPath);
    }
}