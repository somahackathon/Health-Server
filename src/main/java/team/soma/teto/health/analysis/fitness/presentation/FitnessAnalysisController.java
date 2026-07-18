package team.soma.teto.health.analysis.fitness.presentation;

import jakarta.validation.Valid;
import java.time.Clock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import team.soma.teto.health.analysis.fitness.application.FitnessAnalysisService;
import team.soma.teto.health.global.config.RequestHeaderNames;
import team.soma.teto.health.global.response.ApiResponse;

@RestController
public class FitnessAnalysisController {

    private final FitnessAnalysisService fitnessAnalysisService;
    private final Clock clock;

    public FitnessAnalysisController(FitnessAnalysisService fitnessAnalysisService, Clock clock) {
        this.fitnessAnalysisService = fitnessAnalysisService;
        this.clock = clock;
    }

    @PostMapping("/api/fitness-analyses")
    public ResponseEntity<ApiResponse<FitnessAnalysisResponse>> analyze(
            @RequestHeader(RequestHeaderNames.INSTALLATION_HASH) String installationHash,
            @Valid @RequestBody FitnessAnalysisRequest request
    ) {
        FitnessAnalysisResponse response = fitnessAnalysisService.analyze(installationHash, request);
        return ResponseEntity.ok(ApiResponse.success(response, clock));
    }
}
