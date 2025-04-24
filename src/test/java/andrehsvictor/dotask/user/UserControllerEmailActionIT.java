package andrehsvictor.dotask.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import andrehsvictor.dotask.AbstractIntegrationTest;
import andrehsvictor.dotask.email.EmailService;
import andrehsvictor.dotask.user.dto.PostUserDto;
import andrehsvictor.dotask.user.dto.SendActionEmailDto;
import io.restassured.http.ContentType;
import net.datafaker.Faker;

class UserControllerEmailActionIT extends AbstractIntegrationTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Faker faker;

    private String userEmail;

    @BeforeEach
    void setup() {
        doNothing().when(emailService).send(anyString(), anyString(), anyString());

        userEmail = faker.internet().emailAddress();

        if (!userRepository.findByEmail(userEmail).isPresent()) {
            PostUserDto newUser = PostUserDto.builder()
                    .name(faker.name().fullName())
                    .email(userEmail)
                    .password(faker.internet().password(8, 20, true, true, true))
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(newUser)
                    .when()
                    .post("/api/v1/users");
        }
    }

    @ParameterizedTest
    @EnumSource(EmailSendingAction.class)
    void shouldSendActionEmail(EmailSendingAction action) {
        SendActionEmailDto actionEmailDto = SendActionEmailDto.builder()
                .action(action)
                .email(userEmail)
                .url(faker.internet().url())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(actionEmailDto)
                .when()
                .post("/api/v1/users/send-action-email")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        verify(emailService, times(1)).send(eq(userEmail), anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid-url", "httpx://example.com", "example", "http:/example.com" })
    void shouldReturn400WhenSendingActionEmailWithInvalidUrl(String invalidUrl) {
        SendActionEmailDto invalidUrlAction = SendActionEmailDto.builder()
                .action(EmailSendingAction.VERIFY_EMAIL)
                .email(userEmail)
                .url(invalidUrl)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalidUrlAction)
                .when()
                .post("/api/v1/users/send-action-email")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("$", hasKey("errors"))
                .body("errors", hasKey("url"));
    }

    @Test
    void shouldReturn400WhenSendingActionEmailWithMissingFields() {
        SendActionEmailDto missingFieldsAction = SendActionEmailDto.builder()
                .action(EmailSendingAction.VERIFY_EMAIL)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(missingFieldsAction)
                .when()
                .post("/api/v1/users/send-action-email")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("$", hasKey("errors"))
                .body("errors", hasKey("email"))
                .body("errors", hasKey("url"));
    }

    @Test
    void shouldReturn404WhenSendingActionEmailToNonExistentUser() {
        String nonExistentEmail = "non-existent-" + faker.internet().emailAddress();

        SendActionEmailDto nonExistentUserAction = SendActionEmailDto.builder()
                .action(EmailSendingAction.VERIFY_EMAIL)
                .email(nonExistentEmail)
                .url(faker.internet().url())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(nonExistentUserAction)
                .when()
                .post("/api/v1/users/send-action-email")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}