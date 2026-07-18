package team.soma.teto.health.ai.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.ai.dto.GeminiGenerateResponse;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;

@Component
public class RestGeminiClient implements GeminiClient {

    private final RestClient restClient;
    private final GeminiProperties properties;
    private final GeminiResponseParser parser;

    public RestGeminiClient(
            @Qualifier("geminiRestClient") RestClient restClient,
            GeminiProperties properties,
            GeminiResponseParser parser
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.parser = parser;
    }

    @Override
    public <T> T generate(GeminiGenerateRequest request, Class<T> resultType) {
        GeminiGenerateResponse response;
        try {
            response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", properties.model())
                    .header("x-goog-api-key", properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiGenerateResponse.class);
        } catch (ResourceAccessException e) {
            throw new AiClientException(AiFailureCode.AI_TIMEOUT, "Gemini request timed out or connection failed");
        } catch (RestClientResponseException e) {
            throw new AiClientException(AiFailureCode.AI_SERVER_ERROR, "Gemini returned status " + e.getStatusCode().value());
        }
        return parser.parse(response, resultType);
    }
}
