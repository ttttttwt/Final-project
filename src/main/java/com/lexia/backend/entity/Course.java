package com.lexia.backend.entity;

import com.lexia.backend.file.entity.FileEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a CEFR-leveled English learning course.
 * 
 * <p>
 * Courses contain sections which organize lessons into logical groups.
 * Only published courses are visible to learners.
 * </p>
 * 
 * <p>
 * <strong>Database Table:</strong> courses
 * </p>
 * <p>
 * <strong>Primary Key:</strong> BIGSERIAL (auto-incrementing Long)
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see Section
 */
@Entity
@Table(name = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "sections")
public class Course {

    /**
     * Primary key, auto-incrementing BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Course title displayed to users.
     * Must be unique and non-blank.
     */
    @NotBlank(message = "Course title is required")
    @Size(max = 255, message = "Course title must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Detailed course description.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * URL to course thumbnail image (external URL).
     * For backward compatibility with external URLs.
     */
    @Size(max = 255, message = "Thumbnail URL must not exceed 255 characters")
    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    /**
     * Reference to uploaded thumbnail file.
     * If set, takes precedence over thumbnailUrl.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail_file_id")
    private FileEntity thumbnailFile;

    /**
     * Common European Framework of Reference level.
     * Valid values: A1, A2, B1, B2, C1, C2
     */
    @NotBlank(message = "CEFR level is required")
    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)$", message = "CEFR level must be one of: A1, A2, B1, B2, C1, C2")
    @Column(name = "cefr_level", nullable = false, length = 2)
    private String cefrLevel;

    /**
     * Publication status - only published courses are visible to learners.
     */
    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;

    /**
     * Timestamp when course was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when course was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sections belonging to this course.
     * Ordered by orderIndex.
     * Cascade ALL operations to sections, remove orphaned sections.
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Section> sections = new ArrayList<>();

    /**
     * Helper method to add a section to this course.
     * Maintains bidirectional relationship.
     * 
     * @param section the section to add
     */
    public void addSection(Section section) {
        sections.add(section);
        section.setCourse(this);
    }

    /**
     * Helper method to remove a section from this course.
     * Maintains bidirectional relationship.
     * 
     * @param section the section to remove
     */
    public void removeSection(Section section) {
        sections.remove(section);
        section.setCourse(null);
    }

    /**
     * Get the effective thumbnail URL.
     * Returns uploaded file URL if available, otherwise returns external URL.
     *
     * @return the thumbnail URL or null if no thumbnail is set
     */
    public String getEffectiveThumbnailUrl() {
        if (thumbnailFile != null) {
            return "/api/v1/files/" + thumbnailFile.getId() + "/download";
        }
        return thumbnailUrl;
    }
}
