package team.soma.teto.health.ai.posture.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.soma.teto.health.ai.client.AiClientException;
import team.soma.teto.health.ai.client.GeminiClient;
import team.soma.teto.health.ai.client.GeminiProperties;
import team.soma.teto.health.ai.client.PoseClient;
import team.soma.teto.health.ai.client.PosturePromptFactory;
import team.soma.teto.health.ai.dto.GeminiGenerateRequest;
import team.soma.teto.health.ai.dto.PoseExtractionResult;
import team.soma.teto.health.ai.dto.PostureAiResult;
import team.soma.teto.health.ai.error.AiErrorCode;
import team.soma.teto.health.ai.job.application.AnalysisJobRecorder;
import team.soma.teto.health.ai.job.domain.AiAnalysisJob;
import team.soma.teto.health.ai.job.domain.AiFailureCode;
import team.soma.teto.health.ai.job.domain.AnalysisType;
import team.soma.teto.health.ai.posture.domain.ExerciseType;
import team.soma.teto.health.ai.posture.dto.PostureAnalysisResponse;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.global.error.CommonErrorCode;

@Service
public class PostureAnalysisService {

    private final VideoValidator videoValidator;
    private final AnalysisJobRecorder jobRecorder;
    private final PoseClient poseClient;
    private final GeminiClient geminiClient;
    private final PosturePromptFactory promptFactory;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    public PostureAnalysisService(
            VideoValidator videoValidator,
            AnalysisJobRecorder jobRecorder,
            PoseClient poseClient,
            GeminiClient geminiClient,
            PosturePromptFactory promptFactory,
            GeminiProperties geminiProperties,
            ObjectMapper objectMapper
    ) {
        this.videoValidator = videoValidator;
        this.jobRecorder = jobRecorder;
        this.poseClient = poseClient;
        this.geminiClient = geminiClient;
        this.promptFactory = promptFactory;
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
    }

    public PostureAnalysisResponse analyze(MultipartFile video, String installationId, String exerciseTypeValue) {
        if (installationId == null || installationId.isBlank()) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
        videoValidator.validate(video);
        ExerciseType exerciseType = ExerciseType.from(exerciseTypeValue);

        AiAnalysisJob job = jobRecorder.createProcessing(
                installationId, AnalysisType.POSTURE, metadataJson(exerciseType, video));

        try {
            byte[] videoBytes = readBytes(video);
            PoseExtractionResult poseResult = poseClient.extract(videoBytes, exerciseType.name());
            GeminiGenerateRequest geminiRequest = promptFactory.create(exerciseType, poseResult);
            PostureAiResult result = geminiClient.generate(geminiRequest, PostureAiResult.class);
            jobRecorder.complete(job, writeJson(result), geminiProperties.model());
            return PostureAnalysisResponse.of(job.getPublicId(), exerciseType.name(), result, geminiProperties.model());
        } catch (AiClientException e) {
            jobRecorder.fail(job, e.failureCode(), e.getMessage());
            throw new BusinessException(AiErrorCode.from(e.failureCode()));
        } catch (VideoReadException e) {
            jobRecorder.fail(job, AiFailureCode.VIDEO_PROCESSING_FAILED, "video read failed");
            throw new BusinessException(AiErrorCode.AI_VIDEO_PROCESSING_FAILED);
        }
    }

    private byte[] readBytes(MultipartFile video) {
        try {
            return video.getBytes();
        } catch (IOException e) {
            throw new VideoReadException();
        }
    }

    private String metadataJson(ExerciseType exerciseType, MultipartFile video) {
        try {
            return objectMapper.writeValueAsString(new PostureRequestMetadata(
                    exerciseType.name(), video.getSize(), video.getContentType()));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String writeJson(PostureAiResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private record PostureRequestMetadata(String exerciseType, long sizeBytes, String contentType) {
    }

    private static class VideoReadException extends RuntimeException {
    }
}
