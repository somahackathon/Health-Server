package team.soma.teto.health.ai.client;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiRequest;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiResponse;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiResponse.Recommendation;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "mock")
public class MockFitnessAiClient implements FitnessAiClient {

    private static final String MOCK_MODEL_VERSION = "mock-fitness-v1";

    @Override
    public FitnessAnalysisAiResponse analyze(FitnessAnalysisAiRequest request) {
        return new FitnessAnalysisAiResponse(
                request.correlationId(),
                MOCK_MODEL_VERSION,
                "측정 항목 기반 임시 분석 결과입니다.",
                List.of(
                        new Recommendation("규칙적인 유산소 운동", "주 3회, 회당 30분 이상의 유산소 운동을 권장합니다."),
                        new Recommendation("스트레칭 습관", "운동 전후 10분씩 스트레칭을 진행하세요.")
                )
        );
    }
}
