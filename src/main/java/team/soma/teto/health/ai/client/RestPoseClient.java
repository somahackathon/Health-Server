package team.soma.teto.health.ai.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import team.soma.teto.health.ai.dto.PoseExtractionResult;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;

@Component
public class RestPoseClient implements PoseClient {

    private final RestClient restClient;

    public RestPoseClient(@Qualifier("poseRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public PoseExtractionResult extract(byte[] videoBytes, String exerciseType) {
        MultiValueMap<String, Object> body = buildMultipartBody(videoBytes, exerciseType);

        try {
            return restClient.post()
                    .uri("/pose/extract")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(PoseExtractionResult.class);
        } catch (ResourceAccessException e) {
            throw new AiClientException(AiFailureCode.AI_TIMEOUT, "Pose server request timed out or connection failed");
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 422 || e.getStatusCode().value() == 400) {
                throw new AiClientException(AiFailureCode.VIDEO_PROCESSING_FAILED, "Pose server could not process the video");
            }
            throw new AiClientException(AiFailureCode.AI_SERVER_ERROR, "Pose server returned status " + e.getStatusCode().value());
        }
    }

    /**
     * Built without {@code MultipartBodyBuilder} — that class requires
     * org.reactivestreams.Publisher to be on the classpath (a WebFlux/Reactor
     * type this webmvc-only project doesn't depend on), which triggers a
     * NoClassDefFoundError at class-load time even though its reactive
     * methods are never called. A plain HttpEntity-based MultiValueMap is
     * fully supported by RestClient's FormHttpMessageConverter without that
     * dependency.
     */
    private MultiValueMap<String, Object> buildMultipartBody(byte[] videoBytes, String exerciseType) {
        HttpHeaders videoHeaders = new HttpHeaders();
        videoHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        videoHeaders.setContentDispositionFormData("video", "video");
        ByteArrayResource videoResource = new ByteArrayResource(videoBytes) {
            @Override
            public String getFilename() {
                return "video";
            }
        };
        HttpEntity<ByteArrayResource> videoPart = new HttpEntity<>(videoResource, videoHeaders);

        HttpHeaders exerciseTypeHeaders = new HttpHeaders();
        exerciseTypeHeaders.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> exerciseTypePart = new HttpEntity<>(exerciseType, exerciseTypeHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("video", videoPart);
        body.add("exerciseType", exerciseTypePart);
        return body;
    }
}
