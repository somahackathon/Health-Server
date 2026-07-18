ALTER TABLE paps_standard
    ADD COLUMN school_grade INT NOT NULL DEFAULT 1 AFTER school_level;

ALTER TABLE paps_standard
    DROP INDEX uk_paps_standard_range_grade;

ALTER TABLE paps_standard
    ADD CONSTRAINT uk_paps_standard_range_grade UNIQUE (
        version_id,
        test_item_id,
        school_level,
        school_grade,
        gender,
        grade
    );

ALTER TABLE paps_standard
    ADD CONSTRAINT ck_paps_standard_school_grade CHECK (school_grade BETWEEN 1 AND 6);

DROP INDEX idx_paps_standard_lookup ON paps_standard;

CREATE INDEX idx_paps_standard_lookup
    ON paps_standard (version_id, test_item_id, school_level, school_grade, gender);
