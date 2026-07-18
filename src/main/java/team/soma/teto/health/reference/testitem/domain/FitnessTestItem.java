package team.soma.teto.health.reference.testitem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import team.soma.teto.health.global.domain.BaseTimeEntity;
import team.soma.teto.health.reference.component.domain.FitnessComponent;

@Entity
@Table(name = "fitness_test_item")
public class FitnessTestItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_id", nullable = false)
    private FitnessComponent component;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private FitnessTestItemCode code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 30)
    private MeasurementUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 30)
    private MeasurementValueType valueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "better_direction", nullable = false, length = 30)
    private BetterDirection betterDirection;

    @Column(name = "minimum_input", precision = 10, scale = 2)
    private BigDecimal minimumInput;

    @Column(name = "maximum_input", precision = 10, scale = 2)
    private BigDecimal maximumInput;

    @Column(name = "decimal_scale", nullable = false)
    private Integer decimalScale;

    @Column(name = "active", nullable = false)
    private Boolean active;

    protected FitnessTestItem() {
    }

    private FitnessTestItem(
            FitnessComponent component,
            FitnessTestItemCode code,
            String name,
            MeasurementUnit unit,
            MeasurementValueType valueType,
            BetterDirection betterDirection,
            BigDecimal minimumInput,
            BigDecimal maximumInput,
            Integer decimalScale
    ) {
        if (component == null || code == null || name == null || name.isBlank() || unit == null || valueType == null || betterDirection == null) {
            throw new IllegalArgumentException("fitness test item requires component, code, name, unit, valueType, and betterDirection");
        }
        if (decimalScale == null || decimalScale < 0) {
            throw new IllegalArgumentException("decimalScale must be greater than or equal to 0");
        }
        if (minimumInput != null && maximumInput != null && minimumInput.compareTo(maximumInput) > 0) {
            throw new IllegalArgumentException("minimumInput cannot be greater than maximumInput");
        }
        this.component = component;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.valueType = valueType;
        this.betterDirection = betterDirection;
        this.minimumInput = minimumInput;
        this.maximumInput = maximumInput;
        this.decimalScale = decimalScale;
        this.active = true;
    }

    public static FitnessTestItem create(
            FitnessComponent component,
            FitnessTestItemCode code,
            String name,
            MeasurementUnit unit,
            MeasurementValueType valueType,
            BetterDirection betterDirection,
            BigDecimal minimumInput,
            BigDecimal maximumInput,
            Integer decimalScale
    ) {
        return new FitnessTestItem(component, code, name, unit, valueType, betterDirection, minimumInput, maximumInput, decimalScale);
    }

    public Long getId() {
        return id;
    }

    public FitnessComponent getComponent() {
        return component;
    }

    public FitnessTestItemCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public MeasurementUnit getUnit() {
        return unit;
    }

    public MeasurementValueType getValueType() {
        return valueType;
    }

    public BetterDirection getBetterDirection() {
        return betterDirection;
    }

    public BigDecimal getMinimumInput() {
        return minimumInput;
    }

    public BigDecimal getMaximumInput() {
        return maximumInput;
    }

    public Integer getDecimalScale() {
        return decimalScale;
    }

    public Boolean getActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
