package team.soma.teto.health.global.error;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.soma.teto.health.global.config.CorrelationIdFilter;
import team.soma.teto.health.global.response.ApiResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnBusinessExceptionResponse() throws Exception {
        mockMvc.perform(get("/test/global/business-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"))
                .andExpect(jsonPath("$.error.message").value("요청 값이 올바르지 않습니다."));
    }

    @Test
    void returnBeanValidationErrorResponse() throws Exception {
        mockMvc.perform(post("/test/global/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"heightCm\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"))
                .andExpect(jsonPath("$.error.details[0].field").value("heightCm"));
    }

    @Test
    void returnJsonErrorWhenUnknownFieldIsProvided() throws Exception {
        mockMvc.perform(post("/test/global/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"heightCm\":170,\"unknown\":\"value\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_JSON"));
    }

    @Test
    void returnMissingHeaderAsBadRequest() throws Exception {
        mockMvc.perform(get("/test/global/required-header"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"))
                .andExpect(jsonPath("$.error.details[0].field").value("X-Test-Header"));
    }

    @Test
    void hideUnexpectedExceptionDetails() throws Exception {
        mockMvc.perform(get("/test/global/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.error.message", not(containsString("secret-video-path"))));
    }

    @Test
    void generateCorrelationIdWhenHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/test/global/success"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER,
                        matchesPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
    }

    @Test
    void returnProvidedCorrelationIdWhenHeaderIsValid() throws Exception {
        String correlationId = UUID.randomUUID().toString();

        mockMvc.perform(get("/test/global/success")
                        .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId));
    }

    @Test
    void returnTypeMismatchResponse() throws Exception {
        mockMvc.perform(get("/test/global/type/not-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_TYPE_MISMATCH"))
                .andExpect(jsonPath("$.error.details[0].field").value("id"));
    }

    @RestController
    @RequestMapping("/test/global")
    static class TestController {

        private final Clock clock;

        TestController(Clock clock) {
            this.clock = clock;
        }

        @GetMapping("/success")
        ApiResponse<Map<String, String>> success() {
            return ApiResponse.success(Map.of("result", "ok"), clock);
        }

        @GetMapping("/business-error")
        void businessError() {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }

        @PostMapping("/validation")
        void validation(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/required-header")
        void requiredHeader(@RequestHeader("X-Test-Header") String ignored) {
        }

        @GetMapping("/internal-error")
        void internalError() {
            throw new IllegalStateException("secret-video-path");
        }

        @GetMapping("/type/{id}")
        void typeMismatch(@PathVariable Integer id) {
        }
    }

    record TestRequest(@Positive Integer heightCm) {
    }
}
