package team.soma.teto.health.ai.client;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.PoseExtractionAiResponse;
import team.soma.teto.health.ai.dto.PostureAnalysisAiRequest;
import team.soma.teto.health.ai.dto.PostureAnalysisAiResponse;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnExpression("'${app.ai.posture-mode:real}'.equalsIgnoreCase('real')")
public class HttpPostureAiClient extends HttpAiClientSupport implements PostureAiClient {

    private static final String POSE_MODEL_VERSION = "ai-python-pose-v1";

    private final AiProperties aiProperties;
    private final GeminiPostureFeedbackGenerator feedbackGenerator;

    public HttpPostureAiClient(AiProperties aiProperties, GeminiPostureFeedbackGenerator feedbackGenerator, ObjectMapper objectMapper) {
        super(aiProperties, objectMapper);
        this.aiProperties = aiProperties;
        this.feedbackGenerator = feedbackGenerator;
    }

    @Override
    public PostureAnalysisAiResponse analyze(PostureAnalysisAiRequest request, Path videoPath) {
        String boundary = "health-" + UUID.randomUUID();
        HttpRequest.BodyPublisher body = multipartBody(boundary, request, videoPath);
        PoseExtractionAiResponse response = postMultipart(aiProperties.getPosturePath(), request.correlationId(), boundary, body, PoseExtractionAiResponse.class);
        return toPostureResponse(request, response);
    }

    private HttpRequest.BodyPublisher multipartBody(String boundary, PostureAnalysisAiRequest request, Path videoPath) {
        try {
            return HttpRequest.BodyPublishers.concat(
                    textPart(boundary, "exerciseType", "text/plain", request.exerciseType().getBytes(StandardCharsets.UTF_8)),
                    filePart(boundary, request.video().contentType(), videoPath),
                    HttpRequest.BodyPublishers.ofString("--" + boundary + "--\r\n", StandardCharsets.UTF_8)
            );
        } catch (IOException exception) {
            throw new AiClientException(AiFailureCode.TEMPORARY_FILE_ERROR, "temporary video could not be read");
        }
    }

    private HttpRequest.BodyPublisher textPart(String boundary, String name, String contentType, byte[] content) {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n";
        return HttpRequest.BodyPublishers.concat(
                HttpRequest.BodyPublishers.ofString(header, StandardCharsets.UTF_8),
                HttpRequest.BodyPublishers.ofByteArray(content),
                HttpRequest.BodyPublishers.ofString("\r\n", StandardCharsets.UTF_8)
        );
    }

    private HttpRequest.BodyPublisher filePart(String boundary, String contentType, Path videoPath) throws IOException {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"video\"; filename=\"video\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n";
        return HttpRequest.BodyPublishers.concat(
                HttpRequest.BodyPublishers.ofString(header, StandardCharsets.UTF_8),
                HttpRequest.BodyPublishers.ofFile(videoPath),
                HttpRequest.BodyPublishers.ofString("\r\n", StandardCharsets.UTF_8)
        );
    }

    private PostureAnalysisAiResponse toPostureResponse(PostureAnalysisAiRequest request, PoseExtractionAiResponse response) {
        return new PostureAnalysisAiResponse(
                request.correlationId(),
                POSE_MODEL_VERSION,
                AnalysisStatus.COMPLETED,
                feedback(request, response)
        );
    }

    private List<PostureAnalysisAiResponse.Feedback> feedback(PostureAnalysisAiRequest request, PoseExtractionAiResponse response) {
        return feedbackGenerator.generate(request.exerciseType(), response.metrics());
    }
}
