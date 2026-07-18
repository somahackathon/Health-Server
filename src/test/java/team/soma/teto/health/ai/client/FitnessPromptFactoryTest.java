package team.soma.teto.health.ai.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.ai.fitness.dto.FitnessAnalysisRequest;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;

class FitnessPromptFactoryTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-18T00:00:00Z"), ZoneOffset.UTC);
    private final FitnessPromptFactory factory = new FitnessPromptFactory(clock);

    @Test
    void buildsRequestWithSchemaAndKoreanContent() {
        FitnessAnalysisRequest request = new FitnessAnalysisRequest(
                "install-1",
                new FitnessAnalysisRequest.Profile(LocalDate.of(2011, 3, 5), Gender.MALE, 158.5, 47.2, 3),
                List.of(new FitnessAnalysisRequest.Record(
                        FitnessTestItemCode.PUSH_UP, 21.0, MeasurementUnit.COUNT, LocalDate.of(2026, 7, 10)))
        );

        GeminiGenerateRequest geminiRequest = factory.create(request);

        String userText = geminiRequest.contents().get(0).parts().get(0).text();
        assertThat(userText).contains("PUSH_UP").contains("만 15세");
        assertThat(geminiRequest.systemInstruction().parts().get(0).text())
                .contains("의학적 진단이 아닌 참고 정보");

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) geminiRequest.generationConfig()
                .responseSchema().get("properties");
        assertThat(properties).containsKeys("summary", "overallLevel", "weakAreas", "solutions");
        assertThat((List<String>) geminiRequest.generationConfig().responseSchema().get("required"))
                .contains("summary", "overallLevel", "weakAreas", "solutions");
    }
}
