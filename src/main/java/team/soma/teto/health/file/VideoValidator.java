package team.soma.teto.health.file;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import team.soma.teto.health.file.error.VideoErrorCode;
import team.soma.teto.health.global.error.BusinessException;

@Component
public class VideoValidator {

    private final VideoProperties videoProperties;

    public VideoValidator(VideoProperties videoProperties) {
        this.videoProperties = videoProperties;
    }

    public void validate(MultipartFile video) {
        if (video == null || video.isEmpty()) {
            throw new BusinessException(VideoErrorCode.EMPTY_VIDEO);
        }
        if (!videoProperties.getAllowedContentTypes().contains(video.getContentType())) {
            throw new BusinessException(VideoErrorCode.UNSUPPORTED_VIDEO_TYPE);
        }
        if (!videoProperties.getAllowedExtensions().contains(resolveExtension(video))) {
            throw new BusinessException(VideoErrorCode.UNSUPPORTED_VIDEO_TYPE);
        }
        if (video.getSize() > videoProperties.getMaxSizeBytes()) {
            throw new BusinessException(VideoErrorCode.VIDEO_TOO_LARGE);
        }
    }

    private String resolveExtension(MultipartFile video) {
        String originalFilename = video.getOriginalFilename();
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        return dotIndex >= 0 ? originalFilename.substring(dotIndex).toLowerCase(java.util.Locale.ROOT) : "";
    }
}
