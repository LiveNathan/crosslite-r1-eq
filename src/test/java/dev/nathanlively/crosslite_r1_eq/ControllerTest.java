package dev.nathanlively.crosslite_r1_eq;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.*;

class ControllerTest {
    @Test
    void givenCrossliteFile_whenConvertToR1_thenCreateR1EqFile() {
        final String filename = "1keq.rcp";
        Resource resource = new ClassPathResource(filename);
        assertThat(resource.exists())
                .isTrue();
    }

}