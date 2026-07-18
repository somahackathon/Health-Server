package team.soma.teto.health.ai.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties("ai.pose")
public record PoseProperties(
        @DefaultValue("http://localhost:8000") String baseUrl,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("60s") Duration readTimeout,
        @DefaultValue("20MB") DataSize maxVideoSize
) {
}
