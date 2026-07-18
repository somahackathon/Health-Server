package team.soma.teto.health.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiContent(List<GeminiPart> parts) {

    public static GeminiContent ofText(String text) {
        return new GeminiContent(List.of(GeminiPart.of(text)));
    }
}
