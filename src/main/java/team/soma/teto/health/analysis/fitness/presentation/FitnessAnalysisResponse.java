package team.soma.teto.health.analysis.fitness.presentation;

import java.util.List;
import java.util.UUID;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;

public record FitnessAnalysisResponse(
        UUID jobId,
        AnalysisStatus status,
        String modelVersion,
        String summary,
        List<Recommendation> recommendations
) {

    public record Recommendation(String title, String description) {
    }
}
