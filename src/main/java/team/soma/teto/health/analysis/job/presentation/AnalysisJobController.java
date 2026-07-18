package team.soma.teto.health.analysis.job.presentation;

import java.time.Clock;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import team.soma.teto.health.analysis.job.application.AiAnalysisJobService;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.global.config.RequestHeaderNames;
import team.soma.teto.health.global.response.ApiResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
public class AnalysisJobController {

    private final AiAnalysisJobService aiAnalysisJobService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AnalysisJobController(AiAnalysisJobService aiAnalysisJobService, ObjectMapper objectMapper, Clock clock) {
        this.aiAnalysisJobService = aiAnalysisJobService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @GetMapping("/api/analysis-jobs/{publicId}")
    public ResponseEntity<ApiResponse<AnalysisJobResponse>> getJob(
            @RequestHeader(RequestHeaderNames.INSTALLATION_HASH) String installationHash,
            @PathVariable UUID publicId
    ) {
        AiAnalysisJob job = aiAnalysisJobService.getJob(installationHash, publicId);
        return ResponseEntity.ok(ApiResponse.success(toResponse(job), clock));
    }

    private AnalysisJobResponse toResponse(AiAnalysisJob job) {
        return new AnalysisJobResponse(
                job.getPublicId(),
                job.getAnalysisType(),
                job.getStatus(),
                job.getModelVersion(),
                readJson(job.getResultPayload()),
                job.getFailureCode(),
                job.getFailureMessage(),
                job.getExpiresAt(),
                job.getCompletedAt()
        );
    }

    private JsonNode readJson(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(payload);
        } catch (JacksonException exception) {
            return null;
        }
    }
}
