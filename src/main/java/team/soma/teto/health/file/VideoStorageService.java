package team.soma.teto.health.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import team.soma.teto.health.file.error.VideoErrorCode;
import team.soma.teto.health.global.error.BusinessException;

@Component
public class VideoStorageService {

    private static final Logger log = LoggerFactory.getLogger(VideoStorageService.class);

    private final VideoProperties videoProperties;

    public VideoStorageService(VideoProperties videoProperties) {
        this.videoProperties = videoProperties;
    }

    public Path storeTemporarily(MultipartFile video) {
        try {
            Path tempDir = Path.of(videoProperties.getTempDir());
            Files.createDirectories(tempDir);
            Path target = tempDir.resolve(UUID.randomUUID() + resolveExtension(video));
            video.transferTo(target);
            return target;
        } catch (IOException exception) {
            throw new BusinessException(VideoErrorCode.TEMP_FILE_ERROR);
        }
    }

    public void delete(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.warn("failed to delete temporary video file: {}", path, exception);
        }
    }

    private String resolveExtension(MultipartFile video) {
        String originalFilename = video.getOriginalFilename();
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        return dotIndex >= 0 ? originalFilename.substring(dotIndex) : "";
    }
}
