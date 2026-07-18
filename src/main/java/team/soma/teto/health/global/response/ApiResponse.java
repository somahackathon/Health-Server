package team.soma.teto.health.global.response;

import java.time.Clock;
import java.time.OffsetDateTime;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorResponse error,
        OffsetDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data, Clock clock) {
        return new ApiResponse<>(true, data, null, OffsetDateTime.now(clock));
    }

    public static ApiResponse<Void> success(Clock clock) {
        return new ApiResponse<>(true, null, null, OffsetDateTime.now(clock));
    }

    public static ApiResponse<Void> failure(ErrorResponse error, Clock clock) {
        return new ApiResponse<>(false, null, error, OffsetDateTime.now(clock));
    }
}
