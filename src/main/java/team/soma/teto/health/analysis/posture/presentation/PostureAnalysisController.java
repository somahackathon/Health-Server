package team.soma.teto.health.analysis.posture.presentation;

import jakarta.validation.constraints.NotBlank;
import java.time.Clock;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.soma.teto.health.analysis.posture.application.PostureAnalysisService;
import team.soma.teto.health.global.config.RequestHeaderNames;
import team.soma.teto.health.global.response.ApiResponse;

@Validated
@RestController
public class PostureAnalysisController {

    private final PostureAnalysisService postureAnalysisService;
    private final Clock clock;

    public PostureAnalysisController(PostureAnalysisService postureAnalysisService, Clock clock) {
        this.postureAnalysisService = postureAnalysisService;
        this.clock = clock;
    }

    @PostMapping(value = "/api/posture-analyses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostureAnalysisResponse>> analyze(
            @RequestHeader(RequestHeaderNames.INSTALLATION_HASH) String installationHash,
            @RequestParam @NotBlank String exerciseType,
            @RequestParam MultipartFile video
    ) {
        PostureAnalysisResponse response = postureAnalysisService.analyze(installationHash, exerciseType, video);
        return ResponseEntity.ok(ApiResponse.success(response, clock));
    }
}
