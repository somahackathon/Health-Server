package team.soma.teto.health.analysis.job.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnalysisJobCleanupScheduler.class);

    private final AiAnalysisJobService aiAnalysisJobService;

    public AnalysisJobCleanupScheduler(AiAnalysisJobService aiAnalysisJobService) {
        this.aiAnalysisJobService = aiAnalysisJobService;
    }

    @Scheduled(fixedDelayString = "${app.analysis-job.cleanup-interval-ms:300000}")
    public void cleanupExpiredJobs() {
        AnalysisJobCleanupResult result = aiAnalysisJobService.cleanupExpiredJobs();
        if (result.expiredJobCount() > 0 || result.payloadsPurgedCount() > 0) {
            log.info("analysis job cleanup: expired={}, payloadsPurged={}", result.expiredJobCount(), result.payloadsPurgedCount());
        }
    }
}
