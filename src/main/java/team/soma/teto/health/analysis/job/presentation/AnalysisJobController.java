package team.soma.teto.health.analysis.job.presentation;

import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.Clock;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import team.soma.teto.health.analysis.job.application.AiAnalysisJobService;
import team.soma.teto.health.analysis.job.application.InstallationIdHasher;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.global.config.RequestHeaderNames;
import team.soma.teto.health.global.response.ApiResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Validated
@RestController
public class AnalysisJobController {

    private static final String INSTALLATION_ID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private final AiAnalysisJobService aiAnalysisJobService;
    private final InstallationIdHasher installationIdHasher;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AnalysisJobController(AiAnalysisJobService aiAnalysisJobService, InstallationIdHasher installationIdHasher, ObjectMapper objectMapper, Clock clock) {
        this.aiAnalysisJobService = aiAnalysisJobService;
        this.installationIdHasher = installationIdHasher;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @GetMapping("/api/analysis-jobs/{publicId}")
    @Operation(summary = "AI 분석 작업 조회", description = "분석 작업 publicId와 RN 앱 설치 ID를 함께 확인해 임시 작업 상태와 결과를 조회합니다.")
    public ResponseEntity<ApiResponse<AnalysisJobResponse>> getJob(
            @Parameter(description = "RN 앱 설치 UUID")
            @RequestHeader(RequestHeaderNames.INSTALLATION_ID)
            @Pattern(regexp = INSTALLATION_ID_PATTERN, message = "installation ID must be a UUID") String installationId,
            @PathVariable UUID publicId
    ) {
        String installationHash = installationIdHasher.hash(installationId);
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
