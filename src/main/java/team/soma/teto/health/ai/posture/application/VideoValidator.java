package team.soma.teto.health.ai.posture.application;

import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import team.soma.teto.health.ai.client.PoseProperties;
import team.soma.teto.health.ai.error.AiErrorCode;
import team.soma.teto.health.global.error.BusinessException;

@Component
public class VideoValidator {

    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of("video/mp4", "video/quicktime");

    private final PoseProperties properties;

    public VideoValidator(PoseProperties properties) {
        this.properties = properties;
    }

    public void validate(MultipartFile video) {
        if (video == null || video.isEmpty()) {
            throw new BusinessException(AiErrorCode.AI_VIDEO_PROCESSING_FAILED, "empty video file");
        }
        String contentType = video.getContentType();
        if (contentType == null || !SUPPORTED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(AiErrorCode.AI_UNSUPPORTED_VIDEO_TYPE);
        }
        DataSize maxSize = properties.maxVideoSize();
        if (video.getSize() > maxSize.toBytes()) {
            throw new BusinessException(AiErrorCode.AI_VIDEO_TOO_LARGE);
        }
    }
}
