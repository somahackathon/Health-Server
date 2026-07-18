package team.soma.teto.health.analysis.job.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;
import team.soma.teto.health.analysis.job.domain.AnalysisType;
import team.soma.teto.health.analysis.job.error.AnalysisJobErrorCode;
import team.soma.teto.health.analysis.job.repository.AiAnalysisJobRepository;
import team.soma.teto.health.global.error.BusinessException;

@Service
@Transactional(readOnly = true)
public class AiAnalysisJobService {

    private final AiAnalysisJobRepository aiAnalysisJobRepository;
    private final Clock clock;

    public AiAnalysisJobService(AiAnalysisJobRepository aiAnalysisJobRepository, Clock clock) {
        this.aiAnalysisJobRepository = aiAnalysisJobRepository;
        this.clock = clock;
    }

    @Transactional
    public AiAnalysisJob createJob(String installationHash, AnalysisType analysisType, String requestPayload, Duration ttl) {
        AiAnalysisJob job = AiAnalysisJob.create(installationHash, analysisType, requestPayload, Instant.now(clock).plus(ttl));
        return aiAnalysisJobRepository.save(job);
    }

    @Transactional
    public void start(AiAnalysisJob job) {
        job.start(Instant.now(clock));
    }

    @Transactional
    public void complete(AiAnalysisJob job, String resultPayload, String modelVersion) {
        job.complete(resultPayload, modelVersion, Instant.now(clock));
    }

    @Transactional
    public void fail(AiAnalysisJob job, AiFailureCode failureCode, String failureMessage) {
        job.fail(failureCode, failureMessage, Instant.now(clock));
    }

    public AiAnalysisJob getJob(String installationHash, UUID publicId) {
        return aiAnalysisJobRepository.findByInstallationHashAndPublicId(installationHash, publicId)
                .orElseThrow(() -> new BusinessException(AnalysisJobErrorCode.JOB_NOT_FOUND));
    }

    public List<AiAnalysisJob> listJobs(String installationHash, Pageable pageable) {
        return aiAnalysisJobRepository.findAllByInstallationHashOrderByCreatedAtDesc(installationHash, pageable);
    }

    @Transactional
    public AnalysisJobCleanupResult cleanupExpiredJobs() {
        Instant now = Instant.now(clock);

        List<AiAnalysisJob> overdueActiveJobs = aiAnalysisJobRepository.findExpiredJobsByStatuses(
                List.of(AnalysisStatus.PENDING, AnalysisStatus.PROCESSING), now);
        overdueActiveJobs.forEach(job -> job.expire(now));

        List<AiAnalysisJob> expiredJobs = aiAnalysisJobRepository.findAllByExpiresAtBefore(now);
        expiredJobs.forEach(job -> job.clearExpiredPayloads(now));

        return new AnalysisJobCleanupResult(overdueActiveJobs.size(), expiredJobs.size());
    }
}
