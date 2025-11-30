package com.lexia.backend.file.entity;

import com.lexia.backend.entity.User;
import com.lexia.backend.file.enums.FileCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing metadata for uploaded files.
 * Actual file content is stored in the filesystem or cloud storage.
 */
@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Original filename as uploaded by the user.
     */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * Relative path from upload root directory.
     * Format: {category}/{yyyy-MM}/{uuid}.{ext}
     */
    @Column(name = "storage_path", nullable = false, unique = true, length = 500)
    private String storagePath;

    /**
     * MIME type of the file (e.g., image/jpeg, audio/mpeg).
     */
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    /**
     * File size in bytes.
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * Category of the file (AVATAR, COURSE_THUMBNAIL, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private FileCategory category;

    /**
     * User who uploaded the file.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    /**
     * Timestamp when the file was uploaded.
     */
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    // Optional metadata for images
    /**
     * Image width in pixels (null for non-image files).
     */
    @Column(name = "width")
    private Integer width;

    /**
     * Image height in pixels (null for non-image files).
     */
    @Column(name = "height")
    private Integer height;

    /**
     * Audio duration in seconds (null for non-audio files).
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // Access control
    /**
     * If true, file can be accessed without authentication.
     */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Number of times the file has been accessed/downloaded.
     */
    @Column(name = "access_count", nullable = false)
    @Builder.Default
    private Integer accessCount = 0;

    /**
     * Timestamp of last access.
     */
    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    /**
     * Increment access count and update last accessed timestamp.
     */
    public void recordAccess() {
        this.accessCount++;
        this.lastAccessedAt = Instant.now();
    }
}
