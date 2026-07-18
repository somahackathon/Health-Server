package team.soma.teto.health.reference.standard.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import team.soma.teto.health.reference.component.domain.FitnessComponent;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.testitem.domain.BetterDirection;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;
import team.soma.teto.health.reference.testitem.domain.MeasurementValueType;

class PapsStandardTest {

    private final PapsStandardVersion version = PapsStandardVersion.create(
            "TEST_VERSION",
            "Test Version",
            StandardSourceType.INTERNAL,
            null,
            null,
            null,
            null,
            false
    );
    private final FitnessComponent component = FitnessComponent.create(FitnessComponentCode.CARDIO_ENDURANCE, "Cardio", null, 1);
    private final FitnessTestItem testItem = FitnessTestItem.create(
            component,
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
    void rejectInvalidAgeRange() {
        assertThatThrownBy(() -> createStandard(15, 14, 1, BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectInvalidGrade() {
        assertThatThrownBy(() -> createStandard(13, 15, 6, BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectInvalidValueRange() {
        assertThatThrownBy(() -> createStandard(13, 15, 1, BigDecimal.TEN, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private PapsStandard createStandard(Integer minimumAge, Integer maximumAge, Integer grade, BigDecimal minimumValue, BigDecimal maximumValue) {
        return PapsStandard.create(
                version,
                testItem,
                SchoolLevel.MIDDLE,
                Gender.MALE,
                minimumAge,
                maximumAge,
                grade,
                minimumValue,
                maximumValue,
                true,
                true
        );
    }
}
