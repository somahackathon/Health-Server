package team.soma.teto.health.evaluation.presentation;

import java.util.List;

public record PapsEvaluationResponse(
        PapsStandardVersionSummary standardVersion,
        PapsEvaluationProfileResponse profile,
        PapsEvaluationCompletenessResponse completeness,
        List<PapsMeasurementResultResponse> measurements
) {

    public PapsEvaluationResponse {
        measurements = List.copyOf(measurements);
    }
}
