package team.soma.teto.health.ai.dto;

import java.math.BigDecimal;
import java.util.Map;

public record PoseExtractionAiResponse(
        String exerciseType,
        BigDecimal durationSec,
        Integer sampledFps,
        Boolean personDetected,
        Map<String, Object> metrics
) {
}
