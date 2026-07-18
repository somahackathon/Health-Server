package team.soma.teto.health.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TimeConfigTest {

    @Test
    void clockUsesKoreanBusinessDate() {
        Clock clock = new TimeConfig().clock();

        LocalDate date = LocalDate.ofInstant(Instant.parse("2026-07-18T15:30:00Z"), clock.getZone());

        assertThat(clock.getZone()).isEqualTo(TimeConfig.SERVICE_ZONE);
        assertThat(date).isEqualTo(LocalDate.of(2026, 7, 19));
    }
}
