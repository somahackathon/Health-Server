package team.soma.teto.health.analysis.posture.presentation;

import java.util.List;
import java.util.UUID;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;

public record PostureAnalysisResponse(
        UUID jobId,
        AnalysisStatus status,
        String modelVersion,
        List<Feedback> feedback
) {

    public record Feedback(String code, String message, String severity) {
    }
}
