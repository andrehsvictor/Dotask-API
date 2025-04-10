package andrehsvictor.dotask.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andrehsvictor.dotask.model.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {

}
