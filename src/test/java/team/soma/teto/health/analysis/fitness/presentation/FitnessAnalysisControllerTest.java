package team.soma.teto.health.analysis.fitness.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import team.soma.teto.health.ai.client.GeminiClient;
import team.soma.teto.health.ai.dto.FitnessAiResult;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FitnessAnalysisControllerTest.StubConfig.class)
class FitnessAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeminiClient geminiClient;

    @AfterEach
    void reset() {
        ((StubGeminiClient) geminiClient).reset();
    }

    @Test
    void returnsValidationErrorWhenProfileMissing() throws Exception {
        String body = """
                {"installationId":"install-1","records":[]}
                """;

        mockMvc.perform(post("/api/v1/analysis/fitness")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    void returnsValidationErrorWhenRecordsEmpty() throws Exception {
        String body = """
                {"installationId":"install-1","profile":{"birthDate":"2011-03-05","gender":"MALE","heightCm":158.5,"weightKg":47.2},"records":[]}
                """;

        mockMvc.perform(post("/api/v1/analysis/fitness")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    void returnsSuccessEnvelopeOnHappyPath() throws Exception {
        ((StubGeminiClient) geminiClient).toReturn = new FitnessAiResult(
                "요약", "GOOD", java.util.List.of(), java.util.List.of());

        String body = """
                {
                  "installationId":"install-1",
                  "profile":{"birthDate":"2011-03-05","gender":"MALE","heightCm":158.5,"weightKg":47.2,"weeklyExerciseFrequency":3},
                  "records":[{"itemCode":"PUSH_UP","value":21,"unit":"COUNT","measuredAt":"2026-07-10"}]
                }
                """;

        mockMvc.perform(post("/api/v1/analysis/fitness")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.analysisId").exists())
                .andExpect(jsonPath("$.data.summary").value("요약"))
                .andExpect(jsonPath("$.data.disclaimer").value("본 분석 결과는 참고용 정보이며 의학적 진단이 아닙니다."));
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        GeminiClient stubGeminiClient() {
            return new StubGeminiClient();
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
