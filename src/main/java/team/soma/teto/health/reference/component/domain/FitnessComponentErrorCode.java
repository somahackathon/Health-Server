package team.soma.teto.health.reference.component.domain;

import org.springframework.http.HttpStatus;
import team.soma.teto.health.global.error.ErrorCode;

public enum FitnessComponentErrorCode implements ErrorCode {
    PAPS_COMPONENT_NOT_FOUND("PAPS_COMPONENT_NOT_FOUND", "PAPS 체력 요소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;

    FitnessComponentErrorCode(String code, String message, HttpStatus status) {
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
