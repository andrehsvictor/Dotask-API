package andrehsvictor.dotask.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidJwtTypeException extends AuthenticationException {

    private static final long serialVersionUID = -4881993560796354489L;

    public InvalidJwtTypeException(String msg) {
        super(msg);
    }

}
