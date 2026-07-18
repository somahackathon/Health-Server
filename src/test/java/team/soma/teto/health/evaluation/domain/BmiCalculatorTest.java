package team.soma.teto.health.evaluation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import team.soma.teto.health.global.error.BusinessException;

class BmiCalculatorTest {

    private final BmiCalculator bmiCalculator = new BmiCalculator();

    @Test
    void calculateBmiWithCentimeterHeight() {
        assertThat(bmiCalculator.calculate(new BigDecimal("175.2"), new BigDecimal("65.4")))
                .isEqualByComparingTo("21.3");
    }

    @Test
    void roundBmiHalfUpToOneDecimalPlace() {
        assertThat(bmiCalculator.calculate(new BigDecimal("170"), new BigDecimal("66.0")))
                .isEqualByComparingTo("22.8");
    }

    @Test
    void keepEnoughPrecisionForSmallDecimalInputs() {
        assertThat(bmiCalculator.calculate(new BigDecimal("160.5"), new BigDecimal("50.2")))
                .isEqualByComparingTo("19.5");
    }

    @Test
    void rejectZeroOrNegativeHeight() {
        assertThatThrownBy(() -> bmiCalculator.calculate(BigDecimal.ZERO, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode().code())
                        .isEqualTo("PAPS_INVALID_HEIGHT"));
    }

    @Test
    void rejectZeroOrNegativeWeight() {
        assertThatThrownBy(() -> bmiCalculator.calculate(BigDecimal.TEN, BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode().code())
                        .isEqualTo("PAPS_INVALID_WEIGHT"));
    }
}
