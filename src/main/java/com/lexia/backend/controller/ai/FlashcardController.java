package com.lexia.backend.controller.ai;

import com.lexia.backend.dto.ai.*;
import com.lexia.backend.entity.User;
import com.lexia.backend.service.ai.FlashcardService;
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

import java.util.UUID;

/**
 * REST Controller for AI-Powered Flashcard Operations.
 * Provides endpoints for generating, managing, and studying flashcard decks.
 * 
 * <p>Base path: /api/v1/ai/flashcards</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>AI-generated flashcards from lesson content</li>
 *   <li>Manual deck creation and management</li>
 *   <li>SM-2 spaced repetition study sessions</li>
 *   <li>Progress tracking with mastery levels</li>
 * </ul>
 * 
 * <p>Security:</p>
 * <ul>
 *   <li>All endpoints require JWT authentication</li>
 *   <li>Users can only access their own decks</li>
 *   <li>AI operations are rate-limited per user</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@RestController
@RequestMapping("/api/v1/ai/flashcards")
@Tag(name = "Flashcard API", description = "AI-powered flashcard generation and spaced repetition study")
@SecurityRequirement(name = "bearerAuth")
public class FlashcardController {

    private static final Logger LOG = LoggerFactory.getLogger(FlashcardController.class);

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    // ========== Generation Endpoints ==========

    /**
     * Generate flashcards from lesson content using AI.
     * 
     * <p>Uses Google Gemini AI to extract vocabulary from lesson content
     * and generate comprehensive flashcards with definitions, examples,
     * and pronunciation.</p>
     * 
     * @param user the authenticated user
     * @param request the generation request with lesson ID and options
     * @return 201 Created with generated flashcard deck
     */
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Generate flashcards from lesson",
        description = "Uses AI to generate flashcards from completed lesson content. " +
                      "Extracts key vocabulary and creates cards with definitions, " +
                      "example sentences, and pronunciation (IPA)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Flashcards generated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FlashcardDeckDTO.class),
                examples = @ExampleObject(name = "Generated Deck", value = """
                    {
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "userId": "123e4567-e89b-12d3-a456-426614174000",
                      "title": "Lesson 5: Business Negotiations",
                      "description": "AI-generated vocabulary from lesson content",
                      "sourceType": "LESSON",
                      "sourceId": 5,
                      "cefrLevel": "B2",
                      "cardCount": 20,
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
            responseCode = "404",
            description = "Lesson not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Deck already exists for this lesson",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded - daily flashcard quota reached",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<FlashcardDeckDTO> generateFromLesson(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody GenerateFlashcardsDTO request) {

        LOG.info("User {} generating flashcards from lesson {}", user.getEmail(), request.getLessonId());

        FlashcardDeckDTO deck = flashcardService.generateFromLesson(request, user.getId());

        LOG.info("User {} generated deck '{}' with {} cards", 
                user.getEmail(), deck.getTitle(), deck.getCardCount());

        return ResponseEntity.status(HttpStatus.CREATED).body(deck);
    }

    // ========== Deck CRUD Endpoints ==========

    /**
     * Create a new flashcard deck manually.
     * 
     * @param user the authenticated user
     * @param request the deck creation request
     * @return 201 Created with the new deck
     */
    @PostMapping(value = "/decks", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Create custom flashcard deck",
        description = "Creates a new flashcard deck with optional initial cards. " +
                      "Use sourceType=USER_CREATED for manual decks."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Deck created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FlashcardDeckDTO.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<FlashcardDeckDTO> createDeck(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateFlashcardDeckDTO request) {

        LOG.info("User {} creating flashcard deck '{}'", user.getEmail(), request.getTitle());

        // Validate source type constraints
        request.validate();

        FlashcardDeckDTO deck = flashcardService.createDeck(request, user.getId());

        LOG.info("User {} created deck '{}' (ID: {})", 
                user.getEmail(), deck.getTitle(), deck.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(deck);
    }

    /**
     * List all flashcard decks for the authenticated user.
     * 
     * @param user the authenticated user
     * @param page page number (0-based)
     * @param size page size (default 20, max 100)
     * @param sort sort field (default: createdAt)
     * @param direction sort direction (ASC or DESC)
     * @return 200 OK with paginated deck list
     */
    @GetMapping(value = "/decks", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "List user's flashcard decks",
        description = "Retrieves all flashcard decks owned by the authenticated user with pagination."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved deck list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<FlashcardDeckDTO>> getUserDecks(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") 
            @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction") 
            @RequestParam(defaultValue = "DESC") String direction) {

        LOG.debug("User {} fetching decks (page={}, size={})", user.getEmail(), page, size);

        // Validate and cap page size
        size = Math.min(size, 100);

        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(direction), sort));

        Page<FlashcardDeckDTO> decks = flashcardService.getUserDecks(user.getId(), pageable);

        return ResponseEntity.ok(decks);
    }

    /**
     * Get a specific flashcard deck by ID.
     * 
     * @param user the authenticated user
     * @param deckId the deck UUID
     * @return 200 OK with deck details including cards
     */
    @GetMapping(value = "/decks/{deckId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get flashcard deck details",
        description = "Retrieves a specific deck with all its flashcards."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved deck",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FlashcardDeckDTO.class),
                examples = @ExampleObject(name = "Deck with Cards", value = """
                    {
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "title": "Business Meeting Vocabulary",
                      "description": "Key phrases for meetings",
                      "sourceType": "AI_GENERATED",
                      "cefrLevel": "B2",
                      "cardCount": 25,
                      "cards": [
                        {
                          "front": "agenda",
                          "back": "a list of topics to be discussed at a meeting",
                          "example": "Let's review the agenda before the meeting.",
                          "pronunciation": "/əˈdʒendə/"
                        }
                      ],
                      "dueCount": 5,
                      "masteredCount": 10
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(
            responseCode = "404",
            description = "Deck not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "status": 404,
                      "error": "Not Found",
                      "message": "Flashcard deck not found: 550e8400-e29b-41d4-a716-446655440000"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<FlashcardDeckDTO> getDeck(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Deck UUID", required = true) 
            @PathVariable UUID deckId) {

        LOG.debug("User {} fetching deck {}", user.getEmail(), deckId);

        FlashcardDeckDTO deck = flashcardService.getDeck(deckId, user.getId());

        return ResponseEntity.ok(deck);
    }

    /**
     * Update an existing flashcard deck.
     * 
     * @param user the authenticated user
     * @param deckId the deck UUID
     * @param request the update request
     * @return 200 OK with updated deck
     */
    @PutMapping(value = "/decks/{deckId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Update flashcard deck",
        description = "Updates deck metadata or cards. Only non-null fields are updated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Deck updated successfully",
            content = @Content(schema = @Schema(implementation = FlashcardDeckDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Deck not found")
    })
    public ResponseEntity<FlashcardDeckDTO> updateDeck(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Deck UUID", required = true) 
            @PathVariable UUID deckId,
            @Valid @RequestBody UpdateFlashcardDeckDTO request) {

        LOG.info("User {} updating deck {}", user.getEmail(), deckId);

        FlashcardDeckDTO deck = flashcardService.updateDeck(deckId, request, user.getId());

        LOG.info("User {} updated deck '{}' successfully", user.getEmail(), deck.getTitle());

        return ResponseEntity.ok(deck);
    }

    /**
     * Delete a flashcard deck.
     * 
     * @param user the authenticated user
     * @param deckId the deck UUID
     * @return 204 No Content on successful deletion
     */
    @DeleteMapping(value = "/decks/{deckId}")
    @Operation(
        summary = "Delete flashcard deck",
        description = "Permanently deletes a deck and all associated progress records."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Deck deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Deck not found")
    })
    public ResponseEntity<Void> deleteDeck(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Deck UUID", required = true) 
            @PathVariable UUID deckId) {

        LOG.info("User {} deleting deck {}", user.getEmail(), deckId);

        flashcardService.deleteDeck(deckId, user.getId());

        LOG.info("User {} deleted deck {} successfully", user.getEmail(), deckId);

        return ResponseEntity.noContent().build();
    }

    // ========== Study Session Endpoints ==========

    /**
     * Get cards for a study session.
     * 
     * <p>Returns cards prioritized by SM-2 spaced repetition algorithm:</p>
     * <ol>
     *   <li>Overdue cards (most overdue first)</li>
     *   <li>New cards (never reviewed)</li>
     *   <li>Regular due cards</li>
     * </ol>
     * 
     * @param user the authenticated user
     * @param deckId the deck UUID
     * @param maxCards maximum cards to return (default 20)
     * @return 200 OK with study session data
     */
    @GetMapping(value = "/decks/{deckId}/study", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get study session",
        description = "Retrieves cards due for review, ordered by spaced repetition priority. " +
                      "Includes progress data for each card."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Study session retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FlashcardStudySessionDTO.class),
                examples = @ExampleObject(name = "Study Session", value = """
                    {
                      "deckId": "550e8400-e29b-41d4-a716-446655440000",
                      "deckTitle": "Business Meeting Vocabulary",
                      "sessionStartedAt": "2025-12-13T10:30:00Z",
                      "totalCards": 25,
                      "dueCards": 8,
                      "newCards": 3,
                      "sessionSize": 11,
                      "cardsToStudy": [
                        {
                          "cardIndex": 0,
                          "front": "agenda",
                          "back": "a list of topics to be discussed",
                          "masteryLevel": 2,
                          "easeFactor": 2.5,
                          "interval": 3,
                          "isNew": false
                        }
                      ],
                      "stats": {
                        "newCount": 5,
                        "learningCount": 10,
                        "reviewCount": 7,
                        "masteredCount": 3
                      }
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Deck not found")
    })
    public ResponseEntity<FlashcardStudySessionDTO> getStudySession(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Deck UUID", required = true) 
            @PathVariable UUID deckId,
            @Parameter(description = "Maximum cards to include (default 20, max 50)") 
            @RequestParam(required = false, defaultValue = "20") Integer maxCards) {

        LOG.info("User {} starting study session for deck {}", user.getEmail(), deckId);

        // Cap maxCards at 50
        maxCards = Math.min(maxCards, 50);

        FlashcardStudySessionDTO session = flashcardService.getStudySession(deckId, user.getId(), maxCards);

        LOG.info("User {} study session: {} cards to study ({} due, {} new)", 
                user.getEmail(), session.getSessionSize(), session.getDueCards(), session.getNewCards());

        return ResponseEntity.ok(session);
    }

    /**
     * Submit review results for studied cards.
     * 
     * <p>Updates SM-2 spaced repetition data for each reviewed card,
     * including ease factor, interval, next review date, and mastery level.</p>
     * 
     * @param user the authenticated user
     * @param deckId the deck UUID
     * @param results the review results with quality ratings
     * @return 200 OK with updated study session
     */
    @PostMapping(value = "/decks/{deckId}/review", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Submit review results",
        description = "Submits quality ratings for reviewed cards. Updates spaced repetition " +
                      "data (ease factor, interval, next review date) based on SM-2 algorithm."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Review submitted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FlashcardStudySessionDTO.class),
                examples = @ExampleObject(name = "Review Submitted", value = """
                    {
                      "deckId": "550e8400-e29b-41d4-a716-446655440000",
                      "deckTitle": "Business Meeting Vocabulary",
                      "totalCards": 25,
                      "dueCards": 0,
                      "newCards": 2,
                      "stats": {
                        "newCount": 2,
                        "learningCount": 8,
                        "reviewCount": 10,
                        "masteredCount": 5
                      }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid review data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Quality rating must be between 0 and 5",
                      "validationErrors": [
                        {"field": "reviews[0].quality", "message": "must be less than or equal to 5"}
                      ]
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Deck not found")
    })
    public ResponseEntity<FlashcardStudySessionDTO> submitReview(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Deck UUID", required = true) 
            @PathVariable UUID deckId,
            @Valid @RequestBody FlashcardReviewResultDTO results) {

        LOG.info("User {} submitting review for deck {} ({} cards)", 
                user.getEmail(), deckId, results.getReviews().size());

        FlashcardStudySessionDTO session = flashcardService.submitReview(deckId, user.getId(), results);

        LOG.info("User {} review submitted. Next session: {} due, {} new", 
                user.getEmail(), session.getDueCards(), session.getNewCards());

        return ResponseEntity.ok(session);
    }

    // ========== Helper Endpoints ==========

    /**
     * Get count of cards due for review across all decks.
     * 
     * @param user the authenticated user
     * @return 200 OK with total due count
     */
    @GetMapping(value = "/due-count", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get total due cards count",
        description = "Returns the total number of cards due for review across all user's decks."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved due count",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "totalDueCards": 42
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<DueCountResponse> getTotalDueCount(
            @AuthenticationPrincipal User user) {

        long dueCount = flashcardService.getTotalDueCardCount(user.getId());

        return ResponseEntity.ok(new DueCountResponse(dueCount));
    }

    /**
     * Check if a deck exists for a specific lesson.
     * 
     * @param user the authenticated user
     * @param lessonId the lesson ID
     * @return 200 OK with exists flag and optional deck ID
     */
    @GetMapping(value = "/lessons/{lessonId}/deck", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Check for lesson deck",
        description = "Checks if a flashcard deck has already been generated from a specific lesson."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Check completed",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Deck Exists", value = """
                        {
                          "exists": true,
                          "deckId": "550e8400-e29b-41d4-a716-446655440000"
                        }
                        """),
                    @ExampleObject(name = "No Deck", value = """
                        {
                          "exists": false,
                          "deckId": null
                        }
                        """)
                }
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<LessonDeckCheckResponse> checkLessonDeck(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Lesson ID", required = true) 
            @PathVariable Long lessonId) {

        FlashcardDeckDTO deck = flashcardService.getLessonDeck(lessonId, user.getId());

        LessonDeckCheckResponse response = new LessonDeckCheckResponse(
                deck != null,
                deck != null ? deck.getId() : null
        );

        return ResponseEntity.ok(response);
    }

    // ========== Response DTOs ==========

    /**
     * Response DTO for total due cards count.
     */
    @Schema(description = "Total due cards count response")
    public record DueCountResponse(
        @Schema(description = "Total cards due for review", example = "42")
        long totalDueCards
    ) {}

    /**
     * Response DTO for lesson deck check.
     */
    @Schema(description = "Lesson deck check response")
    public record LessonDeckCheckResponse(
        @Schema(description = "Whether a deck exists for this lesson")
        boolean exists,
        @Schema(description = "Deck ID if exists")
        UUID deckId
    ) {}
}
