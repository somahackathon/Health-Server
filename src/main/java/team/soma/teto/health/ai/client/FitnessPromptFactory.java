package team.soma.teto.health.ai.client;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.ai.fitness.dto.FitnessAnalysisRequest;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;

@Component
public class FitnessPromptFactory {

    private static final String SYSTEM_INSTRUCTION = """
            당신은 대한민국 학생건강체력평가(PAPS) 데이터를 분석하는 청소년 체력 코치입니다.
            학생의 신체 정보와 PAPS 측정 기록을 같은 나이·성별 또래의 일반적인 수준과 비교하여
            부족한 체력 요인을 진단하고, 학생이 실천할 수 있는 맞춤 운동 솔루션을 제시하세요.
            규칙:
            - 반드시 지정된 JSON 스키마 형식으로만 응답합니다.
            - 존댓말을 사용하고, 청소년이 이해하기 쉬운 표현을 씁니다.
            - 부족 영역은 5개 체력 요인(CARDIO_ENDURANCE=심폐지구력, FLEXIBILITY=유연성,
              MUSCULAR_STRENGTH_ENDURANCE=근력·근지구력, POWER=순발력, BODY_COMPOSITION=신체조성)
              관점에서 판단합니다.
            - 솔루션은 2~4개 제시하고, 각 솔루션에 구체적인 실천 빈도를 포함합니다.
            - 이 결과는 의학적 진단이 아닌 참고 정보임을 전제로 작성합니다.
            - overallLevel은 EXCELLENT, GOOD, AVERAGE, NEEDS_IMPROVEMENT 중 하나입니다.
            """;

    private final Clock clock;

    public FitnessPromptFactory(Clock clock) {
        this.clock = clock;
    }

    public GeminiGenerateRequest create(FitnessAnalysisRequest request) {
        return GeminiGenerateRequest.of(SYSTEM_INSTRUCTION, buildUserText(request), responseSchema());
    }

    private String buildUserText(FitnessAnalysisRequest request) {
        FitnessAnalysisRequest.Profile profile = request.profile();
        int age = Period.between(profile.birthDate(), LocalDate.now(clock)).getYears();
        double bmi = profile.weightKg() / Math.pow(profile.heightCm() / 100.0, 2);

        String records = request.records().stream()
                .map(record -> "- %s: %s %s (측정일 %s)".formatted(
                        record.itemCode().name(),
                        trimNumber(record.value()),
                        record.unit() == null ? "" : record.unit().name(),
                        record.measuredAt()))
                .collect(Collectors.joining("\n"));

        return """
                [학생 정보]
                나이: 만 %d세
                성별: %s
                키: %s cm
                몸무게: %s kg
                BMI: %.1f
                주간 운동 빈도: %s

                [PAPS 측정 기록]
                %s

                위 정보를 바탕으로 또래 평균과 비교하여 부족한 체력 요인을 진단하고
                맞춤 운동 솔루션을 지정된 JSON 스키마로 작성해 주세요.
                """.formatted(
                age,
                profile.gender().name(),
                trimNumber(profile.heightCm()),
                trimNumber(profile.weightKg()),
                bmi,
                profile.weeklyExerciseFrequency() == null ? "미제공" : profile.weeklyExerciseFrequency() + "회",
                records);
    }

    private Map<String, Object> responseSchema() {
        Map<String, Object> weakAreaProps = new LinkedHashMap<>();
        weakAreaProps.put("itemCode", GeminiSchemas.nullableStringEnum(enumNames(FitnessTestItemCode.values())));
        weakAreaProps.put("componentCode", GeminiSchemas.stringEnum(enumNames(FitnessComponentCode.values())));
        weakAreaProps.put("reason", GeminiSchemas.string());
        Map<String, Object> weakArea = GeminiSchemas.object(weakAreaProps, List.of("componentCode", "reason"));

        Map<String, Object> solutionProps = new LinkedHashMap<>();
        solutionProps.put("title", GeminiSchemas.string());
        solutionProps.put("description", GeminiSchemas.string());
        solutionProps.put("frequency", GeminiSchemas.string());
        Map<String, Object> solution = GeminiSchemas.object(solutionProps, List.of("title", "description"));

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("summary", GeminiSchemas.string());
        props.put("overallLevel", GeminiSchemas.stringEnum(List.of("EXCELLENT", "GOOD", "AVERAGE", "NEEDS_IMPROVEMENT")));
        props.put("weakAreas", GeminiSchemas.array(weakArea));
        props.put("solutions", GeminiSchemas.array(solution));
        return GeminiSchemas.object(props, List.of("summary", "overallLevel", "weakAreas", "solutions"));
    }

    private List<String> enumNames(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }

    private String trimNumber(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
