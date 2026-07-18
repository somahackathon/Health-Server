package team.soma.teto.health.ai.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;

class RestGeminiClientTest {

    private record SampleResult(String summary) {
    }

    private final GeminiProperties properties = new GeminiProperties(
            "test-api-key", "http://gemini.test", "test-model", Duration.ofSeconds(5), Duration.ofSeconds(90));

    @Test
    void sendsCorrectUrlHeaderAndBody() {
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.baseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        RestGeminiClient client = new RestGeminiClient(restClient, properties, new GeminiResponseParser(new com.fasterxml.jackson.databind.ObjectMapper()));

        server.expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-api-key"))
                .andExpect(content().string(containsString("responseSchema")))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"{\\"summary\\":\\"ok\\"}"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        GeminiGenerateRequest request = GeminiGenerateRequest.of("system", "user", Map.of("type", "OBJECT"));
        SampleResult result = client.generate(request, SampleResult.class);

        assertThat(result.summary()).isEqualTo("ok");
        server.verify();
    }

    @Test
    void mapsServerErrorToAiServerError() {
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.baseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        RestGeminiClient client = new RestGeminiClient(restClient, properties, new GeminiResponseParser(new com.fasterxml.jackson.databind.ObjectMapper()));

        server.expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andRespond(withServerError());

        GeminiGenerateRequest request = GeminiGenerateRequest.of("system", "user", Map.of("type", "OBJECT"));

        assertThatThrownBy(() -> client.generate(request, SampleResult.class))
                .isInstanceOf(AiClientException.class)
                .satisfies(e -> assertThat(((AiClientException) e).failureCode()).isEqualTo(AiFailureCode.AI_SERVER_ERROR));
    }

    @Test
    void mapsConnectionFailureToAiTimeout() {
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.baseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        RestGeminiClient client = new RestGeminiClient(restClient, properties, new GeminiResponseParser(new com.fasterxml.jackson.databind.ObjectMapper()));

        server.expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andRespond(request -> {
                    throw new SocketTimeoutException("timeout");
                });

        GeminiGenerateRequest request = GeminiGenerateRequest.of("system", "user", Map.of("type", "OBJECT"));

        assertThatThrownBy(() -> client.generate(request, SampleResult.class))
                .isInstanceOf(AiClientException.class)
                .satisfies(e -> assertThat(((AiClientException) e).failureCode()).isEqualTo(AiFailureCode.AI_TIMEOUT));
    }
}
