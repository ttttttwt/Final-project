package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Section responses.
 * Used to return section information to clients.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Section information with lesson count")
public class SectionDTO {

    /**
     * Section unique identifier (auto-generated).
     */
    @Schema(description = "Section unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    /**
     * Parent course ID.
     */
    @Schema(description = "ID of the parent course", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long courseId;

    /**
     * Section title displayed to users.
     */
    @Schema(description = "Section title", example = "Getting Started", maxLength = 255)
    private String title;

    /**
     * Order position within the course (0-based).
     */
    @Schema(description = "Order position within the course (0-based)", example = "0")
    private Integer orderIndex;

    /**
     * Number of lessons in this section (derived field).
     */
    @Schema(description = "Number of lessons in the section", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer lessonCount;

    /**
     * Timestamp when section was created.
     */
    @Schema(description = "Section creation timestamp", example = "2025-10-30T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
