package team.soma.teto.health.reference.standard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import team.soma.teto.health.global.domain.BaseTimeEntity;

@Entity
@Table(name = "paps_standard_version")
public class PapsStandardVersion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private StandardSourceType sourceType;

    @Column(name = "source_name", length = 200)
    private String sourceName;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "official", nullable = false)
    private Boolean official;

    @Column(name = "active", nullable = false)
    private Boolean active;

    protected PapsStandardVersion() {
    }

    private PapsStandardVersion(
            String code,
            String name,
            StandardSourceType sourceType,
            String sourceName,
            String sourceUrl,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Boolean official
    ) {
        if (code == null || code.isBlank() || name == null || name.isBlank() || sourceType == null || official == null) {
            throw new IllegalArgumentException("standard version requires code, name, sourceType, and official");
        }
        if (effectiveFrom != null && effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("effectiveTo cannot be before effectiveFrom");
        }
        this.code = code;
        this.name = name;
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.official = official;
        this.active = true;
    }

    public static PapsStandardVersion create(
            String code,
            String name,
            StandardSourceType sourceType,
            String sourceName,
            String sourceUrl,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Boolean official
    ) {
        return new PapsStandardVersion(code, name, sourceType, sourceName, sourceUrl, effectiveFrom, effectiveTo, official);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public StandardSourceType getSourceType() {
        return sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public Boolean getOfficial() {
        return official;
    }

    public Boolean getActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
