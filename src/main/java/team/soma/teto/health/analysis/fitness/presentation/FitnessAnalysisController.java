package team.soma.teto.health.analysis.fitness.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.Clock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import team.soma.teto.health.analysis.job.application.InstallationIdHasher;
import team.soma.teto.health.analysis.fitness.application.FitnessAnalysisService;
import team.soma.teto.health.global.config.RequestHeaderNames;
import team.soma.teto.health.global.response.ApiResponse;

@Validated
@RestController
public class FitnessAnalysisController {

    private static final String INSTALLATION_ID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private final FitnessAnalysisService fitnessAnalysisService;
    private final InstallationIdHasher installationIdHasher;
    private final Clock clock;

    public FitnessAnalysisController(FitnessAnalysisService fitnessAnalysisService, InstallationIdHasher installationIdHasher, Clock clock) {
        this.fitnessAnalysisService = fitnessAnalysisService;
        this.installationIdHasher = installationIdHasher;
        this.clock = clock;
    }

    @PostMapping("/api/fitness-analyses")
    @Operation(summary = "체력 AI 분석 요청", description = "RN 앱의 체력 분석 입력을 AI 서버로 전달하고 임시 분석 작업 결과를 반환합니다. Installation ID는 서버에서 SHA-256으로 해시되어 저장됩니다.")
    public ResponseEntity<ApiResponse<FitnessAnalysisResponse>> analyze(
            @Parameter(description = "RN 앱 설치 UUID")
            @RequestHeader(RequestHeaderNames.INSTALLATION_ID)
            @Pattern(regexp = INSTALLATION_ID_PATTERN, message = "installation ID must be a UUID") String installationId,
            @Valid @RequestBody FitnessAnalysisRequest request
    ) {
        String installationHash = installationIdHasher.hash(installationId);
        FitnessAnalysisResponse response = fitnessAnalysisService.analyze(installationHash, request);
        return ResponseEntity.ok(ApiResponse.success(response, clock));
    }
}
