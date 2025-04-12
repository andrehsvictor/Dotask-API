package andrehsvictor.dotask.token;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andrehsvictor.dotask.token.dto.CredentialsDto;
import andrehsvictor.dotask.token.dto.GetTokenDto;
import andrehsvictor.dotask.token.dto.PostRefreshTokenDto;
import andrehsvictor.dotask.token.dto.RevokeTokenDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping
    public ResponseEntity<GetTokenDto> request(
            @RequestBody @Valid CredentialsDto credentials) {
        GetTokenDto tokenResponse = tokenService.request(credentials);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<GetTokenDto> refresh(
            @RequestBody @Valid PostRefreshTokenDto refreshTokenDto) {
        GetTokenDto tokenResponse = tokenService.refresh(refreshTokenDto);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(
            @RequestBody @Valid RevokeTokenDto revokeTokenDto) {
        tokenService.revoke(revokeTokenDto);
        return ResponseEntity.noContent().build();
    }
}