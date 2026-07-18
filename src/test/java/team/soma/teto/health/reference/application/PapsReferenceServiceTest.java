package team.soma.teto.health.reference.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.component.application.GetFitnessComponentsService;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.component.repository.FitnessComponentRepository;
import team.soma.teto.health.reference.standard.application.GetCurrentPapsStandardVersionService;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;
import team.soma.teto.health.reference.standard.domain.StandardSourceType;
import team.soma.teto.health.reference.standard.repository.PapsStandardVersionRepository;
import team.soma.teto.health.reference.testitem.application.GetFitnessTestItemsService;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.repository.FitnessTestItemRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PapsReferenceServiceTest {

    @Autowired
    private GetFitnessComponentsService getFitnessComponentsService;

    @Autowired
    private GetFitnessTestItemsService getFitnessTestItemsService;

    @Autowired
    private GetCurrentPapsStandardVersionService getCurrentPapsStandardVersionService;

    @Autowired
    private FitnessComponentRepository fitnessComponentRepository;

    @Autowired
    private FitnessTestItemRepository fitnessTestItemRepository;

    @Autowired
    private PapsStandardVersionRepository papsStandardVersionRepository;

    @Test
    void getActiveComponentsOrderedByDisplayOrder() {
        fitnessComponentRepository.findByCode(FitnessComponentCode.FLEXIBILITY).orElseThrow().deactivate();

        assertThat(getFitnessComponentsService.getComponents().components())
                .extracting(component -> component.code())
                .containsExactly(
                        "CARDIO_ENDURANCE",
                        "MUSCULAR_STRENGTH_ENDURANCE",
                        "POWER",
                        "BODY_COMPOSITION"
                );
    }

    @Test
    void getAllActiveTestItemsOrderedByComponentAndName() {
        fitnessTestItemRepository.findByCode(FitnessTestItemCode.STEP_TEST).orElseThrow().deactivate();

        assertThat(getFitnessTestItemsService.getTestItems(null).testItems())
                .extracting(testItem -> testItem.code())
                .containsExactly(
                        "LONG_RUN_WALK",
                        "SHUTTLE_RUN",
                        "SIT_AND_REACH",
                        "TOTAL_FLEXIBILITY",
                        "CURL_UP",
                        "GRIP_STRENGTH",
                        "PUSH_UP",
                        "SPRINT_50M",
                        "STANDING_LONG_JUMP",
                        "BMI",
                        "BODY_FAT_PERCENTAGE"
                );
    }

    @Test
    void getActiveTestItemsByComponent() {
        fitnessTestItemRepository.findByCode(FitnessTestItemCode.STEP_TEST).orElseThrow().deactivate();

        assertThat(getFitnessTestItemsService.getTestItems(FitnessComponentCode.CARDIO_ENDURANCE).testItems())
                .extracting(testItem -> testItem.code())
                .containsExactly("LONG_RUN_WALK", "SHUTTLE_RUN");
    }

    @Test
    void getCurrentActiveStandardVersion() {
        assertThat(getCurrentPapsStandardVersionService.getCurrentVersion())
                .satisfies(version -> {
                    assertThat(version.code()).isEqualTo("HACKATHON_V1");
                    assertThat(version.sourceType()).isEqualTo("INTERNAL");
                    assertThat(version.official()).isFalse();
                });
    }

    @Test
    void throwWhenActiveStandardVersionDoesNotExist() {
        papsStandardVersionRepository.findByCode("HACKATHON_V1").orElseThrow().deactivate();

        assertThatThrownBy(() -> getCurrentPapsStandardVersionService.getCurrentVersion())
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode().code())
                        .isEqualTo("PAPS_STANDARD_VERSION_NOT_FOUND"));
    }

    @Test
    void throwWhenMultipleActiveStandardVersionsExist() {
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

        assertThatThrownBy(() -> getCurrentPapsStandardVersionService.getCurrentVersion())
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode().code())
                        .isEqualTo("PAPS_MULTIPLE_ACTIVE_STANDARD_VERSIONS"));
    }

    @Test
    void throwWhenComponentIsNotRegistered() {
        FitnessTestItemRepository testItemRepository = Mockito.mock(FitnessTestItemRepository.class);
        FitnessComponentRepository componentRepository = Mockito.mock(FitnessComponentRepository.class);
        GetFitnessTestItemsService service = new GetFitnessTestItemsService(testItemRepository, componentRepository);

        assertThatThrownBy(() -> service.getTestItems(FitnessComponentCode.CARDIO_ENDURANCE))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode().code())
                        .isEqualTo("PAPS_COMPONENT_NOT_FOUND"));
    }
}
