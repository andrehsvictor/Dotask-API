package andrehsvictor.dotask.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

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

class UserEmailSendingIT extends AbstractIntegrationTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    private String userEmail;

    @BeforeEach
    void setup() {
        doNothing().when(emailService).send(anyString(), anyString(), anyString());

        userEmail = "test-email-" + UUID.randomUUID() + "@example.com";

        if (!userRepository.findByEmail(userEmail).isPresent()) {
            PostUserDto newUser = PostUserDto.builder()
                    .name("Test User")
                    .email(userEmail)
                    .password("TestPassword123!")
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
                .url("https://example.com/action?token=test-token")
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
                .body("errors", hasItems(
                        hasEntry("field", "url"),
                        hasEntry("message", "URL must start with http:// or https://")));
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
                .body("errors", hasItems(
                        hasEntry("field", "email"),
                        hasEntry("message", "Email is required"),
                        hasEntry("field", "url"),
                        hasEntry("message", "URL is required")));
    }

    @Test
    void shouldReturn404WhenSendingActionEmailToNonExistentUser() {
        String nonExistentEmail = "non-existent-" + UUID.randomUUID() + "@example.com";

        SendActionEmailDto nonExistentUserAction = SendActionEmailDto.builder()
                .action(EmailSendingAction.VERIFY_EMAIL)
                .email(nonExistentEmail)
                .url("https://example.com/verify?token=test-token")
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