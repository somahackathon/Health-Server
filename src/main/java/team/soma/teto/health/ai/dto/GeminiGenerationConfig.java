package team.soma.teto.health.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiGenerationConfig(
        String responseMimeType,
        Map<String, Object> responseSchema
) {

    public static GeminiGenerationConfig jsonWithSchema(Map<String, Object> responseSchema) {
        return new GeminiGenerationConfig("application/json", responseSchema);
    }
}
