package com.lexia.backend.controller;

import com.lexia.backend.dto.CompleteLessonRequest;
import com.lexia.backend.dto.LessonProgressDTO;
import com.lexia.backend.dto.StreakDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.ProgressService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Lesson Progress and Streak Management.
 * Handles lesson completion tracking and learning streak calculation.
 * 
 * <p>
 * Base path: /api/v1/progress
 * </p>
 * 
 * <p>
 * Security:
 * </p>
 * <ul>
 * <li>All endpoints require authentication</li>
 * <li>Users can only manage their own progress</li>
 * <li>Users can only view their own streak data</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@RestController
@RequestMapping("/api/v1/progress")
@Tag(name = "Progress API", description = "Endpoints for tracking lesson completion and learning streaks")
@SecurityRequirement(name = "bearerAuth")
public class ProgressController {

    private static final Logger LOG = LoggerFactory.getLogger(ProgressController.class);

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * Mark a lesson as complete with result details.
     * 
     * @param user     the authenticated user (injected by Spring Security)
     * @param lessonId the ID of the lesson to mark complete
     * @param request  the completion request containing result details as JSON
     * @return 201 Created with updated lesson progress
     */
    @PostMapping(value = "/lessons/{lessonId}/complete", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mark lesson as complete", description = "Records lesson completion for the authenticated user. Stores result details as JSONB, increments attempt counter, and triggers enrollment progress recalculation. The result details JSON structure varies by lesson type (READING, LISTENING, QUIZ, SPEAKING).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lesson marked as complete successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LessonProgressDTO.class), examples = @ExampleObject(name = "Lesson Completed", value = """
                    {
                      "id": "123e4567-e89b-12d3-a456-426614174000",
                      "lessonId": 1,
                      "lessonTitle": "Introduction to English Alphabet",
                      "lessonType": "READING",
                      "status": "COMPLETED",
                      "score": 100,
                      "attempts": 1,
                      "startedAt": "2025-11-05T10:30:00",
                      "completedAt": "2025-11-05T10:45:00",
                      "resultDetails": {
                        "score": 100,
                        "correctAnswers": 20,
                        "totalQuestions": 20,
                        "timeSpent": 900,
                        "comprehensionScore": 95
                      },
                      "updatedAt": "2025-11-05T10:45:00"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request - malformed JSON or invalid lesson ID", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "Invalid JSON", value = """
                            {
                              "status": 400,
                              "error": "Bad Request",
                              "message": "Invalid JSON format in resultDetailsJson",
                              "path": "/api/v1/progress/lessons/1/complete",
                              "timestamp": "2025-11-05T10:30:00"
                            }
                            """),
                    @ExampleObject(name = "Validation Error", value = """
                            {
                              "status": 400,
                              "error": "Validation Failed",
                              "message": "Input validation failed. Please check the validation errors.",
                              "path": "/api/v1/progress/lessons/1/complete",
                              "timestamp": "2025-11-05T10:30:00",
                              "validationErrors": [
                                {
                                  "field": "resultDetailsJson",
                                  "rejectedValue": null,
                                  "message": "Result details JSON is required"
                                }
                              ]
                            }
                            """)
            })),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Full authentication is required to access this resource",
                      "path": "/api/v1/progress/lessons/1/complete",
                      "timestamp": "2025-11-05T10:30:00"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Lesson not found or not enrolled in course", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "Lesson Not Found", value = """
                            {
                              "status": 404,
                              "error": "Lesson Not Found",
                              "message": "Lesson with ID 999 not found",
                              "path": "/api/v1/progress/lessons/999/complete",
                              "timestamp": "2025-11-05T10:30:00"
                            }
                            """),
                    @ExampleObject(name = "Not Enrolled", value = """
                            {
                              "status": 404,
                              "error": "Enrollment Not Found",
                              "message": "User is not enrolled in the course containing this lesson",
                              "path": "/api/v1/progress/lessons/1/complete",
                              "timestamp": "2025-11-05T10:30:00"
                            }
                            """)
            }))
    })
    public ResponseEntity<LessonProgressDTO> completeLesson(
            @AuthenticationPrincipal User user,
            @Parameter(description = "ID of the lesson to mark complete", required = true, example = "1") @PathVariable Long lessonId,
            @Valid @RequestBody CompleteLessonRequest request) {

        LOG.info("User {} completing lesson {}", user.getEmail(), lessonId);
        LessonProgressDTO progress = progressService.completeLesson(user, lessonId, request.getResultDetailsJson());
        LOG.info("User {} successfully completed lesson {} with score: {}", user.getEmail(), lessonId,
                progress.getScore());

        return ResponseEntity.status(HttpStatus.CREATED).body(progress);
    }

    /**
     * Get the authenticated user's progress for a specific course.
     * 
     * @param user     the authenticated user (injected by Spring Security)
     * @param courseId the course ID
     * @return 200 OK with course progress information
     */
    @GetMapping(value = "/courses/{courseId}/lessons", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get course progress", description = "Retrieves the completion status of all lessons in a specific course for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved course progress", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.lexia.backend.dto.CourseProgressDTO.class))),
            @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Course Not Found", value = """
                    {
                      "status": 404,
                      "error": "Resource Not Found",
                      "message": "Course not found with id: 1",
                      "path": "/api/v1/progress/courses/1/lessons",
                      "timestamp": "2025-11-05T10:30:00"
                    }
                    """)))
    })
    public ResponseEntity<com.lexia.backend.dto.CourseProgressDTO> getCourseProgress(
            @AuthenticationPrincipal User user,
            @Parameter(description = "ID of the course", required = true, example = "1") @PathVariable Long courseId) {

        LOG.info("Fetching progress for user {} in course {}", user.getEmail(), courseId);
        com.lexia.backend.dto.CourseProgressDTO progress = progressService.getCourseProgress(user, courseId);
        
        return ResponseEntity.ok(progress);
    }

    /**
     * Get the authenticated user's learning streak.
     * 
     * @param user the authenticated user (injected by Spring Security)
     * @return 200 OK with streak information
     */
    @GetMapping(value = "/streak", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get learning streak", description = "Calculates the user's current and longest learning streaks. A streak is defined as consecutive days with at least one lesson completion. Uses timezone-aware date calculations to determine 'today' correctly.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved streak information", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StreakDTO.class), examples = {
                    @ExampleObject(name = "Active Streak", value = """
                            {
                              "currentStreak": 7,
                              "longestStreak": 15,
                              "lastActivityDate": "2025-11-05",
                              "isActiveToday": true,
                              "totalActiveDays": 42
                            }
                            """),
                    @ExampleObject(name = "Inactive Streak", value = """
                            {
                              "currentStreak": 0,
                              "longestStreak": 15,
                              "lastActivityDate": "2025-11-03",
                              "isActiveToday": false,
                              "totalActiveDays": 42
                            }
                            """),
                    @ExampleObject(name = "No Activity", value = """
                            {
                              "currentStreak": 0,
                              "longestStreak": 0,
                              "lastActivityDate": null,
                              "isActiveToday": false,
                              "totalActiveDays": 0
                            }
                            """)
            })),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Full authentication is required to access this resource",
                      "path": "/api/v1/progress/streak",
                      "timestamp": "2025-11-05T10:30:00"
                    }
                    """)))
    })
    public ResponseEntity<StreakDTO> getStreak(
            @AuthenticationPrincipal User user) {

        LOG.info("Fetching learning streak for user {}", user.getEmail());
        StreakDTO streak = progressService.getStreak(user);
        LOG.info("User {} has current streak: {} days, longest: {} days", user.getEmail(), streak.getCurrentStreak(),
                streak.getLongestStreak());

        return ResponseEntity.ok(streak);
    }
}
