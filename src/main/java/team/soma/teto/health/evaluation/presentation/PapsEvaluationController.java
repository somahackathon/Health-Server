package team.soma.teto.health.evaluation.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.Clock;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.soma.teto.health.evaluation.application.EvaluatePapsService;
import team.soma.teto.health.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/paps/evaluations")
public class PapsEvaluationController {

    private final EvaluatePapsService evaluatePapsService;
    private final Clock clock;

    public PapsEvaluationController(EvaluatePapsService evaluatePapsService, Clock clock) {
        this.evaluatePapsService = evaluatePapsService;
        this.clock = clock;
    }

    @Operation(summary = "PAPS 기록 평가", description = "신체 정보와 PAPS 측정 기록을 검증하고 활성 기준 버전으로 종목별 등급을 판정합니다. BMI는 서버에서 계산하며 평가 결과는 서버에 저장하지 않습니다.")
    @PostMapping
    public ApiResponse<PapsEvaluationResponse> evaluate(@Valid @RequestBody PapsEvaluationRequest request) {
        return ApiResponse.success(evaluatePapsService.evaluate(request), clock);
    }
}
