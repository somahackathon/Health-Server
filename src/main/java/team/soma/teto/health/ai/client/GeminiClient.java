package team.soma.teto.health.ai.client;

import java.util.Map;

public interface GeminiClient {

    <T> T generate(String systemInstruction, String userText, Map<String, Object> responseSchema, Class<T> resultType);
}
