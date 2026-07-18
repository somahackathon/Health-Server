package team.soma.teto.health.evaluation.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import team.soma.teto.health.reference.standard.domain.SchoolLevel;
import team.soma.teto.health.reference.standard.domain.Gender;

public record PapsEvaluationRequest(
        @NotNull LocalDate birthDate,
        @NotNull Gender gender,
        @NotNull SchoolLevel schoolLevel,
        @NotNull @Min(1) @Max(6) Integer schoolGrade,
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
        this(birthDate, gender, SchoolLevel.HIGH, 1, assessmentDate, heightCm, weightKg, measurements);
    }

    public PapsEvaluationRequest(
            LocalDate birthDate,
            Gender gender,
            Integer schoolGrade,
            LocalDate assessmentDate,
            BigDecimal heightCm,
            BigDecimal weightKg,
            List<PapsMeasurementRequest> measurements
    ) {
        this(birthDate, gender, SchoolLevel.HIGH, schoolGrade, assessmentDate, heightCm, weightKg, measurements);
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
