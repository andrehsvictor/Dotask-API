package andrehsvictor.dotask.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = org.springframework.http.HttpStatus.UNAUTHORIZED, reason = "Token expired")
public class TokenExpiredException extends AuthenticationException {

    private static final long serialVersionUID = 544541277848372217L;

    public TokenExpiredException(String msg) {
        super(msg);
    }

}
