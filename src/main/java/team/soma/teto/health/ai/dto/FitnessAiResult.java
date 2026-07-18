package team.soma.teto.health.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FitnessAiResult(
        String summary,
        String overallLevel,
        List<WeakArea> weakAreas,
        List<Solution> solutions
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeakArea(String itemCode, String componentCode, String reason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Solution(String title, String description, String frequency) {
    }
}
