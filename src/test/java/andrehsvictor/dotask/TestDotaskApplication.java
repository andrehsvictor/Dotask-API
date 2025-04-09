package andrehsvictor.dotask;

import org.springframework.boot.SpringApplication;

public class TestDotaskApplication {

	public static void main(String[] args) {
		SpringApplication.from(DotaskApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
