package andrehsvictor.dotask.project;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByIdAndUserId(UUID id, UUID userId);

    Optional<Project> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            SELECT p
            FROM Project p
            WHERE p.user.id = :userId
            AND (:query IS NULL OR
                LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))
                """)
    Page<Project> findAllByUserIdWithFilter(
            UUID userId,
            String query,
            Pageable pageable);

    @Modifying
    @Transactional
    Integer deleteByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Transactional
    Integer deleteAllByUserIdAndIdIn(UUID userId, Collection<UUID> ids);

}
