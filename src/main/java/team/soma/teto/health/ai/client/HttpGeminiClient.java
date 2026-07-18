package team.soma.teto.health.ai.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.client.GeminiApiModels.Candidate;
import team.soma.teto.health.ai.client.GeminiApiModels.GenerateRequest;
import team.soma.teto.health.ai.client.GeminiApiModels.GenerateResponse;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class HttpGeminiClient implements GeminiClient {

    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public HttpGeminiClient(GeminiProperties geminiProperties, ObjectMapper objectMapper) {
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(geminiProperties.getConnectTimeout())
                .build();
    }

    @Override
    public <T> T generate(String systemInstruction, String userText, Map<String, Object> responseSchema, Class<T> resultType) {
        GenerateRequest requestBody = GenerateRequest.of(systemInstruction, userText, responseSchema);
        HttpRequest request = HttpRequest.newBuilder(resolveUri())
                .timeout(geminiProperties.getReadTimeout())
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiProperties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofByteArray(writeJson(requestBody)))
                .build();

        GenerateResponse response = send(request);
        String text = extractText(response);
        return readJson(text.getBytes(StandardCharsets.UTF_8), resultType);
    }

    private URI resolveUri() {
        String baseUrl = geminiProperties.getBaseUrl().toString();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return URI.create(normalizedBaseUrl + "/v1beta/models/" + geminiProperties.getModel() + ":generateContent");
    }

    private GenerateResponse send(HttpRequest request) {
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 400 && response.statusCode() < 500) {
                throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "Gemini rejected the request");
            }
            if (response.statusCode() >= 500) {
                throw new AiClientException(AiFailureCode.AI_SERVER_ERROR, "Gemini server error");
            }
            return readJson(response.body(), GenerateResponse.class);
        } catch (HttpTimeoutException exception) {
            throw new AiClientException(AiFailureCode.AI_TIMEOUT, "Gemini request timed out");
        } catch (IOException exception) {
            throw new AiClientException(AiFailureCode.AI_SERVER_ERROR, "Gemini request failed");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AiClientException(AiFailureCode.AI_TIMEOUT, "Gemini request was interrupted");
        }
    }

    private String extractText(GenerateResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "Gemini response has no candidates");
        }
        Candidate candidate = response.candidates().get(0);
        if (candidate.content() == null || candidate.content().parts() == null || candidate.content().parts().isEmpty()) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "Gemini response has no content parts");
        }
        String text = candidate.content().parts().get(0).text();
        if (text == null || text.isBlank()) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "Gemini response text is empty");
        }
        return text;
    }

    private byte[] writeJson(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JacksonException exception) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "failed to serialize Gemini request");
        }
    }

    private <T> T readJson(byte[] bytes, Class<T> type) {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (JacksonException exception) {
            throw new AiClientException(AiFailureCode.INVALID_AI_RESPONSE, "Gemini response could not be parsed");
        }
    }
}
