package team.soma.teto.health.reference.component.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import team.soma.teto.health.global.domain.BaseTimeEntity;

@Entity
@Table(name = "fitness_component")
public class FitnessComponent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private FitnessComponentCode code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "active", nullable = false)
    private Boolean active;

    protected FitnessComponent() {
    }

    private FitnessComponent(FitnessComponentCode code, String name, String description, Integer displayOrder) {
        if (code == null || name == null || name.isBlank() || displayOrder == null) {
            throw new IllegalArgumentException("fitness component requires code, name, and displayOrder");
        }
        this.code = code;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    public static FitnessComponent create(FitnessComponentCode code, String name, String description, Integer displayOrder) {
        return new FitnessComponent(code, name, description, displayOrder);
    }

    public Long getId() {
        return id;
    }

    public FitnessComponentCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
