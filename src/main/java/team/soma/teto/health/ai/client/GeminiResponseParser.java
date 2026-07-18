package team.soma.teto.health.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.GeminiContent;
import team.soma.teto.health.ai.dto.GeminiGenerateResponse;
import team.soma.teto.health.ai.job.domain.AiFailureCode;

@Component
public class GeminiResponseParser {

    private final ObjectMapper objectMapper;

    public GeminiResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T parse(GeminiGenerateResponse response, Class<T> resultType) {
        String json = extractText(response);
        try {
            return objectMapper.readValue(json, resultType);
        } catch (JsonProcessingException e) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "Gemini result JSON parsing failed");
        }
    }

    private String extractText(GeminiGenerateResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "Gemini response has no candidates");
        }
        GeminiContent content = response.candidates().get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "Gemini response has no content parts");
        }
        String text = content.parts().get(0).text();
        if (text == null || text.isBlank()) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "Gemini response text is empty");
        }
        return text;
    }
}
