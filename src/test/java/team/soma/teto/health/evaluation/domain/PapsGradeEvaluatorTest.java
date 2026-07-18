package team.soma.teto.health.evaluation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.component.domain.FitnessComponent;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.standard.domain.PapsStandard;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;
import team.soma.teto.health.reference.standard.domain.SchoolLevel;
import team.soma.teto.health.reference.standard.domain.StandardSourceType;
import team.soma.teto.health.reference.testitem.domain.BetterDirection;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;
import team.soma.teto.health.reference.testitem.domain.MeasurementValueType;

class PapsGradeEvaluatorTest {

    private final PapsGradeEvaluator evaluator = new PapsGradeEvaluator();
    private final PapsStandardVersion version = PapsStandardVersion.create("TEST", "Test", StandardSourceType.INTERNAL, null, null, null, null, false);
    private final FitnessTestItem item = FitnessTestItem.create(
            FitnessComponent.create(FitnessComponentCode.CARDIO_ENDURANCE, "Cardio", null, 1),
            FitnessTestItemCode.SHUTTLE_RUN,
            "Shuttle Run",
            MeasurementUnit.COUNT,
            MeasurementValueType.INTEGER,
            BetterDirection.HIGHER,
            null,
            null,
            0
    );

    @Test
    void matchInclusiveMinimumBoundary() {
        assertThat(evaluator.evaluate(new BigDecimal("10"), List.of(standard(1, new BigDecimal("10"), null, true, true))))
                .isEqualTo(1);
    }

    @Test
    void rejectExclusiveMinimumBoundary() {
        assertError(() -> evaluator.evaluate(new BigDecimal("10"), List.of(standard(1, new BigDecimal("10"), null, false, true))),
                "PAPS_STANDARD_NOT_FOUND");
    }

    @Test
    void matchInclusiveMaximumBoundary() {
        assertThat(evaluator.evaluate(new BigDecimal("20"), List.of(standard(2, null, new BigDecimal("20"), true, true))))
                .isEqualTo(2);
    }

    @Test
    void rejectExclusiveMaximumBoundary() {
        assertError(() -> evaluator.evaluate(new BigDecimal("20"), List.of(standard(2, null, new BigDecimal("20"), true, false))),
                "PAPS_STANDARD_NOT_FOUND");
    }

    @Test
    void matchOpenLowerBoundAndOpenUpperBound() {
        assertThat(evaluator.evaluate(new BigDecimal("100"), List.of(standard(1, new BigDecimal("50"), null, true, true))))
                .isEqualTo(1);
        assertThat(evaluator.evaluate(new BigDecimal("5"), List.of(standard(5, null, new BigDecimal("10"), true, true))))
                .isEqualTo(5);
    }

    @Test
    void throwWhenNoStandardMatches() {
        assertError(() -> evaluator.evaluate(new BigDecimal("30"), List.of(standard(1, new BigDecimal("10"), new BigDecimal("20"), true, true))),
                "PAPS_STANDARD_NOT_FOUND");
    }

    @Test
    void throwWhenMultipleStandardsMatch() {
        assertError(() -> evaluator.evaluate(new BigDecimal("15"), List.of(
                standard(1, new BigDecimal("10"), new BigDecimal("20"), true, true),
                standard(2, new BigDecimal("15"), new BigDecimal("30"), true, true)
        )), "PAPS_STANDARD_OVERLAPPED");
    }

    private PapsStandard standard(int grade, BigDecimal minimumValue, BigDecimal maximumValue, boolean minimumInclusive, boolean maximumInclusive) {
        return PapsStandard.create(version, item, SchoolLevel.HIGH, Gender.MALE, 17, 17, grade, minimumValue, maximumValue, minimumInclusive, maximumInclusive);
    }

    private void assertError(Runnable runnable, String code) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode().code()).isEqualTo(code));
    }
}
