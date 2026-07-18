package team.soma.teto.health.evaluation.presentation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;

public record PapsMeasurementRequest(
        @NotNull FitnessTestItemCode testItemCode,
        @NotNull @PositiveOrZero BigDecimal value
) {
}
