package andrehsvictor.dotask.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import andrehsvictor.dotask.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    public Authentication authenticate(String username, String password) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
            return authenticationManager.authenticate(authToken);
        } catch (DisabledException e) {
            throw new UnauthorizedException("You should verify your email first");
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid username or password");
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            throw new UnauthorizedException("An unexpected error occurred during authentication");
        }
    }
}