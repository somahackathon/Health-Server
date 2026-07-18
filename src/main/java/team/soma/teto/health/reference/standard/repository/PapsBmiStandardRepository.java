package team.soma.teto.health.reference.standard.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.soma.teto.health.reference.standard.domain.Gender;
import team.soma.teto.health.reference.standard.domain.PapsBmiStandard;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;
import team.soma.teto.health.reference.standard.domain.SchoolLevel;

public interface PapsBmiStandardRepository extends JpaRepository<PapsBmiStandard, Long> {

    @Query("""
            select standard
            from PapsBmiStandard standard
            where standard.version = :version
              and standard.schoolLevel = :schoolLevel
              and standard.schoolGrade = :schoolGrade
              and standard.gender = :gender
            order by standard.category asc
            """)
    List<PapsBmiStandard> findCandidateStandards(
            @Param("version") PapsStandardVersion version,
            @Param("schoolLevel") SchoolLevel schoolLevel,
            @Param("schoolGrade") int schoolGrade,
            @Param("gender") Gender gender
    );

    List<PapsBmiStandard> findAllByVersion(PapsStandardVersion version);
}
