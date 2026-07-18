package team.soma.teto.health.evaluation.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import team.soma.teto.health.reference.standard.domain.Gender;

public record PapsEvaluationRequest(
        @NotNull LocalDate birthDate,
        @NotNull Gender gender,
        @NotNull @Min(1) @Max(3) Integer schoolGrade,
        @NotNull LocalDate assessmentDate,
        @NotNull @Positive BigDecimal heightCm,
        @NotNull @Positive BigDecimal weightKg,
        @NotNull List<@Valid PapsMeasurementRequest> measurements
) {

    public PapsEvaluationRequest(
            LocalDate birthDate,
            Gender gender,
            LocalDate assessmentDate,
            BigDecimal heightCm,
            BigDecimal weightKg,
            List<PapsMeasurementRequest> measurements
    ) {
        this(birthDate, gender, 1, assessmentDate, heightCm, weightKg, measurements);
    }
}
