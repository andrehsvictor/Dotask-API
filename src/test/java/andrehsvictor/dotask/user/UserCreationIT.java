package andrehsvictor.dotask.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import andrehsvictor.dotask.AbstractIntegrationTest;
import andrehsvictor.dotask.user.dto.PostUserDto;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import net.datafaker.Faker;

@DisplayName("User creation integration tests")
class UserCreationIT extends AbstractIntegrationTest {

    private static final String PATH = "/api/v1/users";
    private static final String VALID_PASSWORD = "Password1!";
    private static final int HTTP_CREATED = 201;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_CONFLICT = 409;
    private static final int HTTP_UNSUPPORTED_MEDIA_TYPE = 415;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Faker faker;

    private PostUserDto validUserDto;

    @BeforeEach
    void setUp() {
        validUserDto = PostUserDto.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .password(VALID_PASSWORD)
                .build();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private ValidatableResponse responseForUserCreation(PostUserDto postUserDto) {
        return given()
                .contentType(ContentType.JSON)
                .body(postUserDto)
                .when()
                .post(PATH)
                .then()
                .log().ifValidationFails();
    }

    @Nested
    @DisplayName("Successful user creation")
    class SuccessfulUserCreation {
        @Test
        @DisplayName("Should create a user successfully")
        void shouldCreateUserSuccessfully() {
            ValidatableResponse response = responseForUserCreation(validUserDto)
                    .statusCode(HTTP_CREATED);

            response
                    .body("id", notNullValue())
                    .body("name", equalTo(validUserDto.getName()))
                    .body("email", equalTo(validUserDto.getEmail()))
                    .body("emailVerified", equalTo(false))
                    .body("createdAt", notNullValue())
                    .body("updatedAt", notNullValue());

            assertTrue(userRepository.existsByEmail(validUserDto.getEmail()));

            User createdUser = userRepository.findByEmail(validUserDto.getEmail()).orElse(null);
            assertNotNull(createdUser);
            assertEquals(validUserDto.getName(), createdUser.getName());
            assertEquals(validUserDto.getEmail(), createdUser.getEmail());
            assertFalse(createdUser.isEmailVerified());
        }

        @Test
        @DisplayName("Should not store password in plain text")
        void shouldNotStorePasswordInPlainText() {
            responseForUserCreation(validUserDto)
                    .statusCode(HTTP_CREATED);

            User storedUser = userRepository.findByEmail(validUserDto.getEmail()).orElseThrow();

            assertNotEquals(validUserDto.getPassword(), storedUser.getPassword());
            assertTrue(storedUser.getPassword().length() > 0);
        }

        @Test
        @DisplayName("Should trim whitespace from name and email fields")
        void shouldTrimWhitespaceFromNameAndEmailFields() {
            PostUserDto userWithWhitespace = PostUserDto.builder()
                    .name("  " + faker.name().fullName() + "  ")
                    .email("  " + faker.internet().emailAddress() + "  ")
                    .password(VALID_PASSWORD)
                    .build();

            String trimmedName = userWithWhitespace.getName().trim();
            String trimmedEmail = userWithWhitespace.getEmail().trim();

            responseForUserCreation(userWithWhitespace)
                    .statusCode(HTTP_CREATED)
                    .body("name", equalTo(trimmedName))
                    .body("email", equalTo(trimmedEmail));

            User storedUser = userRepository.findByEmail(trimmedEmail).orElseThrow();
            assertEquals(trimmedName, storedUser.getName());
            assertEquals(trimmedEmail, storedUser.getEmail());
        }
    }

    @Nested
    @DisplayName("Validation errors")
    class ValidationErrors {
        @Test
        @DisplayName("Should return 400 when email is invalid")
        void shouldReturn400WhenEmailIsInvalid() {
            PostUserDto invalidEmailUser = createUserWithCustomEmail("invalid-email");

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidEmailUser)
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(HTTP_BAD_REQUEST)
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("email"))
                    .body("errors[0].message", equalTo("Invalid email format"));

            assertFalse(userRepository.existsByEmail(invalidEmailUser.getEmail()));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "password",
                "PASSWORD1!",
                "Password!",
                "Password1"
        })
        @DisplayName("Should return 400 when password doesn't meet complexity requirements")
        void shouldReturn400WhenPasswordDoesNotMeetComplexityRequirements(String password) {
            PostUserDto weakPasswordUser = createUserWithCustomPassword(password);

            given()
                    .contentType(ContentType.JSON)
                    .body(weakPasswordUser)
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(HTTP_BAD_REQUEST)
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("password"))
                    .body("errors[0].message", containsString("Password must contain"));

            assertFalse(userRepository.existsByEmail(weakPasswordUser.getEmail()));
        }

        @Test
        @DisplayName("Should return 400 when name is too short")
        void shouldReturn400WhenNameIsTooShort() {
            PostUserDto shortNameUser = createUserWithCustomName("A");

            given()
                    .contentType(ContentType.JSON)
                    .body(shortNameUser)
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(HTTP_BAD_REQUEST)
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("name"))
                    .body("errors[0].message", equalTo("Name must be between 2 and 100 characters"));

            assertFalse(userRepository.existsByEmail(shortNameUser.getEmail()));
        }

        @Test
        @DisplayName("Should return 400 when name is too long")
        void shouldReturn400WhenNameIsTooLong() {
            String tooLongName = "A".repeat(101);
            PostUserDto longNameUser = createUserWithCustomName(tooLongName);

            given()
                    .contentType(ContentType.JSON)
                    .body(longNameUser)
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(HTTP_BAD_REQUEST)
                    .body("errors", hasSize(1))
                    .body("errors[0].field", equalTo("name"))
                    .body("errors[0].message", equalTo("Name must be between 2 and 100 characters"));

            assertFalse(userRepository.existsByEmail(longNameUser.getEmail()));
        }

        @Test
        @DisplayName("Should return 400 when email is too long")
        void shouldReturn400WhenEmailIsTooLong() {
            String longLocalPart = "a".repeat(246);
            String tooLongEmail = longLocalPart + "@example.com";
            PostUserDto longEmailUser = createUserWithCustomEmail(tooLongEmail);

            given()
                    .contentType(ContentType.JSON)
                    .body(longEmailUser)
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(HTTP_BAD_REQUEST)
                    .body("errors", hasSize(2))
                    .body("errors.field", hasItem("email"))
                    .body("errors.message", hasItem("Email must be less than 255 characters"))
                    .body("errors.message", hasItem("Invalid email format"));

            assertFalse(userRepository.existsByEmail(longEmailUser.getEmail()));
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void shouldReturn400WhenRequiredFieldsAreMissing() {
            PostUserDto emptyUser = PostUserDto.builder().build();

            given()
                    .contentType(ContentType.JSON)
                    .body(emptyUser)
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(HTTP_BAD_REQUEST)
                    .body("errors", hasSize(3))
                    .body("errors.field", hasItem("name"))
                    .body("errors.field", hasItem("email"))
                    .body("errors.field", hasItem("password"))
                    .body("errors.message", hasItem("Name is required"))
                    .body("errors.message", hasItem("Email is required"))
                    .body("errors.message", hasItem("Password is required"));

            assertEquals(0, userRepository.count());
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {
        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn409WhenEmailAlreadyExists() {
            responseForUserCreation(validUserDto)
                    .statusCode(HTTP_CREATED);

            PostUserDto duplicateEmailUser = createUserWithCustomPassword("AnotherPass1!");
            duplicateEmailUser.setEmail(validUserDto.getEmail());

            given()
                    .contentType(ContentType.JSON)
                    .body(duplicateEmailUser)
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(HTTP_CONFLICT)
                    .body("errors[0]", equalTo("Email already in use: " + validUserDto.getEmail()));

            assertEquals(1, userRepository.findAll().stream()
                    .filter(user -> user.getEmail().equals(validUserDto.getEmail()))
                    .count());
        }

        @Test
        @DisplayName("Should return 400 when sending invalid JSON")
        void shouldReturn400WhenSendingInvalidJson() {
            String invalidJson = "{\"name\": \"Test User\", \"email\": \"test@example.com\", \"password\": }";

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidJson)
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(HTTP_BAD_REQUEST);

            assertEquals(0, userRepository.count());
        }

        @Test
        @DisplayName("Should return 415 when content type is not JSON")
        void shouldReturn415WhenContentTypeIsNotJson() {
            String userFormData = "name=Test+User&email=test@example.com&password=" + VALID_PASSWORD;

            given()
                    .contentType(ContentType.URLENC)
                    .body(userFormData)
                    .when()
                    .post(PATH)
                    .then()
                    .statusCode(HTTP_UNSUPPORTED_MEDIA_TYPE);

            assertEquals(0, userRepository.count());
        }
    }

    // Helper methods to create users with specific attributes
    private PostUserDto createUserWithCustomEmail(String email) {
        return PostUserDto.builder()
                .name(faker.name().fullName())
                .email(email)
                .password(VALID_PASSWORD)
                .build();
    }

    private PostUserDto createUserWithCustomName(String name) {
        return PostUserDto.builder()
                .name(name)
                .email(faker.internet().emailAddress())
                .password(VALID_PASSWORD)
                .build();
    }

    private PostUserDto createUserWithCustomPassword(String password) {
        return PostUserDto.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .password(password)
                .build();
    }
}