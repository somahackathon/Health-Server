package team.soma.teto.health.reference.standard.domain;

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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import team.soma.teto.health.global.domain.BaseTimeEntity;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;

@Entity
@Table(
        name = "paps_standard",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_paps_standard_range_grade",
                        columnNames = {"version_id", "test_item_id", "school_level", "gender", "minimum_age", "maximum_age", "grade"}
                )
        }
)
public class PapsStandard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private PapsStandardVersion version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_item_id", nullable = false)
    private FitnessTestItem testItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "school_level", nullable = false, length = 30)
    private SchoolLevel schoolLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Column(name = "minimum_age", nullable = false)
    private Integer minimumAge;

    @Column(name = "maximum_age", nullable = false)
    private Integer maximumAge;

    @Column(name = "grade", nullable = false)
    private Integer grade;

    @Column(name = "minimum_value", precision = 10, scale = 2)
    private BigDecimal minimumValue;

    @Column(name = "maximum_value", precision = 10, scale = 2)
    private BigDecimal maximumValue;

    @Column(name = "minimum_inclusive", nullable = false)
    private Boolean minimumInclusive;

    @Column(name = "maximum_inclusive", nullable = false)
    private Boolean maximumInclusive;

    protected PapsStandard() {
    }

    private PapsStandard(
            PapsStandardVersion version,
            FitnessTestItem testItem,
            SchoolLevel schoolLevel,
            Gender gender,
            Integer minimumAge,
            Integer maximumAge,
            Integer grade,
            BigDecimal minimumValue,
            BigDecimal maximumValue,
            Boolean minimumInclusive,
            Boolean maximumInclusive
    ) {
        validate(version, testItem, schoolLevel, gender, minimumAge, maximumAge, grade, minimumValue, maximumValue, minimumInclusive, maximumInclusive);
        this.version = version;
        this.testItem = testItem;
        this.schoolLevel = schoolLevel;
        this.gender = gender;
        this.minimumAge = minimumAge;
        this.maximumAge = maximumAge;
        this.grade = grade;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.minimumInclusive = minimumInclusive;
        this.maximumInclusive = maximumInclusive;
    }

    public static PapsStandard create(
            PapsStandardVersion version,
            FitnessTestItem testItem,
            SchoolLevel schoolLevel,
            Gender gender,
            Integer minimumAge,
            Integer maximumAge,
            Integer grade,
            BigDecimal minimumValue,
            BigDecimal maximumValue,
            Boolean minimumInclusive,
            Boolean maximumInclusive
    ) {
        return new PapsStandard(version, testItem, schoolLevel, gender, minimumAge, maximumAge, grade, minimumValue, maximumValue, minimumInclusive, maximumInclusive);
    }

    private static void validate(
            PapsStandardVersion version,
            FitnessTestItem testItem,
            SchoolLevel schoolLevel,
            Gender gender,
            Integer minimumAge,
            Integer maximumAge,
            Integer grade,
            BigDecimal minimumValue,
            BigDecimal maximumValue,
            Boolean minimumInclusive,
            Boolean maximumInclusive
    ) {
        if (version == null || testItem == null || schoolLevel == null || gender == null || minimumAge == null || maximumAge == null || grade == null || minimumInclusive == null || maximumInclusive == null) {
            throw new IllegalArgumentException("paps standard requires version, testItem, schoolLevel, gender, age range, grade, and inclusive flags");
        }
        if (minimumAge < 0 || maximumAge < minimumAge) {
            throw new IllegalArgumentException("age range is invalid");
        }
        if (grade < 1 || grade > 5) {
            throw new IllegalArgumentException("grade must be between 1 and 5");
        }
        if (minimumValue != null && maximumValue != null && minimumValue.compareTo(maximumValue) > 0) {
            throw new IllegalArgumentException("minimumValue cannot be greater than maximumValue");
        }
    }

    public Long getId() {
        return id;
    }

    public PapsStandardVersion getVersion() {
        return version;
    }

    public FitnessTestItem getTestItem() {
        return testItem;
    }

    public SchoolLevel getSchoolLevel() {
        return schoolLevel;
    }

    public Gender getGender() {
        return gender;
    }

    public Integer getMinimumAge() {
        return minimumAge;
    }

    public Integer getMaximumAge() {
        return maximumAge;
    }

    public Integer getGrade() {
        return grade;
    }

    public BigDecimal getMinimumValue() {
        return minimumValue;
    }

    public BigDecimal getMaximumValue() {
        return maximumValue;
    }

    public Boolean getMinimumInclusive() {
        return minimumInclusive;
    }

    public Boolean getMaximumInclusive() {
        return maximumInclusive;
    }
}
