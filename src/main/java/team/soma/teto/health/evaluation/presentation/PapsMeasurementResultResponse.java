package team.soma.teto.health.evaluation.presentation;

import java.math.BigDecimal;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;

public record PapsMeasurementResultResponse(
        String component,
        String testItemCode,
        String testItemName,
        BigDecimal value,
        String unit,
        Integer grade,
        String bmiCategory
) {

    public static PapsMeasurementResultResponse from(FitnessTestItem item, BigDecimal value, Integer grade) {
        return from(item, value, grade, null);
    }

    public static PapsMeasurementResultResponse from(FitnessTestItem item, BigDecimal value, Integer grade, String bmiCategory) {
        return new PapsMeasurementResultResponse(
                item.getComponent().getCode().name(),
                item.getCode().name(),
                item.getName(),
                value,
                item.getUnit().name(),
                grade,
                bmiCategory
        );
    }
}
