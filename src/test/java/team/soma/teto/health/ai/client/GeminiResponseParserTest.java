package team.soma.teto.health.ai.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import team.soma.teto.health.ai.dto.GeminiCandidate;
import team.soma.teto.health.ai.dto.GeminiContent;
import team.soma.teto.health.ai.dto.GeminiGenerateResponse;
import team.soma.teto.health.ai.dto.GeminiPart;
import team.soma.teto.health.analysis.job.domain.AiFailureCode;

class GeminiResponseParserTest {

    private final GeminiResponseParser parser = new GeminiResponseParser(new ObjectMapper());

    private record SampleResult(String summary) {
    }

    @Test
    void parsesValidJsonTextIntoRecord() {
        GeminiGenerateResponse response = responseWithText("{\"summary\":\"좋아요\"}");

        SampleResult result = parser.parse(response, SampleResult.class);

        assertThat(result.summary()).isEqualTo("좋아요");
    }

    @Test
    void throwsInvalidAiResponseWhenNoCandidates() {
        GeminiGenerateResponse response = new GeminiGenerateResponse(List.of());

        assertThatThrownBy(() -> parser.parse(response, SampleResult.class))
                .isInstanceOf(AiClientException.class)
                .satisfies(e -> assertThat(((AiClientException) e).failureCode()).isEqualTo(AiFailureCode.INVALID_AI_RESPONSE));
    }

    @Test
    void throwsInvalidAiResponseWhenNullResponse() {
        assertThatThrownBy(() -> parser.parse(null, SampleResult.class))
                .isInstanceOf(AiClientException.class)
                .satisfies(e -> assertThat(((AiClientException) e).failureCode()).isEqualTo(AiFailureCode.INVALID_AI_RESPONSE));
    }

    @Test
    void throwsInvalidAiResponseWhenPartsMissing() {
        GeminiGenerateResponse response = new GeminiGenerateResponse(
                List.of(new GeminiCandidate(new GeminiContent(List.of()), "STOP")));

        assertThatThrownBy(() -> parser.parse(response, SampleResult.class))
                .isInstanceOf(AiClientException.class)
                .satisfies(e -> assertThat(((AiClientException) e).failureCode()).isEqualTo(AiFailureCode.INVALID_AI_RESPONSE));
    }

    @Test
    void throwsInvalidAiResponseWhenTextIsMalformedJson() {
        GeminiGenerateResponse response = responseWithText("not-json{");

        assertThatThrownBy(() -> parser.parse(response, SampleResult.class))
                .isInstanceOf(AiClientException.class)
                .satisfies(e -> assertThat(((AiClientException) e).failureCode()).isEqualTo(AiFailureCode.INVALID_AI_RESPONSE));
    }

    private GeminiGenerateResponse responseWithText(String text) {
        return new GeminiGenerateResponse(
                List.of(new GeminiCandidate(new GeminiContent(List.of(new GeminiPart(text))), "STOP")));
    }
}
