package team.soma.teto.health.analysis.job.error;

import org.springframework.http.HttpStatus;
import team.soma.teto.health.global.error.ErrorCode;

public enum AnalysisJobErrorCode implements ErrorCode {
    JOB_NOT_FOUND("ANALYSIS_JOB_NOT_FOUND", "분석 작업을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    AI_ANALYSIS_FAILED("ANALYSIS_JOB_AI_FAILED", "AI 분석 요청이 실패했습니다.", HttpStatus.BAD_GATEWAY);

    private final String code;
    private final String message;
    private final HttpStatus status;

    AnalysisJobErrorCode(String code, String message, HttpStatus status) {
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
