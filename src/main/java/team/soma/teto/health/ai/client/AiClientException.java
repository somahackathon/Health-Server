package team.soma.teto.health.ai.client;

import team.soma.teto.health.analysis.job.domain.AiFailureCode;

public class AiClientException extends RuntimeException {

    private final AiFailureCode failureCode;

    public AiClientException(AiFailureCode failureCode, String message) {
        super(message);
        this.failureCode = failureCode;
    }

    public AiFailureCode failureCode() {
        return failureCode;
    }
}
