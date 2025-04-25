package andrehsvictor.dotask.token.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevokeTokenDto {

    @NotBlank(message = "Token cannot be blank")
    @Pattern(message = "Token must be a valid JWT", regexp = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$")
    private String token;

}
