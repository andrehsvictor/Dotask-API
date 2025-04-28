package andrehsvictor.dotask.token;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import andrehsvictor.dotask.AbstractIntegrationTest;
import andrehsvictor.dotask.token.dto.CredentialsDto;
import andrehsvictor.dotask.token.dto.GetTokenDto;
import andrehsvictor.dotask.token.dto.PostRefreshTokenDto;
import andrehsvictor.dotask.token.dto.RevokeTokenDto;
import andrehsvictor.dotask.user.User;
import andrehsvictor.dotask.user.UserRepository;
import andrehsvictor.dotask.user.dto.PostUserDto;
import io.restassured.http.ContentType;

class TokenControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private String userEmail;
    private String userPassword;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        userEmail = "test-user-" + UUID.randomUUID() + "@example.com";
        userPassword = "Test123!@#";

        PostUserDto user = PostUserDto.builder()
                .name("Test User")
                .email(userEmail)
                .password(userPassword)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/api/v1/users");

        User createdUser = userRepository.findByEmail(userEmail).orElseThrow();
        createdUser.setEmailVerified(true);
        userRepository.save(createdUser);
    }

    @Test
    void shouldRequestToken() {
        CredentialsDto credentials = new CredentialsDto(userEmail, userPassword);

        GetTokenDto response = given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("expiresIn", notNullValue())
                .extract()
                .as(GetTokenDto.class);

        accessToken = response.getAccessToken();
        refreshToken = response.getRefreshToken();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
        assertThat(response.getExpiresIn()).isGreaterThan(0);
    }

    @Test
    void shouldFailRequestTokenWithInvalidCredentials() {
        CredentialsDto invalidCredentials = new CredentialsDto(userEmail, "WrongPassword123!");

        given()
                .contentType(ContentType.JSON)
                .body(invalidCredentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldFailRequestTokenWithNonExistentUser() {
        CredentialsDto nonExistentUser = new CredentialsDto("nonexistent@example.com", userPassword);

        given()
                .contentType(ContentType.JSON)
                .body(nonExistentUser)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldFailRequestTokenWithUnverifiedEmail() {
        String unverifiedEmail = "unverified-user-" + UUID.randomUUID() + "@example.com";
        String password = "Test123!@#";

        PostUserDto unverifiedUser = PostUserDto.builder()
                .name("Unverified User")
                .email(unverifiedEmail)
                .password(password)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(unverifiedUser)
                .when()
                .post("/api/v1/users");

        CredentialsDto credentials = new CredentialsDto(unverifiedEmail, password);

        given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldRefreshToken() {
        requestTokens();

        PostRefreshTokenDto refreshRequest = new PostRefreshTokenDto(refreshToken);

        given()
                .contentType(ContentType.JSON)
                .body(refreshRequest)
                .when()
                .post("/api/v1/token/refresh")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("expiresIn", notNullValue());
    }

    @Test
    void shouldFailRefreshWithInvalidToken() {
        PostRefreshTokenDto invalidRefresh = new PostRefreshTokenDto("invalid.token.here");

        given()
                .contentType(ContentType.JSON)
                .body(invalidRefresh)
                .when()
                .post("/api/v1/token/refresh")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldRevokeToken() {
        requestTokens();

        RevokeTokenDto revokeRequest = new RevokeTokenDto(accessToken);

        given()
                .contentType(ContentType.JSON)
                .body(revokeRequest)
                .when()
                .post("/api/v1/token/revoke")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldFailRevokeWithInvalidToken() {
        RevokeTokenDto invalidRevoke = new RevokeTokenDto("invalid.token.here");

        given()
                .contentType(ContentType.JSON)
                .body(invalidRevoke)
                .when()
                .post("/api/v1/token/revoke")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldFailRefreshWithRevokedToken() {
        requestTokens();

        RevokeTokenDto revokeRequest = new RevokeTokenDto(refreshToken);

        given()
                .contentType(ContentType.JSON)
                .body(revokeRequest)
                .when()
                .post("/api/v1/token/revoke")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        PostRefreshTokenDto refreshRequest = new PostRefreshTokenDto(refreshToken);

        given()
                .contentType(ContentType.JSON)
                .body(refreshRequest)
                .when()
                .post("/api/v1/token/refresh")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private void requestTokens() {
        if (accessToken == null || refreshToken == null) {
            CredentialsDto credentials = new CredentialsDto(userEmail, userPassword);

            GetTokenDto response = given()
                    .contentType(ContentType.JSON)
                    .body(credentials)
                    .when()
                    .post("/api/v1/token")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(GetTokenDto.class);

            accessToken = response.getAccessToken();
            refreshToken = response.getRefreshToken();
        }
    }
}