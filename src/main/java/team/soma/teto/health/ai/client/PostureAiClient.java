package team.soma.teto.health.ai.client;

import java.nio.file.Path;
import team.soma.teto.health.ai.dto.PostureAnalysisAiRequest;
import team.soma.teto.health.ai.dto.PostureAnalysisAiResponse;

public interface PostureAiClient {

    PostureAnalysisAiResponse analyze(PostureAnalysisAiRequest request, Path videoPath);
}
