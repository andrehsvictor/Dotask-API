package andrehsvictor.dotask.task;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import andrehsvictor.dotask.AbstractIntegrationTest;
import andrehsvictor.dotask.project.ProjectRepository;
import andrehsvictor.dotask.project.dto.GetProjectDto;
import andrehsvictor.dotask.project.dto.PostProjectDto;
import andrehsvictor.dotask.task.dto.GetTaskDto;
import andrehsvictor.dotask.task.dto.PostTaskDto;
import andrehsvictor.dotask.user.UserRepository;
import andrehsvictor.dotask.user.dto.PostUserDto;
import io.restassured.http.ContentType;

class ProjectTaskControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private String accessToken;
    private UUID projectId;
    private int taskCounter = 0;

    @BeforeEach
    void setup() {
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

        createTestProject();
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateTaskWithProject() {
        String title = "Test Task Title";
        String description = "This is a test task description";

        PostTaskDto taskDto = PostTaskDto.builder()
                .title(title)
                .description(description)
                .status("PENDING")
                .priority("MEDIUM")
                .dueDate(LocalDate.now().plusDays(5).toString())
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(taskDto)
                .when()
                .post("/api/v1/projects/{projectId}/tasks", projectId)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("project.id", equalTo(projectId.toString()));
    }

    @Test
    void shouldGetTasksByProjectId() {
        createTasksForProject(3);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", "")
                .when()
                .get("/api/v1/projects/{projectId}/tasks", projectId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThanOrEqualTo(3)))
                .body("content.findAll { it.project.id == '" + projectId + "' }.size()", equalTo(3));
    }

    @Test
    void shouldFilterProjectTasksByStatus() {
        createTaskForProjectWithStatus(TaskStatus.IN_PROGRESS);
        createTaskForProjectWithStatus(TaskStatus.PENDING);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", "")
                .queryParam("status", TaskStatus.IN_PROGRESS)
                .when()
                .get("/api/v1/projects/{projectId}/tasks", projectId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.findAll { it.status == 'IN_PROGRESS' }.size()", greaterThanOrEqualTo(1))
                .body("content.findAll { it.status == 'PENDING' }.size()", equalTo(0));
    }

    @Test
    void shouldFilterProjectTasksByPriority() {
        createTaskForProjectWithPriority("LOW");
        createTaskForProjectWithPriority("MEDIUM");
        createTaskForProjectWithPriority("HIGH");

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", "")
                .queryParam("priority", "HIGH")
                .when()
                .get("/api/v1/projects/{projectId}/tasks", projectId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.findAll { it.priority == 'HIGH' }.size()", greaterThanOrEqualTo(1))
                .body("content.findAll { it.priority == 'LOW' }.size()", equalTo(0));
    }

    @Test
    void shouldFilterTasksByProjectPresence() {
        createTaskWithoutProject();
        createTaskForProject();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", "")
                .queryParam("hasProject", true)
                .when()
                .get("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.findAll { it.project.id != null }.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", "")
                .queryParam("hasProject", false)
                .when()
                .get("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.findAll { it.projectId == null }.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldReturnNotFoundForNonExistentProject() {
        UUID nonExistentId = UUID.randomUUID();
        String title = "Test Task Title";
        String description = "This is a test task description";

        PostTaskDto taskDto = PostTaskDto.builder()
                .title(title)
                .description(description)
                .status("PENDING")
                .priority("MEDIUM")
                .dueDate(LocalDate.now().plusDays(5).toString())
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(taskDto)
                .when()
                .post("/api/v1/projects/{projectId}/tasks", nonExistentId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private void createTestProject() {
        String description = "This is a test project description";
        String name = "Test Project";

        PostProjectDto projectDto = PostProjectDto.builder()
                .name(name)
                .description(description)
                .color("#123456")
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

    private UUID createTaskForProject() {
        taskCounter++;
        String title = "Project Task " + taskCounter;
        String description = "This is a test task description " + taskCounter;

        PostTaskDto taskDto = PostTaskDto.builder()
                .title(title)
                .description(description)
                .status("PENDING")
                .priority("MEDIUM")
                .dueDate(LocalDate.now().plusDays(5).toString())
                .build();

        return given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(taskDto)
                .when()
                .post("/api/v1/projects/{projectId}/tasks", projectId)
                .then()
                .extract()
                .as(GetTaskDto.class)
                .getId();
    }

    private void createTasksForProject(int count) {
        for (int i = 0; i < count; i++) {
            createTaskForProject();
        }
    }

    private void createTaskForProjectWithStatus(TaskStatus status) {
        taskCounter++;
        String title = "Task with status " + status + " " + taskCounter;
        String description = "This is a task with status " + status;

        PostTaskDto taskDto = PostTaskDto.builder()
                .title(title)
                .description(description)
                .status(status.toString())
                .priority("MEDIUM")
                .dueDate(LocalDate.now().plusDays(5).toString())
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(taskDto)
                .when()
                .post("/api/v1/projects/{projectId}/tasks", projectId);
    }

    private void createTaskForProjectWithPriority(String priority) {
        taskCounter++;
        String title = "Task with priority " + priority + " " + taskCounter;
        String description = "This is a task with priority " + priority;

        PostTaskDto taskDto = PostTaskDto.builder()
                .title(title)
                .description(description)
                .status("PENDING")
                .priority(priority)
                .dueDate(LocalDate.now().plusDays(5).toString())
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(taskDto)
                .when()
                .post("/api/v1/projects/{projectId}/tasks", projectId);
    }

    private void createTaskWithoutProject() {
        taskCounter++;
        String title = "Task without project " + taskCounter;
        String description = "This task is not associated with any project " + taskCounter;

        PostTaskDto taskDto = PostTaskDto.builder()
                .title(title)
                .description(description)
                .status("PENDING")
                .priority("MEDIUM")
                .dueDate(LocalDate.now().plusDays(5).toString())
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(taskDto)
                .when()
                .post("/api/v1/tasks");
    }
}