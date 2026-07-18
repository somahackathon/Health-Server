CREATE TABLE ai_analysis_job (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id BINARY(16) NOT NULL,
    installation_hash VARCHAR(128) NOT NULL,
    analysis_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    request_payload LONGTEXT NULL,
    result_payload LONGTEXT NULL,
    model_version VARCHAR(100) NULL,
    failure_code VARCHAR(50) NULL,
    failure_message VARCHAR(1000) NULL,
    retry_count INT NOT NULL DEFAULT 0,
    expires_at DATETIME(6) NOT NULL,
    started_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ai_analysis_job_public_id UNIQUE (public_id),
    CONSTRAINT ck_ai_analysis_job_retry_count CHECK (retry_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ai_analysis_job_expires_at ON ai_analysis_job (expires_at);
CREATE INDEX idx_ai_analysis_job_status_expires_at ON ai_analysis_job (status, expires_at);
CREATE INDEX idx_ai_analysis_job_installation_created_at ON ai_analysis_job (installation_hash, created_at);
CREATE INDEX idx_ai_analysis_job_installation_public_id ON ai_analysis_job (installation_hash, public_id);
