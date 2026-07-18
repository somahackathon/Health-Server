package team.soma.teto.health.analysis.job.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.analysis")
public class AnalysisCleanupProperties {

    private int cleanupBatchSize = 100;

    public int getCleanupBatchSize() {
        return cleanupBatchSize;
    }

    public void setCleanupBatchSize(int cleanupBatchSize) {
        this.cleanupBatchSize = Math.max(1, cleanupBatchSize);
    }
}
