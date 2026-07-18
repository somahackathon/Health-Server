package team.soma.teto.health.global.response;

public record FieldErrorResponse(
        String field,
        String reason
) {
}
