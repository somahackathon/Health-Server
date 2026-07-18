package team.soma.teto.health.ai.client;

import team.soma.teto.health.ai.dto.FitnessAnalysisAiRequest;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiResponse;

public interface FitnessAiClient {

    FitnessAnalysisAiResponse analyze(FitnessAnalysisAiRequest request);
}
