package team.soma.teto.health.reference.presentation;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.component.repository.FitnessComponentRepository;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;
import team.soma.teto.health.reference.standard.domain.StandardSourceType;
import team.soma.teto.health.reference.standard.repository.PapsStandardVersionRepository;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.repository.FitnessTestItemRepository;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PapsReferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FitnessComponentRepository fitnessComponentRepository;

    @Autowired
    private FitnessTestItemRepository fitnessTestItemRepository;

    @Autowired
    private PapsStandardVersionRepository papsStandardVersionRepository;

    @Test
    void getComponents() throws Exception {
        mockMvc.perform(get("/api/v1/paps/components"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.components", hasSize(5)))
                .andExpect(jsonPath("$.data.components[0].code").value("CARDIO_ENDURANCE"))
                .andExpect(jsonPath("$.data.components[0].displayOrder").value(1));
    }

    @Test
    void getAllTestItems() throws Exception {
        mockMvc.perform(get("/api/v1/paps/test-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.testItems", hasSize(12)))
                .andExpect(jsonPath("$.data.testItems[0].componentCode").value("CARDIO_ENDURANCE"))
                .andExpect(jsonPath("$.data.testItems[0].unit").value("SECOND"))
                .andExpect(jsonPath("$.data.testItems[0].valueType").value("DECIMAL"));
    }

    @Test
    void getTestItemsByComponent() throws Exception {
        mockMvc.perform(get("/api/v1/paps/test-items")
                        .param("component", "CARDIO_ENDURANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.testItems", hasSize(3)))
                .andExpect(jsonPath("$.data.testItems[0].code").value("LONG_RUN_WALK"));
    }

    @Test
    void returnBadRequestWhenComponentValueIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/paps/test-items")
                        .param("component", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_TYPE_MISMATCH"));
    }

    @Test
    void returnNotFoundWhenComponentIsNotActive() throws Exception {
        fitnessComponentRepository.findByCode(FitnessComponentCode.CARDIO_ENDURANCE).orElseThrow().deactivate();

        mockMvc.perform(get("/api/v1/paps/test-items")
                        .param("component", "CARDIO_ENDURANCE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PAPS_COMPONENT_NOT_FOUND"));
    }

    @Test
    void inactiveItemsAreNotReturned() throws Exception {
        fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow().deactivate();

        mockMvc.perform(get("/api/v1/paps/test-items")
                        .param("component", "CARDIO_ENDURANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.testItems", hasSize(2)));
    }

    @Test
    void getCurrentStandardVersion() throws Exception {
        mockMvc.perform(get("/api/v1/paps/standards/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("HACKATHON_V1"))
                .andExpect(jsonPath("$.data.sourceType").value("INTERNAL"))
                .andExpect(jsonPath("$.data.official").value(false));
    }

    @Test
    void returnErrorWhenCurrentStandardVersionDoesNotExist() throws Exception {
        papsStandardVersionRepository.findByCode("HACKATHON_V1").orElseThrow().deactivate();

        mockMvc.perform(get("/api/v1/paps/standards/current"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PAPS_STANDARD_VERSION_NOT_FOUND"));
    }

    @Test
    void returnErrorWhenMultipleCurrentStandardVersionsExist() throws Exception {
        papsStandardVersionRepository.save(PapsStandardVersion.create(
                "TEST_ACTIVE_V2",
                "Test Active V2",
                StandardSourceType.INTERNAL,
                null,
                null,
                LocalDate.now(),
                null,
                false
        ));

        mockMvc.perform(get("/api/v1/paps/standards/current"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PAPS_MULTIPLE_ACTIVE_STANDARD_VERSIONS"));
    }
}
