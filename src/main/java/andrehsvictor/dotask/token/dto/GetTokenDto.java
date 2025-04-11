package andrehsvictor.dotask.token.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetTokenDto {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;
    
}
