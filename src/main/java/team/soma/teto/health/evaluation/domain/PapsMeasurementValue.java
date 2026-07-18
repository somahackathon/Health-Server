package team.soma.teto.health.evaluation.domain;

import java.math.BigDecimal;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;

public record PapsMeasurementValue(FitnessTestItem testItem, BigDecimal value) {
}
