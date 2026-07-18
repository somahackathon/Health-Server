package team.soma.teto.health.ai.dto;

import java.util.List;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;

public record PostureAnalysisAiResponse(
        String correlationId,
        String modelVersion,
        AnalysisStatus status,
        List<Feedback> feedback
) {

    public record Feedback(String code, String message, String severity) {
    }
}
