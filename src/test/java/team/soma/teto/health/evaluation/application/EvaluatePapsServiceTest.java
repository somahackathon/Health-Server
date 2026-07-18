package team.soma.teto.health.evaluation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.evaluation.presentation.PapsEvaluationRequest;
import team.soma.teto.health.evaluation.presentation.PapsEvaluationResponse;
import team.soma.teto.health.evaluation.presentation.PapsMeasurementRequest;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.standard.domain.PapsStandard;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;
import team.soma.teto.health.reference.standard.domain.SchoolLevel;
import team.soma.teto.health.reference.standard.domain.StandardSourceType;
import team.soma.teto.health.reference.standard.repository.PapsStandardRepository;
import team.soma.teto.health.reference.standard.repository.PapsStandardVersionRepository;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.repository.FitnessTestItemRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EvaluatePapsServiceTest {

    @Autowired
    private EvaluatePapsService evaluatePapsService;

    @Autowired
    private FitnessTestItemRepository fitnessTestItemRepository;

    @Autowired
    private PapsStandardVersionRepository papsStandardVersionRepository;

    @Autowired
    private PapsStandardRepository papsStandardRepository;

    @Test
    void evaluateFullPapsRecordsWithServerCalculatedBmi() {
        saveStandards(FitnessTestItemCode.SHUTTLE_RUN, FitnessTestItemCode.SIT_AND_REACH, FitnessTestItemCode.PUSH_UP, FitnessTestItemCode.STANDING_LONG_JUMP, FitnessTestItemCode.BMI);

        PapsEvaluationResponse response = evaluatePapsService.evaluate(fullRequest());

        assertThat(response.standardVersion().code()).isEqualTo("HACKATHON_V1");
        assertThat(response.standardVersion().official()).isFalse();
        assertThat(response.profile().age()).isEqualTo(17);
        assertThat(response.profile().schoolLevel()).isEqualTo("HIGH");
        assertThat(response.profile().schoolGrade()).isEqualTo(1);
        assertThat(response.profile().bmi()).isEqualByComparingTo("21.3");
        assertThat(response.completeness().complete()).isTrue();
        assertThat(response.completeness().evaluatedComponentCount()).isEqualTo(5);
        assertThat(response.measurements()).extracting(measurement -> measurement.testItemCode())
                .containsExactly("SHUTTLE_RUN", "SIT_AND_REACH", "PUSH_UP", "STANDING_LONG_JUMP", "BMI");
        assertThat(response.measurements()).extracting(measurement -> measurement.grade())
                .containsExactly(3, 3, 3, 3, 2);
    }

    @Test
    void allowPartialEvaluationAndReturnMissingComponents() {
        saveStandards(FitnessTestItemCode.SHUTTLE_RUN, FitnessTestItemCode.BMI);

        PapsEvaluationResponse response = evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")))
        ));

        assertThat(response.completeness().complete()).isFalse();
        assertThat(response.completeness().evaluatedComponentCount()).isEqualTo(2);
        assertThat(response.completeness().missingComponents())
                .containsExactly("FLEXIBILITY", "MUSCULAR_STRENGTH_ENDURANCE", "POWER");
    }

    @Test
    void rejectDuplicateTestItem() {
        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(
                        new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")),
                        new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("53"))
                )
        )), "PAPS_DUPLICATE_TEST_ITEM");
    }

    @Test
    void rejectDuplicateComponent() {
        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(
                        new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")),
                        new PapsMeasurementRequest(FitnessTestItemCode.STEP_TEST, new BigDecimal("70.1"))
                )
        )), "PAPS_DUPLICATE_COMPONENT");
    }

    @Test
    void rejectClientProvidedBmi() {
        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.BMI, new BigDecimal("21.3")))
        )), "PAPS_CLIENT_BMI_NOT_ALLOWED");
    }

    @Test
    void rejectInactiveTestItem() {
        fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow().deactivate();

        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")))
        )), "PAPS_TEST_ITEM_INACTIVE");
    }

    @Test
    void rejectMissingTestItem() {
        fitnessTestItemRepository.delete(fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow());
        fitnessTestItemRepository.flush();

        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")))
        )), "PAPS_TEST_ITEM_NOT_FOUND");
    }

    @Test
    void rejectInvalidIntegerMeasurementValue() {
        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52.1")))
        )), "PAPS_INVALID_MEASUREMENT_VALUE");
    }

    @Test
    void rejectEmptyMeasurements() {
        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of()
        )), "PAPS_EMPTY_MEASUREMENTS");
    }

    @Test
    void rejectFutureAssessmentDate() {
        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2099, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")))
        )), "PAPS_INVALID_DATE_RANGE");
    }

    @Test
    void rejectWhenActiveStandardVersionDoesNotExist() {
        papsStandardVersionRepository.findByCode("HACKATHON_V1").orElseThrow().deactivate();

        assertError(() -> evaluatePapsService.evaluate(fullRequest()), "PAPS_STANDARD_VERSION_NOT_FOUND");
    }

    @Test
    void rejectWhenMultipleActiveStandardVersionsExist() {
        papsStandardVersionRepository.save(PapsStandardVersion.create("TEST_V2", "Test V2", StandardSourceType.INTERNAL, null, null, null, null, false));

        assertError(() -> evaluatePapsService.evaluate(fullRequest()), "PAPS_MULTIPLE_ACTIVE_STANDARD_VERSIONS");
    }

    @Test
    void rejectWhenBmiTestItemIsInactive() {
        saveStandards(FitnessTestItemCode.SHUTTLE_RUN);
        fitnessTestItemRepository.findByCode(FitnessTestItemCode.BMI).orElseThrow().deactivate();

        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")))
        )), "PAPS_BMI_TEST_ITEM_NOT_FOUND");
    }

    @Test
    void rejectWhenStandardDataDoesNotExist() {
        assertError(() -> evaluatePapsService.evaluate(fullRequest()), "PAPS_STANDARD_NOT_FOUND");
    }

    @Test
    void rejectWhenStandardRangesOverlap() {
        FitnessTestItem item = fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow();
        PapsStandardVersion version = papsStandardVersionRepository.findByCode("HACKATHON_V1").orElseThrow();
        papsStandardRepository.save(PapsStandard.create(version, item, SchoolLevel.HIGH, Gender.MALE, 0, 99, 1, null, new BigDecimal("60"), true, true));
        papsStandardRepository.save(PapsStandard.create(version, item, SchoolLevel.HIGH, Gender.MALE, 0, 99, 2, new BigDecimal("50"), null, true, true));
        saveStandards(FitnessTestItemCode.BMI);

        assertError(() -> evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")))
        )), "PAPS_STANDARD_OVERLAPPED");
    }

    @Test
    void useOnlyMatchingVersionSchoolLevelGradeAndGenderStandards() {
        FitnessTestItem item = fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow();
        FitnessTestItem bmiItem = fitnessTestItemRepository.findByCode(FitnessTestItemCode.BMI).orElseThrow();
        PapsStandardVersion activeVersion = papsStandardVersionRepository.findByCode("HACKATHON_V1").orElseThrow();
        PapsStandardVersion inactiveVersion = PapsStandardVersion.create("INACTIVE_V1", "Inactive", StandardSourceType.INTERNAL, null, null, null, null, false);
        inactiveVersion.deactivate();
        papsStandardVersionRepository.save(inactiveVersion);
        papsStandardRepository.save(PapsStandard.create(inactiveVersion, item, SchoolLevel.HIGH, Gender.MALE, 0, 99, 1, null, null, true, true));
        papsStandardRepository.save(PapsStandard.create(activeVersion, item, SchoolLevel.MIDDLE, Gender.MALE, 0, 99, 2, null, null, true, true));
        papsStandardRepository.save(PapsStandard.create(activeVersion, item, SchoolLevel.HIGH, Gender.FEMALE, 0, 99, 4, null, null, true, true));
        papsStandardRepository.save(PapsStandard.create(activeVersion, item, SchoolLevel.HIGH, 2, Gender.MALE, 0, 99, 5, null, null, true, true));
        papsStandardRepository.save(PapsStandard.create(activeVersion, item, SchoolLevel.HIGH, 1, Gender.MALE, 0, 99, 3, null, null, true, true));
        papsStandardRepository.save(PapsStandard.create(activeVersion, bmiItem, SchoolLevel.HIGH, 1, Gender.MALE, 0, 99, 2, null, null, true, true));

        PapsEvaluationResponse response = evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                SchoolLevel.HIGH,
                1,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")))
        ));

        assertThat(response.measurements().get(0).grade()).isEqualTo(3);
    }

    @Test
    void evaluatesMiddleSchoolRequestWithMiddleSchoolGradeStandards() {
        FitnessTestItem item = fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow();
        FitnessTestItem bmiItem = fitnessTestItemRepository.findByCode(FitnessTestItemCode.BMI).orElseThrow();
        PapsStandardVersion version = papsStandardVersionRepository.findByCode("HACKATHON_V1").orElseThrow();
        papsStandardRepository.save(PapsStandard.create(version, item, SchoolLevel.HIGH, 1, Gender.MALE, 0, 99, 4, null, null, true, true));
        papsStandardRepository.save(PapsStandard.create(version, item, SchoolLevel.MIDDLE, 2, Gender.MALE, 0, 99, 3, null, null, true, true));
        papsStandardRepository.save(PapsStandard.create(version, bmiItem, SchoolLevel.MIDDLE, 2, Gender.MALE, 0, 99, 2, null, null, true, true));

        PapsEvaluationResponse response = evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2012, 2, 24),
                Gender.MALE,
                SchoolLevel.MIDDLE,
                2,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("165.2"),
                new BigDecimal("55.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")))
        ));

        assertThat(response.profile().schoolLevel()).isEqualTo("MIDDLE");
        assertThat(response.profile().schoolGrade()).isEqualTo(2);
        assertThat(response.measurements().get(0).grade()).isEqualTo(3);
    }

    @Test
    void doesNotPersistEvaluationResult() {
        long standardCount = papsStandardRepository.count();
        saveStandards(FitnessTestItemCode.SHUTTLE_RUN, FitnessTestItemCode.BMI);

        evaluatePapsService.evaluate(new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")))
        ));

        assertThat(papsStandardRepository.count()).isEqualTo(standardCount + 2);
    }

    private PapsEvaluationRequest fullRequest() {
        return new PapsEvaluationRequest(
                LocalDate.of(2009, 2, 24),
                Gender.MALE,
                LocalDate.of(2026, 7, 18),
                new BigDecimal("175.2"),
                new BigDecimal("65.4"),
                List.of(
                        new PapsMeasurementRequest(FitnessTestItemCode.SHUTTLE_RUN, new BigDecimal("52")),
                        new PapsMeasurementRequest(FitnessTestItemCode.SIT_AND_REACH, new BigDecimal("14.5")),
                        new PapsMeasurementRequest(FitnessTestItemCode.PUSH_UP, new BigDecimal("25")),
                        new PapsMeasurementRequest(FitnessTestItemCode.STANDING_LONG_JUMP, new BigDecimal("185"))
                )
        );
    }

    private void saveStandards(FitnessTestItemCode... codes) {
        PapsStandardVersion version = papsStandardVersionRepository.findByCode("HACKATHON_V1").orElseThrow();
        for (FitnessTestItemCode code : codes) {
            FitnessTestItem item = fitnessTestItemRepository.findByCode(code).orElseThrow();
            int grade = code == FitnessTestItemCode.BMI ? 2 : 3;
            papsStandardRepository.save(PapsStandard.create(version, item, SchoolLevel.HIGH, Gender.MALE, 0, 99, grade, null, null, true, true));
        }
    }

    private void assertError(Runnable runnable, String code) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode().code()).isEqualTo(code));
    }
}
