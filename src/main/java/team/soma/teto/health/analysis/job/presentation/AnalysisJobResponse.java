package team.soma.teto.health.analysis.job.presentation;

import java.time.Instant;
import java.util.UUID;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;
import team.soma.teto.health.analysis.job.domain.AnalysisType;
import tools.jackson.databind.JsonNode;

public record AnalysisJobResponse(
        UUID jobId,
        AnalysisType analysisType,
        AnalysisStatus status,
        String modelVersion,
        JsonNode result,
        AiFailureCode failureCode,
        String failureMessage,
        Instant expiresAt,
        Instant completedAt
) {
}
