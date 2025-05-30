package andrehsvictor.dotask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends AuthenticationException {

    private static final long serialVersionUID = -3520576034390946093L;

    public UnauthorizedException(String msg) {
        super(msg);
    }

}
