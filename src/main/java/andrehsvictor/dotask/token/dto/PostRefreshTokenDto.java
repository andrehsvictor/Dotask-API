package andrehsvictor.dotask.token.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PostRefreshTokenDto {

    @NotBlank(message = "Refresh token is required")
    @Pattern(regexp = "^[A-Za-z0-9-_.]+\\.[A-Za-z0-9-_.]+\\.[A-Za-z0-9-_.]+$", message = "Invalid refresh token format")
    private String refreshToken;

}
