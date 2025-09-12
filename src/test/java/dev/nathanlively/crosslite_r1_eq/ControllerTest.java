package dev.nathanlively.crosslite_r1_eq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringJUnitConfig
class ConversionCommandsTest {
    
    @Test
    void givenCrossliteFile_whenConvertToR1_thenCreateR1EqFile() {
        final String filenameCl = "example1.txt";
        final String filenameR1 = "example1.rcp";
        Resource resourceR1 = new ClassPathResource(filenameR1);
        Resource resourceCl = new ClassPathResource(filenameCl);
        assertThat(resourceR1.exists()).isTrue();
        assertThat(resourceCl.exists()).isTrue();
    }
}