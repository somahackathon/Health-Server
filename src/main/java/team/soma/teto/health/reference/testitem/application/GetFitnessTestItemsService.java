package team.soma.teto.health.reference.testitem.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.component.domain.FitnessComponent;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.component.domain.FitnessComponentErrorCode;
import team.soma.teto.health.reference.component.repository.FitnessComponentRepository;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;
import team.soma.teto.health.reference.testitem.presentation.FitnessTestItemResponse;
import team.soma.teto.health.reference.testitem.repository.FitnessTestItemRepository;

@Service
@Transactional(readOnly = true)
public class GetFitnessTestItemsService {

    private final FitnessTestItemRepository fitnessTestItemRepository;
    private final FitnessComponentRepository fitnessComponentRepository;

    public GetFitnessTestItemsService(
            FitnessTestItemRepository fitnessTestItemRepository,
            FitnessComponentRepository fitnessComponentRepository
    ) {
        this.fitnessTestItemRepository = fitnessTestItemRepository;
        this.fitnessComponentRepository = fitnessComponentRepository;
    }

    public FitnessTestItemResponse getTestItems(FitnessComponentCode componentCode) {
        List<FitnessTestItem> testItems = componentCode == null
                ? fitnessTestItemRepository.findActiveItems()
                : fitnessTestItemRepository.findActiveItemsByComponent(getActiveComponent(componentCode));
        return new FitnessTestItemResponse(testItems.stream()
                .map(FitnessTestItemResponse.TestItem::from)
                .toList());
    }

    private FitnessComponent getActiveComponent(FitnessComponentCode componentCode) {
        FitnessComponent component = fitnessComponentRepository.findByCode(componentCode)
                .orElseThrow(() -> new BusinessException(FitnessComponentErrorCode.PAPS_COMPONENT_NOT_FOUND));
        if (!Boolean.TRUE.equals(component.getActive())) {
            throw new BusinessException(FitnessComponentErrorCode.PAPS_COMPONENT_NOT_FOUND);
        }
        return component;
    }
}
