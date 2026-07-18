package team.soma.teto.health.global.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-07-18T06:30:00Z"), ZoneOffset.UTC);

    @Test
    void createSuccessResponseWithData() {
        ApiResponse<Map<String, String>> response = ApiResponse.success(Map.of("result", "ok"), fixedClock);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).containsEntry("result", "ok");
        assertThat(response.error()).isNull();
        assertThat(response.timestamp()).isEqualTo("2026-07-18T06:30Z");
    }

    @Test
    void createSuccessResponseWithoutData() {
        ApiResponse<Void> response = ApiResponse.success(fixedClock);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNull();
        assertThat(response.error()).isNull();
        assertThat(response.timestamp()).isEqualTo("2026-07-18T06:30Z");
    }
}
