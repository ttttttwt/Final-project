package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new section within a course.
 * Contains validation rules for section input data.
 *
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new section within a course")
public class CreateSectionDTO {

    /**
     * Section title (required).
     */
    @NotBlank(message = "Section title is required")
    @Size(max = 255, message = "Section title must not exceed 255 characters")
    @Schema(description = "Section title displayed to users", example = "Getting Started", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    private String title;

    /**
     * Order index within the course (optional).
     * If not provided, section will be added at the end.
     */
    @Min(value = 0, message = "Order index must be non-negative")
    @Schema(description = "Order position within the course (0-based). If not provided, section is added at the end.", example = "0", minimum = "0", nullable = true)
    private Integer orderIndex;
}
