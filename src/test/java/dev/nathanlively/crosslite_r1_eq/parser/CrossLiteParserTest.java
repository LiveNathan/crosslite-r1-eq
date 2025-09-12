package dev.nathanlively.crosslite_r1_eq.parser;

import dev.nathanlively.crosslite_r1_eq.domain.CrossLiteSettings;
import dev.nathanlively.crosslite_r1_eq.domain.EqBand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrossLiteParserTest {

    private final CrossLiteParser parser = new CrossLiteParser();

    @Test
    void shouldParseSingleEqBand() {
        String content = "Frequency= 1001.0Hz Gain= -6.0dB Qbp= 0.750";

        CrossLiteSettings result = parser.parse(content);

        assertThat(result.eqBands()).hasSize(1);
        EqBand band = result.eqBands().get(0);
        assertThat(band.frequency()).isEqualTo(1001.0);
        assertThat(band.gain()).isEqualTo(-6.0);
        assertThat(band.qFactor()).isEqualTo(0.750);
    }

    @Test
    void shouldParseMultipleEqBands() {
        String content = """
            Frequency= 1001.0Hz Gain= -6.0dB Qbp= 0.750
            Frequency= 102.0Hz Gain= 4.3dB Qbp= 1.200
            """;

        CrossLiteSettings result = parser.parse(content);

        assertThat(result.eqBands()).hasSize(2);

        EqBand band1 = result.eqBands().get(0);
        assertThat(band1.frequency()).isEqualTo(1001.0);
        assertThat(band1.gain()).isEqualTo(-6.0);
        assertThat(band1.qFactor()).isEqualTo(0.750);

        EqBand band2 = result.eqBands().get(1);
        assertThat(band2.frequency()).isEqualTo(102.0);
        assertThat(band2.gain()).isEqualTo(4.3);
        assertThat(band2.qFactor()).isEqualTo(1.200);
    }

    @Test
    void shouldParseExtremeValues() {
        String content = """
            Frequency= 1001.0Hz Gain= -40.0dB Qbp= 0.100
            Frequency= 102.0Hz Gain= 40.0dB Qbp= 128.000
            """;

        CrossLiteSettings result = parser.parse(content);

        assertThat(result.eqBands()).hasSize(2);

        EqBand band1 = result.eqBands().get(0);
        assertThat(band1.frequency()).isEqualTo(1001.0);
        assertThat(band1.gain()).isEqualTo(-40.0);
        assertThat(band1.qFactor()).isEqualTo(0.100);

        EqBand band2 = result.eqBands().get(1);
        assertThat(band2.frequency()).isEqualTo(102.0);
        assertThat(band2.gain()).isEqualTo(40.0);
        assertThat(band2.qFactor()).isEqualTo(128.000);
    }

    @Test
    void shouldReturnEmptyListWhenNoEqBandsFound() {
        String content = "Some random content without EQ bands";

        CrossLiteSettings result = parser.parse(content);

        assertThat(result.eqBands()).isEmpty();
    }

    @Test
    void shouldIgnoreIrrelevantContent() {
        String content = """
            Layer 1
            IIR Bypassed.L 1 Ch 1
            1) 1Parametric EQ
            Frequency= 1001.0Hz Gain= -6.0dB Qbp= 0.750
            Magnitude Mode Normal, Phase Mode Normal.
            biquad1,
            b0=0.926144439211431525,
            Layer 2
            IIR Bypassed.
            """;

        CrossLiteSettings result = parser.parse(content);

        assertThat(result.eqBands()).hasSize(1);
        EqBand band = result.eqBands().get(0);
        assertThat(band.frequency()).isEqualTo(1001.0);
        assertThat(band.gain()).isEqualTo(-6.0);
        assertThat(band.qFactor()).isEqualTo(0.750);
    }
}