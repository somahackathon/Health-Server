package team.soma.teto.health.analysis.job.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import team.soma.teto.health.global.domain.BaseTimeEntity;

@Entity
@Table(name = "ai_analysis_job")
public class AiAnalysisJob extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID publicId;

    @Column(name = "installation_hash", nullable = false, length = 128)
    private String installationHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false, length = 30)
    private AnalysisType analysisType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AnalysisStatus status;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "request_payload", columnDefinition = "LONGTEXT")
    private String requestPayload;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "result_payload", columnDefinition = "LONGTEXT")
    private String resultPayload;

    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_code", length = 50)
    private AiFailureCode failureCode;

    @Column(name = "failure_message", length = 1000)
    private String failureMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected AiAnalysisJob() {
    }

    private AiAnalysisJob(UUID publicId, String installationHash, AnalysisType analysisType, String requestPayload, Instant expiresAt) {
        if (publicId == null || installationHash == null || installationHash.isBlank() || analysisType == null || expiresAt == null) {
            throw new IllegalArgumentException("analysis job requires publicId, installationHash, analysisType, and expiresAt");
        }
        this.publicId = publicId;
        this.installationHash = installationHash;
        this.analysisType = analysisType;
        this.status = AnalysisStatus.PENDING;
        this.requestPayload = requestPayload;
        this.retryCount = 0;
        this.expiresAt = expiresAt;
    }

    public static AiAnalysisJob create(String installationHash, AnalysisType analysisType, String requestPayload, Instant expiresAt) {
        return new AiAnalysisJob(UUID.randomUUID(), installationHash, analysisType, requestPayload, expiresAt);
    }

    public static AiAnalysisJob create(UUID publicId, String installationHash, AnalysisType analysisType, String requestPayload, Instant expiresAt) {
        return new AiAnalysisJob(publicId, installationHash, analysisType, requestPayload, expiresAt);
    }

    public void start(Instant startedAt) {
        if (status != AnalysisStatus.PENDING) {
            throw new IllegalStateException("only pending analysis job can start");
        }
        if (startedAt == null) {
            throw new IllegalArgumentException("startedAt is required");
        }
        status = AnalysisStatus.PROCESSING;
        this.startedAt = startedAt;
    }

    public void complete(String resultPayload, String modelVersion, Instant completedAt) {
        if (status != AnalysisStatus.PROCESSING) {
            throw new IllegalStateException("only processing analysis job can complete");
        }
        if (completedAt == null) {
            throw new IllegalArgumentException("completedAt is required");
        }
        status = AnalysisStatus.COMPLETED;
        this.resultPayload = resultPayload;
        this.modelVersion = modelVersion;
        this.completedAt = completedAt;
        this.failureCode = null;
        this.failureMessage = null;
    }

    public void fail(AiFailureCode failureCode, String failureMessage, Instant completedAt) {
        if (status != AnalysisStatus.PROCESSING) {
            throw new IllegalStateException("only processing analysis job can fail");
        }
        if (failureCode == null || completedAt == null) {
            throw new IllegalArgumentException("failureCode and completedAt are required");
        }
        if (failureMessage != null && failureMessage.contains("\n\tat ")) {
            throw new IllegalArgumentException("failureMessage must not contain a stack trace");
        }
        status = AnalysisStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.completedAt = completedAt;
    }

    public void expire(Instant completedAt) {
        if (status == AnalysisStatus.COMPLETED || status == AnalysisStatus.EXPIRED) {
            throw new IllegalStateException("completed or expired analysis job cannot expire again");
        }
        if (completedAt == null) {
            throw new IllegalArgumentException("completedAt is required");
        }
        status = AnalysisStatus.EXPIRED;
        this.completedAt = completedAt;
    }

    public void increaseRetryCount() {
        if (status == AnalysisStatus.COMPLETED || status == AnalysisStatus.EXPIRED) {
            throw new IllegalStateException("completed or expired analysis job cannot retry");
        }
        retryCount++;
    }

    public void clearExpiredPayloads(Instant now) {
        if (now == null || expiresAt.isAfter(now)) {
            throw new IllegalStateException("cannot clear payloads before expiration");
        }
        requestPayload = null;
        resultPayload = null;
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getInstallationHash() {
        return installationHash;
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public String getResultPayload() {
        return resultPayload;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public AiFailureCode getFailureCode() {
        return failureCode;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
