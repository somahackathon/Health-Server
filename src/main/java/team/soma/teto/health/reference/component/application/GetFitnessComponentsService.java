package team.soma.teto.health.reference.component.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.reference.component.presentation.FitnessComponentResponse;
import team.soma.teto.health.reference.component.repository.FitnessComponentRepository;

@Service
@Transactional(readOnly = true)
public class GetFitnessComponentsService {

    private final FitnessComponentRepository fitnessComponentRepository;

    public GetFitnessComponentsService(FitnessComponentRepository fitnessComponentRepository) {
        this.fitnessComponentRepository = fitnessComponentRepository;
    }

    public FitnessComponentResponse getComponents() {
        List<FitnessComponentResponse.Component> components = fitnessComponentRepository.findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(FitnessComponentResponse.Component::from)
                .toList();
        return new FitnessComponentResponse(components);
    }
}
