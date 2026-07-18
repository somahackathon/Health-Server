package team.soma.teto.health.analysis.fitness.presentation;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.soma.teto.health.global.config.RequestHeaderNames;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FitnessAnalysisControllerTest {

    private static final String INSTALLATION_ID = "11111111-1111-4111-8111-111111111111";
    private static final String OTHER_INSTALLATION_ID = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void analyzeFitnessReturnsCompletedJobAndIsLaterRetrievable() throws Exception {
        String requestBody = """
                {
                  "profile": {
                    "birthDate": "2010-05-01",
                    "gender": "MALE",
                    "heightCm": 170.5,
                    "weightKg": 60.2
                  },
                  "records": [
                    {
                      "itemCode": "PUSH_UP",
                      "value": 20,
                      "unit": "COUNT",
                      "measuredAt": "2026-07-01T00:00:00Z"
                    }
                  ]
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/fitness-analyses")
                        .header(RequestHeaderNames.INSTALLATION_ID, INSTALLATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.modelVersion").value("mock-fitness-v1"))
                .andExpect(jsonPath("$.data.recommendations", not(empty())))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String jobId = body.at("/data/jobId").asText();

        mockMvc.perform(get("/api/analysis-jobs/{jobId}", jobId)
                        .header(RequestHeaderNames.INSTALLATION_ID, INSTALLATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisType").value("FITNESS"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.result.summary").exists());

        mockMvc.perform(get("/api/analysis-jobs/{jobId}", jobId)
                        .header(RequestHeaderNames.INSTALLATION_ID, OTHER_INSTALLATION_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ANALYSIS_JOB_NOT_FOUND"));
    }

    @Test
    void rejectInvalidFitnessRequestBody() throws Exception {
        String requestBody = """
                {
                  "profile": {
                    "birthDate": "2010-05-01",
                    "gender": "MALE",
                    "heightCm": -1,
                    "weightKg": 60.2
                  },
                  "records": []
                }
                """;

        mockMvc.perform(post("/api/fitness-analyses")
                        .header(RequestHeaderNames.INSTALLATION_ID, INSTALLATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    void rejectMissingInstallationIdHeader() throws Exception {
        mockMvc.perform(post("/api/fitness-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profile": {
                                    "birthDate": "2010-05-01",
                                    "gender": "MALE",
                                    "heightCm": 170.5,
                                    "weightKg": 60.2
                                  },
                                  "records": [
                                    {
                                      "itemCode": "PUSH_UP",
                                      "value": 20,
                                      "unit": "COUNT",
                                      "measuredAt": "2026-07-01T00:00:00Z"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    void rejectInvalidInstallationIdHeader() throws Exception {
        mockMvc.perform(post("/api/fitness-analyses")
                        .header(RequestHeaderNames.INSTALLATION_ID, "plain-installation-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profile": {
                                    "birthDate": "2010-05-01",
                                    "gender": "MALE",
                                    "heightCm": 170.5,
                                    "weightKg": 60.2
                                  },
                                  "records": [
                                    {
                                      "itemCode": "PUSH_UP",
                                      "value": 20,
                                      "unit": "COUNT",
                                      "measuredAt": "2026-07-01T00:00:00Z"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }
}
