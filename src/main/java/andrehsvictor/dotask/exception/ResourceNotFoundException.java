package andrehsvictor.dotask.exception;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 6180085085167977695L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Class<?> clazz, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", clazz.getSimpleName(), field, value));
    }
}
