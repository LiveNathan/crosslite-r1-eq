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

        // On Windows, try reconstructing the path
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            String reconstructed = reconstructWindowsPath(pathString);
            if (!reconstructed.equals(pathString)) {
                Path altPath = Paths.get(reconstructed);
                if (Files.exists(altPath)) {
                    return altPath;
                }
            }
        }

        // Return original path (will likely fail downstream with better error)
        return path;
    }

    /**
     * Attempt to reconstruct a Windows path that may have had backslashes stripped.
     */
    public static String reconstructWindowsPath(String mangledPath) {
        // If it doesn't look like a mangled Windows path, return as-is
        if (!mangledPath.matches("^[A-Za-z]:[A-Za-z].*")) {
            return mangledPath;
        }

        StringBuilder result = new StringBuilder();
        String remaining = mangledPath;

        // Handle drive letter (C:)
        result.append(remaining.substring(0, 2)).append('\\');
        remaining = remaining.substring(2);

        // Handle known Windows folder patterns
        String[] knownFolders = {"Users", "Downloads", "Documents", "Desktop", "Program Files", "Windows"};

        while (!remaining.isEmpty()) {
            boolean foundFolder = false;

            // Check if remaining starts with a known folder
            for (String folder : knownFolders) {
                if (remaining.startsWith(folder)) {
                    result.append(folder);
                    remaining = remaining.substring(folder.length());

                    // Add backslash if there's more content and it's not already a backslash
                    if (!remaining.isEmpty() && !remaining.startsWith("\\")) {
                        result.append('\\');
                    }
                    foundFolder = true;
                    break;
                }
            }

            if (!foundFolder) {
                // Check if this looks like a filename (contains a dot followed by 2-4 characters)
                if (remaining.matches(".*\\.[a-zA-Z]{2,4}$")) {
                    // This is likely a filename, don't split it
                    result.append(remaining);
                    break;
                }

                // If no known folder found, look for the next capital letter or end
                // This handles usernames and other folder names
                int nextCapital = findNextCapitalOrKnownFolder(remaining, knownFolders);

                if (nextCapital > 0) {
                    result.append(remaining.substring(0, nextCapital)).append('\\');
                    remaining = remaining.substring(nextCapital);
                } else {
                    // No more folders, append the rest (likely filename)
                    result.append(remaining);
                    break;
                }
            }
        }

        return result.toString();
    }

    private static int findNextCapitalOrKnownFolder(String text, String[] knownFolders) {
        // First check for known folders
        for (String folder : knownFolders) {
            int index = text.indexOf(folder);
            if (index > 0) {
                return index;
            }
        }

        // Look for capital letters (but skip the first character)
        // Also check if this might be a filename by looking for extensions
        for (int i = 1; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                // Check if the rest of the string from this point looks like a filename
                String remaining = text.substring(i);
                if (remaining.matches(".*\\.[a-zA-Z]{2,4}$")) {
                    // This capital letter is part of a filename, don't split here
                    continue;
                }
                return i;
            }
        }

        return -1; // Not found
    }
}