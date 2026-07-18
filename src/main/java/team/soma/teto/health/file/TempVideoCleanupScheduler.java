package team.soma.teto.health.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TempVideoCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TempVideoCleanupScheduler.class);

    private final VideoProperties videoProperties;
    private final Clock clock;

    public TempVideoCleanupScheduler(VideoProperties videoProperties, Clock clock) {
        this.videoProperties = videoProperties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.video.cleanup-interval-ms:600000}")
    public void deleteStaleTempVideos() {
        Path tempDir = Path.of(videoProperties.getTempDir());
        if (!Files.isDirectory(tempDir)) {
            return;
        }
        Instant staleBefore = Instant.now(clock).minus(videoProperties.getStaleAfter());
        try (Stream<Path> files = Files.list(tempDir)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> isStale(path, staleBefore))
                    .forEach(this::deleteQuietly);
        } catch (IOException exception) {
            log.warn("failed to scan temp video directory: {}", tempDir, exception);
        }
    }

    private boolean isStale(Path path, Instant staleBefore) {
        try {
            return Files.getLastModifiedTime(path).toInstant().isBefore(staleBefore);
        } catch (IOException exception) {
            return false;
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
            log.info("deleted stale temp video file: {}", path);
        } catch (IOException exception) {
            log.warn("failed to delete stale temp video file: {}", path, exception);
        }
    }
}
