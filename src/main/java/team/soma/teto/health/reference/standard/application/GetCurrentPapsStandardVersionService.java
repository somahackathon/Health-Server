package team.soma.teto.health.reference.standard.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersionErrorCode;
import team.soma.teto.health.reference.standard.presentation.PapsStandardVersionResponse;
import team.soma.teto.health.reference.standard.repository.PapsStandardVersionRepository;

@Service
@Transactional(readOnly = true)
public class GetCurrentPapsStandardVersionService {

    private final PapsStandardVersionRepository papsStandardVersionRepository;

    public GetCurrentPapsStandardVersionService(PapsStandardVersionRepository papsStandardVersionRepository) {
        this.papsStandardVersionRepository = papsStandardVersionRepository;
    }

    public PapsStandardVersionResponse getCurrentVersion() {
        List<PapsStandardVersion> activeVersions = papsStandardVersionRepository.findAllByActiveTrue();
        if (activeVersions.isEmpty()) {
            throw new BusinessException(PapsStandardVersionErrorCode.PAPS_STANDARD_VERSION_NOT_FOUND);
        }
        if (activeVersions.size() > 1) {
            throw new BusinessException(PapsStandardVersionErrorCode.PAPS_MULTIPLE_ACTIVE_STANDARD_VERSIONS);
        }
        return PapsStandardVersionResponse.from(activeVersions.get(0));
    }
}
