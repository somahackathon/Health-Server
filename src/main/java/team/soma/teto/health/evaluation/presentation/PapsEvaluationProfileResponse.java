package team.soma.teto.health.evaluation.presentation;

import java.math.BigDecimal;

public record PapsEvaluationProfileResponse(
        Integer age,
        String schoolLevel,
        Integer schoolGrade,
        String gender,
        BigDecimal heightCm,
        BigDecimal weightKg,
        BigDecimal bmi
) {
}
