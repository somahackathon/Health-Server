package team.soma.teto.health.evaluation.domain;

import java.time.LocalDate;
import java.time.Period;
import team.soma.teto.health.global.error.BusinessException;

public class AgeCalculator {

    public int calculate(LocalDate birthDate, LocalDate assessmentDate) {
        if (birthDate == null || assessmentDate == null || birthDate.isAfter(assessmentDate)) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_DATE_RANGE);
        }
        return Period.between(birthDate, assessmentDate).getYears();
    }
}
