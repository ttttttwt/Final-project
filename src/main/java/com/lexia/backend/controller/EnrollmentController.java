package com.lexia.backend.controller;

import com.lexia.backend.dto.CourseProgressDTO;
import com.lexia.backend.dto.EnrollmentDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Course Enrollment Management.
 * Handles course enrollment operations and progress tracking.
 * 
 * <p>
 * Base path: /api/v1/enrollments
 * </p>
 * 
 * <p>
 * Security:
 * </p>
 * <ul>
 * <li>All endpoints require authentication</li>
 * <li>Users can only manage their own enrollments</li>
 * <li>Users can only view their own progress</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@RestController
@RequestMapping("/api/v1/enrollments")
@Tag(name = "Enrollment API", description = "Endpoints for managing course enrollments and tracking progress")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private static final Logger LOG = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * Enroll the authenticated user in a course.
     * 
     * @param user     the authenticated user (injected by Spring Security)
     * @param courseId the ID of the course to enroll in
     * @return 201 Created with enrollment details
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Enroll in a course", description = "Enrolls the authenticated user in a specified course. Creates a new enrollment record with initial progress at 0%. Returns 409 Conflict if already enrolled.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully enrolled in course", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentDTO.class), examples = @ExampleObject(name = "Enrollment Created", value = """
                    {
                      "id": 1,
                      "courseId": 1,
                      "courseTitle": "English Basics (A1)",
                      "thumbnailUrl": "https://example.com/thumbnails/english-basics.jpg",
                      "cefrLevel": "A1",
                      "enrolledAt": "2025-11-05T10:30:00",
                      "progressPercentage": 0,
                      "completedAt": null,
                      "isCompleted": false
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid course ID", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Invalid Input", value = """
                    {
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Course ID must be a positive number",
                      "path": "/api/v1/enrollments",
                      "timestamp": "2025-11-05T10:30:00"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Full authentication is required to access this resource",
                      "path": "/api/v1/enrollments",
                      "timestamp": "2025-11-05T10:30:00"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Course Not Found", value = """
                    {
                      "status": 404,
                      "error": "Course Not Found",
                      "message": "Course with ID 999 not found",
                      "path": "/api/v1/enrollments",
                      "timestamp": "2025-11-05T10:30:00"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "Already enrolled in this course", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Already Enrolled", value = """
                    {
                      "status": 409,
                      "error": "Conflict",
                      "message": "User is already enrolled in course with ID 1",
                      "path": "/api/v1/enrollments",
                      "timestamp": "2025-11-05T10:30:00"
                    }
                    """)))
    })
    public ResponseEntity<EnrollmentDTO> enrollInCourse(
            @AuthenticationPrincipal User user,
            @Parameter(description = "ID of the course to enroll in", required = true, example = "1") @RequestParam Long courseId) {

        LOG.info("User {} enrolling in course {}", user.getEmail(), courseId);
        EnrollmentDTO enrollment = enrollmentService.enroll(user, courseId);
        LOG.info("User {} successfully enrolled in course {}", user.getEmail(), courseId);

        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    /**
     * Get all enrollments for the authenticated user.
     * 
     * @param user the authenticated user (injected by Spring Security)
     * @return 200 OK with list of enrollments (empty list if none)
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get my enrollments", description = "Retrieves all course enrollments for the authenticated user, ordered by enrollment date (most recent first).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved enrollments", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentDTO.class), examples = {
                    @ExampleObject(name = "With Enrollments", value = """
                            [
                              {
                                "id": 1,
                                "courseId": 1,
                                "courseTitle": "English Basics (A1)",
                                "thumbnailUrl": "https://example.com/thumbnails/english-basics.jpg",
                                "cefrLevel": "A1",
                                "enrolledAt": "2025-11-05T10:30:00",
                                "progressPercentage": 45,
                                "completedAt": null,
                                "isCompleted": false
                              },
                              {
                                "id": 2,
                                "courseId": 2,
                                "courseTitle": "Elementary English (A2)",
                                "thumbnailUrl": "https://example.com/thumbnails/elementary-english.jpg",
                                "cefrLevel": "A2",
                                "enrolledAt": "2025-11-01T09:15:00",
                                "progressPercentage": 100,
                                "completedAt": "2025-11-03T18:45:00",
                                "isCompleted": true
                              }
                            ]
                            """),
                    @ExampleObject(name = "No Enrollments", value = "[]")
            })),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Full authentication is required to access this resource",
                      "path": "/api/v1/enrollments",
                      "timestamp": "2025-11-05T10:30:00"
                    }
                    """)))
    })
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments(
            @AuthenticationPrincipal User user) {

        LOG.info("Fetching enrollments for user {}", user.getEmail());
        List<EnrollmentDTO> enrollments = enrollmentService.getMyEnrollments(user);
        LOG.info("Found {} enrollments for user {}", enrollments.size(), user.getEmail());

        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get detailed progress for a specific course enrollment.
     * 
     * @param user     the authenticated user (injected by Spring Security)
     * @param courseId the ID of the course
     * @return 200 OK with detailed course progress
     */
    @GetMapping(value = "/{courseId}/progress", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get course progress", description = "Retrieves detailed progress for a specific course enrollment, including lesson-by-lesson breakdown with status, scores, and attempts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved course progress", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseProgressDTO.class), examples = @ExampleObject(name = "Course Progress", value = """
                    {
                      "courseId": 1,
                      "courseTitle": "English Basics (A1)",
                      "cefrLevel": "A1",
                      "totalLessons": 18,
                      "completedLessons": 8,
                      "progressPercentage": 44,
                      "lessonProgress": [
                        {
                          "lessonId": 1,
                          "lessonTitle": "Introduction to English Alphabet",
                          "lessonType": "READING",
                          "sectionTitle": "Getting Started",
                          "status": "COMPLETED",
                          "score": 100,
                          "attempts": 1
                        },
                        {
                          "lessonId": 2,
                          "lessonTitle": "Basic Greetings",
                          "lessonType": "LISTENING",
                          "sectionTitle": "Getting Started",
                          "status": "IN_PROGRESS",
                          "score": null,
                          "attempts": 0
                        },
                        {
                          "lessonId": 3,
                          "lessonTitle": "Numbers 1-10",
                          "lessonType": "QUIZ",
                          "sectionTitle": "Numbers",
                          "status": "NOT_STARTED",
                          "score": null,
                          "attempts": 0
                        }
                      ]
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Full authentication is required to access this resource",
                      "path": "/api/v1/enrollments/1/progress",
                      "timestamp": "2025-11-05T10:30:00"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Course not found or not enrolled", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "Course Not Found", value = """
                            {
                              "status": 404,
                              "error": "Course Not Found",
                              "message": "Course with ID 999 not found",
                              "path": "/api/v1/enrollments/999/progress",
                              "timestamp": "2025-11-05T10:30:00"
                            }
                            """),
                    @ExampleObject(name = "Not Enrolled", value = """
                            {
                              "status": 404,
                              "error": "Enrollment Not Found",
                              "message": "User is not enrolled in course with ID 1",
                              "path": "/api/v1/enrollments/1/progress",
                              "timestamp": "2025-11-05T10:30:00"
                            }
                            """)
            }))
    })
    public ResponseEntity<CourseProgressDTO> getCourseProgress(
            @AuthenticationPrincipal User user,
            @Parameter(description = "ID of the course", required = true, example = "1") @PathVariable Long courseId) {

        LOG.info("Fetching course progress for user {} in course {}", user.getEmail(), courseId);
        CourseProgressDTO progress = enrollmentService.getCourseProgress(user, courseId);
        LOG.info("Retrieved course progress: {}% complete ({}/{})", progress.getProgressPercentage(),
                progress.getCompletedLessons(), progress.getTotalLessons());

        return ResponseEntity.ok(progress);
    }
}
