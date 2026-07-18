package team.soma.teto.health.analysis.posture.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import team.soma.teto.health.file.VideoProperties;
import team.soma.teto.health.global.config.RequestHeaderNames;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostureAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VideoProperties videoProperties;

    @Test
    void analyzePostureReturnsFeedbackAndDeletesTempVideo() throws Exception {
        MockMultipartFile video = new MockMultipartFile("video", "squat.mp4", "video/mp4", "fake-video-bytes".getBytes());

        mockMvc.perform(multipart("/api/posture-analyses")
                        .file(video)
                        .param("exerciseType", "SQUAT")
                        .header(RequestHeaderNames.INSTALLATION_HASH, "install-posture-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.modelVersion").value("mock-posture-v1"))
                .andExpect(jsonPath("$.data.feedback", not(empty())));

        Path tempDir = Path.of(videoProperties.getTempDir());
        if (Files.isDirectory(tempDir)) {
            try (Stream<Path> files = Files.list(tempDir)) {
                assertThat(files.count()).isZero();
            }
        }
    }

    @Test
    void rejectUnsupportedVideoContentType() throws Exception {
        MockMultipartFile video = new MockMultipartFile("video", "clip.avi", "video/x-msvideo", "fake".getBytes());

        mockMvc.perform(multipart("/api/posture-analyses")
                        .file(video)
                        .param("exerciseType", "SQUAT")
                        .header(RequestHeaderNames.INSTALLATION_HASH, "install-posture-test"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.code").value("VIDEO_UNSUPPORTED_TYPE"));
    }
}
