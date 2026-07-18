package team.soma.teto.health.ai.client;

import java.util.List;
import java.util.Map;

/**
 * Helpers for building Gemini responseSchema maps (OpenAPI subset used by the Gemini API).
 */
final class GeminiSchemas {

    private GeminiSchemas() {
    }

    static Map<String, Object> object(Map<String, Object> properties, List<String> required) {
        return Map.of(
                "type", "OBJECT",
                "properties", properties,
                "required", required
        );
    }

    static Map<String, Object> array(Map<String, Object> items) {
        return Map.of("type", "ARRAY", "items", items);
    }

    static Map<String, Object> string() {
        return Map.of("type", "STRING");
    }

    static Map<String, Object> stringEnum(List<String> values) {
        return Map.of("type", "STRING", "enum", values);
    }

    static Map<String, Object> nullableStringEnum(List<String> values) {
        return Map.of("type", "STRING", "enum", values, "nullable", true);
    }

    static Map<String, Object> integer() {
        return Map.of("type", "INTEGER");
    }

    static Map<String, Object> number() {
        return Map.of("type", "NUMBER");
    }
}
