package andrehsvictor.dotask.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostTaskDto {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 50, message = "Title must be between 3 and 50 characters")
    private String title;

    @Size(max = 200, message = "Description must be less than 200 characters")
    private String description;

    @Pattern(regexp = "^(PENDING|IN_PROGRESS)$", message = "Status must be either PENDING or IN_PROGRESS")
    private String status;

    @Pattern(message = "Due date must be in the format yyyy-MM-dd", regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    @Size(min = 10, max = 10, message = "Due date must be in the format yyyy-MM-dd")
    private String dueDate;

    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$", message = "Priority must be either LOW, MEDIUM or HIGH")
    private String priority;

}
