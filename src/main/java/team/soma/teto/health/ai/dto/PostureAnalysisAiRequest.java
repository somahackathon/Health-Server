package team.soma.teto.health.ai.dto;

public record PostureAnalysisAiRequest(
        String correlationId,
        String modelVersion,
        String exerciseType,
        VideoMeta video
) {

    public record VideoMeta(String contentType, long sizeBytes) {
    }
}
