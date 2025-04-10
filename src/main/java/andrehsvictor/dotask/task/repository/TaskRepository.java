package andrehsvictor.dotask.task.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andrehsvictor.dotask.task.model.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {

}
