package team.soma.teto.health.analysis.fitness.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import team.soma.teto.health.ai.client.AiClientException;
import team.soma.teto.health.ai.client.GeminiClient;
import team.soma.teto.health.ai.dto.FitnessAiResult;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.ai.error.AiErrorCode;
import team.soma.teto.health.analysis.fitness.dto.FitnessAnalysisRequest;
import team.soma.teto.health.analysis.fitness.dto.FitnessAnalysisResponse;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;
import team.soma.teto.health.analysis.job.repository.AiAnalysisJobRepository;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;

@SpringBootTest
@ActiveProfiles("test")
@Import(FitnessAnalysisServiceTest.StubConfig.class)
class FitnessAnalysisServiceTest {

    @Autowired
    private FitnessAnalysisService service;

    @Autowired
    private AiAnalysisJobRepository jobRepository;

    @Autowired
    private GeminiClient geminiClient;

    @AfterEach
    void resetStub() {
        ((StubGeminiClient) geminiClient).reset();
    }

    private FitnessAnalysisRequest sampleRequest() {
        return new FitnessAnalysisRequest(
                "install-fitness-test",
                new FitnessAnalysisRequest.Profile(LocalDate.of(2011, 3, 5), Gender.MALE, 158.5, 47.2, 3),
                List.of(new FitnessAnalysisRequest.Record(
                        FitnessTestItemCode.PUSH_UP, 21.0, MeasurementUnit.COUNT, LocalDate.of(2026, 7, 10)))
        );
    }

    @Test
    void completesJobAndReturnsResponseOnSuccess() {
        FitnessAiResult result = new FitnessAiResult(
                "요약", "AVERAGE",
                List.of(new FitnessAiResult.WeakArea(null, "FLEXIBILITY", "부족")),
                List.of(new FitnessAiResult.Solution("스트레칭", "설명", "주 3회")));
        ((StubGeminiClient) geminiClient).toReturn = result;

        FitnessAnalysisResponse response = service.analyze(sampleRequest());

        assertThat(response.summary()).isEqualTo("요약");
        assertThat(response.disclaimer()).contains("의학적 진단이 아닙니다");

        var job = jobRepository.findByPublicId(response.analysisId()).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(job.getModelVersion()).isNotBlank();
        assertThat(job.getRequestPayload())
                .doesNotContain("158.5")
                .doesNotContain("47.2")
                .doesNotContain("2011-03-05");
    }

    @Test
    void failsJobAndThrowsBusinessExceptionOnGeminiTimeout() {
        ((StubGeminiClient) geminiClient).toThrow = new AiClientException(AiFailureCode.AI_TIMEOUT, "timeout");

        assertThatThrownBy(() -> service.analyze(sampleRequest()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode()).isEqualTo(AiErrorCode.AI_TIMEOUT));

        Optional<team.soma.teto.health.analysis.job.domain.AiAnalysisJob> job = jobRepository
                .findAllByInstallationHashOrderByCreatedAtDesc(sha256("install-fitness-test"), org.springframework.data.domain.PageRequest.of(0, 1))
                .stream().findFirst();
        assertThat(job).isPresent();
        assertThat(job.get().getStatus()).isEqualTo(AnalysisStatus.FAILED);
        assertThat(job.get().getFailureCode()).isEqualTo(AiFailureCode.AI_TIMEOUT);
    }

    private String sha256(String value) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        volatile RuntimeException toThrow;

        void reset() {
            toReturn = null;
            toThrow = null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T generate(GeminiGenerateRequest request, Class<T> resultType) {
            if (toThrow != null) {
                throw toThrow;
            }
            return (T) toReturn;
        }
    }
}
