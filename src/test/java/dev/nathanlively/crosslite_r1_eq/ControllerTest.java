package dev.nathanlively.crosslite_r1_eq;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerTest {
    @Test
    void givenCrossliteFile_whenConvertToR1_thenCreateR1EqFile() {
        final String filenameCl = "coef.txt";
        final String filenameR1 = "1keq.rcp";
        Resource resourceR1 = new ClassPathResource(filenameR1);
        Resource resourceCl = new ClassPathResource(filenameCl);
        assertThat(resourceR1.exists()).isTrue();
        assertThat(resourceCl.exists()).isTrue();
    }

}