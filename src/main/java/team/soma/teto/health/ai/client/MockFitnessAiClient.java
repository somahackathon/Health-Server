package team.soma.teto.health.ai.client;

import java.util.List;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiRequest;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiResponse;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiResponse.Recommendation;

/**
 * Stands in for the real fitness AI server. No network call is made; a fixed
 * response is returned so the rest of the analysis pipeline can be built and
 * tested before the AI team's server is available.
 */
@Component
public class MockFitnessAiClient implements FitnessAiClient {

    private static final String MOCK_MODEL_VERSION = "mock-fitness-v1";

    @Override
    public FitnessAnalysisAiResponse analyze(FitnessAnalysisAiRequest request) {
        return new FitnessAnalysisAiResponse(
                request.correlationId(),
                MOCK_MODEL_VERSION,
                "측정 항목을 기반으로 한 임시 분석 결과입니다.",
                List.of(
                        new Recommendation("규칙적인 유산소 운동", "주 3회, 회당 30분 이상의 유산소 운동을 권장합니다."),
                        new Recommendation("스트레칭 습관화", "운동 전후 10분씩 스트레칭을 진행하세요.")
                )
        );
    }
}
