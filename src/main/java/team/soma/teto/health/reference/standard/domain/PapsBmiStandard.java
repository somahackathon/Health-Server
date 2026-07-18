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

@Entity
@Table(
        name = "paps_bmi_standard",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_paps_bmi_standard_category",
                        columnNames = {"version_id", "school_level", "school_grade", "gender", "category"}
                )
        }
)
public class PapsBmiStandard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private PapsStandardVersion version;

    @Enumerated(EnumType.STRING)
    @Column(name = "school_level", nullable = false, length = 30)
    private SchoolLevel schoolLevel;

    @Column(name = "school_grade", nullable = false)
    private Integer schoolGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private BmiCategory category;

    @Column(name = "minimum_value", precision = 10, scale = 2)
    private BigDecimal minimumValue;

    @Column(name = "maximum_value", precision = 10, scale = 2)
    private BigDecimal maximumValue;

    @Column(name = "minimum_inclusive", nullable = false)
    private Boolean minimumInclusive;

    @Column(name = "maximum_inclusive", nullable = false)
    private Boolean maximumInclusive;

    protected PapsBmiStandard() {
    }

    private PapsBmiStandard(
            PapsStandardVersion version,
            SchoolLevel schoolLevel,
            Integer schoolGrade,
            Gender gender,
            BmiCategory category,
            BigDecimal minimumValue,
            BigDecimal maximumValue,
            Boolean minimumInclusive,
            Boolean maximumInclusive
    ) {
        validate(version, schoolLevel, schoolGrade, gender, category, minimumValue, maximumValue, minimumInclusive, maximumInclusive);
        this.version = version;
        this.schoolLevel = schoolLevel;
        this.schoolGrade = schoolGrade;
        this.gender = gender;
        this.category = category;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.minimumInclusive = minimumInclusive;
        this.maximumInclusive = maximumInclusive;
    }

    public static PapsBmiStandard create(
            PapsStandardVersion version,
            SchoolLevel schoolLevel,
            Integer schoolGrade,
            Gender gender,
            BmiCategory category,
            BigDecimal minimumValue,
            BigDecimal maximumValue,
            Boolean minimumInclusive,
            Boolean maximumInclusive
    ) {
        return new PapsBmiStandard(version, schoolLevel, schoolGrade, gender, category, minimumValue, maximumValue, minimumInclusive, maximumInclusive);
    }

    private static void validate(
            PapsStandardVersion version,
            SchoolLevel schoolLevel,
            Integer schoolGrade,
            Gender gender,
            BmiCategory category,
            BigDecimal minimumValue,
            BigDecimal maximumValue,
            Boolean minimumInclusive,
            Boolean maximumInclusive
    ) {
        if (version == null || schoolLevel == null || schoolGrade == null || gender == null || category == null || minimumInclusive == null || maximumInclusive == null) {
            throw new IllegalArgumentException("bmi standard requires version, schoolLevel, schoolGrade, gender, category, and inclusive flags");
        }
        if (schoolGrade < 1 || schoolGrade > 6) {
            throw new IllegalArgumentException("schoolGrade must be between 1 and 6");
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

    public SchoolLevel getSchoolLevel() {
        return schoolLevel;
    }

    public Integer getSchoolGrade() {
        return schoolGrade;
    }

    public Gender getGender() {
        return gender;
    }

    public BmiCategory getCategory() {
        return category;
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
