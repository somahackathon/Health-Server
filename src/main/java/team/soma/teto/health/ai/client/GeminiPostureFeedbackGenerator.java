package team.soma.teto.health.ai.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.PostureAnalysisAiResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Turns the raw joint-angle metrics computed by the ai-python pose extractor (pure computer
 * vision, no LLM) into Korean, human-readable posture feedback via Gemini. This is where an
 * actual AI judgment is made about the pose data — the metric extraction itself stays
 * mechanical and lives entirely in ai-python.
 */
@Component
public class GeminiPostureFeedbackGenerator {

    private static final String SYSTEM_INSTRUCTION = """
            당신은 대한민국 학생 체육 실기 동작을 분석하는 자세 분석 전문가입니다.
            컴퓨터 비전으로 추출한 관절 각도·정렬 지표(metrics)를 근거로 자세 문제와 개선 방법을 진단하세요.
            규칙:
            - 반드시 지정된 JSON 스키마 형식으로만 응답합니다.
            - code는 영문 대문자와 언더스코어로만 구성된 간결한 식별자로 작성합니다 (예: ELBOW_ANGLE_SHALLOW).
            - message는 존댓말로, 청소년이 이해하기 쉽게 문제와 개선 방법을 함께 설명합니다.
            - message 어딘가에 이 결과가 참고용 정보이며 의학적 진단이 아니라는 점을 짧게 덧붙입니다.
            - severity는 LOW, MEDIUM, HIGH 중 하나입니다.
            - metrics에 특별한 문제가 없다면 잘된 점을 칭찬하는 항목 1개만 LOW로 반환해도 됩니다.
            """;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public GeminiPostureFeedbackGenerator(GeminiClient geminiClient, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public List<PostureAnalysisAiResponse.Feedback> generate(String exerciseType, Map<String, Object> metrics) {
        GeminiPostureResult result = geminiClient.generate(
                SYSTEM_INSTRUCTION, buildUserText(exerciseType, metrics), responseSchema(), GeminiPostureResult.class);
        return result.feedback();
    }

    private String buildUserText(String exerciseType, Map<String, Object> metrics) {
        return """
                [분석 대상]
                운동 종목: %s

                [추출된 자세 지표(metrics)]
                %s

                위 지표를 근거로 자세 문제와 개선 방법을 지정된 JSON 스키마로 작성해 주세요.
                """.formatted(exerciseType, writeMetrics(metrics));
    }

    private String writeMetrics(Map<String, Object> metrics) {
        if (metrics == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (JacksonException exception) {
            return "{}";
        }
    }

    private Map<String, Object> responseSchema() {
        Map<String, Object> feedbackProps = new LinkedHashMap<>();
        feedbackProps.put("code", Map.of("type", "STRING"));
        feedbackProps.put("message", Map.of("type", "STRING"));
        feedbackProps.put("severity", Map.of("type", "STRING", "enum", List.of("LOW", "MEDIUM", "HIGH")));
        Map<String, Object> feedbackItem = Map.of(
                "type", "OBJECT",
                "properties", feedbackProps,
                "required", List.of("code", "message", "severity"));

        Map<String, Object> props = Map.of("feedback", Map.of("type", "ARRAY", "items", feedbackItem));

        return Map.of(
                "type", "OBJECT",
                "properties", props,
                "required", List.of("feedback"));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiPostureResult(List<PostureAnalysisAiResponse.Feedback> feedback) {
    }
}
