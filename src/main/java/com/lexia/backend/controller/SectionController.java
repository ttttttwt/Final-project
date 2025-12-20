package com.lexia.backend.controller;

import com.lexia.backend.dto.CreateSectionDTO;
import com.lexia.backend.dto.ReorderSectionsDTO;
import com.lexia.backend.dto.SectionDTO;
import com.lexia.backend.dto.UpdateSectionDTO;
import com.lexia.backend.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Section Management.
 * Handles CRUD operations for course sections.
 * 
 * <p>
 * Base path: /api/v1/courses/{courseId}/sections
 * </p>
 * 
 * <p>
 * Security:
 * </p>
 * <ul>
 * <li>All endpoints require authentication</li>
 * <li>Create/Update/Delete operations require CONTENT_MANAGER role</li>
 * <li>Read operations available to all authenticated users</li>
 * </ul>
 *
 * @author LEXIA Team
 * @since Sprint 2
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/sections")
@Tag(name = "Section Management API", description = "Endpoints for managing sections within courses")
@SecurityRequirement(name = "bearerAuth")
public class SectionController {

    private static final Logger LOG = LoggerFactory.getLogger(SectionController.class);

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    /**
     * Get all sections for a course.
     */
    @Operation(summary = "Get all sections for a course", description = "Retrieves all sections within a course, ordered by their order index. "
            +
            "Includes lesson count for each section.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sections", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = SectionDTO.class)), examples = @ExampleObject(name = "Success Response", value = """
                    [
                      {
                        "id": 1,
                        "courseId": 1,
                        "title": "Getting Started",
                        "orderIndex": 0,
                        "lessonCount": 3,
                        "createdAt": "2025-10-30T10:15:30"
                      },
                      {
                        "id": 2,
                        "courseId": 1,
                        "title": "Basic Grammar",
                        "orderIndex": 1,
                        "lessonCount": 5,
                        "createdAt": "2025-10-30T10:20:00"
                      }
                    ]
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @GetMapping
    public ResponseEntity<List<SectionDTO>> getSections(
            @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long courseId) {

        LOG.info("Fetching sections for course ID: {}", courseId);

        List<SectionDTO> sections = sectionService.getByCourseId(courseId);

        LOG.info("Retrieved {} sections for course ID: {}", sections.size(), courseId);

        return ResponseEntity.ok(sections);
    }

    /**
     * Get a specific section by ID.
     */
    @Operation(summary = "Get section by ID", description = "Retrieves a specific section by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Section found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SectionDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Section not found")
    })
    @GetMapping("/{sectionId}")
    public ResponseEntity<SectionDTO> getSection(
            @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long courseId,
            @Parameter(description = "Section ID", required = true, example = "1") @PathVariable Long sectionId) {

        LOG.info("Fetching section ID: {} for course ID: {}", sectionId, courseId);

        SectionDTO section = sectionService.getById(sectionId);

        // Verify section belongs to the course
        if (!section.getCourseId().equals(courseId)) {
            LOG.warn("Section {} does not belong to course {}", sectionId, courseId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(section);
    }

    /**
     * Create a new section.
     */
    @Operation(summary = "Create a new section", description = "Creates a new section within a course. Requires CONTENT_MANAGER role. "
            +
            "If orderIndex is not provided, the section is added at the end.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Section creation data", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateSectionDTO.class), examples = @ExampleObject(name = "Create Section Example", value = """
                    {
                      "title": "Advanced Grammar",
                      "orderIndex": 2
                    }
                    """))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Section created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SectionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<SectionDTO> createSection(
            @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long courseId,
            @Valid @RequestBody CreateSectionDTO dto) {

        LOG.info("Creating new section for course ID: {} with title: {}", courseId, dto.getTitle());

        SectionDTO createdSection = sectionService.create(courseId, dto);

        LOG.info("Section created successfully with ID: {}", createdSection.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSection);
    }

    /**
     * Update an existing section.
     */
    @Operation(summary = "Update a section", description = "Updates a section's title or order. Requires CONTENT_MANAGER role. "
            +
            "Only provided fields are updated.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Section update data", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateSectionDTO.class), examples = @ExampleObject(name = "Update Section Example", value = """
                    {
                      "title": "Updated Section Title"
                    }
                    """))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Section updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SectionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role"),
            @ApiResponse(responseCode = "404", description = "Section not found")
    })
    @PutMapping("/{sectionId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<SectionDTO> updateSection(
            @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long courseId,
            @Parameter(description = "Section ID", required = true, example = "1") @PathVariable Long sectionId,
            @Valid @RequestBody UpdateSectionDTO dto) {

        LOG.info("Updating section ID: {} for course ID: {}", sectionId, courseId);

        SectionDTO updatedSection = sectionService.update(sectionId, dto);

        LOG.info("Section updated successfully: {}", updatedSection.getId());

        return ResponseEntity.ok(updatedSection);
    }

    /**
     * Delete a section.
     */
    @Operation(summary = "Delete a section", description = "Deletes a section and all its lessons. Requires CONTENT_MANAGER role. "
            +
            "Remaining sections are automatically re-indexed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Section deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role"),
            @ApiResponse(responseCode = "404", description = "Section not found")
    })
    @DeleteMapping("/{sectionId}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteSection(
            @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long courseId,
            @Parameter(description = "Section ID", required = true, example = "1") @PathVariable Long sectionId) {

        LOG.info("Deleting section ID: {} from course ID: {}", sectionId, courseId);

        sectionService.delete(sectionId);

        LOG.info("Section deleted successfully: {}", sectionId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Reorder sections within a course.
     */
    @Operation(summary = "Reorder sections", description = "Reorders all sections within a course. Requires CONTENT_MANAGER role. "
            +
            "The sectionIds array must contain all section IDs in the desired new order.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New section order", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReorderSectionsDTO.class), examples = @ExampleObject(name = "Reorder Sections Example", value = """
                    {
                      "sectionIds": [3, 1, 2]
                    }
                    """))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sections reordered successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = SectionDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid section IDs - must match all course sections"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PostMapping("/reorder")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<SectionDTO>> reorderSections(
            @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long courseId,
            @Valid @RequestBody ReorderSectionsDTO dto) {

        LOG.info("Reordering sections for course ID: {}", courseId);

        List<SectionDTO> reorderedSections = sectionService.reorder(courseId, dto);

        LOG.info("Successfully reordered {} sections", reorderedSections.size());

        return ResponseEntity.ok(reorderedSections);
    }
}
