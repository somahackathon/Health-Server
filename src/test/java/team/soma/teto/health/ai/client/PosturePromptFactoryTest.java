package team.soma.teto.health.ai.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.ai.dto.PoseExtractionResult;
import team.soma.teto.health.ai.posture.domain.ExerciseType;

class PosturePromptFactoryTest {

    private final PosturePromptFactory factory = new PosturePromptFactory(new ObjectMapper());

    @Test
    void buildsPushUpPromptWithCriteriaAndMetrics() {
        PoseExtractionResult poseResult = new PoseExtractionResult(
                "PUSH_UP", 12.5, 10, true, Map.of("repCount", 5));

        GeminiGenerateRequest request = factory.create(ExerciseType.PUSH_UP, poseResult);

        String userText = request.contents().get(0).parts().get(0).text();
        assertThat(userText).contains("팔꿈치").contains("허리 처짐").contains("repCount");
        assertThat(request.systemInstruction().parts().get(0).text())
                .contains("의학적 진단이 아닌 참고 정보");
    }

    @Test
    void buildsSitAndReachPromptWithCriteria() {
        PoseExtractionResult poseResult = new PoseExtractionResult(
                "SIT_AND_REACH", 20.0, 10, true, Map.of("minKneeAngleDeg", 170.0));

        GeminiGenerateRequest request = factory.create(ExerciseType.SIT_AND_REACH, poseResult);

        String userText = request.contents().get(0).parts().get(0).text();
        assertThat(userText).contains("무릎").contains("반동");
    }

    @Test
    void responseSchemaHasRequiredFields() {
        PoseExtractionResult poseResult = new PoseExtractionResult("PUSH_UP", 10.0, 10, true, Map.of());
        GeminiGenerateRequest request = factory.create(ExerciseType.PUSH_UP, poseResult);

        Map<String, Object> schema = request.generationConfig().responseSchema();
        assertThat((List<String>) schema.get("required"))
                .contains("postureScore", "problemSegments", "improvements");
    }
}
