package team.soma.teto.health.ai.client;

import team.soma.teto.health.ai.dto.PostureAnalysisAiRequest;
import team.soma.teto.health.ai.dto.PostureAnalysisAiResponse;

public interface PostureAiClient {

    PostureAnalysisAiResponse analyze(PostureAnalysisAiRequest request);
}
