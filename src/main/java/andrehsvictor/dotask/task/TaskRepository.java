package andrehsvictor.dotask.task;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findAllByUserId(UUID userId, Pageable pageable);

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.user.id = :userId
            AND t.project.id = :projectId
            AND (
                LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(t.project.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR :query IS NULL
            )
            AND (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            AND (t.dueDate >= :startDate OR CAST(:startDate AS date) IS NULL)
            AND (t.dueDate <= :endDate OR CAST(:endDate AS date) IS NULL)
            """)
    @QueryHints({
            @QueryHint(name = "org.hibernate.fetchSize", value = "100"),
            @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    Page<Task> findAllByUserIdAndProjectIdWithFilters(
            UUID userId,
            UUID projectId,
            String query,
            TaskStatus status,
            TaskPriority priority,
            LocalDate startDate,
            LocalDate endDate,
            Boolean hasProject,
            Pageable pageable);

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.user.id = :userId
            AND (
                LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))
                OR :query IS NULL
            )
            AND (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            AND (t.dueDate >= :startDate OR CAST(:startDate AS date) IS NULL)
            AND (t.dueDate <= :endDate OR CAST(:endDate AS date) IS NULL)
            AND (:hasProject IS NULL OR
                 (:hasProject = TRUE AND t.project IS NOT NULL) OR
                 (:hasProject = FALSE AND t.project IS NULL)
                )
            """)
    @QueryHints({
            @QueryHint(name = "org.hibernate.fetchSize", value = "100"),
            @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    Page<Task> findAllByUserIdWithFilters(
            UUID userId,
            String query,
            TaskStatus status,
            TaskPriority priority,
            LocalDate startDate,
            LocalDate endDate,
            Boolean hasProject,
            Pageable pageable);

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    List<Task> findAllByUserIdAndIdIn(UUID userId, Collection<UUID> ids);

}