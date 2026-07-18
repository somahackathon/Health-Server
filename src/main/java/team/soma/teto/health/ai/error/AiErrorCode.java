package team.soma.teto.health.ai.error;

import org.springframework.http.HttpStatus;
import team.soma.teto.health.ai.job.domain.AiFailureCode;
import team.soma.teto.health.global.error.ErrorCode;

public enum AiErrorCode implements ErrorCode {
    AI_TIMEOUT("AI_TIMEOUT", "AI 분석 응답이 지연되었습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.GATEWAY_TIMEOUT),
    AI_SERVER_ERROR("AI_SERVER_ERROR", "AI 분석 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.BAD_GATEWAY),
    AI_INVALID_RESPONSE("AI_INVALID_RESPONSE", "AI 분석 결과를 해석할 수 없습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.BAD_GATEWAY),
    AI_VIDEO_PROCESSING_FAILED("AI_VIDEO_PROCESSING_FAILED", "영상을 분석할 수 없습니다. 전신이 나오도록 다시 촬영해 주세요.", HttpStatus.UNPROCESSABLE_ENTITY),
    AI_VIDEO_TOO_LARGE("AI_VIDEO_TOO_LARGE", "영상 크기가 허용 범위를 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE),
    AI_UNSUPPORTED_VIDEO_TYPE("AI_UNSUPPORTED_VIDEO_TYPE", "지원하지 않는 영상 형식입니다. mp4 또는 mov 파일을 사용해 주세요.", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    AI_UNSUPPORTED_EXERCISE_TYPE("AI_UNSUPPORTED_EXERCISE_TYPE", "지원하지 않는 운동 종목입니다.", HttpStatus.BAD_REQUEST),
    AI_UNKNOWN_ERROR("AI_UNKNOWN_ERROR", "AI 분석 중 알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    AiErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public static AiErrorCode from(AiFailureCode failureCode) {
        return switch (failureCode) {
            case AI_TIMEOUT -> AI_TIMEOUT;
            case AI_SERVER_ERROR -> AI_SERVER_ERROR;
            case INVALID_AI_RESPONSE -> AI_INVALID_RESPONSE;
            case VIDEO_PROCESSING_FAILED, TEMPORARY_FILE_ERROR -> AI_VIDEO_PROCESSING_FAILED;
            case UNKNOWN -> AI_UNKNOWN_ERROR;
        };
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
