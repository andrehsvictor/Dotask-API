package andrehsvictor.dotask.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import andrehsvictor.dotask.AbstractIntegrationTest;
import andrehsvictor.dotask.email.EmailService;
import andrehsvictor.dotask.user.dto.PostUserDto;
import andrehsvictor.dotask.user.dto.SendActionEmailDto;
import net.datafaker.Faker;

@DisplayName("Action Email Sending Integration Tests")
class ActionEmailSendingIT extends AbstractIntegrationTest {

    private static final String SEND_ACTION_EMAIL_PATH = "/api/v1/users/email";

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private Faker faker;

    private String baseUrl;
    private String validEmail;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:8080";
        validEmail = faker.internet().emailAddress();
    }

    @Nested
    @DisplayName("Email Verification Tests")
    class EmailVerification {

        @Test
        @DisplayName("Should send email verification email successfully")
        void shouldSendEmailVerificationEmailSuccessfully() {
            PostUserDto userDto = PostUserDto.builder()
                    .name(faker.name().fullName())
                    .email(validEmail)
                    .password("P@ssw0rd123")
                    .build();

            given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(userDto)
                    .when()
                    .post("/api/v1/users")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("email", equalTo(validEmail));

            SendActionEmailDto actionEmailDto = SendActionEmailDto.builder()
                    .action(EmailSendingAction.VERIFY_EMAIL)
                    .email(validEmail)
                    .url(baseUrl + "/verify-email")
                    .build();

            given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(actionEmailDto)
                    .when()
                    .post(SEND_ACTION_EMAIL_PATH)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            verify(emailService, times(1)).send(
                    eq(validEmail),
                    eq("Verify Your Email - Dotask"),
                    contains("Verify Your Email Address"));

            User user = userRepository.findByEmail(validEmail).orElse(null);
            assertNotNull(user);
            assertNotNull(user.getEmailVerificationToken());
            assertNotNull(user.getEmailVerificationTokenExpiresAt());
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordReset {

        @Test
        @DisplayName("Should send password reset email successfully")
        void shouldSendPasswordResetEmailSuccessfully() {
            PostUserDto userDto = PostUserDto.builder()
                    .name(faker.name().fullName())
                    .email(validEmail)
                    .password("P@ssw0rd123")
                    .build();

            given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(userDto)
                    .when()
                    .post("/api/v1/users")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("email", equalTo(validEmail));

            SendActionEmailDto actionEmailDto = SendActionEmailDto.builder()
                    .action(EmailSendingAction.RESET_PASSWORD)
                    .email(validEmail)
                    .url(baseUrl + "/reset-password")
                    .build();

            given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(actionEmailDto)
                    .when()
                    .post(SEND_ACTION_EMAIL_PATH)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            verify(emailService, times(1)).send(
                    eq(validEmail),
                    eq("Reset Your Password - Dotask"),
                    contains("Reset Your Password"));

            User user = userRepository.findByEmail(validEmail).orElse(null);
            assertNotNull(user);
            assertNotNull(user.getPasswordResetToken());
            assertNotNull(user.getPasswordResetTokenExpiresAt());
        }
    }

    @Test
    @DisplayName("Should return 404 when user email does not exist")
    void shouldReturn404WhenUserEmailDoesNotExist() {
        String nonExistentEmail = "nonexistent@example.com";

        SendActionEmailDto actionEmailDto = SendActionEmailDto.builder()
                .action(EmailSendingAction.VERIFY_EMAIL)
                .email(nonExistentEmail)
                .url(baseUrl + "/verify-email")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(actionEmailDto)
                .when()
                .post(SEND_ACTION_EMAIL_PATH)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}