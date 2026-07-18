package team.soma.teto.health.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PostureAiResult(
        Integer postureScore,
        List<ProblemSegment> problemSegments,
        List<Improvement> improvements
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProblemSegment(
            Double startSec,
            Double endSec,
            String bodyPart,
            String issue,
            String severity
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Improvement(String bodyPart, String suggestion) {
    }
}
