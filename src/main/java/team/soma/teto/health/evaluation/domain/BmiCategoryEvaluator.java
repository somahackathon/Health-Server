package team.soma.teto.health.evaluation.domain;

import java.math.BigDecimal;
import java.util.List;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.standard.domain.BmiCategory;
import team.soma.teto.health.reference.standard.domain.PapsBmiStandard;

public class BmiCategoryEvaluator {

    public BmiCategory evaluate(BigDecimal value, List<PapsBmiStandard> candidates) {
        List<PapsBmiStandard> matchedStandards = candidates.stream()
                .filter(standard -> matches(value, standard))
                .toList();
        if (matchedStandards.isEmpty()) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_BMI_STANDARD_NOT_FOUND);
        }
        if (matchedStandards.size() > 1) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_BMI_STANDARD_OVERLAPPED);
        }
        return matchedStandards.get(0).getCategory();
    }

    private boolean matches(BigDecimal value, PapsBmiStandard standard) {
        return matchesMinimum(value, standard) && matchesMaximum(value, standard);
    }

    private boolean matchesMinimum(BigDecimal value, PapsBmiStandard standard) {
        if (standard.getMinimumValue() == null) {
            return true;
        }
        int comparison = value.compareTo(standard.getMinimumValue());
        return Boolean.TRUE.equals(standard.getMinimumInclusive()) ? comparison >= 0 : comparison > 0;
    }

    private boolean matchesMaximum(BigDecimal value, PapsBmiStandard standard) {
        if (standard.getMaximumValue() == null) {
            return true;
        }
        int comparison = value.compareTo(standard.getMaximumValue());
        return Boolean.TRUE.equals(standard.getMaximumInclusive()) ? comparison <= 0 : comparison < 0;
    }
}
