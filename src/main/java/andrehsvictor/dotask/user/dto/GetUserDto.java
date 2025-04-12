package andrehsvictor.dotask.user.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetUserDto {

    private UUID id;
    private String name;
    private String email;
    private boolean emailVerified;
    private String createdAt;
    private String updatedAt;
    private Integer taskCount;
    private Integer projectCount;
    
}