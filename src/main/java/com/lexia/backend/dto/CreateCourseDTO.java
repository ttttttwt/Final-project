package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for creating a new course.
 * Contains validation rules for course input data.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new course")
public class CreateCourseDTO {

    /**
     * Course title (required).
     */
    @NotBlank(message = "Course title is required")
    @Size(max = 255, message = "Course title must not exceed 255 characters")
    @Schema(description = "Course title", example = "Business English for Professionals", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    private String title;

    /**
     * Course description (optional, max 1000 characters).
     */
    @Size(max = 1000, message = "Course description must not exceed 1000 characters")
    @Schema(description = "Detailed course description", example = "Learn practical business English for office communication, presentations, and negotiations", maxLength = 1000, nullable = true)
    private String description;

    /**
     * URL to course thumbnail image (optional, must be valid URL).
     */
    @URL(message = "Thumbnail URL must be a valid URL")
    @Size(max = 255, message = "Thumbnail URL must not exceed 255 characters")
    @Schema(description = "URL to course thumbnail image", example = "https://cdn.lexia.com/courses/business-english.jpg", nullable = true, format = "uri", maxLength = 255)
    private String thumbnailUrl;

    /**
     * CEFR level (required, must be A1, A2, B1, B2, C1, or C2).
     */
    @NotBlank(message = "CEFR level is required")
    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)$", message = "CEFR level must be one of: A1, A2, B1, B2, C1, C2")
    @Schema(description = "CEFR level of the course", example = "B1", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {
            "A1", "A2", "B1", "B2", "C1", "C2" })
    private String cefrLevel;
}
