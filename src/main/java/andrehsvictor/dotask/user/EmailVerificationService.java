package andrehsvictor.dotask.user;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import andrehsvictor.dotask.email.EmailService;
import andrehsvictor.dotask.exception.EmailAlreadyVerfiedException;
import andrehsvictor.dotask.exception.UnauthorizedException;
import andrehsvictor.dotask.file.FileService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserService userService;
    private final EmailService emailService;
    private final FileService fileService;

    @Value("${security.token.email-verification.lifespan}")
    private Duration tokenLifespan;

    public void sendVerificationEmail(String url, String email) {
        User user = userService.findByEmail(email);
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerfiedException();
        }
        String token = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        LocalDateTime expiresAt = LocalDateTime.now().plus(tokenLifespan);

        userService.setEmailVerificationToken(user, token, expiresAt);

        String expiresInPhrase = formatExpirationTime(tokenLifespan);

        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("url", url);
        templateVariables.put("token", token);
        templateVariables.put("expiresInPhrase", expiresInPhrase);

        String emailTemplate = fileService.readFile("classpath:templates/verify-email.html");
        String emailContent = processTemplate(emailTemplate, templateVariables);

        emailService.send(email, "Verify Your Email - Dotask", emailContent);
    }

    public boolean verifyEmail(String token) {
        User user = userService.findByEmailVerificationToken(token);

        if (user.getEmailVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Token expired");
        }

        userService.markEmailAsVerified(user);
        return true;
    }

    private String formatExpirationTime(Duration duration) {
        long hours = duration.toHours();

        if (hours < 1) {
            return duration.toMinutes() + " minutes";
        } else if (hours == 1) {
            return "1 hour";
        } else if (hours < 24) {
            return hours + " hours";
        } else {
            long days = hours / 24;
            return days == 1 ? "1 day" : days + " days";
        }
    }

    private String processTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}