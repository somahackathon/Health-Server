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
              and standard.schoolGrade = :schoolGrade
              and standard.gender = :gender
            order by standard.grade asc
            """)
    List<PapsStandard> findCandidateStandards(
            @Param("version") PapsStandardVersion version,
            @Param("testItem") FitnessTestItem testItem,
            @Param("schoolLevel") SchoolLevel schoolLevel,
            @Param("schoolGrade") int schoolGrade,
            @Param("gender") Gender gender
    );

    @Query("""
            select standard
            from PapsStandard standard
            join fetch standard.testItem testItem
            where standard.version = :version
              and standard.testItem in :testItems
              and standard.schoolLevel = :schoolLevel
              and standard.schoolGrade = :schoolGrade
              and standard.gender = :gender
            order by testItem.id asc, standard.grade asc
            """)
    List<PapsStandard> findCandidateStandards(
            @Param("version") PapsStandardVersion version,
            @Param("testItems") List<FitnessTestItem> testItems,
            @Param("schoolLevel") SchoolLevel schoolLevel,
            @Param("schoolGrade") int schoolGrade,
            @Param("gender") Gender gender
    );

    List<PapsStandard> findAllByVersion(PapsStandardVersion version);

    List<PapsStandard> findAllByTestItem(FitnessTestItem testItem);

    @Query("""
            select standard
            from PapsStandard standard
            where standard.version = :version
              and standard.testItem = :testItem
              and standard.schoolLevel = :schoolLevel
              and standard.schoolGrade = :schoolGrade
              and standard.gender = :gender
              and standard.grade = :grade
            """)
    List<PapsStandard> findGradeRangeCandidates(
            @Param("version") PapsStandardVersion version,
            @Param("testItem") FitnessTestItem testItem,
            @Param("schoolLevel") SchoolLevel schoolLevel,
            @Param("schoolGrade") int schoolGrade,
            @Param("gender") Gender gender,
            @Param("grade") int grade
    );
}
