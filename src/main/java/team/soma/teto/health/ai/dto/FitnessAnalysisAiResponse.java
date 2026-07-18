package team.soma.teto.health.ai.dto;

import java.util.List;

public record FitnessAnalysisAiResponse(
        String correlationId,
        String modelVersion,
        String summary,
        List<Recommendation> recommendations
) {

    public record Recommendation(String title, String description) {
    }
}
