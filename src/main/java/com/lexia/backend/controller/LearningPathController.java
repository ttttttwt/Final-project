package com.lexia.backend.controller;

import com.lexia.backend.dto.LearningPathDTO;
import com.lexia.backend.dto.UserPathProgressDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.LearningPathService;
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
 * REST Controller for Learning Path Management.
 * Handles learning path retrieval, recommendations, enrollment, and progress
 * tracking.
 * 
 * <p>
 * Base path: /api/v1/learning-paths
 * </p>
 * 
 * <p>
 * Security:
 * </p>
 * <ul>
 * <li>All endpoints require authentication</li>
 * <li>All operations available to authenticated users</li>
 * <li>Users can only view their own progress</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@RestController
@RequestMapping("/api/v1/learning-paths")
@Tag(name = "Learning Path API", description = "Endpoints for managing learning paths, including browsing, recommendations, enrollment, and progress tracking")
@SecurityRequirement(name = "bearerAuth")
public class LearningPathController {

    private static final Logger LOG = LoggerFactory.getLogger(LearningPathController.class);

    private final LearningPathService learningPathService;

    public LearningPathController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    /**
     * Get all learning paths.
     * Available to all authenticated users.
     */
    @Operation(summary = "Get all learning paths", description = "Retrieves a list of all available learning paths organized by CEFR level (A1-C2). "
            + "Each path contains a curated sequence of courses designed to guide learners through a specific proficiency level.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved learning paths", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LearningPathDTO.class), examples = @ExampleObject(name = "Success Response", value = """
                    [
                      {
                        "id": 1,
                        "name": "Beginner Path (A1)",
                        "description": "A comprehensive path for complete beginners starting their English learning journey",
                        "cefrLevel": "A1",
                        "isDefault": true,
                        "courses": [
                          {
                            "courseId": 1,
                            "courseTitle": "English Basics (A1)",
                            "courseThumbnailUrl": "https://cdn.lexia.com/courses/a1-basics.jpg",
                            "courseCefrLevel": "A1",
                            "orderIndex": 0,
                            "sectionCount": 3
                          }
                        ],
                        "totalCourses": 1,
                        "estimatedHours": 15,
                        "createdAt": "2025-11-03T10:00:00",
                        "updatedAt": "2025-11-03T10:00:00"
                      }
                    ]
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping
    public ResponseEntity<List<LearningPathDTO>> getAllPaths() {
        LOG.info("Fetching all learning paths");

        List<LearningPathDTO> paths = learningPathService.getAllPaths();

        LOG.info("Retrieved {} learning paths", paths.size());

        return ResponseEntity.ok(paths);
    }

    /**
     * Get learning path by ID.
     * Available to all authenticated users.
     */
    @Operation(summary = "Get learning path by ID", description = "Retrieves detailed information about a specific learning path including all associated courses in sequence.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Learning path found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LearningPathDTO.class), examples = @ExampleObject(name = "Success Response", value = """
                    {
                      "id": 4,
                      "name": "Upper Intermediate Path (B2)",
                      "description": "Develop fluency in complex conversations and detailed texts",
                      "cefrLevel": "B2",
                      "isDefault": true,
                      "courses": [
                        {
                          "courseId": 2,
                          "courseTitle": "Intermediate English (B1)",
                          "courseThumbnailUrl": "https://cdn.lexia.com/courses/b1-intermediate.jpg",
                          "courseCefrLevel": "B1",
                          "orderIndex": 0,
                          "sectionCount": 2
                        },
                        {
                          "courseId": 3,
                          "courseTitle": "Advanced English (C1)",
                          "courseThumbnailUrl": "https://cdn.lexia.com/courses/c1-advanced.jpg",
                          "courseCefrLevel": "C1",
                          "orderIndex": 1,
                          "sectionCount": 2
                        }
                      ],
                      "totalCourses": 2,
                      "estimatedHours": 30,
                      "createdAt": "2025-11-03T10:00:00",
                      "updatedAt": "2025-11-03T10:00:00"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Learning path not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    public ResponseEntity<LearningPathDTO> getPathById(
            @Parameter(description = "Learning path ID", required = true, example = "1") @PathVariable Long id) {

        LOG.info("Fetching learning path by ID: {}", id);

        LearningPathDTO path = learningPathService.getPathById(id);

        LOG.info("Retrieved learning path: {}", path.getName());

        return ResponseEntity.ok(path);
    }

    /**
     * Get recommended learning path for current user.
     * Available to all authenticated users.
     */
    @Operation(summary = "Get recommended learning path", description = "Returns a personalized learning path recommendation based on the user's current CEFR level. "
            + "If no level is set in the user profile, defaults to A1 (beginner). "
            + "Future enhancement: Will consider completed paths for progressive recommendations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommended path found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LearningPathDTO.class), examples = @ExampleObject(name = "Success Response", value = """
                    {
                      "id": 1,
                      "name": "Beginner Path (A1)",
                      "description": "A comprehensive path for complete beginners",
                      "cefrLevel": "A1",
                      "isDefault": true,
                      "courses": [
                        {
                          "courseId": 1,
                          "courseTitle": "English Basics (A1)",
                          "courseThumbnailUrl": "https://cdn.lexia.com/courses/a1-basics.jpg",
                          "courseCefrLevel": "A1",
                          "orderIndex": 0,
                          "sectionCount": 3
                        }
                      ],
                      "totalCourses": 1,
                      "estimatedHours": 15,
                      "createdAt": "2025-11-03T10:00:00",
                      "updatedAt": "2025-11-03T10:00:00"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "No suitable learning path found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/recommend")
    public ResponseEntity<LearningPathDTO> getRecommendedPath(
            @AuthenticationPrincipal User user) {

        LOG.info("Getting recommended learning path for user: {}", user.getEmail());

        LearningPathDTO path = learningPathService.getRecommendedPath(user);

        LOG.info("Recommended path: {} for user: {}", path.getName(), user.getEmail());

        return ResponseEntity.ok(path);
    }

    /**
     * Start a learning path.
     * Available to all authenticated users.
     */
    @Operation(summary = "Start a learning path", description = "Enrolls the current user in a learning path and sets the starting point to the first course. "
            + "Users can be enrolled in multiple paths simultaneously. "
            + "Returns 409 Conflict if user already started this path.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully started learning path", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserPathProgressDTO.class), examples = @ExampleObject(name = "Success Response", value = """
                    {
                      "enrollmentId": 1,
                      "pathId": 1,
                      "pathName": "Beginner Path (A1)",
                      "pathCefrLevel": "A1",
                      "currentCourseId": 1,
                      "currentCourseTitle": "English Basics (A1)",
                      "coursesCompleted": 0,
                      "totalCourses": 1,
                      "progressPercentage": 0,
                      "startedAt": "2025-11-03T15:30:00",
                      "completedAt": null,
                      "isCompleted": false
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Learning path not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "409", description = "User already started this learning path", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Conflict Response", value = """
                    {
                      "type": "about:blank",
                      "title": "Conflict",
                      "status": 409,
                      "detail": "You have already started this learning path",
                      "instance": "/api/v1/learning-paths/1/start"
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping("/{id}/start")
    public ResponseEntity<UserPathProgressDTO> startPath(
            @Parameter(description = "Learning path ID", required = true, example = "1") @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        LOG.info("User {} starting learning path with ID: {}", user.getEmail(), id);

        UserPathProgressDTO progress = learningPathService.startPath(user, id);

        LOG.info("User {} successfully started learning path: {}", user.getEmail(), progress.getPathName());

        return ResponseEntity.status(HttpStatus.CREATED).body(progress);
    }

    /**
     * Get current user's learning path progress.
     * Available to all authenticated users.
     */
    @Operation(summary = "Get my learning path progress", description = "Retrieves all learning paths the current user has started, including progress details for each. "
            + "Returns both active (incomplete) and completed paths.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved progress", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserPathProgressDTO.class), examples = @ExampleObject(name = "Success Response", value = """
                    [
                      {
                        "enrollmentId": 1,
                        "pathId": 1,
                        "pathName": "Beginner Path (A1)",
                        "pathCefrLevel": "A1",
                        "currentCourseId": 1,
                        "currentCourseTitle": "English Basics (A1)",
                        "coursesCompleted": 0,
                        "totalCourses": 1,
                        "progressPercentage": 0,
                        "startedAt": "2025-11-03T15:30:00",
                        "completedAt": null,
                        "isCompleted": false
                      },
                      {
                        "enrollmentId": 2,
                        "pathId": 3,
                        "pathName": "Intermediate Path (B1)",
                        "pathCefrLevel": "B1",
                        "currentCourseId": 2,
                        "currentCourseTitle": "Intermediate English (B1)",
                        "coursesCompleted": 0,
                        "totalCourses": 1,
                        "progressPercentage": 0,
                        "startedAt": "2025-11-03T16:00:00",
                        "completedAt": null,
                        "isCompleted": false
                      }
                    ]
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/my-progress")
    public ResponseEntity<List<UserPathProgressDTO>> getMyProgress(
            @AuthenticationPrincipal User user) {

        LOG.info("Fetching learning path progress for user: {}", user.getEmail());

        List<UserPathProgressDTO> progress = learningPathService.getMyProgress(user);

        LOG.info("User {} has {} learning path enrollments", user.getEmail(), progress.size());

        return ResponseEntity.ok(progress);
    }
}
