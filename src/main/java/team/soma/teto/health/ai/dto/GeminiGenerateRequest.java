package team.soma.teto.health.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiGenerateRequest(
        List<GeminiContent> contents,
        GeminiContent systemInstruction,
        GeminiGenerationConfig generationConfig
) {

    public static GeminiGenerateRequest of(String systemInstruction, String userText, Map<String, Object> responseSchema) {
        return new GeminiGenerateRequest(
                List.of(GeminiContent.ofText(userText)),
                GeminiContent.ofText(systemInstruction),
                GeminiGenerationConfig.jsonWithSchema(responseSchema)
        );
    }
}
