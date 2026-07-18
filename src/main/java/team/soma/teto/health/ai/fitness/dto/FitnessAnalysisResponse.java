package team.soma.teto.health.ai.fitness.dto;

import java.util.List;
import java.util.UUID;
import team.soma.teto.health.ai.dto.FitnessAiResult;
import team.soma.teto.health.ai.AnalysisDisclaimer;

public record FitnessAnalysisResponse(
        UUID analysisId,
        String summary,
        String overallLevel,
        List<FitnessAiResult.WeakArea> weakAreas,
        List<FitnessAiResult.Solution> solutions,
        String disclaimer,
        String modelVersion
) {

    public static FitnessAnalysisResponse of(UUID analysisId, FitnessAiResult result, String modelVersion) {
        return new FitnessAnalysisResponse(
                analysisId,
                result.summary(),
                result.overallLevel(),
                result.weakAreas(),
                result.solutions(),
                AnalysisDisclaimer.TEXT,
                modelVersion
        );
    }
}
