package team.soma.teto.health.evaluation.domain;

import org.springframework.http.HttpStatus;
import team.soma.teto.health.global.error.ErrorCode;

public enum PapsEvaluationErrorCode implements ErrorCode {
    PAPS_INVALID_DATE_RANGE("PAPS_INVALID_DATE_RANGE", "PAPS 평가 날짜 범위가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    PAPS_INVALID_HEIGHT("PAPS_INVALID_HEIGHT", "키 입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    PAPS_INVALID_WEIGHT("PAPS_INVALID_WEIGHT", "체중 입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    PAPS_EMPTY_MEASUREMENTS("PAPS_EMPTY_MEASUREMENTS", "PAPS 측정 기록은 최소 1개 이상 필요합니다.", HttpStatus.BAD_REQUEST),
    PAPS_TEST_ITEM_NOT_FOUND("PAPS_TEST_ITEM_NOT_FOUND", "PAPS 측정 종목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAPS_TEST_ITEM_INACTIVE("PAPS_TEST_ITEM_INACTIVE", "비활성 PAPS 측정 종목입니다.", HttpStatus.BAD_REQUEST),
    PAPS_DUPLICATE_TEST_ITEM("PAPS_DUPLICATE_TEST_ITEM", "동일한 PAPS 측정 종목이 중복되었습니다.", HttpStatus.BAD_REQUEST),
    PAPS_DUPLICATE_COMPONENT("PAPS_DUPLICATE_COMPONENT", "같은 체력 요소에서는 하나의 측정 종목만 입력할 수 있습니다.", HttpStatus.BAD_REQUEST),
    PAPS_INVALID_MEASUREMENT_VALUE("PAPS_INVALID_MEASUREMENT_VALUE", "PAPS 측정값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    PAPS_INVALID_DECIMAL_SCALE("PAPS_INVALID_DECIMAL_SCALE", "PAPS 측정값의 소수 자릿수가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    PAPS_CLIENT_BMI_NOT_ALLOWED("PAPS_CLIENT_BMI_NOT_ALLOWED", "BMI는 서버에서 계산하므로 직접 입력할 수 없습니다.", HttpStatus.BAD_REQUEST),
    PAPS_BMI_TEST_ITEM_NOT_FOUND("PAPS_BMI_TEST_ITEM_NOT_FOUND", "BMI 측정 종목 설정을 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAPS_STANDARD_NOT_FOUND("PAPS_STANDARD_NOT_FOUND", "PAPS 기준 데이터를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAPS_STANDARD_OVERLAPPED("PAPS_STANDARD_OVERLAPPED", "PAPS 기준 데이터 구간이 중복되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAPS_BMI_STANDARD_NOT_FOUND("PAPS_BMI_STANDARD_NOT_FOUND", "BMI 기준 데이터를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAPS_BMI_STANDARD_OVERLAPPED("PAPS_BMI_STANDARD_OVERLAPPED", "BMI 기준 데이터 구간이 중복되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    PapsEvaluationErrorCode(String code, String message, HttpStatus status) {
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
