package andrehsvictor.dotask;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import andrehsvictor.dotask.email.EmailService;
import net.datafaker.Faker;

@Configuration
public class TestConfig {

    @Bean
    EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean
    Faker faker() {
        return new Faker();
    }

}
