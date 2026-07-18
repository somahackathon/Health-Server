package team.soma.teto.health.reference.standard.presentation;

import io.swagger.v3.oas.annotations.Operation;
import java.time.Clock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.soma.teto.health.global.response.ApiResponse;
import team.soma.teto.health.reference.standard.application.GetCurrentPapsStandardVersionService;

@RestController
@RequestMapping("/api/v1/paps/standards")
public class PapsStandardController {

    private final GetCurrentPapsStandardVersionService getCurrentPapsStandardVersionService;
    private final Clock clock;

    public PapsStandardController(GetCurrentPapsStandardVersionService getCurrentPapsStandardVersionService, Clock clock) {
        this.getCurrentPapsStandardVersionService = getCurrentPapsStandardVersionService;
        this.clock = clock;
    }

    @Operation(summary = "현재 PAPS 기준 버전 조회", description = "RN 앱에서 현재 적용 중인 PAPS 기준 버전과 공식 기준 여부를 확인합니다. official=false는 공식 PAPS 기준이 아닌 자체 기준입니다.")
    @GetMapping("/current")
    public ApiResponse<PapsStandardVersionResponse> getCurrentVersion() {
        return ApiResponse.success(getCurrentPapsStandardVersionService.getCurrentVersion(), clock);
    }
}
