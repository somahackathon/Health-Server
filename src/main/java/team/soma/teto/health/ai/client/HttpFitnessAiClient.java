package team.soma.teto.health.ai.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiRequest;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiResponse;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnExpression("'${app.ai.fitness-mode:real}'.equalsIgnoreCase('real')")
public class HttpFitnessAiClient extends HttpAiClientSupport implements FitnessAiClient {

    private final AiProperties aiProperties;

    public HttpFitnessAiClient(AiProperties aiProperties, ObjectMapper objectMapper) {
        super(aiProperties, objectMapper);
        this.aiProperties = aiProperties;
    }

    @Override
    public FitnessAnalysisAiResponse analyze(FitnessAnalysisAiRequest request) {
        return postJson(aiProperties.getFitnessPath(), request.correlationId(), request, FitnessAnalysisAiResponse.class);
    }
}
