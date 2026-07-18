package team.soma.teto.health.analysis.posture.application;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.soma.teto.health.ai.client.AiClientException;
import team.soma.teto.health.ai.client.PostureAiClient;
import team.soma.teto.health.ai.dto.PostureAnalysisAiRequest;
import team.soma.teto.health.ai.dto.PostureAnalysisAiResponse;
import team.soma.teto.health.analysis.job.application.AiAnalysisJobService;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;
import team.soma.teto.health.analysis.job.domain.AnalysisType;
import team.soma.teto.health.analysis.job.error.AnalysisJobErrorCode;
import team.soma.teto.health.analysis.posture.presentation.PostureAnalysisResponse;
import team.soma.teto.health.file.VideoStorageService;
import team.soma.teto.health.file.VideoValidator;
import team.soma.teto.health.global.config.CorrelationIdFilter;
import team.soma.teto.health.global.error.BusinessException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class PostureAnalysisService {

    private static final Duration JOB_TTL = Duration.ofHours(1);

    private final AiAnalysisJobService aiAnalysisJobService;
    private final PostureAiClient postureAiClient;
    private final VideoValidator videoValidator;
    private final VideoStorageService videoStorageService;
    private final ObjectMapper objectMapper;

    public PostureAnalysisService(
            AiAnalysisJobService aiAnalysisJobService,
            PostureAiClient postureAiClient,
            VideoValidator videoValidator,
            VideoStorageService videoStorageService,
            ObjectMapper objectMapper
    ) {
        this.aiAnalysisJobService = aiAnalysisJobService;
        this.postureAiClient = postureAiClient;
        this.videoValidator = videoValidator;
        this.videoStorageService = videoStorageService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PostureAnalysisResponse analyze(String installationHash, String exerciseType, MultipartFile video) {
        videoValidator.validate(video);

        PostureAnalysisAiRequest aiRequest = toAiRequest(resolveCorrelationId(), exerciseType, video);
        AiAnalysisJob job = aiAnalysisJobService.createJob(installationHash, AnalysisType.POSTURE, writeJson(aiRequest), JOB_TTL);
        aiAnalysisJobService.start(job);

        // The server must not keep raw video beyond the analysis lifecycle, so the
        // temp file is always removed once the AI call finishes, success or failure.
        Path tempVideoPath = videoStorageService.storeTemporarily(video);
        try {
            PostureAnalysisAiResponse aiResponse = requestAnalysis(job, aiRequest);
            aiAnalysisJobService.complete(job, writeJson(aiResponse), aiResponse.modelVersion());
            return toResponse(job, aiResponse);
        } finally {
            videoStorageService.delete(tempVideoPath);
        }
    }

    private PostureAnalysisAiResponse requestAnalysis(AiAnalysisJob job, PostureAnalysisAiRequest aiRequest) {
        try {
            return postureAiClient.analyze(aiRequest);
        } catch (AiClientException exception) {
            aiAnalysisJobService.fail(job, exception.failureCode(), exception.getMessage());
            throw new BusinessException(AnalysisJobErrorCode.AI_ANALYSIS_FAILED, exception.getMessage());
        } catch (RuntimeException exception) {
            aiAnalysisJobService.fail(job, AiFailureCode.VIDEO_PROCESSING_FAILED, "posture ai client unexpected error");
            throw new BusinessException(AnalysisJobErrorCode.AI_ANALYSIS_FAILED);
        }
    }

    private PostureAnalysisResponse toResponse(AiAnalysisJob job, PostureAnalysisAiResponse aiResponse) {
        List<PostureAnalysisResponse.Feedback> feedback = aiResponse.feedback().stream()
                .map(item -> new PostureAnalysisResponse.Feedback(item.code(), item.message(), item.severity()))
                .toList();
        return new PostureAnalysisResponse(job.getPublicId(), job.getStatus(), aiResponse.modelVersion(), feedback);
    }

    private PostureAnalysisAiRequest toAiRequest(String correlationId, String exerciseType, MultipartFile video) {
        PostureAnalysisAiRequest.VideoMeta videoMeta = new PostureAnalysisAiRequest.VideoMeta(video.getContentType(), video.getSize());
        return new PostureAnalysisAiRequest(correlationId, null, exerciseType, videoMeta);
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
