package team.soma.teto.health.analysis.job.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
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
    private final AnalysisCleanupProperties cleanupProperties;

    public AiAnalysisJobService(AiAnalysisJobRepository aiAnalysisJobRepository, Clock clock, AnalysisCleanupProperties cleanupProperties) {
        this.aiAnalysisJobRepository = aiAnalysisJobRepository;
        this.clock = clock;
        this.cleanupProperties = cleanupProperties;
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
        Pageable batch = PageRequest.of(0, cleanupProperties.getCleanupBatchSize());

        int expiredJobCount = 0;
        while (true) {
            List<AiAnalysisJob> overdueActiveJobs = aiAnalysisJobRepository.findExpiredJobsByStatuses(
                    List.of(AnalysisStatus.PENDING, AnalysisStatus.PROCESSING), now, batch);
            if (overdueActiveJobs.isEmpty()) {
                break;
            }
            for (AiAnalysisJob job : overdueActiveJobs) {
                if (expireJob(job, now)) {
                    expiredJobCount++;
                }
            }
        }

        int payloadsPurgedCount = 0;
        while (true) {
            List<AiAnalysisJob> expiredJobs = aiAnalysisJobRepository.findExpiredJobsWithPayloads(now, batch);
            if (expiredJobs.isEmpty()) {
                break;
            }
            int purgedInBatch = 0;
            for (AiAnalysisJob job : expiredJobs) {
                if (clearPayload(job, now)) {
                    payloadsPurgedCount++;
                    purgedInBatch++;
                }
            }
            if (purgedInBatch == 0) {
                break;
            }
        }

        return new AnalysisJobCleanupResult(expiredJobCount, payloadsPurgedCount);
    }

    private boolean expireJob(AiAnalysisJob job, Instant now) {
        try {
            job.expire(now);
            return true;
        } catch (IllegalStateException exception) {
            return false;
        }
    }

    private boolean clearPayload(AiAnalysisJob job, Instant now) {
        String requestPayload = job.getRequestPayload();
        String resultPayload = job.getResultPayload();
        job.clearExpiredPayloads(now);
        return requestPayload != null || resultPayload != null;
    }
}
