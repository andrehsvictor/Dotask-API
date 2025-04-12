package andrehsvictor.dotask.user;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import andrehsvictor.dotask.exception.EmailAlreadyExistsException;
import andrehsvictor.dotask.exception.ResourceNotFoundException;
import andrehsvictor.dotask.jwt.JwtService;
import andrehsvictor.dotask.user.dto.EmailVerificationTokenDto;
import andrehsvictor.dotask.user.dto.GetUserDto;
import andrehsvictor.dotask.user.dto.PostUserDto;
import andrehsvictor.dotask.user.dto.PutUserDto;
import andrehsvictor.dotask.user.dto.ResetPasswordTokenDto;
import andrehsvictor.dotask.user.dto.SendActionEmailDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final ResetPasswordService resetPasswordService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, "ID", id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, "email", email));
    }

    public User findByEmailVerificationToken(String token) {
        return userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User markEmailAsVerified(User user) {
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);
        return userRepository.save(user);
    }

    public User findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
    }

    public User setPasswordResetToken(User user, String token,
            LocalDateTime expiresAt) {
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiresAt(expiresAt);
        return userRepository.save(user);
    }

    public void verifyEmail(EmailVerificationTokenDto emailVerificationTokenDto) {
        emailVerificationService.verifyEmail(emailVerificationTokenDto.getToken());
    }

    public User setEmailVerificationToken(User user, String token,
            LocalDateTime expiresAt) {
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiresAt(expiresAt);
        return userRepository.save(user);
    }

    public void sendActionEmail(SendActionEmailDto sendActionEmailDto) {
        String url = sendActionEmailDto.getUrl();
        String email = sendActionEmailDto.getEmail();
        switch (sendActionEmailDto.getAction()) {
            case VERIFY_EMAIL:
                emailVerificationService.sendVerificationEmail(url, email);
                break;
            case RESET_PASSWORD:
                resetPasswordService.sendResetPasswordEmail(url, email);
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + sendActionEmailDto.getAction());
        }
    }

    public void resetPassword(ResetPasswordTokenDto resetPasswordTokenDto) {
        String token = resetPasswordTokenDto.getToken();
        String newPassword = resetPasswordTokenDto.getNewPassword();
        resetPasswordService.resetPassword(token, newPassword);
    }

    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.save(user);
    }

    public User create(PostUserDto postUserDto) {
        if (existsByEmail(postUserDto.getEmail())) {
            throw new EmailAlreadyExistsException(postUserDto.getEmail());
        }
        User user = userMapper.postUserDtoToUser(postUserDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateMe(PutUserDto putUserDto) {
        UUID userId = jwtService.getCurrentUserUuid();
        User user = findById(userId);
        if (putUserDto.getEmail() != null && !putUserDto.getEmail().equals(user.getEmail())
                && existsByEmail(putUserDto.getEmail())) {
            throw new EmailAlreadyExistsException(putUserDto.getEmail());
        }
        userMapper.updateUserFromPutUserDto(putUserDto, user);
        return userRepository.save(user);
    }

    public GetUserDto toDto(User user) {
        return userMapper.userToGetUserDto(user);
    }

    public User findMe() {
        UUID userId = jwtService.getCurrentUserUuid();
        return findById(userId);
    }

    public void deleteMe() {
        UUID userId = jwtService.getCurrentUserUuid();
        userRepository.deleteById(userId);
    }

}
