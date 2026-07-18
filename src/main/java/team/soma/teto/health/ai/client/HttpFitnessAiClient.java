package team.soma.teto.health.ai.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiRequest;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiResponse;

/**
 * Calls Gemini directly to turn a student's body profile and PAPS records into a Korean
 * fitness summary and exercise recommendations. There is no separate external "fitness AI
 * server" behind {@code app.ai.base-url} for this analysis type — Gemini is the analysis.
 */
@Component
@ConditionalOnExpression("'${app.ai.fitness-mode:real}'.equalsIgnoreCase('real')")
public class HttpFitnessAiClient implements FitnessAiClient {

    private static final String SYSTEM_INSTRUCTION = """
            당신은 대한민국 학생건강체력평가(PAPS) 데이터를 분석하는 청소년 체력 코치입니다.
            학생의 신체 정보와 PAPS 측정 기록을 같은 나이·성별 또래의 일반적인 수준과 비교하여
            부족한 체력 요인을 진단하고, 학생이 실천할 수 있는 맞춤 운동 솔루션을 제시하세요.
            규칙:
            - 반드시 지정된 JSON 스키마 형식으로만 응답합니다.
            - 존댓말을 사용하고, 청소년이 이해하기 쉬운 표현을 씁니다.
            - summary 마지막에는 이 결과가 참고용 정보이며 의학적 진단이 아니라는 점을 짧게 덧붙입니다.
            - recommendations는 2~4개 제시하고, 각 항목에 구체적인 실천 방법을 포함합니다.
            """;

    private final GeminiClient geminiClient;
    private final GeminiProperties geminiProperties;
    private final Clock clock;

    public HttpFitnessAiClient(GeminiClient geminiClient, GeminiProperties geminiProperties, Clock clock) {
        this.geminiClient = geminiClient;
        this.geminiProperties = geminiProperties;
        this.clock = clock;
    }

    @Override
    public FitnessAnalysisAiResponse analyze(FitnessAnalysisAiRequest request) {
        GeminiFitnessResult result = geminiClient.generate(
                SYSTEM_INSTRUCTION, buildUserText(request), responseSchema(), GeminiFitnessResult.class);
        return new FitnessAnalysisAiResponse(
                request.correlationId(),
                geminiProperties.getModel(),
                result.summary(),
                result.recommendations()
        );
    }

    private String buildUserText(FitnessAnalysisAiRequest request) {
        FitnessAnalysisAiRequest.Profile profile = request.profile();
        int age = Period.between(profile.birthDate(), LocalDate.now(clock)).getYears();

        String records = request.records().stream()
                .map(record -> "- %s: %s %s (측정일 %s)".formatted(
                        record.itemCode().name(), record.value(), record.unit().name(), record.measuredAt()))
                .collect(Collectors.joining("\n"));

        return """
                [학생 정보]
                나이: 만 %d세
                성별: %s
                키: %s cm
                몸무게: %s kg

                [PAPS 측정 기록]
                %s

                위 정보를 바탕으로 또래 평균과 비교하여 부족한 체력 요인을 진단하고
                맞춤 운동 솔루션을 지정된 JSON 스키마로 작성해 주세요.
                """.formatted(age, profile.gender().name(), profile.heightCm(), profile.weightKg(), records);
    }

    private Map<String, Object> responseSchema() {
        Map<String, Object> recommendationProps = new LinkedHashMap<>();
        recommendationProps.put("title", Map.of("type", "STRING"));
        recommendationProps.put("description", Map.of("type", "STRING"));
        Map<String, Object> recommendation = Map.of(
                "type", "OBJECT",
                "properties", recommendationProps,
                "required", List.of("title", "description"));

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("summary", Map.of("type", "STRING"));
        props.put("recommendations", Map.of("type", "ARRAY", "items", recommendation));

        return Map.of(
                "type", "OBJECT",
                "properties", props,
                "required", List.of("summary", "recommendations"));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiFitnessResult(String summary, List<FitnessAnalysisAiResponse.Recommendation> recommendations) {
    }
}
