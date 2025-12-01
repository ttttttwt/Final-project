package com.lexia.backend.controller;

import com.lexia.backend.dto.CourseDTO;
import com.lexia.backend.dto.CourseSearchDTO;
import com.lexia.backend.dto.CreateCourseDTO;
import com.lexia.backend.dto.UpdateCourseDTO;
import com.lexia.backend.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.lexia.backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for Course Management.
 * Handles CRUD operations and search functionality for courses.
 * 
 * <p>
 * Base path: /api/v1/courses
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
@RequestMapping("/api/v1/courses")
@Tag(name = "Course Management API", description = "Endpoints for managing courses, including creation, updates, deletion, publishing, and search")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

        private static final Logger LOG = LoggerFactory.getLogger(CourseController.class);

        private final CourseService courseService;

        public CourseController(CourseService courseService) {
                this.courseService = courseService;
        }

        /**
         * Get all published courses with pagination.
         * Available to all authenticated users.
         */
        @Operation(summary = "Get all published courses", description = "Retrieves a paginated list of all published courses visible to learners. "
                        +
                        "Supports pagination and sorting. Default page size is 10, maximum is 100.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved courses", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class), examples = @ExampleObject(name = "Success Response", value = """
                                        {
                                          "content": [
                                            {
                                              "id": 1,
                                              "title": "English Basics (A1)",
                                              "description": "Foundation course for beginners",
                                              "thumbnailUrl": "https://cdn.lexia.com/courses/a1-basics.jpg",
                                              "cefrLevel": "A1",
                                              "isPublished": true,
                                              "sectionCount": 3,
                                              "createdAt": "2025-10-30T10:15:30",
                                              "updatedAt": "2025-10-31T14:22:45"
                                            }
                                          ],
                                          "pageable": {
                                            "pageNumber": 0,
                                            "pageSize": 10
                                          },
                                          "totalPages": 1,
                                          "totalElements": 1,
                                          "last": true,
                                          "first": true,
                                          "numberOfElements": 1
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @GetMapping
        public ResponseEntity<Page<CourseDTO>> getAllPublishedCourses(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size (max 100)", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field and direction", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

                LOG.info("Fetching all published courses - page: {}, size: {}, sort: {}", page, size, sort);

                // Enforce max page size
                size = Math.min(size, 100);

                // Parse sort parameter
                Pageable pageable = createPageable(page, size, sort);

                Page<CourseDTO> courses = courseService.getAllPublished(pageable);

                LOG.info("Retrieved {} published courses", courses.getTotalElements());

                return ResponseEntity.ok(courses);
        }

        /**
         * Get course by ID.
         * Available to all authenticated users.
         */
        @Operation(summary = "Get course by ID", description = "Retrieves detailed information about a specific course by its ID. "
                        +
                        "Returns course metadata without sections. Use /courses/{id}/sections for full details.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CourseDTO.class), examples = @ExampleObject(name = "Success Response", value = """
                                        {
                                          "id": 1,
                                          "title": "English Basics (A1)",
                                          "description": "Foundation course for beginners starting to learn English",
                                          "thumbnailUrl": "https://cdn.lexia.com/courses/a1-basics.jpg",
                                          "cefrLevel": "A1",
                                          "isPublished": true,
                                          "sectionCount": 3,
                                          "createdAt": "2025-10-30T10:15:30",
                                          "updatedAt": "2025-10-31T14:22:45"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @GetMapping("/{id}")
        public ResponseEntity<CourseDTO> getCourseById(
                        @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long id) {

                LOG.info("Fetching course by ID: {}", id);

                CourseDTO course = courseService.getById(id);

                LOG.info("Retrieved course: {}", course.getTitle());

                return ResponseEntity.ok(course);
        }

        /**
         * Search courses with filters and pagination.
         * Available to all authenticated users.
         */
        @Operation(summary = "Search courses", description = "Advanced search for courses with multiple filters. " +
                        "All filter parameters are optional. Supports pagination and sorting. " +
                        "Title search is case-insensitive and uses partial matching.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @GetMapping("/search")
        public ResponseEntity<Page<CourseDTO>> searchCourses(
                        @Parameter(description = "Filter by title (partial match)", example = "business") @RequestParam(required = false) String title,
                        @Parameter(description = "Filter by CEFR level", example = "B1") @RequestParam(required = false) String cefrLevel,
                        @Parameter(description = "Filter by publication status", example = "true") @RequestParam(required = false) Boolean isPublished,
                        @Parameter(description = "Filter by enrollment status", example = "true") @RequestParam(required = false) Boolean isEnrolled,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size (max 100)", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field and direction", example = "title,asc") @RequestParam(defaultValue = "createdAt,desc") String sort) {

                LOG.info("Searching courses - title: {}, cefrLevel: {}, isPublished: {}, isEnrolled: {}, page: {}, size: {}",
                                title, cefrLevel, isPublished, isEnrolled, page, size);

                // Build search DTO
                CourseSearchDTO searchDTO = CourseSearchDTO.builder()
                                .title(title)
                                .cefrLevel(cefrLevel)
                                .isPublished(isPublished)
                                .isEnrolled(isEnrolled)
                                .page(page)
                                .size(Math.min(size, 100))
                                .sort(sort)
                                .build();

                // If enrollment filter is used, we need the user ID
                if (isEnrolled != null) {
                        searchDTO.setUserId(getCurrentUserId());
                }

                Page<CourseDTO> courses = courseService.search(searchDTO);

                LOG.info("Search returned {} courses", courses.getTotalElements());

                return ResponseEntity.ok(courses);
        }

        /**
         * Create a new course.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Create a new course", description = "Creates a new course with the provided details. " +
                        "Requires CONTENT_MANAGER role. Course is created as unpublished by default. " +
                        "Title must be unique across all courses.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Course creation data", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateCourseDTO.class), examples = @ExampleObject(name = "Create Course Example", value = """
                                        {
                                          "title": "Business English for Professionals",
                                          "description": "Master business English for office communication, presentations, and negotiations",
                                          "thumbnailUrl": "https://cdn.lexia.com/courses/business-english.jpg",
                                          "cefrLevel": "B1"
                                        }
                                        """))))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Course created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CourseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data - validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "409", description = "Conflict - course with this title already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CreateCourseDTO dto) {
                LOG.info("Creating new course: {}", dto.getTitle());

                CourseDTO createdCourse = courseService.create(dto);

                LOG.info("Course created successfully with ID: {}", createdCourse.getId());

                return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
        }

        /**
         * Update an existing course.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Update an existing course", description = "Updates course information. Only provided fields will be updated. "
                        +
                        "Requires CONTENT_MANAGER role. If updating title, it must remain unique.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Course update data (all fields optional)", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateCourseDTO.class), examples = @ExampleObject(name = "Update Course Example", value = """
                                        {
                                          "title": "Advanced Business English",
                                          "description": "Master advanced business English for executive-level communication",
                                          "cefrLevel": "C1"
                                        }
                                        """))))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CourseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data - validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "409", description = "Conflict - updated title already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<CourseDTO> updateCourse(
                        @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long id,
                        @Valid @RequestBody UpdateCourseDTO dto) {

                LOG.info("Updating course ID: {}", id);

                CourseDTO updatedCourse = courseService.update(id, dto);

                LOG.info("Course updated successfully: {}", updatedCourse.getTitle());

                return ResponseEntity.ok(updatedCourse);
        }

        /**
         * Delete a course.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Delete a course", description = "Deletes a course by ID. Requires CONTENT_MANAGER role. "
                        +
                        "Published courses cannot be deleted - they must be unpublished first using the unpublish endpoint. "
                        +
                        "This operation will cascade delete all sections and lessons.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Course deleted successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "400", description = "Bad Request - course is published (must unpublish first)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<Void> deleteCourse(
                        @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long id) {

                LOG.info("Deleting course ID: {}", id);

                courseService.delete(id);

                LOG.info("Course deleted successfully: {}", id);

                return ResponseEntity.noContent().build();
        }

        /**
         * Publish a course.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Publish a course", description = "Publishes a course, making it visible to learners. " +
                        "Requires CONTENT_MANAGER role. Course must have at least one section with lessons before it can be published.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course published successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CourseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Bad Request - course has no content (sections/lessons)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @PostMapping("/{id}/publish")
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<CourseDTO> publishCourse(
                        @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long id) {

                LOG.info("Publishing course ID: {}", id);

                CourseDTO publishedCourse = courseService.publish(id);

                LOG.info("Course published successfully: {}", publishedCourse.getTitle());

                return ResponseEntity.ok(publishedCourse);
        }

        /**
         * Unpublish a course.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Unpublish a course", description = "Unpublishes a course, hiding it from learners. " +
                        "Requires CONTENT_MANAGER role. Can be used before deleting a published course.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course unpublished successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CourseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @PostMapping("/{id}/unpublish")
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<CourseDTO> unpublishCourse(
                        @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long id) {

                LOG.info("Unpublishing course ID: {}", id);

                CourseDTO unpublishedCourse = courseService.unpublish(id);

                LOG.info("Course unpublished successfully: {}", unpublishedCourse.getTitle());

                return ResponseEntity.ok(unpublishedCourse);
        }

        /**
         * Upload course thumbnail image.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Upload course thumbnail", description = "Uploads a thumbnail image for the course. " +
                        "Requires CONTENT_MANAGER role. Accepts image files (JPG, PNG, WebP) up to 5MB. " +
                        "Old thumbnail file will be deleted when a new one is uploaded.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thumbnail uploaded successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CourseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid file type or size", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "413", description = "File too large")
        })
        @PostMapping(value = "/{id}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<CourseDTO> uploadThumbnail(
                        @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long id,
                        @RequestParam("file") MultipartFile file) {

                LOG.info("Uploading thumbnail for course ID: {}", id);

                java.util.UUID userId = getCurrentUserId();
                courseService.uploadThumbnail(id, file, userId);

                // Fetch and return updated course
                CourseDTO updatedCourse = courseService.getById(id);

                LOG.info("Thumbnail uploaded successfully for course: {}", updatedCourse.getTitle());

                return ResponseEntity.ok(updatedCourse);
        }

        /**
         * Delete course thumbnail image.
         * Requires CONTENT_MANAGER role.
         */
        @Operation(summary = "Delete course thumbnail", description = "Removes the thumbnail image from the course. " +
                        "Requires CONTENT_MANAGER role. The associated file will be deleted from storage.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thumbnail deleted successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CourseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "403", description = "Forbidden - requires CONTENT_MANAGER role", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
        })
        @DeleteMapping("/{id}/thumbnail")
        @PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
        public ResponseEntity<CourseDTO> deleteThumbnail(
                        @Parameter(description = "Course ID", required = true, example = "1") @PathVariable Long id) {

                LOG.info("Deleting thumbnail for course ID: {}", id);

                courseService.deleteThumbnail(id);

                // Fetch and return updated course
                CourseDTO updatedCourse = courseService.getById(id);

                LOG.info("Thumbnail deleted successfully for course: {}", updatedCourse.getTitle());

                return ResponseEntity.ok(updatedCourse);
        }

        /**
         * Helper method to create Pageable from parameters.
         */
        private Pageable createPageable(int page, int size, String sortParam) {
                // Parse sort parameter (e.g., "createdAt,desc" or "title,asc")
                String[] sortParts = sortParam.split(",");
                String sortField = sortParts[0];
                Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                return PageRequest.of(page, size, Sort.by(direction, sortField));
        }

        /**
         * Helper method to get current user ID from security context.
         */
        private java.util.UUID getCurrentUserId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof User) {
                        return ((User) authentication.getPrincipal()).getId();
                }
                // Fallback or return null if not authenticated (though endpoints are secured)
                return null;
        }
}
