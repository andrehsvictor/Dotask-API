package andrehsvictor.dotask.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andrehsvictor.dotask.exception.ResourceNotFoundException;
import andrehsvictor.dotask.jwt.JwtService;
import andrehsvictor.dotask.project.Project;
import andrehsvictor.dotask.project.ProjectService;
import andrehsvictor.dotask.task.dto.GetTaskDto;
import andrehsvictor.dotask.task.dto.PostTaskDto;
import andrehsvictor.dotask.task.dto.PutTaskDto;
import andrehsvictor.dotask.user.User;
import andrehsvictor.dotask.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private final JwtService jwtService;
    private final ProjectService projectService;

    public GetTaskDto toDto(Task task) {
        return taskMapper.taskToGetTaskDto(task);
    }

    public Page<Task> findAllWithFilters(
            String query,
            TaskStatus status,
            TaskPriority priority,
            LocalDate startDate,
            LocalDate endDate,
            Boolean hasProject,
            Pageable pageable) {
        UUID userId = jwtService.getCurrentUserUuid();
        return taskRepository.findAllByUserIdWithFilters(
                userId,
                query,
                status,
                priority,
                startDate,
                endDate,
                hasProject,
                pageable);
    }

    public Page<Task> findAllByProjectIdWithFilters(
            UUID projectId,
            String query,
            TaskStatus status,
            TaskPriority priority,
            LocalDate startDate,
            LocalDate endDate,
            Boolean hasProject,
            Pageable pageable) {
        UUID userId = jwtService.getCurrentUserUuid();
        return taskRepository.findAllByUserIdAndProjectIdWithFilters(
                userId,
                projectId,
                query,
                status,
                priority,
                startDate,
                endDate,
                hasProject,
                pageable);
    }

    @Transactional(readOnly = true)
    public Task create(PostTaskDto postTaskDto) {
        Task task = taskMapper.postTaskDtoToTask(postTaskDto);
        User user = userService.findMe();
        task.setUser(user);
        task.setProject(null);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Task create(UUID projectId, PostTaskDto postTaskDto) {
        Task task = taskMapper.postTaskDtoToTask(postTaskDto);
        User user = userService.findMe();
        Project project = projectService.findById(projectId);
        task.setUser(user);
        task.setProject(project);
        return taskRepository.save(task);
    }

    public Task findById(UUID id) {
        UUID userId = jwtService.getCurrentUserUuid();
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(Task.class, "ID", id));
    }

    @Transactional
    public Task update(UUID id, PutTaskDto putTaskDto) {
        Task task = findById(id);
        TaskStatus originalStatus = task.getStatus();
        taskMapper.updateTaskFromPutTaskDto(task, putTaskDto);
        if (putTaskDto.getStatus() != null) {
            TaskStatus newStatus = TaskStatus.valueOf(putTaskDto.getStatus());
            if (newStatus == TaskStatus.COMPLETED && originalStatus != TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
            } else if (newStatus != TaskStatus.COMPLETED && originalStatus == TaskStatus.COMPLETED) {
                task.setCompletedAt(null);
            }
            task.setStatus(newStatus);
        }
        return taskRepository.save(task);
    }

    @Transactional
    public void delete(UUID id) {
        Task task = findById(id);
        if (task.getProject() != null) {
            projectService.decrementTaskCount(task.getProject());
        }
        taskRepository.delete(task);
    }

    @Transactional
    public void deleteAllByIdIn(Collection<UUID> ids) {
        UUID userId = jwtService.getCurrentUserUuid();
        List<Task> tasks = taskRepository.findAllByUserIdAndIdIn(userId, ids);
        for (Task task : tasks) {
            if (task.getProject() != null) {
                projectService.decrementTaskCount(task.getProject());
            }
        }
        taskRepository.deleteAll(tasks);
    }

    @Transactional
    public Task patchStatus(UUID id, TaskStatus status) {
        Task task = findById(id);
        task.setStatus(status);
        if (status == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
        return taskRepository.save(task);
    }

}
