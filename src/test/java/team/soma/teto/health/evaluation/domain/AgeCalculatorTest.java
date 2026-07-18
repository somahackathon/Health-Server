package team.soma.teto.health.evaluation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import team.soma.teto.health.global.error.BusinessException;

class AgeCalculatorTest {

    private final AgeCalculator ageCalculator = new AgeCalculator();

    @Test
    void calculateAgeAfterBirthday() {
        assertThat(ageCalculator.calculate(LocalDate.of(2009, 2, 24), LocalDate.of(2026, 7, 18))).isEqualTo(17);
    }

    @Test
    void calculateAgeBeforeBirthday() {
        assertThat(ageCalculator.calculate(LocalDate.of(2009, 12, 24), LocalDate.of(2026, 7, 18))).isEqualTo(16);
    }

    @Test
    void calculateAgeOnBirthday() {
        assertThat(ageCalculator.calculate(LocalDate.of(2009, 7, 18), LocalDate.of(2026, 7, 18))).isEqualTo(17);
    }

    @Test
    void calculateZeroWhenAssessmentDateIsBirthDate() {
        assertThat(ageCalculator.calculate(LocalDate.of(2026, 7, 18), LocalDate.of(2026, 7, 18))).isZero();
    }

    @Test
    void throwWhenBirthDateIsAfterAssessmentDate() {
        assertThatThrownBy(() -> ageCalculator.calculate(LocalDate.of(2026, 7, 19), LocalDate.of(2026, 7, 18)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode().code())
                        .isEqualTo("PAPS_INVALID_DATE_RANGE"));
    }

    @Test
    void calculateLeapDayBirthday() {
        assertThat(ageCalculator.calculate(LocalDate.of(2008, 2, 29), LocalDate.of(2026, 2, 28))).isEqualTo(17);
        assertThat(ageCalculator.calculate(LocalDate.of(2008, 2, 29), LocalDate.of(2026, 3, 1))).isEqualTo(18);
    }
}
