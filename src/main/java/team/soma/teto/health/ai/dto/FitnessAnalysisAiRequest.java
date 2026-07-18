package team.soma.teto.health.ai.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;

public record FitnessAnalysisAiRequest(
        String correlationId,
        String modelVersion,
        Profile profile,
        List<RecordItem> records
) {

    public record Profile(
            LocalDate birthDate,
            Gender gender,
            double heightCm,
            double weightKg
    ) {
    }

    public record RecordItem(
            FitnessTestItemCode itemCode,
            double value,
            MeasurementUnit unit,
            Instant measuredAt
    ) {
    }
}
