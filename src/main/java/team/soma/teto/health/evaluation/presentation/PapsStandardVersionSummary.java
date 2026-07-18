package team.soma.teto.health.evaluation.presentation;

import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;

public record PapsStandardVersionSummary(
        String code,
        String name,
        Boolean official
) {

    public static PapsStandardVersionSummary from(PapsStandardVersion version) {
        return new PapsStandardVersionSummary(version.getCode(), version.getName(), version.getOfficial());
    }
}
