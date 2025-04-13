package andrehsvictor.dotask.task.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetTaskDto {

    private UUID id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String dueDate;

}