package team.soma.teto.health.analysis.job.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.analysis.job.domain.AnalysisStatus;

public interface AiAnalysisJobRepository extends JpaRepository<AiAnalysisJob, Long> {

    Optional<AiAnalysisJob> findByPublicId(UUID publicId);

    Optional<AiAnalysisJob> findByInstallationHashAndPublicId(String installationHash, UUID publicId);

    List<AiAnalysisJob> findAllByStatusAndExpiresAtBefore(AnalysisStatus status, Instant expiresAt);

    @Query("""
            select job
            from AiAnalysisJob job
            where job.expiresAt < :now
              and job.status in :statuses
            order by job.expiresAt asc
            """)
    List<AiAnalysisJob> findExpiredJobsByStatuses(@Param("statuses") List<AnalysisStatus> statuses, @Param("now") Instant now);

    List<AiAnalysisJob> findAllByExpiresAtBefore(Instant expiresAt);

    List<AiAnalysisJob> findAllByInstallationHashOrderByCreatedAtDesc(String installationHash, Pageable pageable);
}
