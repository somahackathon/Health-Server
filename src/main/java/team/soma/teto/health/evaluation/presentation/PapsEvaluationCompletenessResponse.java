package team.soma.teto.health.evaluation.presentation;

import java.util.List;

public record PapsEvaluationCompletenessResponse(
        Integer evaluatedComponentCount,
        Integer requiredComponentCount,
        Boolean complete,
        List<String> missingComponents
) {

    public PapsEvaluationCompletenessResponse {
        missingComponents = List.copyOf(missingComponents);
    }
}
