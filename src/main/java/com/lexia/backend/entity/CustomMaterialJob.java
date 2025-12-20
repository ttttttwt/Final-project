package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity tracking async processing jobs for custom materials.
 * 
 * <p>
 * Provides durability for async processing:
 * </p>
 * <ul>
 * <li>Tracks job status and progress</li>
 * <li>Supports retry on failure</li>
 * <li>Records error messages for debugging</li>
 * <li>Enables job recovery after server restart</li>
 * </ul>
 * 
 * <p>
 * Table: custom_material_jobs (V38 migration)
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see UserCustomMaterial
 */
@Entity
@Table(name = "custom_material_jobs", indexes = {
        @Index(name = "idx_material_jobs_status", columnList = "status"),
        @Index(name = "idx_material_jobs_queued", columnList = "status, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class CustomMaterialJob {

    public static final String STATUS_QUEUED = "QUEUED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false, unique = true)
    private UserCustomMaterial material;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_QUEUED;

    /**
     * Processing progress (0-100).
     */
    @Min(0)
    @Max(100)
    @Column(name = "progress", nullable = false)
    @Builder.Default
    private Integer progress = 0;

    /**
     * Number of retry attempts made.
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Maximum retry attempts allowed.
     */
    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    /**
     * Last error message if job failed.
     */
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    /**
     * When processing actually started.
     */
    @Column(name = "started_at")
    private Instant startedAt;

    /**
     * When processing completed (successfully or with failure).
     */
    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ===== Helper Methods =====

    /**
     * Marks the job as processing.
     */
    public void startProcessing() {
        this.status = STATUS_PROCESSING;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the progress percentage.
     * 
     * @param percent progress (0-100)
     */
    public void updateProgress(int percent) {
        this.progress = Math.min(100, Math.max(0, percent));
        this.updatedAt = Instant.now();
    }

    /**
     * Marks the job as completed successfully.
     */
    public void complete() {
        this.status = STATUS_COMPLETED;
        this.progress = 100;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Marks the job as failed.
     * 
     * @param errorMessage the error message
     */
    public void fail(String errorMessage) {
        this.status = STATUS_FAILED;
        this.lastError = errorMessage;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if retry is allowed.
     * 
     * @return true if retry count < max retries
     */
    public boolean canRetry() {
        return retryCount < maxRetries;
    }

    /**
     * Increments retry count and requeues.
     */
    public void retry() {
        if (canRetry()) {
            this.retryCount++;
            this.status = STATUS_QUEUED;
            this.progress = 0;
            this.startedAt = null;
            this.completedAt = null;
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Checks if job is in a terminal state.
     * 
     * @return true if COMPLETED or FAILED
     */
    public boolean isTerminal() {
        return STATUS_COMPLETED.equals(status) || STATUS_FAILED.equals(status);
    }

    /**
     * Checks if job is waiting in queue.
     * 
     * @return true if QUEUED
     */
    public boolean isQueued() {
        return STATUS_QUEUED.equals(status);
    }
}
