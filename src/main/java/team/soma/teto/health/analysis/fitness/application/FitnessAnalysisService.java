package team.soma.teto.health.analysis.fitness.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import team.soma.teto.health.ai.client.AiClientException;
import team.soma.teto.health.ai.client.FitnessPromptFactory;
import team.soma.teto.health.ai.client.GeminiClient;
import team.soma.teto.health.ai.client.GeminiProperties;
import team.soma.teto.health.ai.dto.FitnessAiResult;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.ai.error.AiErrorCode;
import team.soma.teto.health.analysis.fitness.dto.FitnessAnalysisRequest;
import team.soma.teto.health.analysis.fitness.dto.FitnessAnalysisResponse;
import team.soma.teto.health.analysis.job.application.AnalysisJobRecorder;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.analysis.job.domain.AnalysisType;
import team.soma.teto.health.global.error.BusinessException;

@Service
public class FitnessAnalysisService {

    private final AnalysisJobRecorder jobRecorder;
    private final GeminiClient geminiClient;
    private final FitnessPromptFactory promptFactory;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;

    public FitnessAnalysisService(
            AnalysisJobRecorder jobRecorder,
            GeminiClient geminiClient,
            FitnessPromptFactory promptFactory,
            GeminiProperties properties,
            ObjectMapper objectMapper
    ) {
        this.jobRecorder = jobRecorder;
        this.geminiClient = geminiClient;
        this.promptFactory = promptFactory;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public FitnessAnalysisResponse analyze(FitnessAnalysisRequest request) {
        AiAnalysisJob job = jobRecorder.createProcessing(
                request.installationId(), AnalysisType.FITNESS, metadataJson(request));

        try {
            GeminiGenerateRequest geminiRequest = promptFactory.create(request);
            FitnessAiResult result = geminiClient.generate(geminiRequest, FitnessAiResult.class);
            jobRecorder.complete(job, writeJson(result), properties.model());
            return FitnessAnalysisResponse.of(job.getPublicId(), result, properties.model());
        } catch (AiClientException e) {
            jobRecorder.fail(job, e.failureCode(), e.getMessage());
            throw new BusinessException(AiErrorCode.from(e.failureCode()));
        }
    }

    private String metadataJson(FitnessAnalysisRequest request) {
        List<String> itemCodes = request.records().stream().map(r -> r.itemCode().name()).toList();
        try {
            return objectMapper.writeValueAsString(new FitnessRequestMetadata(itemCodes.size(), itemCodes));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String writeJson(FitnessAiResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private record FitnessRequestMetadata(int recordCount, List<String> itemCodes) {
    }
}
