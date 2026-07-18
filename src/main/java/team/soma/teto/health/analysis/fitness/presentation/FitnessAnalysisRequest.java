package team.soma.teto.health.analysis.fitness.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
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
import team.soma.teto.health.reference.standard.domain.SchoolLevel;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;

public record FitnessAnalysisRequest(
        @NotNull @Valid Profile profile,
        @NotEmpty List<@Valid RecordItem> records
) {

    public record Profile(
            @NotNull @Past LocalDate birthDate,
            @NotNull Gender gender,
            @NotNull SchoolLevel schoolLevel,
            @NotNull @Min(1) @Max(6) Integer schoolGrade,
            @Positive double heightCm,
            @Positive double weightKg
    ) {

        public Profile(LocalDate birthDate, Gender gender, double heightCm, double weightKg) {
            this(birthDate, gender, SchoolLevel.HIGH, 1, heightCm, weightKg);
        }

        public Profile(LocalDate birthDate, Gender gender, Integer schoolGrade, double heightCm, double weightKg) {
            this(birthDate, gender, SchoolLevel.HIGH, schoolGrade, heightCm, weightKg);
        }

        @AssertTrue(message = "schoolGrade is outside the allowed range for schoolLevel")
        public boolean isSchoolGradeValidForSchoolLevel() {
            if (schoolLevel == null || schoolGrade == null) {
                return true;
            }
            return switch (schoolLevel) {
                case ELEMENTARY -> schoolGrade >= 1 && schoolGrade <= 6;
                case MIDDLE, HIGH -> schoolGrade >= 1 && schoolGrade <= 3;
            };
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
