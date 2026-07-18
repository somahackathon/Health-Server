package team.soma.teto.health.reference.standard.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;

public interface PapsStandardVersionRepository extends JpaRepository<PapsStandardVersion, Long> {

    Optional<PapsStandardVersion> findByCode(String code);

    List<PapsStandardVersion> findAllByActiveTrue();

    boolean existsByCode(String code);
}
