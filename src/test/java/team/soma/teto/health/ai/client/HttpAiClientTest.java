package team.soma.teto.health.ai.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
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

    private static final String GEMINI_TEST_MODEL = "test-model";

    private static final AtomicReference<String> postureContentType = new AtomicReference<>();
    private static final AtomicReference<String> postureBody = new AtomicReference<>();
    private static final AtomicReference<String> geminiApiKeyHeader = new AtomicReference<>();
    private static final AtomicReference<String> geminiRequestBody = new AtomicReference<>();
    private static final HttpServer server = startServer();

    @Autowired
    private FitnessAiClient fitnessAiClient;

    @Autowired
    private PostureAiClient postureAiClient;

    @DynamicPropertySource
    static void aiProperties(DynamicPropertyRegistry registry) {
        registry.add("app.ai.mode", () -> "real");
        registry.add("app.ai.fitness-mode", () -> "real");
        registry.add("app.ai.posture-mode", () -> "real");
        registry.add("app.ai.base-url", () -> "http://localhost:" + server.getAddress().getPort() + "/");
        registry.add("app.ai.api-key", () -> "test-api-key");
        registry.add("app.ai.fitness-path", () -> "/fitness");
        registry.add("app.ai.posture-path", () -> "/posture");
        registry.add("app.ai.read-timeout", () -> "1s");
        registry.add("app.ai.connect-timeout", () -> "1s");
        registry.add("app.ai.retry-count", () -> "0");
        registry.add("app.ai.gemini.base-url", () -> "http://localhost:" + server.getAddress().getPort() + "/");
        registry.add("app.ai.gemini.api-key", () -> "test-gemini-key");
        registry.add("app.ai.gemini.model", () -> GEMINI_TEST_MODEL);
        registry.add("app.ai.gemini.read-timeout", () -> "1s");
        registry.add("app.ai.gemini.connect-timeout", () -> "1s");
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void fitnessClientCallsGeminiAndReadsResponse() {
        FitnessAnalysisAiRequest request = new FitnessAnalysisAiRequest(
                "corr-fitness",
                null,
                new FitnessAnalysisAiRequest.Profile(LocalDate.of(2010, 1, 1), Gender.MALE, 170.0, 60.0),
                List.of(new FitnessAnalysisAiRequest.RecordItem(FitnessTestItemCode.PUSH_UP, 20.0, MeasurementUnit.COUNT, Instant.parse("2026-07-01T00:00:00Z")))
        );

        FitnessAnalysisAiResponse response = fitnessAiClient.analyze(request);

        assertThat(response.correlationId()).isEqualTo("corr-fitness");
        assertThat(response.modelVersion()).isEqualTo(GEMINI_TEST_MODEL);
        assertThat(response.summary()).isEqualTo("mock-summary");
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).title()).isEqualTo("title");
        assertThat(geminiApiKeyHeader.get()).isEqualTo("test-gemini-key");
        assertThat(geminiRequestBody.get()).contains("responseSchema");
    }

    @Test
    void postureClientPostsMultipartWithVideoBytesThenGetsGeminiFeedback() throws IOException {
        Path video = Files.createTempFile("health-test-video", ".mp4");
        Files.writeString(video, "fake-video-bytes", StandardCharsets.UTF_8);
        PostureAnalysisAiRequest request = new PostureAnalysisAiRequest(
                "corr-posture",
                null,
                "SQUAT",
                new PostureAnalysisAiRequest.VideoMeta("video/mp4", Files.size(video))
        );

        PostureAnalysisAiResponse response = postureAiClient.analyze(request, video);

        assertThat(response.modelVersion()).isEqualTo("ai-python-pose-v1");
        assertThat(postureContentType.get()).contains("multipart/form-data");
        assertThat(postureBody.get()).contains("name=\"exerciseType\"");
        assertThat(postureBody.get()).contains("SQUAT");
        assertThat(postureBody.get()).contains("name=\"video\"");
        assertThat(postureBody.get()).contains("fake-video-bytes");
        assertThat(postureBody.get()).doesNotContain(video.getFileName().toString());
        assertThat(response.feedback()).hasSize(1);
        assertThat(response.feedback().get(0).code()).isEqualTo("OK");
        Files.deleteIfExists(video);
    }

    @Test
    void fitnessClientMapsGeminiServerError() {
        FitnessAnalysisAiRequest request = new FitnessAnalysisAiRequest(
                "corr-fitness-error",
                null,
                new FitnessAnalysisAiRequest.Profile(LocalDate.of(2010, 1, 1), Gender.MALE, 170.0, 999.0),
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
            httpServer.createContext("/posture", HttpAiClientTest::handlePosture);
            httpServer.createContext("/v1beta/models/" + GEMINI_TEST_MODEL + ":generateContent", HttpAiClientTest::handleGemini);
            httpServer.setExecutor(Executors.newSingleThreadExecutor());
            httpServer.start();
            return httpServer;
        } catch (IOException exception) {
            throw new IllegalStateException("failed to start test HTTP server", exception);
        }
    }

    /**
     * Backs both the fitness and posture-feedback Gemini calls. The canned JSON text
     * carries fields for both response shapes; each caller's record ignores the fields
     * it doesn't declare (see {@code @JsonIgnoreProperties(ignoreUnknown = true)}).
     */
    private static void handleGemini(HttpExchange exchange) throws IOException {
        geminiApiKeyHeader.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        geminiRequestBody.set(body);
        if (body.contains("999.0")) {
            respond(exchange, 500, "{\"error\":{\"message\":\"gemini unavailable\"}}");
            return;
        }
        String innerJson = "{\\\"summary\\\":\\\"mock-summary\\\","
                + "\\\"recommendations\\\":[{\\\"title\\\":\\\"title\\\",\\\"description\\\":\\\"description\\\"}],"
                + "\\\"feedback\\\":[{\\\"code\\\":\\\"OK\\\",\\\"message\\\":\\\"good posture\\\",\\\"severity\\\":\\\"LOW\\\"}]}";
        respond(exchange, 200, "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + innerJson + "\"}]}}]}");
    }

    private static void handlePosture(HttpExchange exchange) throws IOException {
        postureContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
        postureBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        respond(exchange, 200, """
                {
                  "exerciseType": "SQUAT",
                  "durationSec": 1.2,
                  "sampledFps": 10,
                  "personDetected": true,
                  "metrics": {
                    "repCount": 1
                  }
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
