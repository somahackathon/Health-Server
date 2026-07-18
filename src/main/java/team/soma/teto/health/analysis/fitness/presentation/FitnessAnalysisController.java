package team.soma.teto.health.analysis.fitness.presentation;

import jakarta.validation.Valid;
import java.time.Clock;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.soma.teto.health.analysis.fitness.application.FitnessAnalysisService;
import team.soma.teto.health.analysis.fitness.dto.FitnessAnalysisRequest;
import team.soma.teto.health.analysis.fitness.dto.FitnessAnalysisResponse;
import team.soma.teto.health.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/analysis")
public class FitnessAnalysisController {

    private final FitnessAnalysisService service;
    private final Clock clock;

    public FitnessAnalysisController(FitnessAnalysisService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @PostMapping("/fitness")
    public ApiResponse<FitnessAnalysisResponse> analyze(@Valid @RequestBody FitnessAnalysisRequest request) {
        return ApiResponse.success(service.analyze(request), clock);
    }
}
