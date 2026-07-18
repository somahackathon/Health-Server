package team.soma.teto.health.evaluation.domain;

import java.math.BigDecimal;
import java.util.List;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.standard.domain.PapsStandard;

public class PapsGradeEvaluator {

    public int evaluate(BigDecimal value, List<PapsStandard> candidates) {
        List<PapsStandard> matchedStandards = candidates.stream()
                .filter(standard -> matches(value, standard))
                .toList();
        if (matchedStandards.isEmpty()) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_STANDARD_NOT_FOUND);
        }
        if (matchedStandards.size() > 1) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_STANDARD_OVERLAPPED);
        }
        return matchedStandards.get(0).getGrade();
    }

    private boolean matches(BigDecimal value, PapsStandard standard) {
        return matchesMinimum(value, standard) && matchesMaximum(value, standard);
    }

    private boolean matchesMinimum(BigDecimal value, PapsStandard standard) {
        if (standard.getMinimumValue() == null) {
            return true;
        }
        int comparison = value.compareTo(standard.getMinimumValue());
        return Boolean.TRUE.equals(standard.getMinimumInclusive()) ? comparison >= 0 : comparison > 0;
    }

    private boolean matchesMaximum(BigDecimal value, PapsStandard standard) {
        if (standard.getMaximumValue() == null) {
            return true;
        }
        int comparison = value.compareTo(standard.getMaximumValue());
        return Boolean.TRUE.equals(standard.getMaximumInclusive()) ? comparison <= 0 : comparison < 0;
    }
}
