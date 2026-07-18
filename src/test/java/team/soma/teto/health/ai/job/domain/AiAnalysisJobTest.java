package team.soma.teto.health.ai.job.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AiAnalysisJobTest {

    private final Instant now = Instant.parse("2026-07-18T00:00:00Z");

    @Test
    void transitionToCompleted() {
        AiAnalysisJob job = AiAnalysisJob.create("hash", AnalysisType.FITNESS, "{\"request\":true}", now.plusSeconds(3600));

        job.start(now);
        job.complete("{\"result\":true}", "fitness-v1", now.plusSeconds(10));

        assertThat(job.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(job.getResultPayload()).isEqualTo("{\"result\":true}");
        assertThat(job.getModelVersion()).isEqualTo("fitness-v1");
        assertThat(job.getCompletedAt()).isEqualTo(now.plusSeconds(10));
    }

    @Test
    void transitionToFailed() {
        AiAnalysisJob job = AiAnalysisJob.create("hash", AnalysisType.POSTURE, null, now.plusSeconds(3600));

        job.start(now);
        job.fail(AiFailureCode.AI_TIMEOUT, "AI request timed out", now.plusSeconds(5));

        assertThat(job.getStatus()).isEqualTo(AnalysisStatus.FAILED);
        assertThat(job.getFailureCode()).isEqualTo(AiFailureCode.AI_TIMEOUT);
        assertThat(job.getFailureMessage()).isEqualTo("AI request timed out");
        assertThat(job.getCompletedAt()).isEqualTo(now.plusSeconds(5));
    }

    @Test
    void transitionToExpired() {
        AiAnalysisJob job = AiAnalysisJob.create("hash", AnalysisType.FITNESS, null, now.minusSeconds(1));

        job.expire(now);

        assertThat(job.getStatus()).isEqualTo(AnalysisStatus.EXPIRED);
        assertThat(job.getCompletedAt()).isEqualTo(now);
    }

    @Test
    void rejectInvalidTransitionFromPendingToCompleted() {
        AiAnalysisJob job = AiAnalysisJob.create("hash", AnalysisType.FITNESS, null, now.plusSeconds(3600));

        assertThatThrownBy(() -> job.complete("{}", "model", now))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectTransitionFromCompleted() {
        AiAnalysisJob job = AiAnalysisJob.create("hash", AnalysisType.FITNESS, null, now.plusSeconds(3600));
        job.start(now);
        job.complete("{}", "model", now.plusSeconds(1));

        assertThatThrownBy(() -> job.expire(now.plusSeconds(2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectStackTraceFailureMessage() {
        AiAnalysisJob job = AiAnalysisJob.create("hash", AnalysisType.POSTURE, null, now.plusSeconds(3600));
        job.start(now);

        assertThatThrownBy(() -> job.fail(AiFailureCode.UNKNOWN, "failed\n\tat internal.Class", now.plusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
