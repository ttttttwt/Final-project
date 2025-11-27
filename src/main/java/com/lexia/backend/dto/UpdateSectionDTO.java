package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing section.
 * All fields are optional to support partial updates.
 *
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing section (all fields optional)")
public class UpdateSectionDTO {

    /**
     * Section title (optional).
     */
    @Size(max = 255, message = "Section title must not exceed 255 characters")
    @Schema(description = "Updated section title", example = "Introduction to Business English", maxLength = 255, nullable = true)
    private String title;

    /**
     * Order index within the course (optional).
     */
    @Min(value = 0, message = "Order index must be non-negative")
    @Schema(description = "New order position within the course (0-based)", example = "1", minimum = "0", nullable = true)
    private Integer orderIndex;
}
