package andrehsvictor.dotask.exception.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FieldErrorDto {
    private String field;
    private String message;

    public static FieldErrorDto of(String field, String message) {
        return FieldErrorDto.builder()
                .field(field)
                .message(message)
                .build();
    }
}
