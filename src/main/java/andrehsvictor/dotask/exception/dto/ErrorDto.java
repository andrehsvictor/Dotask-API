package andrehsvictor.dotask.exception.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorDto<T> {

    @Builder.Default
    private List<T> errors = new ArrayList<>();

    public static ErrorDto<String> of(String error) {
        return ErrorDto.<String>builder()
                .errors(List.of(error))
                .build();
    }

}
