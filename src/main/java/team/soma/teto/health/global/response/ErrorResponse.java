package team.soma.teto.health.global.response;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldErrorResponse> details
) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }

    public static ErrorResponse of(String code, String message, List<FieldErrorResponse> details) {
        return new ErrorResponse(code, message, List.copyOf(details));
    }
}
