CREATE TABLE fitness_component (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    display_order INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_fitness_component_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fitness_test_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    component_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    value_type VARCHAR(30) NOT NULL,
    better_direction VARCHAR(30) NOT NULL,
    minimum_input DECIMAL(10, 2) NULL,
    maximum_input DECIMAL(10, 2) NULL,
    decimal_scale INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_fitness_test_item_code UNIQUE (code),
    CONSTRAINT fk_fitness_test_item_component FOREIGN KEY (component_id) REFERENCES fitness_component (id),
    CONSTRAINT ck_fitness_test_item_decimal_scale CHECK (decimal_scale >= 0),
    CONSTRAINT ck_fitness_test_item_input_range CHECK (minimum_input IS NULL OR maximum_input IS NULL OR minimum_input <= maximum_input)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_fitness_test_item_component_active ON fitness_test_item (component_id, active);

CREATE TABLE paps_standard_version (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_name VARCHAR(200) NULL,
    source_url VARCHAR(500) NULL,
    effective_from DATE NULL,
    effective_to DATE NULL,
    official BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_paps_standard_version_code UNIQUE (code),
    CONSTRAINT ck_paps_standard_version_effective_range CHECK (effective_from IS NULL OR effective_to IS NULL OR effective_to >= effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_paps_standard_version_active ON paps_standard_version (active);

CREATE TABLE paps_standard (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version_id BIGINT NOT NULL,
    test_item_id BIGINT NOT NULL,
    school_level VARCHAR(30) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    minimum_age INT NOT NULL,
    maximum_age INT NOT NULL,
    grade INT NOT NULL,
    minimum_value DECIMAL(10, 2) NULL,
    maximum_value DECIMAL(10, 2) NULL,
    minimum_inclusive BOOLEAN NOT NULL,
    maximum_inclusive BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_paps_standard_version FOREIGN KEY (version_id) REFERENCES paps_standard_version (id),
    CONSTRAINT fk_paps_standard_test_item FOREIGN KEY (test_item_id) REFERENCES fitness_test_item (id),
    CONSTRAINT uk_paps_standard_range_grade UNIQUE (version_id, test_item_id, school_level, gender, minimum_age, maximum_age, grade),
    CONSTRAINT ck_paps_standard_grade CHECK (grade BETWEEN 1 AND 5),
    CONSTRAINT ck_paps_standard_age_range CHECK (minimum_age >= 0 AND maximum_age >= minimum_age),
    CONSTRAINT ck_paps_standard_value_range CHECK (minimum_value IS NULL OR maximum_value IS NULL OR minimum_value <= maximum_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_paps_standard_lookup ON paps_standard (version_id, test_item_id, school_level, gender, minimum_age, maximum_age);
CREATE INDEX idx_paps_standard_test_item ON paps_standard (test_item_id);
