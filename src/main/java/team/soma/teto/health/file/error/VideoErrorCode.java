package team.soma.teto.health.file.error;

import org.springframework.http.HttpStatus;
import team.soma.teto.health.global.error.ErrorCode;

public enum VideoErrorCode implements ErrorCode {
    EMPTY_VIDEO("VIDEO_EMPTY", "업로드된 영상 파일이 비어 있습니다.", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_VIDEO_TYPE("VIDEO_UNSUPPORTED_TYPE", "지원하지 않는 영상 형식입니다.", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    VIDEO_TOO_LARGE("VIDEO_TOO_LARGE", "영상 파일 크기가 허용된 최대치를 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE),
    TEMP_FILE_ERROR("VIDEO_TEMP_FILE_ERROR", "영상을 임시 저장하는 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    VideoErrorCode(String code, String message, HttpStatus status) {
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
