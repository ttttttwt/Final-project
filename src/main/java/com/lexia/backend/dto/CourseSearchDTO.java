package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for course search and filtering parameters.
 * All fields are optional - acts as a filter criteria builder.
 * Supports pagination through Pageable parameter in controller.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search and filter parameters for course queries. All fields are optional")
public class CourseSearchDTO {

    /**
     * Filter by course title (case-insensitive partial match).
     */
    @Schema(description = "Filter by course title (case-insensitive partial match)", example = "business", nullable = true)
    private String title;

    /**
     * Filter by CEFR level (exact match).
     */
    @Schema(description = "Filter by CEFR level", example = "B1", allowableValues = { "A1", "A2", "B1", "B2", "C1",
            "C2" }, nullable = true)
    private String cefrLevel;

    /**
     * Filter by publication status.
     */
    @Schema(description = "Filter by publication status (true=published, false=unpublished, null=all)", example = "true", nullable = true)
    private Boolean isPublished;

    /**
     * Filter courses created after this date.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Filter courses created after this date", example = "2025-10-01T00:00:00", nullable = true)
    private LocalDateTime createdAfter;

    /**
     * Filter courses created before this date.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Filter courses created before this date", example = "2025-10-31T23:59:59", nullable = true)
    private LocalDateTime createdBefore;

    /**
     * Page number (0-based, default: 0).
     * Handled by Pageable parameter in controller.
     */
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0", nullable = true)
    private Integer page;

    /**
     * Page size (default: 10, max: 100).
     * Handled by Pageable parameter in controller.
     */
    @Schema(description = "Number of items per page", example = "10", defaultValue = "10", nullable = true, maximum = "100")
    private Integer size;

    /**
     * Sort field and direction (e.g., "createdAt,desc" or "title,asc").
     * Handled by Pageable parameter in controller.
     */
    @Schema(description = "Sort field and direction (e.g., 'createdAt,desc' or 'title,asc')", example = "createdAt,desc", nullable = true)
    private String sort;
}
