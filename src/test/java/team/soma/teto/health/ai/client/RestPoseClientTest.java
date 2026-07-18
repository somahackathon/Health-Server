package team.soma.teto.health.ai.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import team.soma.teto.health.ai.dto.PoseExtractionResult;
import team.soma.teto.health.ai.job.domain.AiFailureCode;

class RestPoseClientTest {

    @Test
    void parsesSuccessfulExtractionResponse() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://pose.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        RestPoseClient client = new RestPoseClient(restClient);

        server.expect(requestTo("http://pose.test/pose/extract"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"exerciseType":"PUSH_UP","durationSec":10.0,"sampledFps":10,"personDetected":true,"metrics":{"repCount":3}}
                        """, MediaType.APPLICATION_JSON));

        PoseExtractionResult result = client.extract(new byte[]{1, 2, 3}, "PUSH_UP");

        assertThat(result.exerciseType()).isEqualTo("PUSH_UP");
        assertThat(result.metrics().get("repCount")).isEqualTo(3);
        server.verify();
    }

    @Test
    void mapsUnprocessableEntityToVideoProcessingFailed() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://pose.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        RestPoseClient client = new RestPoseClient(restClient);

        server.expect(requestTo("http://pose.test/pose/extract"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("{\"code\":\"NO_PERSON_DETECTED\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.extract(new byte[]{1}, "PUSH_UP"))
                .isInstanceOf(AiClientException.class)
                .satisfies(e -> assertThat(((AiClientException) e).failureCode()).isEqualTo(AiFailureCode.VIDEO_PROCESSING_FAILED));
    }

    @Test
    void mapsServerErrorToAiServerError() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://pose.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        RestPoseClient client = new RestPoseClient(restClient);

        server.expect(requestTo("http://pose.test/pose/extract"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.extract(new byte[]{1}, "PUSH_UP"))
                .isInstanceOf(AiClientException.class)
                .satisfies(e -> assertThat(((AiClientException) e).failureCode()).isEqualTo(AiFailureCode.AI_SERVER_ERROR));
    }

    @Test
    void mapsConnectionFailureToAiTimeout() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://pose.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        RestPoseClient client = new RestPoseClient(restClient);

        server.expect(requestTo("http://pose.test/pose/extract"))
                .andRespond(request -> {
                    throw new SocketTimeoutException("timeout");
                });

        assertThatThrownBy(() -> client.extract(new byte[]{1}, "PUSH_UP"))
                .isInstanceOf(AiClientException.class)
                .satisfies(e -> assertThat(((AiClientException) e).failureCode()).isEqualTo(AiFailureCode.AI_TIMEOUT));
    }
}
