package team.soma.teto.health.evaluation.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import team.soma.teto.health.reference.standard.domain.Gender;

public record PapsEvaluationRequest(
        @NotNull LocalDate birthDate,
        @NotNull Gender gender,
        @NotNull LocalDate assessmentDate,
        @NotNull @Positive BigDecimal heightCm,
        @NotNull @Positive BigDecimal weightKg,
        @NotNull List<@Valid PapsMeasurementRequest> measurements
) {
}
