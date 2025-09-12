package dev.nathanlively.crosslite_r1_eq.parser;

import dev.nathanlively.crosslite_r1_eq.domain.CrossLiteSettings;
import dev.nathanlively.crosslite_r1_eq.domain.EqBand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses CrossLite EQ settings from text content.
 */
@Component
public class CrossLiteParser {
    
    private static final Pattern EQ_PATTERN = Pattern.compile(
        "Frequency=\\s*(\\d+(?:\\.\\d+)?)Hz\\s+Gain=\\s*(-?\\d+(?:\\.\\d+)?)dB\\s+Qbp=\\s*(\\d+(?:\\.\\d+)?)"
    );
    
    public CrossLiteSettings parse(String content) {
        List<EqBand> eqBands = new ArrayList<>();
        Matcher matcher = EQ_PATTERN.matcher(content);
        
        while (matcher.find()) {
            double frequency = Double.parseDouble(matcher.group(1));
            double gain = Double.parseDouble(matcher.group(2));
            double qFactor = Double.parseDouble(matcher.group(3));
            
            eqBands.add(new EqBand(frequency, gain, qFactor));
        }
        
        return new CrossLiteSettings(eqBands);
    }
}