package team.soma.teto.health.analysis.posture.presentation;

import java.time.Clock;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import team.soma.teto.health.analysis.posture.application.PostureAnalysisService;
import team.soma.teto.health.analysis.posture.dto.PostureAnalysisResponse;
import team.soma.teto.health.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/analysis")
public class PostureAnalysisController {

    private final PostureAnalysisService service;
    private final Clock clock;

    public PostureAnalysisController(PostureAnalysisService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @PostMapping(value = "/posture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostureAnalysisResponse> analyze(
            @RequestPart("video") MultipartFile video,
            @RequestParam("installationId") String installationId,
            @RequestParam("exerciseType") String exerciseType
    ) {
        return ApiResponse.success(service.analyze(video, installationId, exerciseType), clock);
    }
}
