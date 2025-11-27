package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for reordering sections within a course.
 * Contains the new order of section IDs.
 *
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reorder sections within a course")
public class ReorderSectionsDTO {

    /**
     * Ordered list of section IDs representing the new order.
     * All sections of the course must be included.
     */
    @NotEmpty(message = "Section IDs list cannot be empty")
    @Schema(description = "Ordered list of section IDs representing the new order. All sections must be included.", example = "[3, 1, 2]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> sectionIds;
}
