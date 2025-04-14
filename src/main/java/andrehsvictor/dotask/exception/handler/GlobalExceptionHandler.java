package andrehsvictor.dotask.exception.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import andrehsvictor.dotask.exception.EmailAlreadyExistsException;
import andrehsvictor.dotask.exception.EmailAlreadyVerfiedException;
import andrehsvictor.dotask.exception.InvalidJwtTypeException;
import andrehsvictor.dotask.exception.ResourceNotFoundException;
import andrehsvictor.dotask.exception.TokenExpiredException;
import andrehsvictor.dotask.exception.UnauthorizedException;
import andrehsvictor.dotask.exception.dto.ErrorDto;
import andrehsvictor.dotask.exception.dto.FieldErrorDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDto<String>> handleAllExceptions(Exception ex) {
        ErrorDto<String> errorDto = ErrorDto.of("An internal error occurred");
        log.error("An internal error occurred", ex);
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

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public final ResponseEntity<ErrorDto<String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ErrorDto<String> errorDto = ErrorDto.of(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorDto);
    }

    @ExceptionHandler(EmailAlreadyVerfiedException.class)
    public final ResponseEntity<ErrorDto<String>> handleEmailAlreadyVerifiedException(EmailAlreadyVerfiedException ex) {
        ErrorDto<String> errorDto = ErrorDto.of(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorDto);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public final ResponseEntity<ErrorDto<String>> handleTokenExpiredException(TokenExpiredException ex) {
        ErrorDto<String> errorDto = ErrorDto.of(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorDto);
    }

    @ExceptionHandler(InvalidJwtTypeException.class)
    public final ResponseEntity<ErrorDto<String>> handleInvalidJwtTypeException(InvalidJwtTypeException ex) {
        ErrorDto<String> errorDto = ErrorDto.of(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorDto);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public final ResponseEntity<ErrorDto<String>> handleUnauthorizedException(UnauthorizedException ex) {
        ErrorDto<String> errorDto = ErrorDto.of(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorDto);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorDto<FieldErrorDto> errorDto = ErrorDto.<FieldErrorDto>builder()
                .errors(ex.getFieldErrors().stream()
                        .map(fieldError -> FieldErrorDto.of(fieldError.getField(), fieldError.getDefaultMessage()))
                        .toList())
                .build();
        return ResponseEntity
                .status(400)
                .body(errorDto);
    }
}