package team.soma.teto.health.ai.posture.dto;

import java.util.List;
import java.util.UUID;
import team.soma.teto.health.ai.dto.PostureAiResult;
import team.soma.teto.health.ai.AnalysisDisclaimer;

public record PostureAnalysisResponse(
        UUID analysisId,
        String exerciseType,
        Integer postureScore,
        List<PostureAiResult.ProblemSegment> problemSegments,
        List<PostureAiResult.Improvement> improvements,
        String disclaimer,
        String modelVersion
) {

    public static PostureAnalysisResponse of(
            UUID analysisId, String exerciseType, PostureAiResult result, String modelVersion
    ) {
        return new PostureAnalysisResponse(
                analysisId,
                exerciseType,
                result.postureScore(),
                result.problemSegments(),
                result.improvements(),
                AnalysisDisclaimer.TEXT,
                modelVersion
        );
    }
}
