package andrehsvictor.dotask.user.dto;

import andrehsvictor.dotask.user.EmailSendingAction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendActionEmailDto {
    
    private String url;
    private EmailSendingAction action;
    private String email;

}
