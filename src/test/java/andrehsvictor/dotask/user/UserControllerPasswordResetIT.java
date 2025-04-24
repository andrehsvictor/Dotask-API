package andrehsvictor.dotask.user;

import static io.restassured.RestAssured.given;
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
import andrehsvictor.dotask.user.dto.PostUserDto;
import andrehsvictor.dotask.user.dto.ResetPasswordTokenDto;
import io.restassured.http.ContentType;
import net.datafaker.Faker;

class UserControllerPasswordResetIT extends AbstractIntegrationTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private Faker faker;

    private User testUser;
    private String validToken;
    private String validPassword;

    @BeforeEach
    void setup() {
        doNothing().when(emailService).send(anyString(), anyString(), anyString());

        String email = faker.internet().emailAddress();
        validPassword = faker.internet().password(8, 20, true, true, true);

        testUser = userRepository.findByEmail(email).orElseGet(() -> {
            PostUserDto newUser = PostUserDto.builder()
                    .name(faker.name().fullName())
                    .email(email)
                    .password(validPassword)
                    .build();

            return userService.create(newUser);
        });

        validToken = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        testUser = userService.setPasswordResetToken(
                testUser,
                validToken,
                LocalDateTime.now().plusHours(24));
    }

    @Test
    void shouldResetPasswordWithValidToken() {
        String newPassword = faker.internet().password(8, 20, true, true, true);
        ResetPasswordTokenDto resetPasswordDto = ResetPasswordTokenDto.builder()
                .token(validToken)
                .newPassword(newPassword)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .when()
                .post("/api/v1/users/password/reset")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @ParameterizedTest
    @ValueSource(strings = { "short", "nouppercase123", "NOLOWERCASE123", "NoSpecialChar123", "NoNumber@abc" })
    void shouldReturn400WhenResettingPasswordWithWeakPassword(String weakPassword) {
        ResetPasswordTokenDto weakPasswordDto = ResetPasswordTokenDto.builder()
                .token(validToken)
                .newPassword(weakPassword)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(weakPasswordDto)
                .when()
                .post("/api/v1/users/password/reset")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("$", hasKey("errors"))
                .body("errors", hasKey("newPassword"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "   " })
    void shouldReturn400WhenResettingPasswordWithEmptyToken(String emptyToken) {
        ResetPasswordTokenDto emptyTokenDto = ResetPasswordTokenDto.builder()
                .token(emptyToken)
                .newPassword(faker.internet().password(8, 20, true, true, true))
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(emptyTokenDto)
                .when()
                .post("/api/v1/users/password/reset")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("$", hasKey("errors"))
                .body("errors", hasKey("token"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid-token", "123456", "token-not-found" })
    void shouldReturn404WhenResettingPasswordWithInvalidToken(String invalidToken) {
        ResetPasswordTokenDto invalidTokenDto = ResetPasswordTokenDto.builder()
                .token(invalidToken)
                .newPassword(faker.internet().password(8, 20, true, true, true))
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalidTokenDto)
                .when()
                .post("/api/v1/users/password/reset")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturn401WhenResettingPasswordWithExpiredToken() {
        String expiredToken = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        userService.setPasswordResetToken(
                testUser,
                expiredToken,
                LocalDateTime.now().minusHours(1));

        ResetPasswordTokenDto expiredTokenDto = ResetPasswordTokenDto.builder()
                .token(expiredToken)
                .newPassword(faker.internet().password(8, 20, true, true, true))
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(expiredTokenDto)
                .when()
                .post("/api/v1/users/password/reset")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}