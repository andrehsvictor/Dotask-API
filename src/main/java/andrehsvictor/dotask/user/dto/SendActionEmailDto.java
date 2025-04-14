package andrehsvictor.dotask.user.dto;

import andrehsvictor.dotask.user.EmailSendingAction;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendActionEmailDto {

    @Pattern(regexp = "^(http|https)://.*", message = "URL must start with http:// or https://")
    @NotBlank(message = "URL is required")
    private String url;

    @NotNull(message = "Action is required")
    // @Pattern(regexp = "^(VERIFY_EMAIL|RESET_PASSWORD)$", message = "Invalid
    // action")
    private EmailSendingAction action;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

}
