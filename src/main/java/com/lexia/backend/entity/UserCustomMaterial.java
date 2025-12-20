package com.lexia.backend.entity;

import com.lexia.backend.enums.CustomMaterialSourceType;
import com.lexia.backend.enums.CustomMaterialStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a user's custom uploaded material for AI content
 * generation.
 * 
 * <p>
 * Premium users can upload documents (PDF, DOCX, Image), paste URLs (YouTube,
 * Website),
 * or raw text. The system processes the content and generates personalized
 * learning materials.
 * </p>
 * 
 * <p>
 * Table: user_custom_materials (V38 migration)
 * </p>
 * 
 * <p>
 * Generated content structure:
 * </p>
 * 
 * <pre>
 * {
 *   "schemaVersion": 1,
 *   "vocabulary": [{ "id": "v1", "term": "...", "definition": "..." }],
 *   "quiz": [...],
 *   "shadowing": [{ "id": "s1", "sentence": "...", "audioUrl": "..." }],
 *   "roleplay": { ... },
 *   "summary": "..."
 * }
 * </pre>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see UserCustomMaterialSettings
 * @see CustomMaterialJob
 */
@Entity
@Table(name = "user_custom_materials", indexes = {
        @Index(name = "idx_custom_materials_user_id", columnList = "user_id"),
        @Index(name = "idx_custom_materials_status", columnList = "status"),
        @Index(name = "idx_custom_materials_user_status", columnList = "user_id, status"),
        @Index(name = "idx_custom_materials_created_at", columnList = "created_at"),
        @Index(name = "idx_custom_materials_user_created", columnList = "user_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "contentText", "generatedContent" })
public class UserCustomMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private CustomMaterialSourceType sourceType;

    /**
     * URL to the original uploaded file in object storage (MinIO/S3).
     * Required for PDF, DOCX, IMAGE source types.
     */
    @Column(name = "original_file_url", columnDefinition = "TEXT")
    private String originalFileUrl;

    /**
     * Extracted raw text content from the source.
     * For PDF/DOCX: extracted text; for YouTube: transcript; for IMAGE: OCR result.
     */
    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    /**
     * Input metadata for range selection.
     * <ul>
     * <li>For documents: pageStart, pageEnd</li>
     * <li>For videos: timeStart, timeEnd (seconds)</li>
     * </ul>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_metadata", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, Object> inputMetadata = new HashMap<>();

    /**
     * AI-generated learning content.
     * Contains vocabulary, quiz, shadowing, roleplay, summary based on target
     * options.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "generated_content", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, Object> generatedContent = new HashMap<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CustomMaterialStatus status = CustomMaterialStatus.PENDING;

    /**
     * Error message if processing failed.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    // ===== Relationship =====

    @OneToOne(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserCustomMaterialSettings settings;

    @OneToOne(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CustomMaterialJob job;

    // ===== Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
        if (inputMetadata == null) {
            inputMetadata = new HashMap<>();
        }
        if (generatedContent == null) {
            generatedContent = new HashMap<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ===== Helper Methods =====

    /**
     * Checks if the material is ready for learning.
     * 
     * @return true if processing is complete
     */
    public boolean isReady() {
        return status == CustomMaterialStatus.COMPLETED;
    }

    /**
     * Checks if processing failed.
     * 
     * @return true if status is FAILED
     */
    public boolean isFailed() {
        return status == CustomMaterialStatus.FAILED;
    }

    /**
     * Checks if the material is still being processed.
     * 
     * @return true if PENDING or PROCESSING
     */
    public boolean isProcessing() {
        return status.isProcessing();
    }

    /**
     * Validates source type consistency.
     * File source types require originalFileUrl.
     * 
     * @throws IllegalStateException if configuration is invalid
     */
    public void validateSourceConsistency() {
        if (sourceType.requiresFileUpload() && (originalFileUrl == null || originalFileUrl.isBlank())) {
            throw new IllegalStateException(
                    "Source type " + sourceType + " requires a file URL");
        }
    }
}
