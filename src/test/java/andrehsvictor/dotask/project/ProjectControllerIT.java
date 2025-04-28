package andrehsvictor.dotask.project;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import andrehsvictor.dotask.AbstractIntegrationTest;
import andrehsvictor.dotask.email.EmailService;
import andrehsvictor.dotask.project.dto.GetProjectDto;
import andrehsvictor.dotask.project.dto.PostProjectDto;
import andrehsvictor.dotask.project.dto.PutProjectDto;
import andrehsvictor.dotask.task.TaskRepository;
import andrehsvictor.dotask.user.UserRepository;
import andrehsvictor.dotask.user.dto.PostUserDto;
import io.restassured.http.ContentType;

class ProjectControllerIT extends AbstractIntegrationTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private String accessToken;
    private UUID projectId;
    private int projectCounter = 0;

    @BeforeEach
    void setup() {
        doNothing().when(emailService).send(anyString(), anyString(), anyString());
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        String email = "test-user-" + UUID.randomUUID() + "@example.com";
        String password = "Test123!@#";

        PostUserDto user = PostUserDto.builder()
                .name("Test User")
                .email(email)
                .password(password)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/api/v1/users");

        var createdUser = userRepository.findByEmail(email).orElseThrow();
        createdUser.setEmailVerified(true);
        userRepository.save(createdUser);

        accessToken = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("accessToken");
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateProject() {
        String projectName = "Test Project";
        String projectDescription = "This is a test project description";
        String projectColor = "#123456";

        PostProjectDto projectDto = PostProjectDto.builder()
                .name(projectName)
                .description(projectDescription)
                .color(projectColor)
                .build();

        projectId = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("name", equalTo(projectName))
                .body("description", equalTo(projectDescription))
                .body("color", equalTo(projectColor))
                .extract()
                .as(GetProjectDto.class)
                .getId();
    }

    @Test
    void shouldGetProjectById() {
        createTestProject();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/projects/{id}", projectId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(projectId.toString()))
                .body("name", notNullValue())
                .body("description", notNullValue())
                .body("color", notNullValue());
    }

    @Test
    void shouldReturnNotFoundForNonExistentProject() {
        UUID nonExistentId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/projects/{id}", nonExistentId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldFindAllProjects() {
        createMultipleProjects(3);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", "")
                .when()
                .get("/api/v1/projects")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThanOrEqualTo(3)))
                .body("pageable", notNullValue());
    }

    @Test
    void shouldFilterProjectsByQuery() {
        String specialName = "UNIQUE_PROJECT_TEST";
        createProjectWithName(specialName);
        createMultipleProjects(3);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", specialName)
                .when()
                .get("/api/v1/projects")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(1))
                .body("content[0].name", containsString(specialName));
    }

    @Test
    void shouldUpdateProject() {
        createTestProject();

        String newName = "Updated Project";
        String newDescription = "Updated project description";
        String newColor = "#654321";

        PutProjectDto updateDto = PutProjectDto.builder()
                .name(newName)
                .description(newDescription)
                .color(newColor)
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(updateDto)
                .when()
                .put("/api/v1/projects/{id}", projectId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo(newName))
                .body("description", equalTo(newDescription))
                .body("color", equalTo(newColor));
    }

    @Test
    void shouldDeleteProject() {
        createTestProject();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/v1/projects/{id}", projectId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/projects/{id}", projectId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreatingProjectWithoutAuthentication() {
        PostProjectDto projectDto = PostProjectDto.builder()
                .name("Test Project")
                .description("This is a test project description")
                .color("#123456")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturn400WhenCreatingProjectWithEmptyName() {
        PostProjectDto invalidProject = PostProjectDto.builder()
                .name("")
                .description("This is a test project description")
                .color("#123456")
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(invalidProject)
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors.find { it.field == 'name' }.message", notNullValue());
    }

    @Test
    void shouldReturn400WhenCreatingProjectWithInvalidColor() {
        PostProjectDto invalidProject = PostProjectDto.builder()
                .name("Test Project")
                .description("This is a test project description")
                .color("invalid-color")
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(invalidProject)
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors.find { it.field == 'color' }.message", notNullValue());
    }

    @Test
    void shouldReturn400WhenUpdatingProjectWithEmptyName() {
        createTestProject();

        PutProjectDto invalidUpdate = PutProjectDto.builder()
                .name("")
                .description("This is a test project description")
                .color("#123456")
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(invalidUpdate)
                .when()
                .put("/api/v1/projects/{id}", projectId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors.find { it.field == 'name' }.message", notNullValue());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentProject() {
        UUID nonExistentId = UUID.randomUUID();

        PutProjectDto updateDto = PutProjectDto.builder()
                .name("Updated Project")
                .description("Updated project description")
                .color("#123456")
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(updateDto)
                .when()
                .put("/api/v1/projects/{id}", nonExistentId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentProject() {
        UUID nonExistentId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/v1/projects/{id}", nonExistentId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnAccessDeniedWhenAccessingProjectsFromAnotherUser() {
        // Create a project with the current user
        createTestProject();

        // Create a second user and get their token
        String email2 = "test-user2-" + UUID.randomUUID() + "@example.com";
        String password2 = "Test123!@#";

        PostUserDto user2 = PostUserDto.builder()
                .name("Test User 2")
                .email(email2)
                .password(password2)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(user2)
                .when()
                .post("/api/v1/users");

        var createdUser2 = userRepository.findByEmail(email2).orElseThrow();
        createdUser2.setEmailVerified(true);
        userRepository.save(createdUser2);

        String accessToken2 = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email2, "password", password2))
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("accessToken");

        // Try to access the first user's project with the second user's token
        given()
                .header("Authorization", "Bearer " + accessToken2)
                .when()
                .get("/api/v1/projects/{id}", projectId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldDeleteProjectAndRelatedTasks() {
        // Create project
        createTestProject();

        // Create task in project
        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "Test Task",
                        "description", "Test description",
                        "status", "PENDING",
                        "priority", "MEDIUM",
                        "dueDate", LocalDate.now().plusDays(1).toString()))
                .when()
                .post("/api/v1/projects/{projectId}/tasks", projectId)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // Get tasks count before project deletion
        int taskCountBefore = taskRepository.findAll().size();
        assertThat(taskCountBefore).isGreaterThan(0);

        // Delete project
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/v1/projects/{id}", projectId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify tasks were also deleted
        int taskCountAfter = taskRepository.findAll().size();
        assertThat(taskCountAfter).isEqualTo(0);
    }

    private void createTestProject() {
        if (projectId == null) {
            String projectName = "Test Project";
            String projectDescription = "This is a test project description";
            String projectColor = "#123456";

            PostProjectDto projectDto = PostProjectDto.builder()
                    .name(projectName)
                    .description(projectDescription)
                    .color(projectColor)
                    .build();

            projectId = given()
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(ContentType.JSON)
                    .body(projectDto)
                    .when()
                    .post("/api/v1/projects")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract()
                    .as(GetProjectDto.class)
                    .getId();
        }
    }

    private UUID createProjectAndReturnId() {
        projectCounter++;
        String projectName = "Test Project " + projectCounter;
        String projectDescription = "This is test project description " + projectCounter;
        String projectColor = "#" + (123456 + projectCounter);

        PostProjectDto projectDto = PostProjectDto.builder()
                .name(projectName)
                .description(projectDescription)
                .color(projectColor)
                .build();

        return given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/api/v1/projects")
                .then()
                .extract()
                .as(GetProjectDto.class)
                .getId();
    }

    private void createMultipleProjects(int count) {
        for (int i = 0; i < count; i++) {
            createProjectAndReturnId();
        }
    }

    private void createProjectWithName(String name) {
        String projectDescription = "Project with specific name: " + name;
        String projectColor = "#123456";

        PostProjectDto projectDto = PostProjectDto.builder()
                .name(name)
                .description(projectDescription)
                .color(projectColor)
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/api/v1/projects");
    }
}