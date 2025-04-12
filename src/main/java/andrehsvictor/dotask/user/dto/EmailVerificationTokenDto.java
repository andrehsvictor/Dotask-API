package andrehsvictor.dotask.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailVerificationTokenDto {

    @NotBlank(message = "Token is required")
    private String token;

}
