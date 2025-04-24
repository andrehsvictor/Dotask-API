package andrehsvictor.dotask.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailVerificationTokenDto {

    @NotBlank(message = "Token is required")
    private String token;

}
