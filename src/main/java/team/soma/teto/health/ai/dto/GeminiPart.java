package team.soma.teto.health.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiPart(String text) {

    public static GeminiPart of(String text) {
        return new GeminiPart(text);
    }
}
