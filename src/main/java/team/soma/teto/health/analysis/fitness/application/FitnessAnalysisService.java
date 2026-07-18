package team.soma.teto.health.analysis.fitness.application;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.ai.client.AiClientException;
import team.soma.teto.health.ai.client.FitnessAiClient;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiRequest;
import team.soma.teto.health.ai.dto.FitnessAnalysisAiResponse;
import team.soma.teto.health.analysis.fitness.presentation.FitnessAnalysisRequest;
import team.soma.teto.health.analysis.fitness.presentation.FitnessAnalysisResponse;
import team.soma.teto.health.analysis.job.application.AiAnalysisJobService;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import team.soma.teto.health.analysis.job.domain.AnalysisType;
import team.soma.teto.health.analysis.job.error.AnalysisJobErrorCode;
import team.soma.teto.health.global.config.CorrelationIdFilter;
import team.soma.teto.health.global.error.BusinessException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class FitnessAnalysisService {

    private static final Duration JOB_TTL = Duration.ofHours(24);

    private final AiAnalysisJobService aiAnalysisJobService;
    private final FitnessAiClient fitnessAiClient;
    private final ObjectMapper objectMapper;

    public FitnessAnalysisService(AiAnalysisJobService aiAnalysisJobService, FitnessAiClient fitnessAiClient, ObjectMapper objectMapper) {
        this.aiAnalysisJobService = aiAnalysisJobService;
        this.fitnessAiClient = fitnessAiClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public FitnessAnalysisResponse analyze(String installationHash, FitnessAnalysisRequest request) {
        FitnessAnalysisAiRequest aiRequest = toAiRequest(resolveCorrelationId(), request);
        AiAnalysisJob job = aiAnalysisJobService.createJob(installationHash, AnalysisType.FITNESS, writeJson(aiRequest), JOB_TTL);
        aiAnalysisJobService.start(job);

        FitnessAnalysisAiResponse aiResponse = requestAnalysis(job, aiRequest);

        aiAnalysisJobService.complete(job, writeJson(aiResponse), aiResponse.modelVersion());

        return new FitnessAnalysisResponse(
                job.getPublicId(),
                job.getStatus(),
                aiResponse.modelVersion(),
                aiResponse.summary(),
                aiResponse.recommendations().stream()
                        .map(recommendation -> new FitnessAnalysisResponse.Recommendation(recommendation.title(), recommendation.description()))
                        .toList()
        );
    }

    private FitnessAnalysisAiResponse requestAnalysis(AiAnalysisJob job, FitnessAnalysisAiRequest aiRequest) {
        try {
            return fitnessAiClient.analyze(aiRequest);
        } catch (AiClientException exception) {
            aiAnalysisJobService.fail(job, exception.failureCode(), exception.getMessage());
            throw new BusinessException(AnalysisJobErrorCode.AI_ANALYSIS_FAILED, exception.getMessage());
        } catch (RuntimeException exception) {
            aiAnalysisJobService.fail(job, AiFailureCode.UNKNOWN, "fitness ai client unexpected error");
            throw new BusinessException(AnalysisJobErrorCode.AI_ANALYSIS_FAILED);
        }
    }

    private FitnessAnalysisAiRequest toAiRequest(String correlationId, FitnessAnalysisRequest request) {
        FitnessAnalysisAiRequest.Profile profile = new FitnessAnalysisAiRequest.Profile(
                request.profile().birthDate(),
                request.profile().gender(),
                request.profile().schoolLevel(),
                request.profile().schoolGrade(),
                request.profile().heightCm(),
                request.profile().weightKg()
        );
        List<FitnessAnalysisAiRequest.RecordItem> records = request.records().stream()
                .map(item -> new FitnessAnalysisAiRequest.RecordItem(item.itemCode(), item.value(), item.unit(), item.measuredAt()))
                .toList();
        return new FitnessAnalysisAiRequest(correlationId, null, profile, records);
    }

    private String resolveCorrelationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalStateException("failed to serialize analysis payload", exception);
        }
    }
}
