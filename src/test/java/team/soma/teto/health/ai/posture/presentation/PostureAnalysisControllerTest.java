package team.soma.teto.health.ai.posture.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import team.soma.teto.health.ai.client.GeminiClient;
import team.soma.teto.health.ai.client.PoseClient;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.ai.dto.PoseExtractionResult;
import team.soma.teto.health.ai.dto.PostureAiResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostureAnalysisControllerTest.StubConfig.class)
class PostureAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeminiClient geminiClient;

    @Autowired
    private PoseClient poseClient;

    @AfterEach
    void reset() {
        ((StubGeminiClient) geminiClient).reset();
    }

    @Test
    void returnsPayloadTooLargeWhenVideoExceedsLimit() throws Exception {
        byte[] oversized = new byte[21 * 1024 * 1024];
        MockMultipartFile video = new MockMultipartFile("video", "push_up.mp4", "video/mp4", oversized);

        mockMvc.perform(multipart("/api/v1/analysis/posture")
                        .file(video)
                        .param("installationId", "install-1")
                        .param("exerciseType", "PUSH_UP"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error.code").value("AI_VIDEO_TOO_LARGE"));
    }

    @Test
    void returnsUnsupportedMediaTypeWhenContentTypeIsNotVideo() throws Exception {
        MockMultipartFile video = new MockMultipartFile("video", "note.txt", "text/plain", "not a video".getBytes());

        mockMvc.perform(multipart("/api/v1/analysis/posture")
                        .file(video)
                        .param("installationId", "install-1")
                        .param("exerciseType", "PUSH_UP"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.code").value("AI_UNSUPPORTED_VIDEO_TYPE"));
    }

    @Test
    void returnsBadRequestWhenExerciseTypeUnsupported() throws Exception {
        MockMultipartFile video = new MockMultipartFile("video", "squat.mp4", "video/mp4", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/analysis/posture")
                        .file(video)
                        .param("installationId", "install-1")
                        .param("exerciseType", "SQUAT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("AI_UNSUPPORTED_EXERCISE_TYPE"));
    }

    @Test
    void returnsSuccessEnvelopeOnHappyPath() throws Exception {
        ((StubGeminiClient) geminiClient).toReturn = new PostureAiResult(80, List.of(), List.of());
        MockMultipartFile video = new MockMultipartFile("video", "push_up.mp4", "video/mp4", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/analysis/posture")
                        .file(video)
                        .param("installationId", "install-1")
                        .param("exerciseType", "PUSH_UP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.analysisId").exists())
                .andExpect(jsonPath("$.data.postureScore").value(80))
                .andExpect(jsonPath("$.data.disclaimer").value("본 분석 결과는 참고용 정보이며 의학적 진단이 아닙니다."));
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        GeminiClient stubGeminiClient() {
            return new StubGeminiClient();
        }

        @Bean
        @Primary
        PoseClient stubPoseClient() {
            return (videoBytes, exerciseType) -> new PoseExtractionResult(
                    exerciseType, 10.0, 10, true, Map.of("repCount", 1));
        }
    }

    static class StubGeminiClient implements GeminiClient {
        volatile Object toReturn;

        void reset() {
            toReturn = null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T generate(GeminiGenerateRequest request, Class<T> resultType) {
            return (T) toReturn;
        }
    }
}
