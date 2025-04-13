package andrehsvictor.dotask.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetProjectDto {
    private String id;
    private String name;
    private String description;
    private String color;
    private Integer taskCount;
    private String createdAt;
    private String updatedAt;
}