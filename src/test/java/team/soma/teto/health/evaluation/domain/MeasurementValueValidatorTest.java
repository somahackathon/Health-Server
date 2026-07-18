package team.soma.teto.health.evaluation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.component.domain.FitnessComponent;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.testitem.domain.BetterDirection;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;
import team.soma.teto.health.reference.testitem.domain.MeasurementValueType;

class MeasurementValueValidatorTest {

    private final MeasurementValueValidator validator = new MeasurementValueValidator();

    @Test
    void acceptIntegerValue() {
        validator.validate(createItem(MeasurementValueType.INTEGER, null, null, 0), new BigDecimal("52"));
    }

    @Test
    void acceptDecimalValue() {
        validator.validate(createItem(MeasurementValueType.DECIMAL, null, null, 1), new BigDecimal("14.5"));
    }

    @Test
    void rejectDecimalValueForIntegerItem() {
        assertError(() -> validator.validate(createItem(MeasurementValueType.INTEGER, null, null, 0), new BigDecimal("52.1")),
                "PAPS_INVALID_MEASUREMENT_VALUE");
    }

    @Test
    void rejectValueUnderMinimumInput() {
        assertError(() -> validator.validate(createItem(MeasurementValueType.DECIMAL, new BigDecimal("10"), null, 1), new BigDecimal("9.9")),
                "PAPS_INVALID_MEASUREMENT_VALUE");
    }

    @Test
    void rejectValueOverMaximumInput() {
        assertError(() -> validator.validate(createItem(MeasurementValueType.DECIMAL, null, new BigDecimal("20"), 1), new BigDecimal("20.1")),
                "PAPS_INVALID_MEASUREMENT_VALUE");
    }

    @Test
    void rejectExceededDecimalScale() {
        assertError(() -> validator.validate(createItem(MeasurementValueType.DECIMAL, null, null, 1), new BigDecimal("14.55")),
                "PAPS_INVALID_DECIMAL_SCALE");
    }

    private FitnessTestItem createItem(MeasurementValueType valueType, BigDecimal minimumInput, BigDecimal maximumInput, int decimalScale) {
        FitnessComponent component = FitnessComponent.create(FitnessComponentCode.CARDIO_ENDURANCE, "Cardio", null, 1);
        return FitnessTestItem.create(
                component,
                FitnessTestItemCode.SHUTTLE_RUN,
                "Shuttle Run",
                MeasurementUnit.COUNT,
                valueType,
                BetterDirection.HIGHER,
                minimumInput,
                maximumInput,
                decimalScale
        );
    }

    private void assertError(Runnable runnable, String code) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode().code()).isEqualTo(code));
    }
}
