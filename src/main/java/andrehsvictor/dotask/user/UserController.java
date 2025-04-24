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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/v1/users")
    public ResponseEntity<GetUserDto> create(@RequestBody @Valid PostUserDto postUserDto) {
        User user = userService.create(postUserDto);
        return ResponseEntity.status(201).body(userService.toDto(user));
    }

    @GetMapping("/api/v1/users/me")
    public GetUserDto findMe() {
        User user = userService.findMe();
        return userService.toDto(user);
    }

    @PutMapping("/api/v1/users/me")
    public GetUserDto updateMe(@RequestBody @Valid PutUserDto putUserDto) {
        User user = userService.updateMe(putUserDto);
        return userService.toDto(user);
    }

    @PostMapping("/api/v1/users/email")
    public ResponseEntity<?> sendActionEmail(@RequestBody @Valid SendActionEmailDto sendActionEmailDto) {
        userService.sendActionEmail(sendActionEmailDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/users/email/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid EmailVerificationTokenDto emailVerificationTokenDto) {
        userService.verifyEmail(emailVerificationTokenDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/users/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordTokenDto resetPasswordTokenDto) {
        userService.resetPassword(resetPasswordTokenDto);
        return ResponseEntity.noContent().build();
    }

}
