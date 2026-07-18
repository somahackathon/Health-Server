package team.soma.teto.health.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PoseExtractionResult(
        String exerciseType,
        Double durationSec,
        Integer sampledFps,
        Boolean personDetected,
        Map<String, Object> metrics
) {
}
