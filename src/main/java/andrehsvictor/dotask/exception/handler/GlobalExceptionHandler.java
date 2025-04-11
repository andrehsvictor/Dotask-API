package andrehsvictor.dotask.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import andrehsvictor.dotask.exception.ResourceNotFoundException;
import andrehsvictor.dotask.exception.dto.ErrorDto;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDto<String>> handleAllExceptions(Exception ex) {
        ErrorDto<String> errorDto = ErrorDto.of("An internal error occurred");
        return ResponseEntity
                .status(500)
                .body(errorDto);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<ErrorDto<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorDto<String> errorDto = ErrorDto.of(ex.getMessage());
        return ResponseEntity
                .status(404)
                .body(errorDto);
    }

    @ExceptionHandler(AuthenticationException.class)
    public final ResponseEntity<ErrorDto<String>> handleAuthenticationException(AuthenticationException ex) {
        ErrorDto<String> errorDto = ErrorDto.of(ex.getMessage());
        return ResponseEntity
                .status(401)
                .body(errorDto);
    }

}
