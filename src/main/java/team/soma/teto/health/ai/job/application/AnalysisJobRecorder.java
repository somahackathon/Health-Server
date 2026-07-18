package team.soma.teto.health.ai.job.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.ai.job.domain.AiAnalysisJob;
import team.soma.teto.health.ai.job.domain.AiFailureCode;
import team.soma.teto.health.ai.job.domain.AnalysisType;
import team.soma.teto.health.ai.job.repository.AiAnalysisJobRepository;

@Component
public class AnalysisJobRecorder {

    private static final Duration JOB_TTL = Duration.ofHours(24);
    private static final int FAILURE_MESSAGE_MAX_LENGTH = 1000;

    private final AiAnalysisJobRepository repository;
    private final Clock clock;

    public AnalysisJobRecorder(AiAnalysisJobRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public AiAnalysisJob createProcessing(String installationId, AnalysisType analysisType, String requestPayload) {
        Instant now = clock.instant();
        AiAnalysisJob job = AiAnalysisJob.create(hash(installationId), analysisType, requestPayload, now.plus(JOB_TTL));
        job.start(now);
        return repository.save(job);
    }

    @Transactional
    public void complete(AiAnalysisJob job, String resultPayload, String modelVersion) {
        job.complete(resultPayload, modelVersion, clock.instant());
        repository.save(job);
    }

    @Transactional
    public void fail(AiAnalysisJob job, AiFailureCode failureCode, String message) {
        job.fail(failureCode, truncate(message), clock.instant());
        repository.save(job);
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > FAILURE_MESSAGE_MAX_LENGTH ? message.substring(0, FAILURE_MESSAGE_MAX_LENGTH) : message;
    }

    private String hash(String installationId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(installationId.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
