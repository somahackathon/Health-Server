package team.soma.teto.health.analysis.posture.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.Clock;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.soma.teto.health.analysis.job.application.InstallationIdHasher;
import team.soma.teto.health.analysis.posture.application.PostureAnalysisService;
import team.soma.teto.health.global.config.RequestHeaderNames;
import team.soma.teto.health.global.response.ApiResponse;

@Validated
@RestController
public class PostureAnalysisController {

    private static final String INSTALLATION_ID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private final PostureAnalysisService postureAnalysisService;
    private final InstallationIdHasher installationIdHasher;
    private final Clock clock;

    public PostureAnalysisController(PostureAnalysisService postureAnalysisService, InstallationIdHasher installationIdHasher, Clock clock) {
        this.postureAnalysisService = postureAnalysisService;
        this.installationIdHasher = installationIdHasher;
        this.clock = clock;
    }

    @PostMapping(value = "/api/posture-analyses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "자세 영상 AI 분석 요청", description = "운동 영상을 임시 저장한 뒤 metadata와 video multipart로 AI 서버에 전달하고, 성공과 실패 경로 모두에서 임시 영상을 삭제합니다.")
    public ResponseEntity<ApiResponse<PostureAnalysisResponse>> analyze(
            @Parameter(description = "RN 앱 설치 UUID")
            @RequestHeader(RequestHeaderNames.INSTALLATION_ID)
            @Pattern(regexp = INSTALLATION_ID_PATTERN, message = "installation ID must be a UUID") String installationId,
            @RequestParam @NotBlank String exerciseType,
            @RequestParam MultipartFile video
    ) {
        String installationHash = installationIdHasher.hash(installationId);
        PostureAnalysisResponse response = postureAnalysisService.analyze(installationHash, exerciseType, video);
        return ResponseEntity.ok(ApiResponse.success(response, clock));
    }
}
