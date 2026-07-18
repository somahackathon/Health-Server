package team.soma.teto.health.analysis.fitness.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;

public record FitnessAnalysisRequest(
        @NotBlank @Size(max = 100) String installationId,
        @NotNull @Valid Profile profile,
        @NotEmpty @Size(max = 50) @Valid List<Record> records
) {

    public record Profile(
            @NotNull @Past LocalDate birthDate,
            @NotNull Gender gender,
            @NotNull @DecimalMin("50") @DecimalMax("250") Double heightCm,
            @NotNull @DecimalMin("10") @DecimalMax("300") Double weightKg,
            @Min(0) @Max(14) Integer weeklyExerciseFrequency
    ) {
    }

    public record Record(
            @NotNull FitnessTestItemCode itemCode,
            @NotNull @PositiveOrZero Double value,
            MeasurementUnit unit,
            @NotNull @PastOrPresent LocalDate measuredAt
    ) {
    }
}
