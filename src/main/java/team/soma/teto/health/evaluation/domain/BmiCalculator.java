package team.soma.teto.health.evaluation.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import team.soma.teto.health.global.error.BusinessException;

public class BmiCalculator {

    private static final BigDecimal CM_PER_METER = new BigDecimal("100");
    private static final MathContext MATH_CONTEXT = new MathContext(12, RoundingMode.HALF_UP);

    public BigDecimal calculate(BigDecimal heightCm, BigDecimal weightKg) {
        if (heightCm == null || heightCm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_HEIGHT);
        }
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_WEIGHT);
        }
        BigDecimal heightMeter = heightCm.divide(CM_PER_METER, MATH_CONTEXT);
        BigDecimal heightSquare = heightMeter.multiply(heightMeter, MATH_CONTEXT);
        return weightKg.divide(heightSquare, 1, RoundingMode.HALF_UP);
    }
}
