package team.soma.teto.health.reference.component.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import team.soma.teto.health.reference.component.domain.FitnessComponent;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;

public interface FitnessComponentRepository extends JpaRepository<FitnessComponent, Long> {

    Optional<FitnessComponent> findByCode(FitnessComponentCode code);

    List<FitnessComponent> findAllByActiveTrueOrderByDisplayOrderAsc();

    boolean existsByCode(FitnessComponentCode code);
}
