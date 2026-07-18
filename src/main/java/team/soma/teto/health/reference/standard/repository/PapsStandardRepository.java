package team.soma.teto.health.reference.standard.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.standard.domain.PapsStandard;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;
import team.soma.teto.health.reference.standard.domain.SchoolLevel;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;

public interface PapsStandardRepository extends JpaRepository<PapsStandard, Long> {

    @Query("""
            select standard
            from PapsStandard standard
            where standard.version = :version
              and standard.testItem = :testItem
              and standard.schoolLevel = :schoolLevel
              and standard.gender = :gender
              and standard.minimumAge <= :age
              and standard.maximumAge >= :age
            order by standard.grade asc
            """)
    List<PapsStandard> findCandidateStandards(
            @Param("version") PapsStandardVersion version,
            @Param("testItem") FitnessTestItem testItem,
            @Param("schoolLevel") SchoolLevel schoolLevel,
            @Param("gender") Gender gender,
            @Param("age") int age
    );

    List<PapsStandard> findAllByVersion(PapsStandardVersion version);

    List<PapsStandard> findAllByTestItem(FitnessTestItem testItem);

    @Query("""
            select standard
            from PapsStandard standard
            where standard.version = :version
              and standard.testItem = :testItem
              and standard.schoolLevel = :schoolLevel
              and standard.gender = :gender
              and standard.grade = :grade
              and standard.minimumAge <= :age
              and standard.maximumAge >= :age
            """)
    List<PapsStandard> findGradeRangeCandidates(
            @Param("version") PapsStandardVersion version,
            @Param("testItem") FitnessTestItem testItem,
            @Param("schoolLevel") SchoolLevel schoolLevel,
            @Param("gender") Gender gender,
            @Param("grade") int grade,
            @Param("age") int age
    );
}
