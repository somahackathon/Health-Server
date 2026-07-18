package team.soma.teto.health.reference.testitem.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.Clock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.soma.teto.health.global.response.ApiResponse;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.testitem.application.GetFitnessTestItemsService;

@RestController
@RequestMapping("/api/v1/paps/test-items")
public class FitnessTestItemController {

    private final GetFitnessTestItemsService getFitnessTestItemsService;
    private final Clock clock;

    public FitnessTestItemController(GetFitnessTestItemsService getFitnessTestItemsService, Clock clock) {
        this.getFitnessTestItemsService = getFitnessTestItemsService;
        this.clock = clock;
    }

    @Operation(summary = "PAPS 측정 종목 조회", description = "RN 앱에서 입력 UI 구성을 위해 활성 측정 종목을 전체 또는 체력 요소별로 조회합니다.")
    @GetMapping
    public ApiResponse<FitnessTestItemResponse> getTestItems(
            @Parameter(description = "조회할 체력 요소 코드")
            @RequestParam(required = false) FitnessComponentCode component
    ) {
        return ApiResponse.success(getFitnessTestItemsService.getTestItems(component), clock);
    }
}
