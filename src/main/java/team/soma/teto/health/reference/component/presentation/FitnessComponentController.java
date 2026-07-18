package team.soma.teto.health.reference.component.presentation;

import io.swagger.v3.oas.annotations.Operation;
import java.time.Clock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.soma.teto.health.global.response.ApiResponse;
import team.soma.teto.health.reference.component.application.GetFitnessComponentsService;

@RestController
@RequestMapping("/api/v1/paps/components")
public class FitnessComponentController {

    private final GetFitnessComponentsService getFitnessComponentsService;
    private final Clock clock;

    public FitnessComponentController(GetFitnessComponentsService getFitnessComponentsService, Clock clock) {
        this.getFitnessComponentsService = getFitnessComponentsService;
        this.clock = clock;
    }

    @Operation(summary = "PAPS 체력 요소 목록 조회", description = "RN 앱에서 PAPS 입력 화면 구성을 위해 활성 체력 요소만 displayOrder 순서로 조회합니다.")
    @GetMapping
    public ApiResponse<FitnessComponentResponse> getComponents() {
        return ApiResponse.success(getFitnessComponentsService.getComponents(), clock);
    }
}
