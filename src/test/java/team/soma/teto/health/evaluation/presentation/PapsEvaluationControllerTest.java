package team.soma.teto.health.evaluation.presentation;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PapsEvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void evaluate() throws Exception {
        mockMvc.perform(post("/api/v1/paps/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "2009-02-24",
                                  "gender": "MALE",
                                  "schoolLevel": "HIGH",
                                  "schoolGrade": 1,
                                  "assessmentDate": "2026-07-18",
                                  "heightCm": 175.2,
                                  "weightKg": 65.4,
                                  "measurements": [
                                    {"testItemCode": "SHUTTLE_RUN", "value": 52},
                                    {"testItemCode": "SIT_AND_REACH", "value": 14.5},
                                    {"testItemCode": "PUSH_UP", "value": 25},
                                    {"testItemCode": "STANDING_LONG_JUMP", "value": 185}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.standardVersion.code").value("PAPS_OFFICIAL_2025_V1"))
                .andExpect(jsonPath("$.data.profile.age").value(17))
                .andExpect(jsonPath("$.data.profile.schoolLevel").value("HIGH"))
                .andExpect(jsonPath("$.data.profile.schoolGrade").value(1))
                .andExpect(jsonPath("$.data.profile.bmi").value(21.3))
                .andExpect(jsonPath("$.data.completeness.complete").value(true))
                .andExpect(jsonPath("$.data.measurements", hasSize(5)))
                .andExpect(jsonPath("$.data.measurements[0].grade").value(3))
                .andExpect(jsonPath("$.data.measurements[4].testItemCode").value("BMI"))
                .andExpect(jsonPath("$.data.measurements[4].grade").value(nullValue()))
                .andExpect(jsonPath("$.data.measurements[4].bmiCategory").value("NORMAL"));
    }

    @Test
    void returnBeanValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/paps/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": null,
                                  "gender": "MALE",
                                  "schoolLevel": "HIGH",
                                  "schoolGrade": 1,
                                  "assessmentDate": "2026-07-18",
                                  "heightCm": 175.2,
                                  "weightKg": 65.4,
                                  "measurements": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    void returnInvalidJsonForWrongEnum() throws Exception {
        mockMvc.perform(post("/api/v1/paps/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "2009-02-24",
                                  "gender": "UNKNOWN",
                                  "schoolLevel": "HIGH",
                                  "schoolGrade": 1,
                                  "assessmentDate": "2026-07-18",
                                  "heightCm": 175.2,
                                  "weightKg": 65.4,
                                  "measurements": [{"testItemCode": "SHUTTLE_RUN", "value": 52}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_JSON"));
    }

    @Test
    void returnInvalidDateRangeForFutureAssessmentDate() throws Exception {
        mockMvc.perform(post("/api/v1/paps/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "2009-02-24",
                                  "gender": "MALE",
                                  "schoolLevel": "HIGH",
                                  "schoolGrade": 1,
                                  "assessmentDate": "2099-07-18",
                                  "heightCm": 175.2,
                                  "weightKg": 65.4,
                                  "measurements": [{"testItemCode": "SHUTTLE_RUN", "value": 52}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PAPS_INVALID_DATE_RANGE"));
    }

    @Test
    void returnBeanValidationErrorForInvalidSchoolGradeRange() throws Exception {
        mockMvc.perform(post("/api/v1/paps/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "2009-02-24",
                                  "gender": "MALE",
                                  "schoolLevel": "HIGH",
                                  "schoolGrade": 4,
                                  "assessmentDate": "2026-07-18",
                                  "heightCm": 175.2,
                                  "weightKg": 65.4,
                                  "measurements": [{"testItemCode": "SHUTTLE_RUN", "value": 52}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    void returnDuplicateTestItemError() throws Exception {
        mockMvc.perform(post("/api/v1/paps/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "2009-02-24",
                                  "gender": "MALE",
                                  "schoolLevel": "HIGH",
                                  "schoolGrade": 1,
                                  "assessmentDate": "2026-07-18",
                                  "heightCm": 175.2,
                                  "weightKg": 65.4,
                                  "measurements": [
                                    {"testItemCode": "SHUTTLE_RUN", "value": 52},
                                    {"testItemCode": "SHUTTLE_RUN", "value": 53}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PAPS_DUPLICATE_TEST_ITEM"));
    }

    @Test
    void returnStandardMissingErrorForOfficiallyUnsupportedItem() throws Exception {
        mockMvc.perform(post("/api/v1/paps/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "2015-02-24",
                                  "gender": "MALE",
                                  "schoolLevel": "ELEMENTARY",
                                  "schoolGrade": 4,
                                  "assessmentDate": "2026-07-18",
                                  "heightCm": 175.2,
                                  "weightKg": 65.4,
                                  "measurements": [{"testItemCode": "PUSH_UP", "value": 5}]
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PAPS_STANDARD_NOT_FOUND"));
    }
}
