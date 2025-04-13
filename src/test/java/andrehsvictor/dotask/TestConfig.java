package andrehsvictor.dotask;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import andrehsvictor.dotask.email.EmailService;

@TestConfiguration
public class TestConfig {

    @Bean
    EmailService emailService() {
        return mock(EmailService.class);
    }

}
