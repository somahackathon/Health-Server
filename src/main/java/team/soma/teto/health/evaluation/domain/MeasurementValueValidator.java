package team.soma.teto.health.evaluation.domain;

import java.math.BigDecimal;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;
import team.soma.teto.health.reference.testitem.domain.MeasurementValueType;

public class MeasurementValueValidator {

    public void validate(FitnessTestItem testItem, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_MEASUREMENT_VALUE);
        }
        if (testItem.getMinimumInput() != null && value.compareTo(testItem.getMinimumInput()) < 0) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_MEASUREMENT_VALUE);
        }
        if (testItem.getMaximumInput() != null && value.compareTo(testItem.getMaximumInput()) > 0) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_MEASUREMENT_VALUE);
        }
        if (testItem.getValueType() == MeasurementValueType.INTEGER && value.stripTrailingZeros().scale() > 0) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_MEASUREMENT_VALUE);
        }
        if (Math.max(value.stripTrailingZeros().scale(), 0) > testItem.getDecimalScale()) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_DECIMAL_SCALE);
        }
    }
}
