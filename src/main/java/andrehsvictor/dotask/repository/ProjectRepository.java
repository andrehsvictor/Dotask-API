package andrehsvictor.dotask.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andrehsvictor.dotask.model.Project;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    
}
