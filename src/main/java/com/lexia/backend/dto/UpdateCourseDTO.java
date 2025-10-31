package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for updating an existing course.
 * All fields are optional - only provided fields will be updated.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update course information. All fields are optional - only provided fields will be updated")
public class UpdateCourseDTO {

    /**
     * Course title (optional, max 255 characters).
     */
    @Size(max = 255, message = "Course title must not exceed 255 characters")
    @Schema(description = "Course title", example = "Advanced Business English", maxLength = 255, nullable = true)
    private String title;

    /**
     * Course description (optional, max 1000 characters).
     */
    @Size(max = 1000, message = "Course description must not exceed 1000 characters")
    @Schema(description = "Detailed course description", example = "Master advanced business English for executive-level communication", maxLength = 1000, nullable = true)
    private String description;

    /**
     * URL to course thumbnail image (optional, must be valid URL if provided).
     */
    @URL(message = "Thumbnail URL must be a valid URL")
    @Size(max = 255, message = "Thumbnail URL must not exceed 255 characters")
    @Schema(description = "URL to course thumbnail image", example = "https://cdn.lexia.com/courses/advanced-business.jpg", nullable = true, format = "uri", maxLength = 255)
    private String thumbnailUrl;

    /**
     * CEFR level (optional, must be A1, A2, B1, B2, C1, or C2 if provided).
     */
    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)$", message = "CEFR level must be one of: A1, A2, B1, B2, C1, C2")
    @Schema(description = "CEFR level of the course", example = "C1", allowableValues = { "A1", "A2", "B1", "B2", "C1",
            "C2" }, nullable = true)
    private String cefrLevel;

    /**
     * Publication status (optional).
     */
    @Schema(description = "Whether the course should be published", example = "true", nullable = true)
    private Boolean isPublished;
}
