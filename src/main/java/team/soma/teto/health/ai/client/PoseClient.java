package team.soma.teto.health.ai.client;

import team.soma.teto.health.ai.dto.PoseExtractionResult;

public interface PoseClient {

    PoseExtractionResult extract(byte[] videoBytes, String exerciseType);
}
