package andrehsvictor.dotask.project.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andrehsvictor.dotask.project.model.Project;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    
}
