package team.soma.teto.health.reference.testitem.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.soma.teto.health.reference.component.domain.FitnessComponent;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;

public interface FitnessTestItemRepository extends JpaRepository<FitnessTestItem, Long> {

    Optional<FitnessTestItem> findByCode(FitnessTestItemCode code);

    @Query("""
            select item
            from FitnessTestItem item
            join item.component component
            where item.active = true
            order by component.displayOrder asc, item.id asc
            """)
    List<FitnessTestItem> findActiveItems();

    @Query("""
            select item
            from FitnessTestItem item
            where item.component = :component
              and item.active = true
            order by item.id asc
            """)
    List<FitnessTestItem> findActiveItemsByComponent(@Param("component") FitnessComponent component);

    @Query("""
            select item
            from FitnessTestItem item
            join item.component component
            order by component.displayOrder asc, item.id asc
            """)
    List<FitnessTestItem> findAllOrderByComponentDisplayOrder();

    boolean existsByCode(FitnessTestItemCode code);
}
