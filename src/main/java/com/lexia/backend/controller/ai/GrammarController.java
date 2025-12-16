package com.lexia.backend.controller.ai;

import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.ai.GrammarExerciseService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for AI-Powered Grammar Exercise Operations.
 * Provides endpoints for generating, submitting, and tracking grammar exercises.
 * 
 * <p>Base path: /api/v1/ai/grammar</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>AI-generated grammar exercises via Google Gemini</li>
 *   <li>CEFR-level appropriate difficulty</li>
 *   <li>Multiple exercise types: MCQ, fill-in-blank, transformation, error-correction</li>
 *   <li>Answer validation and scoring with 70% passing threshold</li>
 *   <li>Progress tracking and statistics</li>
 *   <li>Fallback content support when AI is unavailable</li>
 * </ul>
 * 
 * <p>Security:</p>
 * <ul>
 *   <li>All endpoints require JWT authentication</li>
 *   <li>Users can only access their own exercise history</li>
 *   <li>AI operations are rate-limited per user (50 exercises/day)</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@RestController
@RequestMapping("/api/v1/ai/grammar")
@Tag(name = "Grammar API", description = "AI-powered grammar exercise generation and practice")
@SecurityRequirement(name = "bearerAuth")
public class GrammarController {

    private static final Logger LOG = LoggerFactory.getLogger(GrammarController.class);

    private static final int MAX_PAGE_SIZE = 100;

    private final GrammarExerciseService grammarExerciseService;

    public GrammarController(GrammarExerciseService grammarExerciseService) {
        this.grammarExerciseService = grammarExerciseService;
    }

    // ========== Generation Endpoints ==========

    /**
     * Generate grammar exercises using AI.
     * 
     * <p>Uses Google Gemini AI to generate grammar exercises based on the specified
     * topic, CEFR level, and optional theme. If AI generation fails, fallback content is used.</p>
     * 
     * @param user the authenticated user
     * @param request the generation request containing grammar topic, CEFR level, etc.
     * @return 201 Created with generated exercise set
     */
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Generate grammar exercises",
        description = "Uses AI to generate grammar exercises based on topic, CEFR level, and theme. " +
                      "Includes explanation, exercises of various types (MCQ, fill-in-blank, transformation, error-correction), " +
                      "and detailed answer explanations. Falls back to pre-seeded content if AI is unavailable."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Exercises generated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GrammarExerciseSetDTO.class),
                examples = @ExampleObject(name = "Generated Exercises", value = """
                    {
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "grammarPoint": "Present Simple",
                      "cefrLevel": "B1",
                      "theme": "workplace",
                      "exerciseCount": 5,
                      "timeLimitSeconds": 600,
                      "isFallback": false,
                      "createdAt": "2025-12-13T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation failed",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded - daily grammar quota reached (50/day)",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<GrammarExerciseSetDTO> generateExercises(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody GrammarRequestDTO request) {

        LOG.info("User {} generating grammar exercises for '{}' at level {}", 
                user.getEmail(), request.getGrammarTopic(), request.getCefrLevel());

        GrammarExerciseSetDTO exerciseSet = grammarExerciseService.generateExercises(request, user.getId());

        LOG.info("User {} generated {} exercises for '{}' (fallback: {})", 
                user.getEmail(), exerciseSet.getExerciseCount(), 
                exerciseSet.getGrammarPoint(), exerciseSet.getIsFallback());

        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseSet);
    }

    // ========== Topic Endpoints ==========

    /**
     * List all available grammar topics.
     * 
     * @return list of all active grammar topics
     */
    @GetMapping(value = "/topics", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "List grammar topics",
        description = "Returns all available grammar topics for exercise generation. " +
                      "Topics are organized by category (Tenses, Modals, etc.) and tagged with applicable CEFR levels."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GrammarTopicDTO.class),
                examples = @ExampleObject(name = "Topics List", value = """
                    [
                      {
                        "id": 1,
                        "name": "Present Simple",
                        "category": "Tenses",
                        "cefrLevels": ["A1", "A2"],
                        "description": "Used for habits, routines, and general truths",
                        "isActive": true
                      }
                    ]
                    """)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<GrammarTopicDTO>> getAllTopics() {
        LOG.debug("Retrieving all grammar topics");
        List<GrammarTopicDTO> topics = grammarExerciseService.getAllTopics();
        return ResponseEntity.ok(topics);
    }

    /**
     * List grammar topics filtered by CEFR level.
     * 
     * @param level the CEFR level to filter by
     * @return list of topics applicable to the specified level
     */
    @GetMapping(value = "/topics/level/{level}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "List topics by CEFR level",
        description = "Returns grammar topics applicable to the specified CEFR level (A1-C2)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid CEFR level"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<GrammarTopicDTO>> getTopicsByLevel(
            @Parameter(description = "CEFR level (A1, A2, B1, B2, C1, C2)", example = "B1")
            @PathVariable String level) {

        validateCefrLevel(level);
        LOG.debug("Retrieving grammar topics for level {}", level);
        List<GrammarTopicDTO> topics = grammarExerciseService.getTopicsByLevel(level);
        return ResponseEntity.ok(topics);
    }

    /**
     * List grammar topics filtered by category.
     * 
     * @param category the category to filter by
     * @return list of topics in the specified category
     */
    @GetMapping(value = "/topics/category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "List topics by category",
        description = "Returns grammar topics in the specified category (Tenses, Modals, etc.)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<GrammarTopicDTO>> getTopicsByCategory(
            @Parameter(description = "Category name", example = "Tenses")
            @PathVariable String category) {

        LOG.debug("Retrieving grammar topics for category '{}'", category);
        List<GrammarTopicDTO> topics = grammarExerciseService.getTopicsByCategory(category);
        return ResponseEntity.ok(topics);
    }

    /**
     * Get all distinct topic categories.
     * 
     * @return list of category names
     */
    @GetMapping(value = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "List topic categories",
        description = "Returns all distinct grammar topic categories."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<String>> getCategories() {
        LOG.debug("Retrieving grammar categories");
        List<String> categories = grammarExerciseService.getCategories();
        return ResponseEntity.ok(categories);
    }

    // ========== Exercise Set Endpoints ==========

    /**
     * Get an exercise set by ID.
     * 
     * @param id the exercise set ID
     * @return the exercise set with all exercises
     */
    @GetMapping(value = "/exercises/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get exercise set",
        description = "Retrieves a complete exercise set by ID, including all exercises and the explanation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise set retrieved successfully",
            content = @Content(schema = @Schema(implementation = GrammarExerciseSetDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Exercise set not found")
    })
    public ResponseEntity<GrammarExerciseSetDTO> getExerciseSet(
            @Parameter(description = "Exercise set UUID")
            @PathVariable UUID id) {

        LOG.debug("Retrieving exercise set {}", id);
        GrammarExerciseSetDTO exerciseSet = grammarExerciseService.getExerciseSet(id);
        return ResponseEntity.ok(exerciseSet);
    }

    /**
     * List user's exercise sets with pagination.
     * 
     * @param user the authenticated user
     * @param page page number (0-based)
     * @param size page size (default 20, max 100)
     * @return page of exercise sets
     */
    @GetMapping(value = "/exercises", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "List user exercise sets",
        description = "Returns paginated list of exercise sets generated for the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exercise sets retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<GrammarExerciseSetDTO>> getUserExerciseSets(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "20") int size) {

        int validatedSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, validatedSize, Sort.by("createdAt").descending());

        LOG.debug("User {} listing exercise sets (page {}, size {})", user.getEmail(), page, validatedSize);
        Page<GrammarExerciseSetDTO> exerciseSets = grammarExerciseService.getUserExerciseSets(user.getId(), pageable);

        return ResponseEntity.ok(exerciseSets);
    }

    // ========== Submission Endpoints ==========

    /**
     * Submit answers for an exercise set.
     * 
     * <p>Validates each answer, calculates the score, and saves the progress.
     * A score of 70% or higher is required to pass.</p>
     * 
     * @param user the authenticated user
     * @param exerciseSetId the exercise set ID
     * @param submission the answer submission
     * @return the result with score and feedback
     */
    @PostMapping(value = "/exercises/{exerciseSetId}/submit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Submit exercise answers",
        description = "Submits answers for an exercise set and returns detailed results. " +
                      "Calculates score, provides feedback per question, and tracks progress. " +
                      "Passing threshold is 70%."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Answers submitted and scored successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GrammarResultDTO.class),
                examples = @ExampleObject(name = "Result", value = """
                    {
                      "exerciseSetId": "550e8400-e29b-41d4-a716-446655440000",
                      "grammarPoint": "Present Simple",
                      "cefrLevel": "B1",
                      "score": 4,
                      "maxScore": 5,
                      "percentage": 80.00,
                      "passed": true,
                      "timeSpentSeconds": 180,
                      "feedback": [
                        {
                          "questionIndex": 0,
                          "userAnswer": "goes",
                          "correctAnswer": "goes",
                          "correct": true,
                          "explanation": "Correct! Third person singular uses 'goes'."
                        }
                      ],
                      "encouragement": "Great job! You have a solid understanding of Present Simple."
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid submission"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Exercise set not found"),
        @ApiResponse(responseCode = "409", description = "Already submitted")
    })
    public ResponseEntity<GrammarResultDTO> submitAnswers(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Exercise set UUID")
            @PathVariable UUID exerciseSetId,
            @Valid @RequestBody GrammarAnswerDTO submission) {

        // Ensure submission has correct exerciseSetId
        submission.setExerciseSetId(exerciseSetId);

        LOG.info("User {} submitting answers for exercise set {}", user.getEmail(), exerciseSetId);

        GrammarResultDTO result = grammarExerciseService.submitAnswers(exerciseSetId, user.getId(), submission);

        LOG.info("User {} scored {}/{} ({}%) on exercise set {} - {}", 
                user.getEmail(), result.getScore(), result.getMaxScore(),
                result.getPercentage(), exerciseSetId, 
                result.getPassed() ? "PASSED" : "FAILED");

        return ResponseEntity.ok(result);
    }

    /**
     * Check if user has already submitted for an exercise set.
     * 
     * @param user the authenticated user
     * @param exerciseSetId the exercise set ID
     * @return true if already submitted
     */
    @GetMapping(value = "/exercises/{exerciseSetId}/submitted", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Check if submitted",
        description = "Checks if the user has already submitted answers for an exercise set."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Boolean> hasSubmitted(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Exercise set UUID")
            @PathVariable UUID exerciseSetId) {

        boolean submitted = grammarExerciseService.hasSubmitted(exerciseSetId, user.getId());
        return ResponseEntity.ok(submitted);
    }

    // ========== History and Statistics Endpoints ==========

    /**
     * Get user's exercise history with pagination.
     * 
     * @param user the authenticated user
     * @param page page number (0-based)
     * @param size page size (default 20, max 100)
     * @return page of progress records
     */
    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get exercise history",
        description = "Returns paginated history of completed exercise sets with scores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<GrammarProgressDTO>> getHistory(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "20") int size) {

        int validatedSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, validatedSize, Sort.by("completedAt").descending());

        LOG.debug("User {} retrieving exercise history (page {}, size {})", user.getEmail(), page, validatedSize);
        Page<GrammarProgressDTO> history = grammarExerciseService.getHistory(user.getId(), pageable);

        return ResponseEntity.ok(history);
    }

    /**
     * Get user's grammar exercise statistics.
     * 
     * @param user the authenticated user
     * @return statistics summary
     */
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get user statistics",
        description = "Returns aggregated statistics including total attempts, pass rate, " +
                      "average score, topic breakdown, and streak information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GrammarStatsDTO.class),
                examples = @ExampleObject(name = "Stats", value = """
                    {
                      "userId": "123e4567-e89b-12d3-a456-426614174000",
                      "totalAttempted": 25,
                      "totalCompleted": 20,
                      "totalPassed": 18,
                      "averageScore": 78.50,
                      "totalTimeSpentSeconds": 7200,
                      "currentStreak": 5,
                      "longestStreak": 14
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<GrammarStatsDTO> getStatistics(
            @AuthenticationPrincipal User user) {

        LOG.debug("User {} retrieving grammar statistics", user.getEmail());
        GrammarStatsDTO stats = grammarExerciseService.getStatistics(user.getId());

        return ResponseEntity.ok(stats);
    }

    /**
     * Get progress for a specific exercise set.
     * 
     * @param user the authenticated user
     * @param exerciseSetId the exercise set ID
     * @return the progress record if exists
     */
    @GetMapping(value = "/exercises/{exerciseSetId}/progress", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get exercise progress",
        description = "Retrieves the user's progress for a specific exercise set."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Progress not found")
    })
    public ResponseEntity<GrammarProgressDTO> getProgress(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Exercise set UUID")
            @PathVariable UUID exerciseSetId) {

        LOG.debug("User {} retrieving progress for exercise set {}", user.getEmail(), exerciseSetId);
        GrammarProgressDTO progress = grammarExerciseService.getProgress(exerciseSetId, user.getId());

        if (progress == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(progress);
    }

    // ========== Fallback Endpoint ==========

    /**
     * Get fallback exercises for a topic and level.
     * 
     * @param topic the grammar topic
     * @param level the CEFR level
     * @return fallback exercise set
     */
    @GetMapping(value = "/fallback", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get fallback exercises",
        description = "Returns pre-seeded fallback exercises for when AI is unavailable or explicitly requested."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fallback exercises retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid CEFR level"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "No fallback content available")
    })
    public ResponseEntity<GrammarExerciseSetDTO> getFallbackExercises(
            @Parameter(description = "Grammar topic", example = "Present Simple")
            @RequestParam String topic,
            @Parameter(description = "CEFR level", example = "B1")
            @RequestParam String level) {

        validateCefrLevel(level);
        LOG.debug("Retrieving fallback exercises for '{}' at level {}", topic, level);

        GrammarExerciseSetDTO fallback = grammarExerciseService.getFallbackExercises(topic, level);

        if (fallback == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(fallback);
    }

    // ========== Helper Methods ==========

    /**
     * Validates that a string is a valid CEFR level.
     * 
     * @param level the level to validate
     * @throws IllegalArgumentException if invalid
     */
    private void validateCefrLevel(String level) {
        if (level == null || !level.matches("^(A1|A2|B1|B2|C1|C2)$")) {
            throw new IllegalArgumentException("Invalid CEFR level: " + level + 
                    ". Must be one of: A1, A2, B1, B2, C1, C2");
        }
    }
}
