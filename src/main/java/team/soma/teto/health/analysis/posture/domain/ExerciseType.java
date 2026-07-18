package team.soma.teto.health.analysis.posture.domain;

import team.soma.teto.health.ai.error.AiErrorCode;
import team.soma.teto.health.global.error.BusinessException;

public enum ExerciseType {
    PUSH_UP,
    SIT_AND_REACH;

    public static ExerciseType from(String value) {
        if (value == null) {
            throw new BusinessException(AiErrorCode.AI_UNSUPPORTED_EXERCISE_TYPE);
        }
        try {
            return ExerciseType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AiErrorCode.AI_UNSUPPORTED_EXERCISE_TYPE);
        }
    }
}
