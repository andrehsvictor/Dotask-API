package andrehsvictor.dotask.user;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import andrehsvictor.dotask.AbstractIntegrationTest;
import andrehsvictor.dotask.email.EmailService;
import andrehsvictor.dotask.user.dto.EmailVerificationTokenDto;
import andrehsvictor.dotask.user.dto.PostUserDto;
import io.restassured.http.ContentType;

class UserEmailVerificationIT extends AbstractIntegrationTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setup() {
        doNothing().when(emailService).send(anyString(), anyString(), anyString());

        String email = "test-user-" + UUID.randomUUID() + "@example.com";

        testUser = userRepository.findByEmail(email).orElseGet(() -> {
            PostUserDto newUser = PostUserDto.builder()
                    .name("Test User")
                    .email(email)
                    .password("TestPassword123!")
                    .build();

            return userService.create(newUser);
        });

        validToken = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        testUser = userService.setEmailVerificationToken(
                testUser,
                validToken,
                LocalDateTime.now().plusHours(24));
    }

    @Test
    void shouldVerifyEmailWithValidToken() {
        EmailVerificationTokenDto tokenDto = new EmailVerificationTokenDto(validToken);

        given()
                .contentType(ContentType.JSON)
                .body(tokenDto)
                .when()
                .post("/api/v1/users/email/verify")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.isEmailVerified()).isTrue();
        assertThat(updatedUser.getEmailVerificationToken()).isNull();
        assertThat(updatedUser.getEmailVerificationTokenExpiresAt()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "   " })
    void shouldReturn400WhenVerifyingEmailWithEmptyToken(String emptyToken) {
        EmailVerificationTokenDto emptyTokenDto = new EmailVerificationTokenDto(emptyToken);

        given()
                .contentType(ContentType.JSON)
                .body(emptyTokenDto)
                .when()
                .post("/api/v1/users/email/verify")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("$", hasKey("errors"))
                .body("errors", hasItems(
                        hasEntry("field", "token"),
                        hasEntry("message", "Token is required")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid-token", "123456", "token-not-found" })
    void shouldReturn404WhenVerifyingEmailWithInvalidToken(String invalidToken) {
        EmailVerificationTokenDto invalidTokenDto = new EmailVerificationTokenDto(invalidToken);

        given()
                .contentType(ContentType.JSON)
                .body(invalidTokenDto)
                .when()
                .post("/api/v1/users/email/verify")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturn401WhenVerifyingEmailWithExpiredToken() {
        String expiredToken = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        userService.setEmailVerificationToken(
                testUser,
                expiredToken,
                LocalDateTime.now().minusHours(1));

        EmailVerificationTokenDto expiredTokenDto = new EmailVerificationTokenDto(expiredToken);

        given()
                .contentType(ContentType.JSON)
                .body(expiredTokenDto)
                .when()
                .post("/api/v1/users/email/verify")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}