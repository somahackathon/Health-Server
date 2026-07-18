package team.soma.teto.health.global.presentation;

import java.time.Clock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import team.soma.teto.health.global.response.ApiResponse;

@RestController
public class HealthCheckController {

    private final Clock clock;

    public HealthCheckController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/health")
    public ApiResponse<HealthCheckResponse> health() {
        return ApiResponse.success(new HealthCheckResponse("ok"), clock);
    }
}
