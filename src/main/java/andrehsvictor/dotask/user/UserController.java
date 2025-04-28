package andrehsvictor.dotask.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import andrehsvictor.dotask.user.dto.EmailVerificationTokenDto;
import andrehsvictor.dotask.user.dto.GetUserDto;
import andrehsvictor.dotask.user.dto.PostUserDto;
import andrehsvictor.dotask.user.dto.PutUserDto;
import andrehsvictor.dotask.user.dto.ResetPasswordTokenDto;
import andrehsvictor.dotask.user.dto.SendActionEmailDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user", description = "Registers a new user in the system. After registration, a verification email will be sent.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = GetUserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping("/api/v1/users")
    public ResponseEntity<GetUserDto> create(@RequestBody @Valid PostUserDto postUserDto) {
        User user = userService.create(postUserDto);
        return ResponseEntity.status(201).body(userService.toDto(user));
    }

    @Operation(summary = "Get authenticated user data", description = "Returns the data of the currently authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully", content = @Content(schema = @Schema(implementation = GetUserDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/users/me")
    public GetUserDto findMe() {
        User user = userService.findMe();
        return userService.toDto(user);
    }

    @Operation(summary = "Update user data", description = "Updates the authenticated user's data. If the email is changed, it will need to be verified again.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = GetUserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/api/v1/users/me")
    public GetUserDto updateMe(@RequestBody @Valid PutUserDto putUserDto) {
        User user = userService.updateMe(putUserDto);
        return userService.toDto(user);
    }

    @Operation(summary = "Send action email", description = "Sends an email to the user based on action type (verification, password reset, etc.)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/api/v1/users/send-action-email")
    public ResponseEntity<?> sendActionEmail(@RequestBody @Valid SendActionEmailDto sendActionEmailDto) {
        userService.sendActionEmail(sendActionEmailDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verify user email", description = "Verifies a user's email using the token sent to their email address")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token"),
            @ApiResponse(responseCode = "401", description = "Token expired"),
            @ApiResponse(responseCode = "404", description = "Token not found")
    })
    @PostMapping("/api/v1/users/email/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid EmailVerificationTokenDto emailVerificationTokenDto) {
        userService.verifyEmail(emailVerificationTokenDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reset password", description = "Resets the user's password using the token sent to their email")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token or password format"),
            @ApiResponse(responseCode = "401", description = "Token expired"),
            @ApiResponse(responseCode = "404", description = "Token not found")
    })
    @PostMapping("/api/v1/users/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordTokenDto resetPasswordTokenDto) {
        userService.resetPassword(resetPasswordTokenDto);
        return ResponseEntity.noContent().build();
    }
}