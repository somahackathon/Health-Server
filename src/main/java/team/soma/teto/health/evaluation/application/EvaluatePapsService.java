package team.soma.teto.health.evaluation.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.soma.teto.health.evaluation.domain.AgeCalculator;
import team.soma.teto.health.evaluation.domain.BmiCategoryEvaluator;
import team.soma.teto.health.evaluation.domain.BmiCalculator;
import team.soma.teto.health.evaluation.domain.MeasurementValueValidator;
import team.soma.teto.health.evaluation.domain.PapsEvaluationErrorCode;
import team.soma.teto.health.evaluation.domain.PapsGradeEvaluator;
import team.soma.teto.health.evaluation.domain.PapsMeasurementValue;
import team.soma.teto.health.evaluation.presentation.PapsEvaluationCompletenessResponse;
import team.soma.teto.health.evaluation.presentation.PapsEvaluationProfileResponse;
import team.soma.teto.health.evaluation.presentation.PapsEvaluationRequest;
import team.soma.teto.health.evaluation.presentation.PapsEvaluationResponse;
import team.soma.teto.health.evaluation.presentation.PapsMeasurementResultResponse;
import team.soma.teto.health.evaluation.presentation.PapsMeasurementRequest;
import team.soma.teto.health.evaluation.presentation.PapsStandardVersionSummary;
import team.soma.teto.health.global.error.BusinessException;
import team.soma.teto.health.reference.component.domain.FitnessComponentCode;
import team.soma.teto.health.reference.standard.domain.BmiCategory;
import team.soma.teto.health.reference.standard.domain.PapsBmiStandard;
import team.soma.teto.health.reference.standard.domain.PapsStandard;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersion;
import team.soma.teto.health.reference.standard.domain.PapsStandardVersionErrorCode;
import team.soma.teto.health.reference.standard.domain.SchoolLevel;
import team.soma.teto.health.reference.standard.repository.PapsBmiStandardRepository;
import team.soma.teto.health.reference.standard.repository.PapsStandardRepository;
import team.soma.teto.health.reference.standard.repository.PapsStandardVersionRepository;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItem;
import team.soma.teto.health.reference.testitem.domain.FitnessTestItemCode;
import team.soma.teto.health.reference.testitem.repository.FitnessTestItemRepository;

@Service
@Transactional(readOnly = true)
public class EvaluatePapsService {

    private static final List<FitnessComponentCode> REQUIRED_COMPONENTS = List.of(
            FitnessComponentCode.CARDIO_ENDURANCE,
            FitnessComponentCode.FLEXIBILITY,
            FitnessComponentCode.MUSCULAR_STRENGTH_ENDURANCE,
            FitnessComponentCode.POWER,
            FitnessComponentCode.BODY_COMPOSITION
    );

    private final FitnessTestItemRepository fitnessTestItemRepository;
    private final PapsStandardVersionRepository papsStandardVersionRepository;
    private final PapsStandardRepository papsStandardRepository;
    private final PapsBmiStandardRepository papsBmiStandardRepository;
    private final Clock clock;
    private final AgeCalculator ageCalculator = new AgeCalculator();
    private final BmiCalculator bmiCalculator = new BmiCalculator();
    private final BmiCategoryEvaluator bmiCategoryEvaluator = new BmiCategoryEvaluator();
    private final MeasurementValueValidator measurementValueValidator = new MeasurementValueValidator();
    private final PapsGradeEvaluator papsGradeEvaluator = new PapsGradeEvaluator();

    public EvaluatePapsService(
            FitnessTestItemRepository fitnessTestItemRepository,
            PapsStandardVersionRepository papsStandardVersionRepository,
            PapsStandardRepository papsStandardRepository,
            PapsBmiStandardRepository papsBmiStandardRepository,
            Clock clock
    ) {
        this.fitnessTestItemRepository = fitnessTestItemRepository;
        this.papsStandardVersionRepository = papsStandardVersionRepository;
        this.papsStandardRepository = papsStandardRepository;
        this.papsBmiStandardRepository = papsBmiStandardRepository;
        this.clock = clock;
    }

    public PapsEvaluationResponse evaluate(PapsEvaluationRequest request) {
        validateDates(request);
        validateHeightAndWeight(request);
        validateMeasurements(request.measurements());

        int age = ageCalculator.calculate(request.birthDate(), request.assessmentDate());
        SchoolLevel schoolLevel = request.schoolLevel();
        PapsStandardVersion standardVersion = getCurrentStandardVersion();
        BigDecimal bmi = bmiCalculator.calculate(request.heightCm(), request.weightKg());

        Map<FitnessTestItemCode, PapsMeasurementRequest> requestMeasurements = toMeasurementMap(request.measurements());
        List<FitnessTestItem> requestedItems = getRequestedItems(requestMeasurements);
        List<PapsMeasurementValue> measurementValues = createMeasurementValues(requestedItems, requestMeasurements);
        measurementValues.add(createBmiMeasurementValue(bmi));
        validateDuplicateComponents(measurementValues);

        Map<FitnessTestItem, List<PapsStandard>> standardsByTestItem = getStandardsByTestItem(
                standardVersion,
                measurementValues.stream()
                        .map(PapsMeasurementValue::testItem)
                        .filter(item -> item.getCode() != FitnessTestItemCode.BMI)
                        .toList(),
                schoolLevel,
                request.gender(),
                request.schoolGrade()
        );
        List<PapsBmiStandard> bmiStandards = papsBmiStandardRepository.findCandidateStandards(
                standardVersion,
                schoolLevel,
                request.schoolGrade(),
                request.gender()
        );
        List<PapsMeasurementResultResponse> measurementResults = measurementValues.stream()
                .map(measurement -> toMeasurementResult(measurement, standardsByTestItem, bmiStandards))
                .toList();

        PapsEvaluationCompletenessResponse completeness = createCompleteness(measurementResults);
        return new PapsEvaluationResponse(
                PapsStandardVersionSummary.from(standardVersion),
                new PapsEvaluationProfileResponse(age, schoolLevel.name(), request.schoolGrade(), request.gender().name(), request.heightCm(), request.weightKg(), bmi),
                completeness,
                measurementResults
        );
    }

    private void validateDates(PapsEvaluationRequest request) {
        if (request.birthDate().isAfter(request.assessmentDate()) || request.assessmentDate().isAfter(LocalDate.now(clock))) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_DATE_RANGE);
        }
    }

    private void validateHeightAndWeight(PapsEvaluationRequest request) {
        if (request.heightCm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_HEIGHT);
        }
        if (request.weightKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_INVALID_WEIGHT);
        }
    }

    private void validateMeasurements(List<PapsMeasurementRequest> measurements) {
        if (measurements.isEmpty()) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_EMPTY_MEASUREMENTS);
        }
    }

    private PapsStandardVersion getCurrentStandardVersion() {
        List<PapsStandardVersion> activeVersions = papsStandardVersionRepository.findAllByActiveTrue();
        if (activeVersions.isEmpty()) {
            throw new BusinessException(PapsStandardVersionErrorCode.PAPS_STANDARD_VERSION_NOT_FOUND);
        }
        if (activeVersions.size() > 1) {
            throw new BusinessException(PapsStandardVersionErrorCode.PAPS_MULTIPLE_ACTIVE_STANDARD_VERSIONS);
        }
        return activeVersions.get(0);
    }

    private Map<FitnessTestItemCode, PapsMeasurementRequest> toMeasurementMap(List<PapsMeasurementRequest> measurements) {
        Map<FitnessTestItemCode, PapsMeasurementRequest> measurementMap = new LinkedHashMap<>();
        for (PapsMeasurementRequest measurement : measurements) {
            if (measurement.testItemCode() == FitnessTestItemCode.BMI) {
                throw new BusinessException(PapsEvaluationErrorCode.PAPS_CLIENT_BMI_NOT_ALLOWED);
            }
            if (measurementMap.putIfAbsent(measurement.testItemCode(), measurement) != null) {
                throw new BusinessException(PapsEvaluationErrorCode.PAPS_DUPLICATE_TEST_ITEM);
            }
        }
        return measurementMap;
    }

    private List<FitnessTestItem> getRequestedItems(Map<FitnessTestItemCode, PapsMeasurementRequest> requestMeasurements) {
        List<FitnessTestItem> items = fitnessTestItemRepository.findAllByCodeIn(new ArrayList<>(requestMeasurements.keySet()));
        Map<FitnessTestItemCode, FitnessTestItem> itemMap = items.stream()
                .collect(Collectors.toMap(FitnessTestItem::getCode, Function.identity()));
        for (FitnessTestItemCode code : requestMeasurements.keySet()) {
            FitnessTestItem item = itemMap.get(code);
            if (item == null) {
                throw new BusinessException(PapsEvaluationErrorCode.PAPS_TEST_ITEM_NOT_FOUND);
            }
            validateActiveItem(item);
        }
        return requestMeasurements.keySet().stream()
                .map(itemMap::get)
                .toList();
    }

    private List<PapsMeasurementValue> createMeasurementValues(
            List<FitnessTestItem> requestedItems,
            Map<FitnessTestItemCode, PapsMeasurementRequest> requestMeasurements
    ) {
        return requestedItems.stream()
                .map(item -> {
                    BigDecimal value = requestMeasurements.get(item.getCode()).value();
                    measurementValueValidator.validate(item, value);
                    return new PapsMeasurementValue(item, value);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PapsMeasurementValue createBmiMeasurementValue(BigDecimal bmi) {
        FitnessTestItem bmiItem = fitnessTestItemRepository.findByCode(FitnessTestItemCode.BMI)
                .orElseThrow(() -> new BusinessException(PapsEvaluationErrorCode.PAPS_BMI_TEST_ITEM_NOT_FOUND));
        validateActiveBmiItem(bmiItem);
        measurementValueValidator.validate(bmiItem, bmi);
        return new PapsMeasurementValue(bmiItem, bmi);
    }

    private void validateActiveItem(FitnessTestItem item) {
        if (!Boolean.TRUE.equals(item.getActive()) || !Boolean.TRUE.equals(item.getComponent().getActive())) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_TEST_ITEM_INACTIVE);
        }
    }

    private void validateActiveBmiItem(FitnessTestItem item) {
        if (!Boolean.TRUE.equals(item.getActive())
                || !Boolean.TRUE.equals(item.getComponent().getActive())
                || item.getComponent().getCode() != FitnessComponentCode.BODY_COMPOSITION) {
            throw new BusinessException(PapsEvaluationErrorCode.PAPS_BMI_TEST_ITEM_NOT_FOUND);
        }
    }

    private void validateDuplicateComponents(List<PapsMeasurementValue> measurements) {
        Map<FitnessComponentCode, FitnessTestItemCode> componentMap = new EnumMap<>(FitnessComponentCode.class);
        for (PapsMeasurementValue measurement : measurements) {
            FitnessComponentCode componentCode = measurement.testItem().getComponent().getCode();
            if (componentMap.putIfAbsent(componentCode, measurement.testItem().getCode()) != null) {
                throw new BusinessException(PapsEvaluationErrorCode.PAPS_DUPLICATE_COMPONENT);
            }
        }
    }

    private Map<FitnessTestItem, List<PapsStandard>> getStandardsByTestItem(
            PapsStandardVersion standardVersion,
            List<FitnessTestItem> testItems,
            SchoolLevel schoolLevel,
            team.soma.teto.health.reference.standard.domain.Gender gender,
            int schoolGrade
    ) {
        if (testItems.isEmpty()) {
            return Map.of();
        }
        return papsStandardRepository.findCandidateStandards(standardVersion, testItems, schoolLevel, schoolGrade, gender)
                .stream()
                .collect(Collectors.groupingBy(PapsStandard::getTestItem));
    }

    private PapsMeasurementResultResponse toMeasurementResult(
            PapsMeasurementValue measurement,
            Map<FitnessTestItem, List<PapsStandard>> standardsByTestItem,
            List<PapsBmiStandard> bmiStandards
    ) {
        if (measurement.testItem().getCode() == FitnessTestItemCode.BMI) {
            BmiCategory category = bmiCategoryEvaluator.evaluate(measurement.value(), bmiStandards);
            return PapsMeasurementResultResponse.from(measurement.testItem(), measurement.value(), null, category.name());
        }
        int grade = papsGradeEvaluator.evaluate(
                measurement.value(),
                standardsByTestItem.getOrDefault(measurement.testItem(), List.of())
        );
        return PapsMeasurementResultResponse.from(measurement.testItem(), measurement.value(), grade);
    }

    private PapsEvaluationCompletenessResponse createCompleteness(List<PapsMeasurementResultResponse> measurementResults) {
        List<String> evaluatedComponents = measurementResults.stream()
                .map(PapsMeasurementResultResponse::component)
                .distinct()
                .toList();
        List<String> missingComponents = REQUIRED_COMPONENTS.stream()
                .map(Enum::name)
                .filter(component -> !evaluatedComponents.contains(component))
                .toList();
        return new PapsEvaluationCompletenessResponse(
                evaluatedComponents.size(),
                REQUIRED_COMPONENTS.size(),
                missingComponents.isEmpty(),
                missingComponents
        );
    }
}
