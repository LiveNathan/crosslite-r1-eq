package dev.nathanlively.crosslite_r1_eq;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Path utilities for cross-platform path handling.
 */
public final class PathUtils {

    private PathUtils() {
        // Utility class
    }

    /**
     * Resolve a path string, trying multiple variations for Windows compatibility.
     */
    public static Path resolvePath(String pathString) {
        // Try original path first
        Path path = Paths.get(pathString);
        if (Files.exists(path)) {
            return path;
        }

        // On Windows, try converting slashes if original doesn't exist
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Try with backslashes converted to forward slashes
            String forwardSlashes = pathString.replace('\\', '/');
            if (!forwardSlashes.equals(pathString)) {
                Path altPath = Paths.get(forwardSlashes);
                if (Files.exists(altPath)) {
                    return altPath;
                }
            }

            // Try with forward slashes converted to backslashes
            String backSlashes = pathString.replace('/', '\\');
            if (!backSlashes.equals(pathString)) {
                Path altPath = Paths.get(backSlashes);
                if (Files.exists(altPath)) {
                    return altPath;
                }
            }
        }

        // Return original path (will likely fail downstream with better error)
        return path;
    }
}