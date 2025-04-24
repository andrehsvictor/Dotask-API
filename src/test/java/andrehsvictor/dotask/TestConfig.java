package andrehsvictor.dotask;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import net.datafaker.Faker;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    Faker faker() {
        return new Faker();
    }

}
