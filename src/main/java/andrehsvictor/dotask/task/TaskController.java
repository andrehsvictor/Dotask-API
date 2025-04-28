package andrehsvictor.dotask.task;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andrehsvictor.dotask.task.dto.GetTaskDto;
import andrehsvictor.dotask.task.dto.PostTaskDto;
import andrehsvictor.dotask.task.dto.PutTaskDto;
import andrehsvictor.dotask.util.StringNormalizer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "API for task management")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Find all tasks", description = "Retrieves all tasks for the authenticated user with optional filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully", content = @Content(schema = @Schema(implementation = GetTaskDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/tasks")
    public ResponseEntity<Page<GetTaskDto>> findAll(
            @Parameter(description = "Search query to filter tasks by title or description") @RequestParam(name = "q", required = false) String query,
            @Parameter(description = "Filter tasks by status") TaskStatus status,
            @Parameter(description = "Filter tasks by priority") TaskPriority priority,
            @Parameter(description = "Filter tasks with due date starting from this date") @RequestParam(name = "dueDate.from", required = false) LocalDate startDate,
            @Parameter(description = "Filter tasks with due date up to this date") @RequestParam(name = "dueDate.to", required = false) LocalDate endDate,
            @Parameter(description = "Filter tasks by project association (true = has project, false = no project)") Boolean hasProject,
            Pageable pageable) {
        query = StringNormalizer.normalize(query);
        Page<Task> tasks = taskService.findAllWithFilters(query,
                status,
                priority,
                startDate,
                endDate,
                hasProject,
                pageable);
        Page<GetTaskDto> taskDtos = tasks.map(taskService::toDto);
        return ResponseEntity.ok(taskDtos);
    }

    @Operation(summary = "Find tasks by project", description = "Retrieves all tasks belonging to a specific project with optional filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully", content = @Content(schema = @Schema(implementation = GetTaskDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<Page<GetTaskDto>> findAllByProjectId(
            @Parameter(description = "Project ID to retrieve tasks from") @PathVariable UUID projectId,
            @Parameter(description = "Search query to filter tasks by title or description") @RequestParam(name = "q", required = false) String query,
            @Parameter(description = "Filter tasks by status") TaskStatus status,
            @Parameter(description = "Filter tasks by priority") TaskPriority priority,
            @Parameter(description = "Filter tasks with due date starting from this date") @RequestParam(name = "dueDate.from", required = false) LocalDate startDate,
            @Parameter(description = "Filter tasks with due date up to this date") @RequestParam(name = "dueDate.to", required = false) LocalDate endDate,
            @Parameter(description = "Filter tasks by project association (true = has project, false = no project)") Boolean hasProject,
            Pageable pageable) {
        query = StringNormalizer.normalize(query);
        Page<Task> tasks = taskService.findAllByProjectIdWithFilters(
                projectId,
                query,
                status,
                priority,
                startDate,
                endDate,
                hasProject,
                pageable);
        Page<GetTaskDto> taskDtos = tasks.map(taskService::toDto);
        return ResponseEntity.ok(taskDtos);
    }

    @Operation(summary = "Find task by ID", description = "Retrieves a specific task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully", content = @Content(schema = @Schema(implementation = GetTaskDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/tasks/{id}")
    public ResponseEntity<GetTaskDto> findById(
            @Parameter(description = "Task ID to retrieve") @PathVariable UUID id) {
        Task task = taskService.findById(id);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.ok(taskDto);
    }

    @Operation(summary = "Create a new task", description = "Creates a new task without associating it with a project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(schema = @Schema(implementation = GetTaskDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid task data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/v1/tasks")
    public ResponseEntity<GetTaskDto> create(
            @Parameter(description = "Task data") @RequestBody @Valid PostTaskDto postTaskDto) {
        Task task = taskService.create(postTaskDto);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.status(201).body(taskDto);
    }

    @Operation(summary = "Create a task in a project", description = "Creates a new task associated with a specific project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(schema = @Schema(implementation = GetTaskDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid task data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<GetTaskDto> create(
            @Parameter(description = "Project ID to associate task with") @PathVariable UUID projectId,
            @Parameter(description = "Task data") @RequestBody @Valid PostTaskDto postTaskDto) {
        Task task = taskService.create(projectId, postTaskDto);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.status(201).body(taskDto);
    }

    @Operation(summary = "Update task status", description = "Updates only the status of a task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task status updated successfully", content = @Content(schema = @Schema(implementation = GetTaskDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/api/v1/tasks/{id}/status")
    public ResponseEntity<GetTaskDto> patchStatus(
            @Parameter(description = "Task ID to update") @PathVariable UUID id,
            @Parameter(description = "New status value") TaskStatus status) {
        Task task = taskService.patchStatus(id, status);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.ok(taskDto);
    }

    @Operation(summary = "Update task", description = "Updates all fields of a task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully", content = @Content(schema = @Schema(implementation = GetTaskDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid task data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/api/v1/tasks/{id}")
    public ResponseEntity<GetTaskDto> update(
            @Parameter(description = "Task ID to update") @PathVariable UUID id,
            @Parameter(description = "Updated task data") @Valid @RequestBody PutTaskDto putTaskDto) {
        Task task = taskService.update(id, putTaskDto);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.ok(taskDto);
    }

    @Operation(summary = "Delete task", description = "Deletes a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/api/v1/tasks/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Task ID to delete") @PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete multiple tasks", description = "Deletes multiple tasks by their IDs")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tasks deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid task IDs"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/api/v1/tasks")
    public ResponseEntity<Void> deleteAllByIdIn(
            @Parameter(description = "Collection of task IDs to delete") @RequestBody Collection<UUID> ids) {
        taskService.deleteAllByIdIn(ids);
        return ResponseEntity.noContent().build();
    }
}