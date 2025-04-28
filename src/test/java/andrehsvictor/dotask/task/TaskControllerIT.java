package andrehsvictor.dotask.task;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDate;
import java.util.Arrays;
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
import andrehsvictor.dotask.task.dto.GetTaskDto;
import andrehsvictor.dotask.task.dto.PostTaskDto;
import andrehsvictor.dotask.task.dto.PutTaskDto;
import andrehsvictor.dotask.user.UserRepository;
import andrehsvictor.dotask.user.dto.PostUserDto;
import io.restassured.http.ContentType;

class TaskControllerIT extends AbstractIntegrationTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private String accessToken;
    private UUID taskId;
    private int taskCounter = 0;

    @BeforeEach
    void setup() {
        doNothing().when(emailService).send(anyString(), anyString(), anyString());
        taskRepository.deleteAll();
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
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateTask() {
        taskCounter++;
        String title = "Test Task " + taskCounter;
        String description = "This is test task description " + taskCounter;

        PostTaskDto taskDto = PostTaskDto.builder()
                .title(title)
                .description(description)
                .status("PENDING")
                .priority("MEDIUM")
                .dueDate(LocalDate.now().plusDays(5).toString())
                .build();

        taskId = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(taskDto)
                .when()
                .post("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("title", equalTo(taskDto.getTitle()))
                .body("description", equalTo(taskDto.getDescription()))
                .body("status", equalTo(taskDto.getStatus().toString()))
                .body("priority", equalTo(taskDto.getPriority().toString()))
                .body("dueDate", equalTo(taskDto.getDueDate().toString()))
                .extract()
                .as(GetTaskDto.class)
                .getId();
    }

    @Test
    void shouldGetTaskById() {
        createTestTask();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/tasks/{id}", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(taskId.toString()));
    }

    @Test
    void shouldReturnNotFoundForNonExistentTask() {
        UUID nonExistentId = UUID.randomUUID();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/tasks/{id}", nonExistentId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldFindAllTasks() {
        createMultipleTasks(5);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThanOrEqualTo(5)))
                .body("pageable", notNullValue());
    }

    @Test
    void shouldFilterTasksByQuery() {
        String specialTitle = "UNIQUE_TITLE_TEST_" + UUID.randomUUID().toString().substring(0, 8);
        createTaskWithTitle(specialTitle);
        createMultipleTasks(3);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", specialTitle)
                .when()
                .get("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(1))
                .body("content[0].title", containsString(specialTitle));
    }

    @Test
    void shouldFilterTasksByStatus() {
        createTaskWithStatus(TaskStatus.IN_PROGRESS);
        createTaskWithStatus(TaskStatus.PENDING);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", "")
                .queryParam("status", TaskStatus.IN_PROGRESS)
                .when()
                .get("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.findAll { it.status == 'COMPLETED' }.size()", greaterThanOrEqualTo(0))
                .body("content.findAll { it.status == 'TODO' }.size()", equalTo(0))
                .body("content.findAll { it.status == 'IN_PROGRESS' }.size()", equalTo(1))
                .body("content.findAll { it.status == 'PENDING' }.size()", equalTo(0));
    }

    @Test
    void shouldFilterTasksByDateRange() {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        createTaskWithDate(futureDate);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("q", "")
                .queryParam("dueDate.from", LocalDate.now().plusDays(5).toString())
                .queryParam("dueDate.to", LocalDate.now().plusDays(15).toString())
                .when()
                .get("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.findAll { it.dueDate >= '" + LocalDate.now().plusDays(5) + "' }.size()",
                        greaterThanOrEqualTo(1));
    }

    @Test
    void shouldUpdateTask() {
        createTestTask();

        taskCounter++;
        String title = "Updated Task " + taskCounter;
        String description = "This is updated task description " + taskCounter;

        PutTaskDto updateDto = PutTaskDto.builder()
                .title(title)
                .description(description)
                .status("IN_PROGRESS")
                .priority("HIGH")
                .dueDate(LocalDate.now().plusDays(7).toString())
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(updateDto)
                .when()
                .put("/api/v1/tasks/{id}", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo(updateDto.getTitle()))
                .body("description", equalTo(updateDto.getDescription()))
                .body("status", equalTo(updateDto.getStatus().toString()))
                .body("priority", equalTo(updateDto.getPriority().toString()))
                .body("dueDate", equalTo(updateDto.getDueDate().toString()));
    }

    @Test
    void shouldPatchTaskStatus() {
        createTestTask();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .queryParam("status", TaskStatus.COMPLETED)
                .when()
                .patch("/api/v1/tasks/{id}/status", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo(TaskStatus.COMPLETED.toString()));
    }

    @Test
    void shouldDeleteTask() {
        createTestTask();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/v1/tasks/{id}", taskId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/tasks/{id}", taskId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldDeleteMultipleTasks() {
        UUID id1 = createTaskAndReturnId();
        UUID id2 = createTaskAndReturnId();
        UUID id3 = createTaskAndReturnId();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(Arrays.asList(id1, id2, id3))
                .when()
                .delete("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/tasks/{id}", id1)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private void createTestTask() {
        if (taskId == null) {
            taskCounter++;
            String title = "Test Task " + taskCounter;
            String description = "This is test task description " + taskCounter;

            PostTaskDto taskDto = PostTaskDto.builder()
                    .title(title)
                    .description(description)
                    .status("PENDING")
                    .priority("MEDIUM")
                    .dueDate(LocalDate.now().plusDays(5).toString())
                    .build();

            taskId = given()
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(ContentType.JSON)
                    .body(taskDto)
                    .when()
                    .post("/api/v1/tasks")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract()
                    .as(GetTaskDto.class)
                    .getId();
        }
    }

    private UUID createTaskAndReturnId() {
        taskCounter++;
        String title = "Test Task " + taskCounter;
        String description = "This is test task description " + taskCounter;

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
                .post("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(GetTaskDto.class)
                .getId();
    }

    private void createMultipleTasks(int count) {
        for (int i = 0; i < count; i++) {
            createTaskAndReturnId();
        }
    }

    private void createTaskWithTitle(String title) {
        taskCounter++;
        String description = "Task with specific title " + taskCounter;

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

    private void createTaskWithStatus(TaskStatus status) {
        taskCounter++;
        String title = "Task with status " + status + " " + taskCounter;
        String description = "This is a task with status " + status + " " + taskCounter;

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
                .post("/api/v1/tasks");
    }

    private void createTaskWithDate(LocalDate date) {
        taskCounter++;
        String title = "Task with date " + date + " " + taskCounter;
        String description = "This is a task with due date " + date + " " + taskCounter;

        PostTaskDto taskDto = PostTaskDto.builder()
                .title(title)
                .description(description)
                .status("PENDING")
                .priority("MEDIUM")
                .dueDate(date.toString())
                .build();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(taskDto)
                .when()
                .post("/api/v1/tasks")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }
}