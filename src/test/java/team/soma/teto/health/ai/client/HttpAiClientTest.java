package team.soma.teto.health.ai.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiRequest;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiResponse;
import team.soma.teto.health.ai.dto.PostureAnalysisAiRequest;
import team.soma.teto.health.ai.dto.PostureAnalysisAiResponse;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;

@SpringBootTest
@ActiveProfiles("test")
class HttpAiClientTest {

    private static final AtomicReference<String> fitnessCorrelationId = new AtomicReference<>();
    private static final AtomicReference<String> postureContentType = new AtomicReference<>();
    private static final AtomicReference<String> postureBody = new AtomicReference<>();
    private static final HttpServer server = startServer();

    @Autowired
    private FitnessAiClient fitnessAiClient;

    @Autowired
    private PostureAiClient postureAiClient;

    @DynamicPropertySource
    static void aiProperties(DynamicPropertyRegistry registry) {
        registry.add("app.ai.mode", () -> "real");
        registry.add("app.ai.base-url", () -> "http://localhost:" + server.getAddress().getPort() + "/");
        registry.add("app.ai.api-key", () -> "test-api-key");
        registry.add("app.ai.fitness-path", () -> "/fitness");
        registry.add("app.ai.posture-path", () -> "/posture");
        registry.add("app.ai.read-timeout", () -> "1s");
        registry.add("app.ai.connect-timeout", () -> "1s");
        registry.add("app.ai.retry-count", () -> "0");
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void fitnessClientPostsJsonAndReadsResponse() {
        FitnessAnalysisAiRequest request = new FitnessAnalysisAiRequest(
                "corr-fitness",
                null,
                new FitnessAnalysisAiRequest.Profile(LocalDate.of(2010, 1, 1), Gender.MALE, 170.0, 60.0),
                List.of(new FitnessAnalysisAiRequest.RecordItem(FitnessTestItemCode.PUSH_UP, 20.0, MeasurementUnit.COUNT, Instant.parse("2026-07-01T00:00:00Z")))
        );

        FitnessAnalysisAiResponse response = fitnessAiClient.analyze(request);

        assertThat(response.modelVersion()).isEqualTo("http-fitness-v1");
        assertThat(response.summary()).isEqualTo("ok");
        assertThat(fitnessCorrelationId.get()).isEqualTo("corr-fitness");
    }

    @Test
    void postureClientPostsMultipartWithVideoBytes() throws IOException {
        Path video = Files.createTempFile("health-test-video", ".mp4");
        Files.writeString(video, "fake-video-bytes", StandardCharsets.UTF_8);
        PostureAnalysisAiRequest request = new PostureAnalysisAiRequest(
                "corr-posture",
                null,
                "SQUAT",
                new PostureAnalysisAiRequest.VideoMeta("video/mp4", Files.size(video))
        );

        PostureAnalysisAiResponse response = postureAiClient.analyze(request, video);

        assertThat(response.modelVersion()).isEqualTo("http-posture-v1");
        assertThat(postureContentType.get()).contains("multipart/form-data");
        assertThat(postureBody.get()).contains("name=\"metadata\"");
        assertThat(postureBody.get()).contains("name=\"video\"");
        assertThat(postureBody.get()).contains("fake-video-bytes");
        assertThat(postureBody.get()).doesNotContain(video.getFileName().toString());
        Files.deleteIfExists(video);
    }

    @Test
    void fitnessClientMapsServerError() {
        FitnessAnalysisAiRequest request = new FitnessAnalysisAiRequest(
                "corr-fitness-error",
                null,
                new FitnessAnalysisAiRequest.Profile(LocalDate.of(2010, 1, 1), Gender.MALE, 170.0, 60.0),
                List.of(new FitnessAnalysisAiRequest.RecordItem(FitnessTestItemCode.PUSH_UP, 20.0, MeasurementUnit.COUNT, Instant.parse("2026-07-01T00:00:00Z")))
        );

        assertThatThrownBy(() -> fitnessAiClient.analyze(request))
                .isInstanceOf(AiClientException.class)
                .extracting("failureCode")
                .isEqualTo(AiFailureCode.AI_SERVER_ERROR);
    }

    private static HttpServer startServer() {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
            httpServer.createContext("/fitness", HttpAiClientTest::handleFitness);
            httpServer.createContext("/posture", HttpAiClientTest::handlePosture);
            httpServer.setExecutor(Executors.newSingleThreadExecutor());
            httpServer.start();
            return httpServer;
        } catch (IOException exception) {
            throw new IllegalStateException("failed to start test HTTP server", exception);
        }
    }

    private static void handleFitness(HttpExchange exchange) throws IOException {
        fitnessCorrelationId.set(exchange.getRequestHeaders().getFirst("X-Correlation-Id"));
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.contains("corr-fitness-error")) {
            respond(exchange, 500, "{\"code\":\"AI_ERROR\"}");
            return;
        }
        respond(exchange, 200, """
                {
                  "correlationId": "corr-fitness",
                  "modelVersion": "http-fitness-v1",
                  "summary": "ok",
                  "recommendations": [
                    {
                      "title": "title",
                      "description": "description"
                    }
                  ]
                }
                """);
    }

    private static void handlePosture(HttpExchange exchange) throws IOException {
        postureContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
        postureBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        respond(exchange, 200, """
                {
                  "correlationId": "corr-posture",
                  "modelVersion": "http-posture-v1",
                  "status": "COMPLETED",
                  "feedback": [
                    {
                      "code": "KNEE_ALIGNMENT",
                      "message": "ok",
                      "severity": "LOW"
                    }
                  ]
                }
                """);
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
