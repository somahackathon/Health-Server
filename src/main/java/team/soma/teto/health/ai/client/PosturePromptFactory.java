package team.soma.teto.health.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.ai.dto.PoseExtractionResult;
import team.soma.teto.health.ai.posture.domain.ExerciseType;

@Component
public class PosturePromptFactory {

    private static final List<String> BODY_PARTS = List.of(
            "HEAD", "NECK", "SHOULDER", "ELBOW", "WRIST", "BACK",
            "WAIST", "CORE", "HIP", "KNEE", "ANKLE", "HAMSTRING");

    private static final String SYSTEM_INSTRUCTION = """
            당신은 대한민국 학생 체육 실기 동작을 분석하는 자세 분석 전문가입니다.
            컴퓨터 비전으로 추출한 관절 각도·정렬 지표(metrics)를 근거로
            자세 점수와 문제 구간, 개선 방법을 진단하세요.
            규칙:
            - 반드시 지정된 JSON 스키마 형식으로만 응답합니다.
            - postureScore는 0~100 정수입니다.
            - 문제 구간의 startSec, endSec는 제공된 metrics의 rep 또는 시계열 초(sec) 값을 기준으로 지정합니다.
            - bodyPart는 지정된 enum 값만 사용합니다.
            - severity는 LOW, MEDIUM, HIGH 중 하나입니다.
            - 존댓말을 사용하고 청소년이 이해하기 쉽게 설명합니다.
            - 이 결과는 의학적 진단이 아닌 참고 정보임을 전제로 작성합니다.
            """;

    private static final String PUSH_UP_CRITERIA = """
            [팔굽혀펴기(PUSH_UP) 판정 기준]
            - 하강 시 팔꿈치 각도가 약 90도까지 내려가야 합니다(minElbowAngleDeg).
            - 어깨-엉덩이-발목이 일직선을 유지해야 합니다. 허리 처짐(maxHipSagDeg)이 크면 감점합니다.
            - 목과 머리가 몸통과 정렬되어야 합니다(neckAlignedRatio).
            - 반복 횟수(repCount)와 각 반복의 가동범위를 평가합니다.
            """;

    private static final String CURL_UP_CRITERIA = """
            [윗몸일으키기(CURL_UP) 판정 기준]
            - 무릎을 약 90도로 굽힌 상태를 반복 내내 유지해야 합니다(minKneeAngleDeg, kneeAngleStableRatio).
            - 상체를 충분히 들어올려야 합니다(maxTrunkLiftDeg가 클수록 완전한 동작).
            - 손으로 목을 무리하게 잡아당기지 않아야 합니다(maxNeckPullDeg가 크면 목 당김 의심).
            - 반복 횟수(repCount)와 각 반복이 충분한 가동범위로 이루어졌는지 평가합니다.
            """;

    private final ObjectMapper objectMapper;

    public PosturePromptFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GeminiGenerateRequest create(ExerciseType exerciseType, PoseExtractionResult poseResult) {
        return GeminiGenerateRequest.of(SYSTEM_INSTRUCTION, buildUserText(exerciseType, poseResult), responseSchema());
    }

    private String buildUserText(ExerciseType exerciseType, PoseExtractionResult poseResult) {
        String criteria = exerciseType == ExerciseType.PUSH_UP ? PUSH_UP_CRITERIA : CURL_UP_CRITERIA;
        return """
                %s
                [분석 대상]
                운동 종목: %s
                영상 길이(초): %s

                [추출된 자세 지표(metrics)]
                %s

                위 지표를 근거로 자세 점수, 문제 구간, 개선 방법을 지정된 JSON 스키마로 작성해 주세요.
                """.formatted(
                criteria,
                exerciseType.name(),
                poseResult.durationSec() == null ? "미제공" : poseResult.durationSec(),
                writeMetrics(poseResult.metrics()));
    }

    private String writeMetrics(Map<String, Object> metrics) {
        if (metrics == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Map<String, Object> responseSchema() {
        Map<String, Object> segmentProps = new LinkedHashMap<>();
        segmentProps.put("startSec", GeminiSchemas.number());
        segmentProps.put("endSec", GeminiSchemas.number());
        segmentProps.put("bodyPart", GeminiSchemas.stringEnum(BODY_PARTS));
        segmentProps.put("issue", GeminiSchemas.string());
        segmentProps.put("severity", GeminiSchemas.stringEnum(List.of("LOW", "MEDIUM", "HIGH")));
        Map<String, Object> segment = GeminiSchemas.object(segmentProps,
                List.of("startSec", "endSec", "bodyPart", "issue", "severity"));

        Map<String, Object> improvementProps = new LinkedHashMap<>();
        improvementProps.put("bodyPart", GeminiSchemas.stringEnum(BODY_PARTS));
        improvementProps.put("suggestion", GeminiSchemas.string());
        Map<String, Object> improvement = GeminiSchemas.object(improvementProps, List.of("bodyPart", "suggestion"));

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("postureScore", GeminiSchemas.integer());
        props.put("problemSegments", GeminiSchemas.array(segment));
        props.put("improvements", GeminiSchemas.array(improvement));
        return GeminiSchemas.object(props, List.of("postureScore", "problemSegments", "improvements"));
    }
}
