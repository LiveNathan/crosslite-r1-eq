package dev.nathanlively.crosslite_r1_eq.parser;

import dev.nathanlively.crosslite_r1_eq.domain.CrossLiteSettings;
import dev.nathanlively.crosslite_r1_eq.domain.EqBand;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CrossLiteParser {

    private static final Pattern EQ_PATTERN = Pattern.compile(
            "Frequency=\\s*([\\d.]+)Hz\\s+Gain=\\s*([\\d.-]+)dB\\s+Qbp=\\s*([\\d.]+)"
    );

    private static final Pattern FIRST_CHANNEL_PATTERN = Pattern.compile(
            "IIR Bypassed\\.(.+)"
    );

    /**
     * Parse a single-channel file (backward compatibility).
     */
    public CrossLiteSettings parse(String content) {
        Map<String, CrossLiteSettings> channels = parseMultiChannel(content);
        if (channels.isEmpty()) {
            return new CrossLiteSettings(List.of());
        }
        // Return the first (and likely only) channel
        return channels.values().iterator().next();
    }

    /**
     * Parse a multi-channel file, returning a map of channel names to settings.
     */
    public Map<String, CrossLiteSettings> parseMultiChannel(String content) {
        Map<String, CrossLiteSettings> channels = new LinkedHashMap<>();
        List<String> lines = content.lines().toList();

        // First, check if this looks like a multi-channel file
        boolean hasMultipleChannels = detectMultipleChannels(lines);

        if (!hasMultipleChannels) {
            // Single channel file - parse all EQ bands as default
            List<EqBand> bands = parseAllEqBands(content);
            if (!bands.isEmpty()) {
                channels.put("default", new CrossLiteSettings(bands));
            }
            return channels;
        }

        // Multi-channel file - parse by sections
        List<ChannelSection> sections = identifyChannelSections(lines);

        for (ChannelSection section : sections) {
            List<EqBand> bands = parseEqBandsFromLines(lines, section.startLine, section.endLine);
            if (!bands.isEmpty()) {  // Skip channels with no EQ bands
                channels.put(section.name, new CrossLiteSettings(bands));
            }
        }

        return channels;
    }

    private boolean detectMultipleChannels(List<String> lines) {
        // Look for clear indicators of multiple channels:
        // 1. Multiple "IIR Bypassed.XXX" patterns with different names
        // 2. Standalone channel names followed by IIR lines

        Set<String> channelNames = new HashSet<>();
        boolean foundFirstChannel = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Check for "IIR Bypassed.channelName"
            Matcher firstChannelMatcher = FIRST_CHANNEL_PATTERN.matcher(line);
            if (firstChannelMatcher.matches()) {
                String name = firstChannelMatcher.group(1);
                // Ignore metadata-like names
                if (!looksLikeMetadata(name)) {
                    channelNames.add(name);
                    foundFirstChannel = true;
                }
            }

            // After finding first channel, look for standalone channel names
            if (foundFirstChannel &&
                !line.isEmpty() &&
                !line.startsWith("IIR") &&
                !line.startsWith("Layer") &&
                !line.contains("Parametric EQ") &&
                !line.contains("Magnitude Mode") &&
                !line.contains("biquad") &&
                !line.contains("=") &&
                !line.matches("^\\d+\\).*")) {

                // Check if next non-empty line is IIR-related or EQ
                for (int j = i + 1; j < lines.size(); j++) {
                    String nextLine = lines.get(j).trim();
                    if (nextLine.isEmpty()) continue;

                    if (nextLine.startsWith("IIR") ||
                        nextLine.contains("Parametric EQ") ||
                        nextLine.contains("Frequency=")) {
                        // This could be a channel name
                        channelNames.add(line);
                        break;
                    }
                    break; // Stop at first non-empty line
                }
            }
        }

        return channelNames.size() > 1;
    }

    private List<ChannelSection> identifyChannelSections(List<String> lines) {
        List<ChannelSection> sections = new ArrayList<>();
        String currentChannel = null;
        int channelStartLine = -1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (line.isEmpty()) continue;

            // Check for "IIR Bypassed.channelName" (first channel pattern)
            Matcher firstChannelMatcher = FIRST_CHANNEL_PATTERN.matcher(line);
            if (firstChannelMatcher.matches()) {
                // Save previous section if exists
                if (currentChannel != null) {
                    sections.add(new ChannelSection(currentChannel, channelStartLine, i));
                }
                currentChannel = firstChannelMatcher.group(1);
                channelStartLine = i;
                continue;
            }

            // Check for standalone channel name followed by IIR Crossover
            if (!line.startsWith("IIR") &&
                !line.startsWith("Layer") &&
                !line.contains("Parametric EQ") &&
                !line.contains("Magnitude Mode") &&
                !line.contains("biquad") &&
                !line.contains("=") &&
                !line.matches("^\\d+\\).*") &&
                i + 1 < lines.size()) {

                String nextLine = lines.get(i + 1).trim();
                // In real data, channel names are always followed by IIR Crossover
                if (nextLine.startsWith("IIR Crossover")) {
                    // Save previous section
                    if (currentChannel != null) {
                        sections.add(new ChannelSection(currentChannel, channelStartLine, i));
                    }
                    currentChannel = line;
                    channelStartLine = i;
                }
            }
        }

        // Add final section
        if (currentChannel != null) {
            sections.add(new ChannelSection(currentChannel, channelStartLine, lines.size()));
        }

        return sections;
    }

    private List<EqBand> parseEqBandsFromLines(List<String> lines, int startLine, int endLine) {
        List<EqBand> bands = new ArrayList<>();

        for (int i = startLine; i < endLine && i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher eqMatcher = EQ_PATTERN.matcher(line);
            if (eqMatcher.find()) {
                double frequency = Double.parseDouble(eqMatcher.group(1));
                double gain = Double.parseDouble(eqMatcher.group(2));
                double qFactor = Double.parseDouble(eqMatcher.group(3));
                bands.add(new EqBand(frequency, gain, qFactor));
            }
        }

        return bands;
    }

    private List<EqBand> parseAllEqBands(String content) {
        List<EqBand> bands = new ArrayList<>();
        Matcher eqMatcher = EQ_PATTERN.matcher(content);
        while (eqMatcher.find()) {
            double frequency = Double.parseDouble(eqMatcher.group(1));
            double gain = Double.parseDouble(eqMatcher.group(2));
            double qFactor = Double.parseDouble(eqMatcher.group(3));
            bands.add(new EqBand(frequency, gain, qFactor));
        }
        return bands;
    }

    private boolean looksLikeMetadata(String channelName) {
        // Check if the channel name looks like metadata rather than a real channel name
        // Examples: "L 1 Ch 1", "Layer 1 Channel 1", etc.
        return channelName.matches(".*\\bL\\s*\\d+\\s*Ch\\s*\\d+.*") ||
               channelName.matches(".*Layer\\s*\\d+.*") ||
               channelName.matches(".*Channel\\s*\\d+.*");
    }

    private static class ChannelSection {
        final String name;
        final int startLine;
        final int endLine;

        ChannelSection(String name, int startLine, int endLine) {
            this.name = name;
            this.startLine = startLine;
            this.endLine = endLine;
        }
    }
}