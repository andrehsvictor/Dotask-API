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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/api/v1/tasks")
    public ResponseEntity<Page<GetTaskDto>> findAll(
            @RequestParam(name = "q", defaultValue = "null") String query,
            TaskStatus status,
            TaskPriority priority,
            LocalDate startDate,
            LocalDate endDate,
            Boolean hasProject,
            Pageable pageable) {
        query = StringNormalizer.normalize(query);
        System.out.println("Query: " + query);
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

    @GetMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<Page<GetTaskDto>> findAllByProjectId(
            @PathVariable UUID projectId,
            @RequestParam(name = "q") String query,
            TaskStatus status,
            TaskPriority priority,
            LocalDate startDate,
            LocalDate endDate,
            Boolean hasProject,
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

    @GetMapping("/api/v1/tasks/{id}")
    public ResponseEntity<GetTaskDto> findById(@PathVariable UUID id) {
        Task task = taskService.findById(id);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.ok(taskDto);
    }

    @PostMapping("/api/v1/tasks")
    public ResponseEntity<GetTaskDto> create(@RequestBody @Valid PostTaskDto postTaskDto) {
        Task task = taskService.create(postTaskDto);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.status(201).body(taskDto);
    }

    @PostMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<GetTaskDto> create(
            @PathVariable UUID projectId, @RequestBody @Valid PostTaskDto postTaskDto) {
        Task task = taskService.create(projectId, postTaskDto);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.status(201).body(taskDto);
    }

    @PatchMapping("/api/v1/tasks/{id}/status")
    public ResponseEntity<GetTaskDto> patchStatus(
            @PathVariable UUID id, TaskStatus status) {
        Task task = taskService.patchStatus(id, status);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.ok(taskDto);
    }

    @PutMapping("/api/v1/tasks/{id}")
    public ResponseEntity<GetTaskDto> update(
            @PathVariable UUID id, @Valid @RequestBody PutTaskDto putTaskDto) {
        Task task = taskService.update(id, putTaskDto);
        GetTaskDto taskDto = taskService.toDto(task);
        return ResponseEntity.ok(taskDto);
    }

    @DeleteMapping("/api/v1/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/tasks")
    public ResponseEntity<Void> deleteAllByIdIn(@RequestBody Collection<UUID> ids) {
        taskService.deleteAllByIdIn(ids);
        return ResponseEntity.noContent().build();
    }

}
