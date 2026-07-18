package team.soma.teto.health.ai.client;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.PostureAnalysisAiRequest;
import team.soma.teto.health.ai.dto.PostureAnalysisAiResponse;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "real", matchIfMissing = true)
public class HttpPostureAiClient extends HttpAiClientSupport implements PostureAiClient {

    private final AiProperties aiProperties;

    public HttpPostureAiClient(AiProperties aiProperties, ObjectMapper objectMapper) {
        super(aiProperties, objectMapper);
        this.aiProperties = aiProperties;
    }

    @Override
    public PostureAnalysisAiResponse analyze(PostureAnalysisAiRequest request, Path videoPath) {
        String boundary = "health-" + UUID.randomUUID();
        HttpRequest.BodyPublisher body = multipartBody(boundary, request, videoPath);
        return postMultipart(aiProperties.getPosturePath(), request.correlationId(), boundary, body, PostureAnalysisAiResponse.class);
    }

    private HttpRequest.BodyPublisher multipartBody(String boundary, PostureAnalysisAiRequest request, Path videoPath) {
        byte[] metadata = writeJson(request);
        try {
            return HttpRequest.BodyPublishers.concat(
                    textPart(boundary, "metadata", "application/json", metadata),
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
}
