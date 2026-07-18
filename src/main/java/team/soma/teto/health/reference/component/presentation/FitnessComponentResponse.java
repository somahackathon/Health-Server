package team.soma.teto.health.reference.component.presentation;

import java.util.List;
import team.soma.teto.health.reference.component.domain.FitnessComponent;

public record FitnessComponentResponse(List<Component> components) {

    public FitnessComponentResponse {
        components = List.copyOf(components);
    }

    public record Component(
            String code,
            String name,
            String description,
            Integer displayOrder
    ) {

        public static Component from(FitnessComponent component) {
            return new Component(
                    component.getCode().name(),
                    component.getName(),
                    component.getDescription(),
                    component.getDisplayOrder()
            );
        }
    }
}
