package andrehsvictor.dotask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 3793107357403726064L;

    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email);
    }

    public EmailAlreadyExistsException() {
        super("Email already in use");
    }
}