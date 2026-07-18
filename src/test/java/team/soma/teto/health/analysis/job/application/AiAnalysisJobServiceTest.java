package team.soma.teto.health.analysis.job.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;
import team.soma.teto.health.analysis.job.domain.AnalysisType;
import team.soma.teto.health.analysis.job.repository.AiAnalysisJobRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiAnalysisJobServiceTest {

    @Autowired
    private AiAnalysisJobRepository aiAnalysisJobRepository;

    @Autowired
    private AiAnalysisJobService aiAnalysisJobService;

    @Test
    void cleanupExpiresOverdueJobsAndPurgesPayloads() {
        Instant past = Instant.now().minusSeconds(120);

        AiAnalysisJob overduePending = AiAnalysisJob.create("install-cleanup-1", AnalysisType.FITNESS, "{\"a\":1}", past);
        AiAnalysisJob alreadyCompleted = AiAnalysisJob.create("install-cleanup-2", AnalysisType.FITNESS, "{\"a\":1}", past);
        alreadyCompleted.start(past.minusSeconds(60));
        alreadyCompleted.complete("{\"result\":1}", "v1", past.minusSeconds(30));

        overduePending = aiAnalysisJobRepository.save(overduePending);
        alreadyCompleted = aiAnalysisJobRepository.save(alreadyCompleted);

        AnalysisJobCleanupResult result = aiAnalysisJobService.cleanupExpiredJobs();

        assertThat(result.expiredJobCount()).isEqualTo(1);
        assertThat(result.payloadsPurgedCount()).isEqualTo(2);

        AiAnalysisJob refreshedPending = aiAnalysisJobRepository.findById(overduePending.getId()).orElseThrow();
        assertThat(refreshedPending.getStatus()).isEqualTo(AnalysisStatus.EXPIRED);
        assertThat(refreshedPending.getRequestPayload()).isNull();

        AiAnalysisJob refreshedCompleted = aiAnalysisJobRepository.findById(alreadyCompleted.getId()).orElseThrow();
        assertThat(refreshedCompleted.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(refreshedCompleted.getResultPayload()).isNull();
    }

    @Test
    void getJobThrowsWhenInstallationHashDoesNotMatch() {
        AiAnalysisJob job = aiAnalysisJobRepository.save(
                AiAnalysisJob.create("install-owner", AnalysisType.POSTURE, null, Instant.now().plusSeconds(3600)));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> aiAnalysisJobService.getJob("someone-else", job.getPublicId()))
                .isInstanceOf(team.soma.teto.health.global.error.BusinessException.class);
    }
}
