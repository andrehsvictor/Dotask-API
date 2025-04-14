package andrehsvictor.dotask.user;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import andrehsvictor.dotask.AbstractIntegrationTest;
import andrehsvictor.dotask.email.EmailService;
import andrehsvictor.dotask.user.dto.PostUserDto;
import andrehsvictor.dotask.user.dto.SendActionEmailDto;
import io.restassured.http.ContentType;
import net.datafaker.Faker;

@DisplayName("Action email sending integration tests")
class ActionEmailSendingIT extends AbstractIntegrationTest {

    @MockitoSpyBean
    private EmailService emailService;

    @Autowired
    private Faker faker;

    @Autowired
    private UserRepository userRepository;

    private String validEmail;
    private String password;

    @BeforeEach
    void setUp() {
        validEmail = faker.internet().emailAddress();
        password = faker.internet().password(8, 20, true, true, true);

        PostUserDto postUserDto = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(validEmail)
                .password(password)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(postUserDto)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(201);
    }

    @AfterEach
    void tearDown() {
        reset(emailService);
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Verify email action tests")
    class VerifyEmailActionTests {

        @Test
        @DisplayName("Should send verification email successfully")
        void shouldSendVerificationEmailSuccessfully() {
            String baseUrl = "http://localhost:8080/verify-email";
            SendActionEmailDto sendActionEmailDto = SendActionEmailDto.builder()
                    .url(baseUrl)
                    .action(EmailSendingAction.VERIFY_EMAIL)
                    .email(validEmail)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(sendActionEmailDto)
                    .when()
                    .post("/api/v1/users/email")
                    .then()
                    .statusCode(204);

            verify(emailService, times(1)).send(
                    eq(validEmail),
                    eq("Verify Your Email - Dotask"),
                    contains(baseUrl));
        }

        @Test
        @DisplayName("Should return 404 when email not found")
        void shouldReturn404WhenEmailNotFound() {
            String nonExistingEmail = "nonexisting@example.com";
            SendActionEmailDto sendActionEmailDto = SendActionEmailDto.builder()
                    .url("http://localhost:8080/verify-email")
                    .action(EmailSendingAction.VERIFY_EMAIL)
                    .email(nonExistingEmail)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(sendActionEmailDto)
                    .when()
                    .post("/api/v1/users/email")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("Reset password action tests")
    class ResetPasswordActionTests {

        @Test
        @DisplayName("Should send reset password email successfully")
        void shouldSendResetPasswordEmailSuccessfully() {
            String baseUrl = "http://localhost:8080/reset-password";
            SendActionEmailDto sendActionEmailDto = SendActionEmailDto.builder()
                    .url(baseUrl)
                    .action(EmailSendingAction.RESET_PASSWORD)
                    .email(validEmail)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(sendActionEmailDto)
                    .when()
                    .post("/api/v1/users/email")
                    .then()
                    .statusCode(204);

            verify(emailService, times(1)).send(
                    eq(validEmail),
                    eq("Reset Your Password - Dotask"),
                    contains(baseUrl));
        }

        @Test
        @DisplayName("Should return 404 when email not found")
        void shouldReturn404WhenEmailNotFound() {
            String nonExistingEmail = "nonexisting@example.com";
            SendActionEmailDto sendActionEmailDto = SendActionEmailDto.builder()
                    .url("http://localhost:8080/reset-password")
                    .action(EmailSendingAction.RESET_PASSWORD)
                    .email(nonExistingEmail)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(sendActionEmailDto)
                    .when()
                    .post("/api/v1/users/email")
                    .then()
                    .statusCode(404);
        }
    }

    @Test
    @DisplayName("Should return 400 when invalid action is provided")
    void shouldReturn400WhenInvalidActionIsProvided() {
        String requestBody = """
                {
                    "url": "http://localhost:8080",
                    "action": "INVALID_ACTION",
                    "email": "%s"
                }
                """.formatted(validEmail);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/users/email")
                .then()
                .statusCode(400);
    }
}