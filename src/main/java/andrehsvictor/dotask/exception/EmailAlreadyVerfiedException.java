package andrehsvictor.dotask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Email already verified")
public class EmailAlreadyVerfiedException extends RuntimeException {

    private static final long serialVersionUID = 7900795507073015917L;

    public EmailAlreadyVerfiedException() {
        super("Email already verified");
    }

}
