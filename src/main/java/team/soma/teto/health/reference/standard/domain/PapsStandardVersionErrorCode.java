package team.soma.teto.health.reference.standard.domain;

import org.springframework.http.HttpStatus;
import team.soma.teto.health.global.error.ErrorCode;

public enum PapsStandardVersionErrorCode implements ErrorCode {
    PAPS_STANDARD_VERSION_NOT_FOUND("PAPS_STANDARD_VERSION_NOT_FOUND", "현재 적용 중인 PAPS 기준 버전을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAPS_MULTIPLE_ACTIVE_STANDARD_VERSIONS("PAPS_MULTIPLE_ACTIVE_STANDARD_VERSIONS", "활성 PAPS 기준 버전이 여러 개입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    PapsStandardVersionErrorCode(String code, String message, HttpStatus status) {
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
