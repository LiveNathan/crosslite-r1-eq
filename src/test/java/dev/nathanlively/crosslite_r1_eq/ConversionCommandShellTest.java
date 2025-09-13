package dev.nathanlively.crosslite_r1_eq;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@ShellTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ConversionCommandShellTest {
    @Autowired
    ShellTestClient client;

    @Test
    void test() {
        ShellTestClient.InteractiveShellSession session = client
                .interactive()
                .run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("shell");
        });
        session.write(session.writeSequence().text("help").carriageReturn().build());
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("AVAILABLE COMMANDS");
        });
    }

    @Disabled("until I learn how to write tests for shell commands")
    @Test
    void givenValidPath_whenConvert_thenSuccess() {
        ShellTestClient.InteractiveShellSession session = client.interactive().run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen()).containsText("shell");
        });

        session.write(session.writeSequence().text("convert").carriageReturn().build());
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("Enter CrossLite file path (.txt):");
        });
    }
}
