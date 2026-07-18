package team.soma.teto.health.ai.client;

import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.PostureAnalysisAiRequest;
import team.soma.teto.health.ai.dto.PostureAnalysisAiResponse;
import team.soma.teto.health.ai.dto.PostureAnalysisAiResponse.Feedback;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;

@Component
@ConditionalOnExpression("'${app.ai.posture-mode:real}'.equalsIgnoreCase('mock')")
public class MockPostureAiClient implements PostureAiClient {

    private static final String MOCK_MODEL_VERSION = "mock-posture-v1";

    @Override
    public PostureAnalysisAiResponse analyze(PostureAnalysisAiRequest request, Path videoPath) {
        return new PostureAnalysisAiResponse(
                request.correlationId(),
                MOCK_MODEL_VERSION,
                AnalysisStatus.COMPLETED,
                List.of(new Feedback("KNEE_ALIGNMENT", "무릎이 발끝보다 과도하게 앞으로 나가지 않도록 주의하세요.", "MEDIUM"))
        );
    }
}
