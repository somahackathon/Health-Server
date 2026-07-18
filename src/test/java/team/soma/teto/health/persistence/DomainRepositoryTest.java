package team.soma.teto.health.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;
import team.soma.teto.health.analysis.job.domain.AnalysisType;
import team.soma.teto.health.analysis.job.repository.AiAnalysisJobRepository;
import team.soma.teto.health.reference.component.domain.FitnessComponent;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.component.repository.FitnessComponentRepository;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.standard.domain.PapsStandard;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;
import team.soma.teto.health.reference.standard.domain.SchoolLevel;
import team.soma.teto.health.reference.standard.domain.StandardSourceType;
import team.soma.teto.health.reference.standard.repository.PapsStandardRepository;
import team.soma.teto.health.reference.standard.repository.PapsStandardVersionRepository;
import team.soma.teto.health.reference.testitem.domain.BetterDirection;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.domain.MeasurementUnit;
import team.soma.teto.health.reference.testitem.domain.MeasurementValueType;
import team.soma.teto.health.reference.testitem.repository.FitnessTestItemRepository;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DomainRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FitnessComponentRepository fitnessComponentRepository;

    @Autowired
    private FitnessTestItemRepository fitnessTestItemRepository;

    @Autowired
    private PapsStandardVersionRepository papsStandardVersionRepository;

    @Autowired
    private PapsStandardRepository papsStandardRepository;

    @Autowired
    private AiAnalysisJobRepository aiAnalysisJobRepository;

    @Test
    void findFitnessComponentByCodeAndActiveOrder() {
        FitnessComponent component = fitnessComponentRepository.findByCode(FitnessComponentCode.CARDIO_ENDURANCE).orElseThrow();

        List<FitnessComponent> activeComponents = fitnessComponentRepository.findAllByActiveTrueOrderByDisplayOrderAsc();

        assertThat(component.getName()).isEqualTo("Cardio Endurance");
        assertThat(activeComponents).extracting(FitnessComponent::getCode)
                .containsExactly(
                        FitnessComponentCode.CARDIO_ENDURANCE,
                        FitnessComponentCode.FLEXIBILITY,
                        FitnessComponentCode.MUSCULAR_STRENGTH_ENDURANCE,
                        FitnessComponentCode.POWER,
                        FitnessComponentCode.BODY_COMPOSITION
                );
        assertThat(fitnessComponentRepository.existsByCode(FitnessComponentCode.POWER)).isTrue();
    }

    @Test
    void preventDuplicateFitnessComponentCode() {
        FitnessComponent duplicate = FitnessComponent.create(FitnessComponentCode.CARDIO_ENDURANCE, "Duplicate", null, 99);

        assertThatThrownBy(() -> {
            fitnessComponentRepository.saveAndFlush(duplicate);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findFitnessTestItemByCodeAndComponent() {
        FitnessComponent cardio = fitnessComponentRepository.findByCode(FitnessComponentCode.CARDIO_ENDURANCE).orElseThrow();

        FitnessTestItem item = fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow();
        List<FitnessTestItem> cardioItems = fitnessTestItemRepository.findActiveItemsByComponent(cardio);

        assertThat(item.getUnit()).isEqualTo(MeasurementUnit.COUNT);
        assertThat(cardioItems).extracting(FitnessTestItem::getCode)
                .containsExactly(FitnessTestItemCode.LONG_RUN_WALK, FitnessTestItemCode.SHUTTLE_RUN, FitnessTestItemCode.STEP_TEST);
        assertThat(fitnessTestItemRepository.findActiveItems()).hasSize(11);
        assertThat(fitnessTestItemRepository.existsByCode(FitnessTestItemCode.BMI)).isTrue();
    }

    @Test
    void mapFitnessTestItemComponentAsLazyAssociation() {
        Long itemId = fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow().getId();
        entityManager.flush();
        entityManager.clear();

        FitnessTestItem item = fitnessTestItemRepository.findById(itemId).orElseThrow();
        PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

        assertThat(persistenceUnitUtil.isLoaded(item, "component")).isFalse();
    }

    @Test
    void findPapsStandardVersionByCodeAndActiveVersions() {
        PapsStandardVersion version = papsStandardVersionRepository.findByCode("HACKATHON_V1").orElseThrow();

        List<PapsStandardVersion> activeVersions = papsStandardVersionRepository.findAllByActiveTrue();

        assertThat(version.getSourceType()).isEqualTo(StandardSourceType.INTERNAL);
        assertThat(version.getOfficial()).isFalse();
        assertThat(activeVersions).extracting(PapsStandardVersion::getCode).containsExactly("PAPS_OFFICIAL_2025_V1");
        assertThat(papsStandardVersionRepository.existsByCode("HACKATHON_V1")).isTrue();
    }

    @Test
    void preventDuplicatePapsStandardVersionCode() {
        PapsStandardVersion duplicate = PapsStandardVersion.create(
                "HACKATHON_V1",
                "Duplicate",
                StandardSourceType.INTERNAL,
                null,
                null,
                LocalDate.now(),
                null,
                false
        );

        assertThatThrownBy(() -> papsStandardVersionRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findPapsStandardCandidatesBySchoolGrade() {
        PapsStandardVersion version = papsStandardVersionRepository.save(PapsStandardVersion.create(
                "TEST_REPOSITORY_V1",
                "Test Repository V1",
                StandardSourceType.INTERNAL,
                null,
                null,
                null,
                null,
                false
        ));
        FitnessTestItem item = fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow();
        PapsStandard standard = PapsStandard.create(
                version,
                item,
                SchoolLevel.MIDDLE,
                2,
                Gender.MALE,
                13,
                15,
                1,
                new BigDecimal("60.00"),
                null,
                true,
                false
        );
        papsStandardRepository.saveAndFlush(standard);

        List<PapsStandard> candidates = papsStandardRepository.findCandidateStandards(version, item, SchoolLevel.MIDDLE, 2, Gender.MALE);
        List<PapsStandard> outsideCandidates = papsStandardRepository.findCandidateStandards(version, item, SchoolLevel.MIDDLE, 1, Gender.MALE);
        List<PapsStandard> gradeCandidates = papsStandardRepository.findGradeRangeCandidates(version, item, SchoolLevel.MIDDLE, 2, Gender.MALE, 1);

        assertThat(candidates).hasSize(1);
        assertThat(outsideCandidates).isEmpty();
        assertThat(gradeCandidates).hasSize(1);
        assertThat(papsStandardRepository.findAllByVersion(version)).hasSize(1);
        assertThat(papsStandardRepository.findAllByTestItem(item)).isNotEmpty();
    }

    @Test
    void preventDuplicatePapsStandardRangeGrade() {
        PapsStandardVersion version = papsStandardVersionRepository.findByCode("HACKATHON_V1").orElseThrow();
        FitnessTestItem item = fitnessTestItemRepository.findByCode(FitnessTestItemCode.SHUTTLE_RUN).orElseThrow();
        PapsStandard first = createStandard(version, item, 1);
        PapsStandard duplicate = createStandard(version, item, 1);
        papsStandardRepository.saveAndFlush(first);

        assertThatThrownBy(() -> papsStandardRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAiAnalysisJobByPublicIdAndExpiration() {
        Instant now = Instant.parse("2026-07-18T00:00:00Z");
        AiAnalysisJob expired = AiAnalysisJob.create(UUID.randomUUID(), "install-a", AnalysisType.FITNESS, "{\"value\":1}", now.minusSeconds(1));
        AiAnalysisJob pending = AiAnalysisJob.create(UUID.randomUUID(), "install-a", AnalysisType.POSTURE, null, now.plusSeconds(3600));
        AiAnalysisJob processing = AiAnalysisJob.create(UUID.randomUUID(), "install-b", AnalysisType.FITNESS, null, now.minusSeconds(10));
        processing.start(now.minusSeconds(20));
        aiAnalysisJobRepository.saveAllAndFlush(List.of(expired, pending, processing));

        assertThat(aiAnalysisJobRepository.findByPublicId(expired.getPublicId())).isPresent();
        assertThat(aiAnalysisJobRepository.findByInstallationHashAndPublicId("install-a", pending.getPublicId())).isPresent();
        assertThat(aiAnalysisJobRepository.findAllByStatusAndExpiresAtBefore(AnalysisStatus.PENDING, now))
                .extracting(AiAnalysisJob::getPublicId)
                .containsExactly(expired.getPublicId());
        assertThat(aiAnalysisJobRepository.findExpiredJobsByStatuses(List.of(AnalysisStatus.PENDING, AnalysisStatus.PROCESSING), now, PageRequest.of(0, 10)))
                .extracting(AiAnalysisJob::getPublicId)
                .containsExactly(processing.getPublicId(), expired.getPublicId());
        assertThat(aiAnalysisJobRepository.findExpiredJobsWithPayloads(now, PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void findRecentAiAnalysisJobsByInstallationHash() throws InterruptedException {
        Instant now = Instant.parse("2026-07-18T00:00:00Z");
        AiAnalysisJob first = aiAnalysisJobRepository.saveAndFlush(AiAnalysisJob.create("install-recent", AnalysisType.FITNESS, null, now.plusSeconds(3600)));
        Thread.sleep(20);
        AiAnalysisJob second = aiAnalysisJobRepository.saveAndFlush(AiAnalysisJob.create("install-recent", AnalysisType.POSTURE, null, now.plusSeconds(3600)));

        List<AiAnalysisJob> recentJobs = aiAnalysisJobRepository.findAllByInstallationHashOrderByCreatedAtDesc("install-recent", PageRequest.of(0, 2));

        assertThat(recentJobs).extracting(AiAnalysisJob::getPublicId)
                .containsExactly(second.getPublicId(), first.getPublicId());
    }

    private PapsStandard createStandard(PapsStandardVersion version, FitnessTestItem item, int grade) {
        return PapsStandard.create(
                version,
                item,
                SchoolLevel.MIDDLE,
                Gender.FEMALE,
                13,
                15,
                grade,
                BigDecimal.ONE,
                BigDecimal.TEN,
                true,
                true
        );
    }
}
