package team.soma.teto.health.analysis.job.application;

public record AnalysisJobCleanupResult(int expiredJobCount, int payloadsPurgedCount) {
}
