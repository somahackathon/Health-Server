package team.soma.teto.health.global.error;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT("COMMON_INVALID_INPUT", "요청 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_JSON("COMMON_INVALID_JSON", "요청 본문을 해석할 수 없습니다.", HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH("COMMON_TYPE_MISMATCH", "요청 값의 타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("COMMON_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("COMMON_METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE("COMMON_UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 미디어 타입입니다.", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    PAYLOAD_TOO_LARGE("COMMON_PAYLOAD_TOO_LARGE", "요청 크기가 허용 범위를 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE),
    INTERNAL_ERROR("COMMON_INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    CommonErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
