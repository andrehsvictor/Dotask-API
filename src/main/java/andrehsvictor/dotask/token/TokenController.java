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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for token-based authentication")
public class TokenController {

    private final TokenService tokenService;

    @Operation(summary = "Request authentication token", description = "Authenticates a user with email and password and returns access and refresh tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = GetTokenDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or unverified email")
    })
    @PostMapping
    public ResponseEntity<GetTokenDto> request(
            @RequestBody @Valid CredentialsDto credentials) {
        GetTokenDto tokenResponse = tokenService.request(credentials);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "Refresh authentication token", description = "Uses a refresh token to generate a new access token without requiring credentials")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = GetTokenDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<GetTokenDto> refresh(
            @RequestBody @Valid PostRefreshTokenDto refreshTokenDto) {
        GetTokenDto tokenResponse = tokenService.refresh(refreshTokenDto);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "Revoke token", description = "Invalidates an access or refresh token, making it unusable for further operations")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Token revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token format"),
            @ApiResponse(responseCode = "401", description = "Token not recognized")
    })
    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(
            @RequestBody @Valid RevokeTokenDto revokeTokenDto) {
        tokenService.revoke(revokeTokenDto);
        return ResponseEntity.noContent().build();
    }
}