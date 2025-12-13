# Implementation Plan: C5 & C6 - GrammarController and Answer Validation

**Created**: December 13, 2025  
**Author**: AI Development Assistant  
**Tasks**: C5 (GrammarController - 0.5 pt) + C6 (Answer Validation & Scoring - 0.5 pt)  
**Total Points**: 1.0 pt

---

## 1. Executive Summary

### 1.1 Objective
Create REST endpoints for grammar exercise functionality, including exercise generation, topic listing, answer submission, and exercise history retrieval.

### 1.2 Dependencies Verified ✅
- **GrammarExerciseService** interface (14 methods) - [GrammarExerciseService.java](../../../src/main/java/com/lexia/backend/service/ai/GrammarExerciseService.java)
- **GrammarExerciseServiceImpl** implementation (~600 lines) - [GrammarExerciseServiceImpl.java](../../../src/main/java/com/lexia/backend/service/ai/impl/GrammarExerciseServiceImpl.java)
- **All DTOs exist**:
  - `GrammarRequestDTO` - Request for generating exercises
  - `GrammarExerciseSetDTO` - Complete exercise set response
  - `GrammarAnswerDTO` - Answer submission with nested `AnswerItem`
  - `GrammarResultDTO` - Scoring results with `QuestionFeedback`
  - `GrammarTopicDTO` - Topic information (record type)
  - `GrammarProgressDTO` - User progress tracking
  - `GrammarStatsDTO` - User statistics

### 1.3 Scope

| Task | Description | Points |
|------|-------------|--------|
| C5 | Create GrammarController with 4 REST endpoints | 0.5 |
| C6 | Answer validation and scoring (already in service) | 0.5 |

**Note**: C6 answer validation and scoring is already implemented in `GrammarExerciseServiceImpl.submitAnswers()`. The controller will simply expose this functionality.

---

## 2. Technical Specification

### 2.1 Endpoints to Implement

| Method | Endpoint | Auth | Description | Service Method |
|--------|----------|------|-------------|----------------|
| POST | `/api/v1/ai/grammar/generate` | USER | Generate grammar exercises | `generateExercises(request, userId)` |
| GET | `/api/v1/ai/grammar/topics` | USER | List available grammar topics | `getAllTopics()` or `getTopicsByLevel()` |
| POST | `/api/v1/ai/grammar/exercises/{setId}/submit` | USER | Submit answers | `submitAnswers(setId, userId, submission)` |
| GET | `/api/v1/ai/grammar/history` | USER | Get exercise history | `getHistory(userId, pageable)` |

### 2.2 Additional Endpoints (Optional Enhancement)

| Method | Endpoint | Auth | Description | Service Method |
|--------|----------|------|-------------|----------------|
| GET | `/api/v1/ai/grammar/exercises/{setId}` | USER | Get exercise set by ID | `getExerciseSet(setId)` |
| GET | `/api/v1/ai/grammar/statistics` | USER | Get user statistics | `getStatistics(userId)` |
| GET | `/api/v1/ai/grammar/topics/categories` | USER | Get topic categories | `getCategories()` |

---

## 3. File Structure

### 3.1 New Files to Create

| File Path | Type | Description |
|-----------|------|-------------|
| `src/main/java/com/lexia/backend/controller/ai/GrammarController.java` | Controller | REST endpoints |
| `src/test/java/com/lexia/backend/controller/ai/GrammarControllerTest.java` | Test | Unit tests |

### 3.2 Files to Modify

| File Path | Change |
|-----------|--------|
| `GlobalExceptionHandler.java` | Add `AiRateLimitException` handler (if not exists) |

---

## 4. Detailed Implementation

### 4.1 GrammarController.java

```java
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
 * REST Controller for AI-powered Grammar Exercise Management.
 * Handles generation, retrieval, submission, and history of grammar exercises.
 * 
 * <p>Base path: /api/v1/ai/grammar</p>
 * 
 * <p>Security:</p>
 * <ul>
 *   <li>All endpoints require USER authentication</li>
 *   <li>Users can only access their own exercise history and submissions</li>
 *   <li>Rate limited via AiUsageTracker (50 requests/day)</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@RestController
@RequestMapping("/api/v1/ai/grammar")
@Tag(name = "Grammar Exercises API", 
     description = "AI-powered grammar exercise generation, submission, and progress tracking")
@SecurityRequirement(name = "bearerAuth")
public class GrammarController {

    private static final Logger LOG = LoggerFactory.getLogger(GrammarController.class);

    private final GrammarExerciseService grammarExerciseService;

    public GrammarController(GrammarExerciseService grammarExerciseService) {
        this.grammarExerciseService = grammarExerciseService;
    }

    // ========== POST /api/v1/ai/grammar/generate ==========
    
    /**
     * Generate a new set of grammar exercises using AI.
     */
    @PostMapping(value = "/generate", 
                 produces = MediaType.APPLICATION_JSON_VALUE, 
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Generate grammar exercises", 
        description = "Generates a new set of grammar exercises based on the specified topic and CEFR level. " +
                      "Uses Google Gemini AI for generation with fallback content support. " +
                      "Subject to daily rate limits (50 requests/day per user)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Exercises generated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GrammarExerciseSetDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request - validation errors",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - invalid or missing token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "429", 
            description = "Rate limit exceeded - daily quota exhausted",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<GrammarExerciseSetDTO> generateExercises(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody GrammarRequestDTO request) {
        
        LOG.info("User {} generating grammar exercises - topic: {}, level: {}",
                user.getEmail(), request.getGrammarTopic(), request.getCefrLevel());
        
        GrammarExerciseSetDTO exerciseSet = grammarExerciseService.generateExercises(request, user.getId());
        
        LOG.info("Generated {} exercises for user {} - set ID: {}",
                exerciseSet.getExerciseCount(), user.getEmail(), exerciseSet.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseSet);
    }

    // ========== GET /api/v1/ai/grammar/topics ==========
    
    /**
     * List available grammar topics.
     */
    @GetMapping(value = "/topics", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "List grammar topics", 
        description = "Retrieves all available grammar topics. Optionally filter by CEFR level or category."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Topics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GrammarTopicDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - invalid or missing token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<List<GrammarTopicDTO>> getTopics(
            @Parameter(description = "Filter by CEFR level (A1-C2)", example = "B1")
            @RequestParam(required = false) String level,
            @Parameter(description = "Filter by category", example = "Tenses")
            @RequestParam(required = false) String category) {
        
        LOG.info("Fetching grammar topics - level: {}, category: {}", level, category);
        
        List<GrammarTopicDTO> topics;
        
        if (level != null && !level.isBlank()) {
            topics = grammarExerciseService.getTopicsByLevel(level.toUpperCase());
        } else if (category != null && !category.isBlank()) {
            topics = grammarExerciseService.getTopicsByCategory(category);
        } else {
            topics = grammarExerciseService.getAllTopics();
        }
        
        LOG.info("Retrieved {} grammar topics", topics.size());
        
        return ResponseEntity.ok(topics);
    }

    // ========== POST /api/v1/ai/grammar/exercises/{setId}/submit ==========
    
    /**
     * Submit answers for an exercise set and receive scoring.
     */
    @PostMapping(value = "/exercises/{setId}/submit", 
                 produces = MediaType.APPLICATION_JSON_VALUE, 
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Submit exercise answers", 
        description = "Submits answers for a grammar exercise set. Returns detailed scoring with feedback. " +
                      "Passing threshold is 70%. Each exercise set can only be submitted once per user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Answers submitted and scored successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GrammarResultDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request - validation errors or already submitted",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - invalid or missing token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Exercise set not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<GrammarResultDTO> submitAnswers(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Exercise set ID", required = true)
            @PathVariable UUID setId,
            @Valid @RequestBody GrammarAnswerDTO submission) {
        
        LOG.info("User {} submitting answers for exercise set {}", user.getEmail(), setId);
        
        GrammarResultDTO result = grammarExerciseService.submitAnswers(setId, user.getId(), submission);
        
        LOG.info("User {} scored {}/{}% on exercise set {} - passed: {}",
                user.getEmail(), result.getScore(), result.getPercentage(), 
                setId, result.getPassed());
        
        return ResponseEntity.ok(result);
    }

    // ========== GET /api/v1/ai/grammar/history ==========
    
    /**
     * Get user's exercise history.
     */
    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get exercise history", 
        description = "Retrieves the authenticated user's grammar exercise history with pagination. " +
                      "Includes completed exercises, scores, and progress data."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "History retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - invalid or missing token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<Page<GrammarProgressDTO>> getHistory(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction", example = "completedAt,desc")
            @RequestParam(defaultValue = "completedAt,desc") String sort) {
        
        LOG.info("Fetching grammar history for user {} - page: {}, size: {}", 
                user.getEmail(), page, size);
        
        // Enforce max page size
        size = Math.min(size, 50);
        
        Pageable pageable = createPageable(page, size, sort);
        Page<GrammarProgressDTO> history = grammarExerciseService.getHistory(user.getId(), pageable);
        
        LOG.info("Retrieved {} history records for user {}", 
                history.getTotalElements(), user.getEmail());
        
        return ResponseEntity.ok(history);
    }

    // ========== GET /api/v1/ai/grammar/exercises/{setId} ==========
    
    /**
     * Get a specific exercise set by ID.
     */
    @GetMapping(value = "/exercises/{setId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get exercise set", 
        description = "Retrieves a specific grammar exercise set by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Exercise set retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GrammarExerciseSetDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - invalid or missing token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Exercise set not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<GrammarExerciseSetDTO> getExerciseSet(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Exercise set ID", required = true)
            @PathVariable UUID setId) {
        
        LOG.info("User {} fetching exercise set {}", user.getEmail(), setId);
        
        GrammarExerciseSetDTO exerciseSet = grammarExerciseService.getExerciseSet(setId);
        
        return ResponseEntity.ok(exerciseSet);
    }

    // ========== GET /api/v1/ai/grammar/statistics ==========
    
    /**
     * Get user's grammar exercise statistics.
     */
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get grammar statistics", 
        description = "Retrieves aggregated statistics for the user's grammar exercise performance."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Statistics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GrammarStatsDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - invalid or missing token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<GrammarStatsDTO> getStatistics(
            @AuthenticationPrincipal User user) {
        
        LOG.info("Fetching grammar statistics for user {}", user.getEmail());
        
        GrammarStatsDTO stats = grammarExerciseService.getStatistics(user.getId());
        
        return ResponseEntity.ok(stats);
    }

    // ========== GET /api/v1/ai/grammar/topics/categories ==========
    
    /**
     * Get all distinct topic categories.
     */
    @GetMapping(value = "/topics/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get topic categories", 
        description = "Retrieves all distinct grammar topic categories."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Categories retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = List.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - invalid or missing token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<List<String>> getCategories() {
        
        LOG.info("Fetching grammar topic categories");
        
        List<String> categories = grammarExerciseService.getCategories();
        
        LOG.info("Retrieved {} categories", categories.size());
        
        return ResponseEntity.ok(categories);
    }

    // ========== Helper Methods ==========
    
    /**
     * Creates a Pageable from request parameters.
     */
    private Pageable createPageable(int page, int size, String sortParam) {
        String[] sortParts = sortParam.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}
```

### 4.2 Exception Handling Enhancement

Add to `GlobalExceptionHandler.java`:

```java
import com.lexia.backend.exception.ai.AiRateLimitException;

/**
 * Handle AI rate limit exception.
 * Returns 429 Too Many Requests when user exceeds AI quota.
 */
@ExceptionHandler(AiRateLimitException.class)
public ResponseEntity<ErrorResponse> handleAiRateLimitException(
        AiRateLimitException ex, HttpServletRequest request) {

    LOG.warn("AI rate limit exceeded: {}", ex.getMessage());

    ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .error("Rate Limit Exceeded")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
}
```

---

## 5. Test Specification

### 5.1 GrammarControllerTest.java

```java
package com.lexia.backend.controller.ai;

// Test class outline with ~15-20 unit tests

@WebMvcTest(GrammarController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("GrammarController Tests")
class GrammarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GrammarExerciseService grammarExerciseService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ========== Test Cases ==========

    @Nested
    @DisplayName("POST /api/v1/ai/grammar/generate")
    class GenerateExercisesTests {
        
        @Test @DisplayName("Should generate exercises successfully")
        void shouldGenerateExercises_Success() { }
        
        @Test @DisplayName("Should return 400 for invalid request")
        void shouldReturn400_InvalidRequest() { }
        
        @Test @DisplayName("Should return 429 when rate limited")
        void shouldReturn429_RateLimited() { }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/topics")
    class GetTopicsTests {
        
        @Test @DisplayName("Should get all topics")
        void shouldGetAllTopics() { }
        
        @Test @DisplayName("Should filter by level")
        void shouldFilterByLevel() { }
        
        @Test @DisplayName("Should filter by category")
        void shouldFilterByCategory() { }
    }

    @Nested
    @DisplayName("POST /api/v1/ai/grammar/exercises/{setId}/submit")
    class SubmitAnswersTests {
        
        @Test @DisplayName("Should submit answers successfully")
        void shouldSubmitAnswers_Success() { }
        
        @Test @DisplayName("Should return 404 for missing exercise set")
        void shouldReturn404_ExerciseSetNotFound() { }
        
        @Test @DisplayName("Should return 400 for already submitted")
        void shouldReturn400_AlreadySubmitted() { }
        
        @Test @DisplayName("Should calculate score correctly - passing")
        void shouldCalculateScore_Passing() { }
        
        @Test @DisplayName("Should calculate score correctly - failing")
        void shouldCalculateScore_Failing() { }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/history")
    class GetHistoryTests {
        
        @Test @DisplayName("Should get history with pagination")
        void shouldGetHistory_WithPagination() { }
        
        @Test @DisplayName("Should enforce max page size")
        void shouldEnforceMaxPageSize() { }
        
        @Test @DisplayName("Should sort by completedAt desc by default")
        void shouldSortByDefault() { }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/exercises/{setId}")
    class GetExerciseSetTests {
        
        @Test @DisplayName("Should get exercise set by ID")
        void shouldGetExerciseSet() { }
        
        @Test @DisplayName("Should return 404 for missing set")
        void shouldReturn404_SetNotFound() { }
    }

    @Nested
    @DisplayName("GET /api/v1/ai/grammar/statistics")
    class GetStatisticsTests {
        
        @Test @DisplayName("Should get user statistics")
        void shouldGetStatistics() { }
    }
}
```

### 5.2 Expected Test Count
- **Generate Exercises**: 3 tests
- **Get Topics**: 3 tests
- **Submit Answers**: 5 tests
- **Get History**: 3 tests
- **Get Exercise Set**: 2 tests
- **Get Statistics**: 1 test
- **Get Categories**: 1 test
- **Total**: ~18 unit tests

---

## 6. C6 Answer Validation & Scoring (Already Implemented)

### 6.1 Existing Implementation in GrammarExerciseServiceImpl

The answer validation and scoring logic is already fully implemented in `GrammarExerciseServiceImpl.submitAnswers()`:

```java
// Key features already implemented:
1. ✅ Ownership verification (IDOR prevention)
2. ✅ HashMap-based answer mapping (handles out-of-order submissions)
3. ✅ Score calculation: correctCount / totalQuestions
4. ✅ Percentage calculation: BigDecimal with 2 decimal places
5. ✅ 70% passing threshold enforcement
6. ✅ Detailed question-by-question feedback
7. ✅ Progress tracking via UserGrammarProgress entity
8. ✅ Duplicate submission prevention
9. ✅ Time tracking (totalTimeSeconds)
10. ✅ Areas for improvement generation
```

### 6.2 Scoring Logic Summary

```java
// From GrammarExerciseServiceImpl:
private static final double PASSING_THRESHOLD = 70.0;

// Score calculation:
int correctCount = 0;
for each answer:
    if (userAnswer.equals(correctAnswer)) correctCount++;

// Percentage:
BigDecimal percentage = BigDecimal.valueOf(correctCount * 100.0 / maxScore)
    .setScale(2, RoundingMode.HALF_UP);

// Pass determination:
boolean passed = percentage.compareTo(BigDecimal.valueOf(PASSING_THRESHOLD)) >= 0;
```

### 6.3 C6 Task Completion
C6 is **already complete** via:
- Existing service implementation
- 28 unit tests covering scoring scenarios
- Security validation (ownership check)
- Edge case handling

The controller simply exposes this functionality without additional business logic.

---

## 7. Execution Steps

### Step 1: Create Controller Directory Structure (5 min)
```
src/main/java/com/lexia/backend/controller/ai/
```

### Step 2: Implement GrammarController.java (45 min)
- Create file with all 7 endpoints
- Add comprehensive Swagger annotations
- Implement logging
- Add pagination helper

### Step 3: Update GlobalExceptionHandler.java (10 min)
- Add AiRateLimitException handler
- Return 429 Too Many Requests status

### Step 4: Create GrammarControllerTest.java (60 min)
- Create test file with all test cases
- Implement mocking setup
- Write 18+ unit tests

### Step 5: Run Tests & Verify (20 min)
```bash
./gradlew test --tests "GrammarControllerTest"
./gradlew test jacocoTestReport
```

### Step 6: Documentation (10 min)
- Update daily-log.md
- Update sprint status

**Total Estimated Time**: ~2.5 hours

---

## 8. Validation Checklist

### Code Quality
- [ ] Controller compiles without errors
- [ ] All imports resolved
- [ ] No Lombok warnings
- [ ] Swagger annotations complete

### Testing
- [ ] 18+ unit tests written
- [ ] All tests passing
- [ ] Coverage ≥ 80% for controller
- [ ] Edge cases covered

### Security
- [ ] @AuthenticationPrincipal used for user context
- [ ] UUID paths validated
- [ ] Rate limiting via service layer
- [ ] No sensitive data logged

### Documentation
- [ ] JavaDoc on all public methods
- [ ] Swagger examples provided
- [ ] daily-log.md updated
- [ ] Conventional commit message

---

## 9. Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Service method signature mismatch | Low | High | Already verified service interface |
| UUID path variable parsing | Low | Medium | Use `@PathVariable UUID` with Spring conversion |
| Pagination parameter validation | Low | Low | Enforce max page size in controller |
| Rate limit exception not caught | Medium | Medium | Add handler to GlobalExceptionHandler |

---

## 10. Definition of Done

- [x] Research complete (service, DTOs, patterns)
- [ ] GrammarController.java created
- [ ] GlobalExceptionHandler.java updated
- [ ] GrammarControllerTest.java created
- [ ] All tests passing
- [ ] Coverage ≥ 70%
- [ ] daily-log.md updated
- [ ] Commit pushed

---

## 11. API Examples

### Generate Exercises Request
```json
POST /api/v1/ai/grammar/generate
Content-Type: application/json
Authorization: Bearer <token>

{
  "grammarTopic": "Present Perfect",
  "cefrLevel": "B1",
  "theme": "workplace",
  "exerciseCount": 5,
  "timeLimitSeconds": 600,
  "useFallback": false
}
```

### Submit Answers Request
```json
POST /api/v1/ai/grammar/exercises/{setId}/submit
Content-Type: application/json
Authorization: Bearer <token>

{
  "exerciseSetId": "uuid",
  "answers": [
    { "questionIndex": 0, "answer": "has been working", "timeMs": 5000 },
    { "questionIndex": 1, "answer": 1, "timeMs": 3000 },
    { "questionIndex": 2, "answer": "went", "timeMs": 4000 }
  ],
  "totalTimeSeconds": 180
}
```

### Submit Answers Response
```json
{
  "exerciseSetId": "uuid",
  "grammarPoint": "Present Perfect",
  "cefrLevel": "B1",
  "score": 4,
  "maxScore": 5,
  "percentage": 80.00,
  "passed": true,
  "timeSpentSeconds": 180,
  "feedback": [
    {
      "questionIndex": 0,
      "userAnswer": "has been working",
      "correctAnswer": "has been working",
      "correct": true,
      "explanation": "Correct! 'has been working' is the Present Perfect Continuous form.",
      "timeMs": 5000
    }
  ],
  "areasToImprove": [],
  "encouragement": "Excellent work! You've mastered the Present Perfect!",
  "completedAt": "2025-12-13T10:30:00Z"
}
```

---

**Plan Status**: ✅ Ready for Implementation
**Assigned To**: Next Session
**Priority**: High
