package team.soma.teto.health.reference.testitem.presentation;

import java.math.BigDecimal;
import java.util.List;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;

public record FitnessTestItemResponse(List<TestItem> testItems) {

    public FitnessTestItemResponse {
        testItems = List.copyOf(testItems);
    }

    public record TestItem(
            String componentCode,
            String code,
            String name,
            String unit,
            String valueType,
            String betterDirection,
            BigDecimal minimumInput,
            BigDecimal maximumInput,
            Integer decimalScale
    ) {

        public static TestItem from(FitnessTestItem testItem) {
            return new TestItem(
                    testItem.getComponent().getCode().name(),
                    testItem.getCode().name(),
                    testItem.getName(),
                    testItem.getUnit().name(),
                    testItem.getValueType().name(),
                    testItem.getBetterDirection().name(),
                    testItem.getMinimumInput(),
                    testItem.getMaximumInput(),
                    testItem.getDecimalScale()
            );
        }
    }
}
