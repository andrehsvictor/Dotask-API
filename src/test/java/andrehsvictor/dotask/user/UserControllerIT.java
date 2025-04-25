package andrehsvictor.dotask.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

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
import andrehsvictor.dotask.user.dto.PutUserDto;
import io.restassured.http.ContentType;
import net.datafaker.Faker;

class UserControllerIT extends AbstractIntegrationTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private Faker faker;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        doNothing().when(emailService).send(anyString(), anyString(), anyString());
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() {
        PostUserDto validUser = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .password("validPassword123!")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(validUser)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("name", equalTo(validUser.getName()))
                .body("email", equalTo(validUser.getEmail()))
                .body("emailVerified", equalTo(false));

        verify(emailService, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldReturn409WhenCreatingUserWithExistingEmail() {
        String email = faker.internet().emailAddress();

        PostUserDto firstUser = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(email)
                .password(faker.internet().password(8, 20, true, true, true))
                .build();

        PostUserDto duplicateUser = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(email)
                .password(faker.internet().password(8, 20, true, true, true))
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(firstUser)
                .when()
                .post("/api/v1/users");

        given()
                .contentType(ContentType.JSON)
                .body(duplicateUser)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid-email", "email@", "@domain.com", "email@domain", "email.domain.com" })
    void shouldReturn400WhenCreatingUserWithInvalidEmail(String invalidEmail) {
        PostUserDto invalidUser = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(invalidEmail)
                .password(faker.internet().password(8, 20, true, true, true))
                .build();

        /*
         * java.lang.AssertionError: 1 expectation failed.
         * JSON path errors doesn't match.
         * Expected: map containing ["email"-]ANYTHING]
         * Actual: [[{field=email, message=Invalid email format}]]
         */

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("$", hasKey("errors"))
                .body("errors", hasItems(hasEntry("field", "email"),
                        hasEntry("message", "Invalid email format")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "nouppercase123", "NOLOWERCASE123", "NoSpecialChar123", "NoNumber@abc" })
    void shouldReturn400WhenCreatingUserWithInvalidPassword(String invalidPassword) {
        PostUserDto invalidUser = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .password(invalidPassword)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("$", hasKey("errors"))
                .body("errors", hasItems(hasEntry("field", "password"),
                        hasEntry("message",
                                "Password must contain at least one digit, one lowercase, one uppercase, one special character, and no whitespace")));
    }

    @Test
    void shouldReturn400WhenCreatingUserWithShortPassword() {
        PostUserDto invalidUser = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .password("short")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("$", hasKey("errors"))
                .body("errors", hasItems(hasEntry("field", "password"),
                        hasEntry("message", "Password must be between 8 and 255 characters")));
    }

    @Test
    void shouldReturnUnauthorizedWhenFindingMeWithoutAuthentication() {
        given()
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnUnauthorizedWhenUpdatingMeWithoutAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .body(PostUserDto.builder()
                        .name(faker.name().fullName())
                        .email(faker.internet().emailAddress())
                        .password(faker.internet().password(8, 20, true, true, true))
                        .build())
                .when()
                .put("/api/v1/users/me")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnUserDetailsWhenFindingMeWithAuthentication() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 20, true, true, true);

        PostUserDto newUser = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(email)
                .password(password)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post("/api/v1/users");

        User createdUser = userRepository.findByEmail(email).orElseThrow();
        createdUser.setEmailVerified(true);
        userRepository.save(createdUser);

        String token = given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("accessToken");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", notNullValue())
                .body("name", equalTo(newUser.getName()))
                .body("email", equalTo(newUser.getEmail()))
                .body("emailVerified", equalTo(true));

        verify(emailService, never()).send(anyString(), anyString(), anyString());

    }

    @Test
    void shouldReturnUserDetailsWhenUpdatingMeWithAuthentication() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 20, true, true, true);

        PutUserDto updateUser = PutUserDto.builder()
                .name(faker.name().fullName())
                .email(email)
                .build();

        PostUserDto newUser = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .password(password)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post("/api/v1/users");

        User createdUser = userRepository.findByEmail(newUser.getEmail()).orElseThrow();
        createdUser.setEmailVerified(true);
        userRepository.save(createdUser);

        String token = given()
                .contentType(ContentType.JSON)
                .body(newUser)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("accessToken");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(updateUser)
                .when()
                .put("/api/v1/users/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", notNullValue())
                .body("name", equalTo(updateUser.getName()))
                .body("email", equalTo(updateUser.getEmail()))
                .body("emailVerified", equalTo(false));

        verify(emailService, never()).send(anyString(), anyString(), anyString());

        User updatedUser = userRepository.findByEmail(updateUser.getEmail()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo(updateUser.getName());
        assertThat(updatedUser.getEmail()).isEqualTo(updateUser.getEmail());
    }

}