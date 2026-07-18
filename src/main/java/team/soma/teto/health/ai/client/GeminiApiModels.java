package team.soma.teto.health.ai.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * Wire records for the Gemini {@code generateContent} REST API. Request records use
 * {@code @JsonInclude(NON_NULL)} and response records use {@code @JsonIgnoreProperties(ignoreUnknown = true)}
 * because {@code spring.jackson.default-property-inclusion=always} and
 * {@code FAIL_ON_UNKNOWN_PROPERTIES=true} are set globally for this application, and Gemini's
 * response carries extra fields (usageMetadata, safetyRatings, ...) this app doesn't model.
 */
final class GeminiApiModels {

    private GeminiApiModels() {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record GenerateRequest(List<Content> contents, Content systemInstruction, GenerationConfig generationConfig) {

        static GenerateRequest of(String systemInstruction, String userText, Map<String, Object> responseSchema) {
            return new GenerateRequest(
                    List.of(Content.ofText(userText)),
                    Content.ofText(systemInstruction),
                    GenerationConfig.jsonWithSchema(responseSchema)
            );
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(List<Part> parts) {

        static Content ofText(String text) {
            return new Content(List.of(new Part(text)));
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(String text) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record GenerationConfig(String responseMimeType, Map<String, Object> responseSchema) {

        static GenerationConfig jsonWithSchema(Map<String, Object> responseSchema) {
            return new GenerationConfig("application/json", responseSchema);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GenerateResponse(List<Candidate> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content, String finishReason) {
    }
}
