package team.soma.teto.health.reference.standard.presentation;

import java.time.LocalDate;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;

public record PapsStandardVersionResponse(
        String code,
        String name,
        String sourceType,
        String sourceName,
        String sourceUrl,
        Boolean official,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {

    public static PapsStandardVersionResponse from(PapsStandardVersion version) {
        return new PapsStandardVersionResponse(
                version.getCode(),
                version.getName(),
                version.getSourceType().name(),
                version.getSourceName(),
                version.getSourceUrl(),
                version.getOfficial(),
                version.getEffectiveFrom(),
                version.getEffectiveTo()
        );
    }
}
