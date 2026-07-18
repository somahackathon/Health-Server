INSERT INTO fitness_component (code, name, description, display_order, active, created_at, updated_at) VALUES
('CARDIO_ENDURANCE', 'Cardio Endurance', NULL, 1, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('FLEXIBILITY', 'Flexibility', NULL, 2, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('MUSCULAR_STRENGTH_ENDURANCE', 'Muscular Strength Endurance', NULL, 3, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('POWER', 'Power', NULL, 4, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('BODY_COMPOSITION', 'Body Composition', NULL, 5, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

INSERT INTO fitness_test_item (
    component_id,
    code,
    name,
    unit,
    value_type,
    better_direction,
    minimum_input,
    maximum_input,
    decimal_scale,
    active,
    created_at,
    updated_at
) VALUES
((SELECT id FROM fitness_component WHERE code = 'CARDIO_ENDURANCE'), 'SHUTTLE_RUN', 'Shuttle Run', 'COUNT', 'INTEGER', 'HIGHER', NULL, NULL, 0, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'CARDIO_ENDURANCE'), 'LONG_RUN_WALK', 'Long Run Walk', 'SECOND', 'DECIMAL', 'LOWER', NULL, NULL, 2, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'CARDIO_ENDURANCE'), 'STEP_TEST', 'Step Test', 'SCORE', 'DECIMAL', 'HIGHER', NULL, NULL, 2, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'FLEXIBILITY'), 'SIT_AND_REACH', 'Sit And Reach', 'CENTIMETER', 'DECIMAL', 'HIGHER', NULL, NULL, 1, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'FLEXIBILITY'), 'TOTAL_FLEXIBILITY', 'Total Flexibility', 'SCORE', 'INTEGER', 'HIGHER', NULL, NULL, 0, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'MUSCULAR_STRENGTH_ENDURANCE'), 'PUSH_UP', 'Push Up', 'COUNT', 'INTEGER', 'HIGHER', NULL, NULL, 0, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'MUSCULAR_STRENGTH_ENDURANCE'), 'CURL_UP', 'Curl Up', 'COUNT', 'INTEGER', 'HIGHER', NULL, NULL, 0, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'MUSCULAR_STRENGTH_ENDURANCE'), 'GRIP_STRENGTH', 'Grip Strength', 'KILOGRAM', 'DECIMAL', 'HIGHER', NULL, NULL, 1, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'POWER'), 'SPRINT_50M', '50m Sprint', 'SECOND', 'DECIMAL', 'LOWER', NULL, NULL, 2, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'POWER'), 'STANDING_LONG_JUMP', 'Standing Long Jump', 'CENTIMETER', 'DECIMAL', 'HIGHER', NULL, NULL, 1, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'BODY_COMPOSITION'), 'BMI', 'BMI', 'BMI', 'DECIMAL', 'RANGE', NULL, NULL, 2, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
((SELECT id FROM fitness_component WHERE code = 'BODY_COMPOSITION'), 'BODY_FAT_PERCENTAGE', 'Body Fat Percentage', 'PERCENT', 'DECIMAL', 'RANGE', NULL, NULL, 1, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

INSERT INTO paps_standard_version (
    code,
    name,
    source_type,
    source_name,
    source_url,
    effective_from,
    effective_to,
    official,
    active,
    created_at,
    updated_at
) VALUES (
    'HACKATHON_V1',
    'Hackathon Internal Standard V1',
    'INTERNAL',
    'Hackathon team internal placeholder',
    NULL,
    NULL,
    NULL,
    0,
    1,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);
