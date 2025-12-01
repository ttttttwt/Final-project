package com.lexia.backend.dto;

import com.lexia.backend.entity.Lesson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Lesson responses.
 * Used to return lesson information to clients.
 * Content is returned as-is (JSON string) without parsing.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lesson information with JSONB content")
public class LessonDTO {

    /**
     * Lesson unique identifier (auto-generated).
     */
    @Schema(description = "Lesson unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    /**
     * Parent section ID.
     */
    @Schema(description = "ID of the parent section", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long sectionId;

    /**
     * Lesson title displayed to users.
     */
    @Schema(description = "Lesson title", example = "Introduction to Greetings", maxLength = 255)
    private String title;

    /**
     * Type of lesson (READING, LISTENING, QUIZ, SPEAKING).
     */
    @Schema(description = "Lesson type", example = "READING", allowableValues = { "READING", "LISTENING", "QUIZ",
            "SPEAKING" })
    private Lesson.LessonType lessonType;

    /**
     * JSONB content - structure varies by lesson type.
     * See DATABASE-SCHEMA.md section 2.3 for schemas.
     */
    @Schema(description = "JSONB content (structure varies by lesson type - see DATABASE-SCHEMA.md section 2.3)", example = "{\"passages\": [{\"text\": \"Hello, my name is John...\"}], \"questions\": [...]}")
    private String content;

    /**
     * Order position within the section (0-based).
     */
    @Schema(description = "Order position within the section (0-based)", example = "0")
    private Integer orderIndex;

    /**
     * Estimated time to complete the lesson (in minutes).
     */
    @Schema(description = "Estimated time to complete the lesson (in minutes)", example = "15", minimum = "1", maximum = "240")
    private Integer durationMinutes;

    /**
     * Timestamp when lesson was created.
     */
    @Schema(description = "Lesson creation timestamp", example = "2025-10-30T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    /**
     * Timestamp when lesson was last updated.
     */
    @Schema(description = "Lesson last update timestamp", example = "2025-10-31T14:22:45", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    /**
     * URL to uploaded audio file (for LISTENING lessons).
     * Only set when an audio file has been uploaded via the file upload API.
     */
    @Schema(description = "URL to uploaded audio file (for LISTENING lessons)", example = "/api/v1/files/550e8400-e29b-41d4-a716-446655440000/download", nullable = true)
    private String audioUrl;
}
