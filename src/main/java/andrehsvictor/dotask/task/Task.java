package andrehsvictor.dotask.task;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import andrehsvictor.dotask.project.Project;
import andrehsvictor.dotask.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task implements Serializable {

    private static final long serialVersionUID = -531968694382333561L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TaskPriority priority = TaskPriority.LOW;

    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}