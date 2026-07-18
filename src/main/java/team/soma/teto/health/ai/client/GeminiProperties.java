package team.soma.teto.health.ai.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("ai.gemini")
public record GeminiProperties(
        @DefaultValue("") String apiKey,
        @DefaultValue("https://generativelanguage.googleapis.com") String baseUrl,
        @DefaultValue("gemini-3.5-flash-lite") String model,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("90s") Duration readTimeout
) {
}
