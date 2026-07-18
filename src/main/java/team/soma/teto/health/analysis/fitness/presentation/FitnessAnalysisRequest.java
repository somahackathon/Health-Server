package team.soma.teto.health.analysis.fitness.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;

public record FitnessAnalysisRequest(
        @NotNull @Valid Profile profile,
        @NotEmpty List<@Valid RecordItem> records
) {

    public record Profile(
            @NotNull @Past LocalDate birthDate,
            @NotNull Gender gender,
            @NotNull @Min(1) @Max(3) Integer schoolGrade,
            @Positive double heightCm,
            @Positive double weightKg
    ) {

        public Profile(LocalDate birthDate, Gender gender, double heightCm, double weightKg) {
            this(birthDate, gender, 1, heightCm, weightKg);
        }
    }

    public record RecordItem(
            @NotNull FitnessTestItemCode itemCode,
            @Positive double value,
            @NotNull MeasurementUnit unit,
            @NotNull Instant measuredAt
    ) {
    }
}
