package team.soma.teto.health.ai.client;

import team.soma.teto.health.ai.dto.GeminiGenerateRequest;

public interface GeminiClient {

    <T> T generate(GeminiGenerateRequest request, Class<T> resultType);
}
