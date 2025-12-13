# D4 - FlashcardService Implementation Plan

**Task**: Implement FlashcardService (Generate from Lesson)  
**Estimated Points**: 1.5  
**Priority**: P0  
**Created**: December 13, 2025  
**Author**: GitHub Copilot

---

## 1. Executive Summary

### 1.1 Task Overview

Implement `FlashcardService` that enables AI-powered flashcard generation from lesson content. The service will:
- Extract vocabulary from lesson content (READING, LISTENING, QUIZ, SPEAKING types)
- Use Google Gemini AI to generate comprehensive flashcard data
- Support manual deck creation and management
- Implement spaced repetition study sessions
- Provide fallback content when AI is unavailable

### 1.2 Current State Analysis

**Existing Infrastructure (Ready to Use):**
| Component | Status | Location |
|-----------|--------|----------|
| `FlashcardDeck` entity | ✅ Complete | `entity/FlashcardDeck.java` |
| `FlashcardCard` POJO | ✅ Complete | `entity/FlashcardCard.java` |
| `FlashcardBack` POJO | ✅ Complete | `entity/FlashcardBack.java` |
| `UserFlashcardProgress` entity | ✅ Complete | `entity/UserFlashcardProgress.java` |
| `FlashcardDeckRepository` | ✅ Complete | `repository/FlashcardDeckRepository.java` |
| `UserFlashcardProgressRepository` | ✅ Complete | `repository/UserFlashcardProgressRepository.java` |
| `FlashcardMapper` | ✅ Complete | `mapper/FlashcardMapper.java` |
| All DTOs | ✅ Complete | `dto/ai/Flashcard*.java` |
| `GeminiClientService` | ✅ Complete | `service/ai/GeminiClientService.java` |
| `AiUsageTracker` | ✅ Complete | `service/ai/AiUsageTracker.java` |
| `PromptTemplateService` | ✅ Complete | `service/ai/PromptTemplateService.java` |
| `LessonService` | ✅ Complete | `service/LessonService.java` |

**To Be Implemented:**
| Component | Priority | Description |
|-----------|----------|-------------|
| `FlashcardService` interface | P0 | Service interface with all method signatures |
| `FlashcardServiceImpl` | P0 | Implementation with AI generation logic |
| `FlashcardServiceImplTest` | P0 | Unit tests (≥80% coverage) |

### 1.3 Key Requirements (from SPRINT-5-SPECIFICATION.md)

| ID | Requirement | Priority | Status |
|----|-------------|----------|--------|
| FL-001 | Generate flashcards from lesson content automatically | P0 | To implement |
| FL-002 | Create vocabulary cards with definitions and examples | P0 | To implement |
| FL-003 | Support spaced repetition scheduling | P1 | Entity ready, service needed |
| FL-004 | Allow manual flashcard creation | P1 | To implement |
| FL-005 | Support front/back card format | P0 | Entity ready |
| FL-006 | Include pronunciation hints (IPA) | P2 | Part of AI generation |
| FL-007 | Track mastery level per card | P1 | Entity ready, service needed |
| FL-008 | Generate cards in batches | P0 | To implement |

---

## 2. Scope Definition

### 2.1 FlashcardService Interface Methods

```java
public interface FlashcardService {

    // ========== Deck Generation ==========
    
    /**
     * Generates flashcards from lesson content using AI.
     * @param userId the user ID
     * @param request generation parameters (lessonId, maxCards, options)
     * @return the generated deck with cards
     */
    FlashcardDeckDTO generateFromLesson(UUID userId, GenerateFlashcardsDTO request);
    
    // ========== Deck CRUD Operations ==========
    
    /**
     * Creates a new flashcard deck (manual/user-created).
     * @param userId the user ID
     * @param request deck creation data
     * @return the created deck
     */
    FlashcardDeckDTO createDeck(UUID userId, CreateFlashcardDeckDTO request);
    
    /**
     * Gets a deck by ID (with ownership check).
     * @param userId the user ID
     * @param deckId the deck ID
     * @return the deck with cards
     */
    FlashcardDeckDTO getDeck(UUID userId, UUID deckId);
    
    /**
     * Lists all decks for a user.
     * @param userId the user ID
     * @param pageable pagination
     * @return page of decks (without full card data)
     */
    Page<FlashcardDeckDTO> getUserDecks(UUID userId, Pageable pageable);
    
    /**
     * Updates a deck's metadata (title, description).
     * @param userId the user ID
     * @param deckId the deck ID
     * @param title new title (nullable)
     * @param description new description (nullable)
     * @return updated deck
     */
    FlashcardDeckDTO updateDeck(UUID userId, UUID deckId, String title, String description);
    
    /**
     * Deletes a deck and all associated progress.
     * @param userId the user ID
     * @param deckId the deck ID
     */
    void deleteDeck(UUID userId, UUID deckId);
    
    // ========== Card Operations ==========
    
    /**
     * Adds a card to an existing deck.
     * @param userId the user ID
     * @param deckId the deck ID
     * @param card the card to add
     * @return updated deck
     */
    FlashcardDeckDTO addCard(UUID userId, UUID deckId, FlashcardCardDTO card);
    
    /**
     * Updates a card in a deck.
     * @param userId the user ID
     * @param deckId the deck ID
     * @param cardIndex index of card to update
     * @param card updated card data
     * @return updated deck
     */
    FlashcardDeckDTO updateCard(UUID userId, UUID deckId, int cardIndex, FlashcardCardDTO card);
    
    /**
     * Removes a card from a deck.
     * @param userId the user ID
     * @param deckId the deck ID
     * @param cardIndex index of card to remove
     * @return updated deck
     */
    FlashcardDeckDTO removeCard(UUID userId, UUID deckId, int cardIndex);
    
    // ========== Study Session Operations ==========
    
    /**
     * Gets cards for a study session (due + new cards).
     * @param userId the user ID
     * @param deckId the deck ID
     * @param maxCards maximum cards to include
     * @return study session with cards and progress
     */
    FlashcardStudySessionDTO getStudySession(UUID userId, UUID deckId, int maxCards);
    
    /**
     * Submits review results for cards.
     * Updates progress using SM-2 algorithm.
     * @param userId the user ID
     * @param deckId the deck ID
     * @param results review quality ratings
     * @return updated progress summary
     */
    FlashcardStudySessionDTO submitReview(UUID userId, UUID deckId, FlashcardReviewResultDTO results);
    
    // ========== Progress Queries ==========
    
    /**
     * Gets progress for all cards in a deck.
     * @param userId the user ID
     * @param deckId the deck ID
     * @return list of progress records
     */
    List<FlashcardProgressDTO> getDeckProgress(UUID userId, UUID deckId);
    
    /**
     * Gets count of cards due for review today.
     * @param userId the user ID
     * @return count of due cards
     */
    int getDueCardCount(UUID userId);
    
    /**
     * Checks if user already has a deck for a lesson.
     * @param userId the user ID
     * @param lessonId the lesson ID
     * @return true if deck exists
     */
    boolean hasLessonDeck(UUID userId, Long lessonId);
}
```

### 2.2 Dependencies

| Dependency | Purpose | Already Available |
|------------|---------|-------------------|
| `FlashcardDeckRepository` | Deck persistence | ✅ Yes |
| `UserFlashcardProgressRepository` | Progress persistence | ✅ Yes |
| `GeminiClientService` | AI generation | ✅ Yes |
| `AiUsageTracker` | Track AI usage | ✅ Yes |
| `PromptTemplateService` | Prompt management | ✅ Yes |
| `LessonService` | Get lesson content | ✅ Yes |
| `FlashcardMapper` | Entity/DTO conversion | ✅ Yes |
| `ObjectMapper` | JSON parsing | ✅ Spring Boot default |

### 2.3 Prompt Template Required

Create prompt template `flashcard_generation_v1`:
```
Category: flashcard
Key: flashcard_generation_v1
Version: 1

Template:
"""
Generate vocabulary flashcards from the following English lesson content.

CEFR Level: {{cefrLevel}}
Lesson Type: {{lessonType}}
Focus Areas: {{focusAreas}}

LESSON CONTENT:
{{lessonContent}}

Generate {{maxCards}} flashcards with vocabulary appropriate for {{cefrLevel}} level learners.

Output strictly valid JSON array matching this schema:
[
  {
    "front": "vocabulary word or phrase",
    "back": {
      "definition": "clear definition in simple English",
      "partOfSpeech": "noun|verb|adjective|adverb|phrase",
      "pronunciation": "/IPA notation/",
      "exampleSentence": "A sentence using the word in context",
      "synonyms": ["synonym1", "synonym2"],
      "collocations": ["common phrase 1", "common phrase 2"]
    },
    "tags": ["topic", "cefr-level"],
    "difficulty": 1-5
  }
]

Requirements:
- Include only words appropriate for {{cefrLevel}} level
- Each definition should be simple and clear
- Example sentences should be relevant to business English
- Include IPA pronunciation for all words
- Difficulty: 1=very easy, 5=very hard for the level
"""
```

---

## 3. Execution Steps

### Step 1: Create FlashcardService Interface (30 min)

**File**: `backend/src/main/java/com/lexia/backend/service/ai/FlashcardService.java`

**Key Implementation Details**:
- Define all 14 methods from Section 2.1
- Add comprehensive JavaDoc with examples
- Follow existing service interface patterns (see `RolePlayService`)

### Step 2: Create Prompt Template for Flashcard Generation (15 min)

**Action**: Insert prompt template into database via migration

**File**: `backend/src/main/resources/db/migration/V30__insert_flashcard_prompt_template.sql`

**Content**:
- Insert `flashcard_generation_v1` template
- Set as default and active
- Include variable placeholders: `{{cefrLevel}}`, `{{lessonType}}`, `{{lessonContent}}`, `{{maxCards}}`, `{{focusAreas}}`

### Step 3: Implement FlashcardServiceImpl - Core Structure (45 min)

**File**: `backend/src/main/java/com/lexia/backend/service/ai/impl/FlashcardServiceImpl.java`

**Key Implementation Details**:
- `@Service`, `@RequiredArgsConstructor`, `@Slf4j`, `@Transactional`
- Inject all dependencies
- Implement basic CRUD methods first: `createDeck`, `getDeck`, `getUserDecks`, `updateDeck`, `deleteDeck`
- Use `FlashcardMapper` for all conversions
- Ownership validation: throw `AccessDeniedException` if user doesn't own deck

### Step 4: Implement Lesson Content Extraction (45 min)

**Add to FlashcardServiceImpl**:

Private helper method `extractLessonContent(LessonDTO lesson)`:
- Parse lesson content JSON based on `lessonType`
- **READING**: Extract passages text + vocabulary array
- **LISTENING**: Extract transcript + vocabulary array
- **QUIZ**: Extract questions and explanations
- **SPEAKING**: Extract prompts + targetVocabulary
- Return concatenated text suitable for AI processing

```java
private String extractLessonContent(LessonDTO lesson) {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode content = mapper.readTree(lesson.getContent());
    
    StringBuilder extracted = new StringBuilder();
    
    switch (lesson.getLessonType()) {
        case READING -> {
            // Extract passages
            JsonNode passages = content.path("passages");
            for (JsonNode passage : passages) {
                extracted.append(passage.path("content").asText()).append("\n\n");
            }
            // Extract vocabulary if present
            appendVocabulary(extracted, content.path("vocabulary"));
        }
        case LISTENING -> {
            extracted.append("Transcript:\n");
            extracted.append(content.path("transcript").asText()).append("\n\n");
            appendVocabulary(extracted, content.path("vocabulary"));
        }
        case QUIZ -> {
            // Extract questions and explanations
            for (JsonNode question : content.path("questions")) {
                extracted.append(question.path("question").asText()).append("\n");
                if (question.has("explanation")) {
                    extracted.append("Explanation: ")
                            .append(question.path("explanation").asText()).append("\n");
                }
            }
        }
        case SPEAKING -> {
            for (JsonNode prompt : content.path("prompts")) {
                extracted.append(prompt.path("prompt").asText()).append("\n");
                appendVocabulary(extracted, prompt.path("targetVocabulary"));
            }
        }
    }
    return extracted.toString();
}
```

### Step 5: Implement generateFromLesson Method (60 min)

**Add to FlashcardServiceImpl**:

```java
@Override
@Transactional
public FlashcardDeckDTO generateFromLesson(UUID userId, GenerateFlashcardsDTO request) {
    // 1. Validate request
    if (request.getLessonId() == null) {
        throw new IllegalArgumentException("Lesson ID is required");
    }
    
    // 2. Check if deck already exists for this lesson
    if (hasLessonDeck(userId, request.getLessonId())) {
        throw new DuplicateResourceException("Flashcard deck already exists for this lesson");
    }
    
    // 3. Get lesson content
    LessonDTO lesson = lessonService.getById(request.getLessonId());
    String cefrLevel = request.getCefrLevel() != null 
        ? request.getCefrLevel() 
        : getCefrLevelFromLesson(lesson);
    
    // 4. Extract content and build prompt
    String lessonContent = extractLessonContent(lesson);
    String prompt = buildFlashcardPrompt(cefrLevel, lesson.getLessonType(), 
                                         lessonContent, request);
    
    // 5. Generate with AI
    List<FlashcardCardDTO> cards = generateCardsWithAi(userId, prompt, request.getMaxCards());
    
    // 6. Create deck
    String title = request.getCustomTitle() != null 
        ? request.getCustomTitle()
        : lesson.getTitle() + " Vocabulary";
        
    FlashcardDeck deck = FlashcardDeck.builder()
        .userId(userId)
        .title(title)
        .description("AI-generated vocabulary from: " + lesson.getTitle())
        .sourceType(FlashcardDeck.SourceType.LESSON)
        .sourceId(request.getLessonId())
        .cefrLevel(cefrLevel)
        .cards(FlashcardMapper.toCardEntityList(cards))
        .build();
    deck.updateCardCount();
    
    deck = deckRepository.save(deck);
    
    // 7. Initialize progress for all cards
    initializeProgress(userId, deck);
    
    return FlashcardMapper.toDeckDTO(deck);
}
```

### Step 6: Implement AI Generation with Fallback (45 min)

**Add to FlashcardServiceImpl**:

```java
private List<FlashcardCardDTO> generateCardsWithAi(UUID userId, String prompt, int maxCards) {
    try {
        // Check quota
        if (aiUsageTracker.isDailyQuotaExceeded(userId, AiUsageTracker.CONTENT_TYPE_FLASHCARD)) {
            log.warn("User {} exceeded flashcard quota, using fallback", userId);
            return getFallbackCards(maxCards);
        }
        
        // Generate with Gemini
        GeminiResponseDTO response = geminiClientService.generateContent(prompt);
        
        // Track usage
        trackUsage(userId, response, true);
        
        // Parse response
        String jsonContent = cleanJson(response.content());
        List<FlashcardCardDTO> cards = parseFlashcards(jsonContent);
        
        // Validate and limit
        return cards.stream()
            .filter(this::isValidCard)
            .limit(maxCards)
            .collect(Collectors.toList());
            
    } catch (Exception e) {
        log.error("AI generation failed, using fallback: {}", e.getMessage());
        trackUsage(userId, null, false);
        return getFallbackCards(maxCards);
    }
}

private List<FlashcardCardDTO> getFallbackCards(int maxCards) {
    // Return pre-defined fallback vocabulary cards
    // These should be generic business English vocabulary
    return FALLBACK_CARDS.stream()
        .limit(maxCards)
        .collect(Collectors.toList());
}

private static final List<FlashcardCardDTO> FALLBACK_CARDS = List.of(
    FlashcardCardDTO.builder()
        .front("agenda")
        .back(FlashcardBackDTO.builder()
            .definition("A list of items to be discussed at a meeting")
            .partOfSpeech("noun")
            .pronunciation("/əˈdʒendə/")
            .exampleSentence("Could you send the agenda before the meeting?")
            .synonyms(List.of("schedule", "plan", "program"))
            .build())
        .tags(List.of("meetings", "business"))
        .difficulty(2)
        .build(),
    // ... more fallback cards
);
```

### Step 7: Implement Study Session Methods (45 min)

**Add to FlashcardServiceImpl**:

```java
@Override
@Transactional(readOnly = true)
public FlashcardStudySessionDTO getStudySession(UUID userId, UUID deckId, int maxCards) {
    FlashcardDeck deck = getDeckWithOwnerCheck(userId, deckId);
    Instant now = Instant.now();
    
    // Get due cards
    List<UserFlashcardProgress> dueProgress = progressRepository
        .findDueCardsForDeck(userId, deckId, now);
    
    // Get new cards (never reviewed)
    List<Integer> existingIndexes = dueProgress.stream()
        .map(UserFlashcardProgress::getCardIndex)
        .collect(Collectors.toSet());
    
    List<Integer> newCardIndexes = IntStream.range(0, deck.getCardCount())
        .filter(i -> !existingIndexes.contains(i))
        .boxed()
        .limit(Math.max(0, maxCards - dueProgress.size()))
        .collect(Collectors.toList());
    
    // Build study session
    List<FlashcardProgressDTO> cardsToStudy = new ArrayList<>();
    
    // Add due cards first
    for (UserFlashcardProgress p : dueProgress.stream().limit(maxCards).toList()) {
        cardsToStudy.add(buildProgressDTO(p, deck.getCards().get(p.getCardIndex())));
    }
    
    // Add new cards
    for (Integer index : newCardIndexes) {
        cardsToStudy.add(buildNewCardProgressDTO(index, deck.getCards().get(index)));
    }
    
    return FlashcardStudySessionDTO.builder()
        .deckId(deckId)
        .deckTitle(deck.getTitle())
        .sessionStartedAt(now)
        .totalCards(deck.getCardCount())
        .dueCards(dueProgress.size())
        .newCards(newCardIndexes.size())
        .sessionSize(cardsToStudy.size())
        .cardsToStudy(cardsToStudy)
        .stats(calculateDeckStats(userId, deckId))
        .build();
}

@Override
@Transactional
public FlashcardStudySessionDTO submitReview(UUID userId, UUID deckId, 
                                              FlashcardReviewResultDTO results) {
    FlashcardDeck deck = getDeckWithOwnerCheck(userId, deckId);
    
    for (FlashcardReviewResultDTO.CardReviewDTO review : results.getReviews()) {
        UserFlashcardProgress progress = progressRepository
            .findByUserIdAndDeckIdAndCardIndex(userId, deckId, review.getCardIndex())
            .orElseGet(() -> UserFlashcardProgress.createNew(userId, deck, review.getCardIndex()));
        
        // Apply SM-2 algorithm
        progress.recordReview(review.getQuality());
        progressRepository.save(progress);
    }
    
    // Return updated session state
    return getStudySession(userId, deckId, 20);
}
```

### Step 8: Implement Card CRUD Operations (30 min)

**Add to FlashcardServiceImpl**:

```java
@Override
@Transactional
public FlashcardDeckDTO addCard(UUID userId, UUID deckId, FlashcardCardDTO card) {
    FlashcardDeck deck = getDeckWithOwnerCheck(userId, deckId);
    
    FlashcardCard cardEntity = FlashcardMapper.toCardEntity(card);
    cardEntity.validate();
    
    deck.addCard(cardEntity);
    deck = deckRepository.save(deck);
    
    return FlashcardMapper.toDeckDTO(deck);
}

@Override
@Transactional
public FlashcardDeckDTO updateCard(UUID userId, UUID deckId, int cardIndex, FlashcardCardDTO card) {
    FlashcardDeck deck = getDeckWithOwnerCheck(userId, deckId);
    
    if (cardIndex < 0 || cardIndex >= deck.getCards().size()) {
        throw new IndexOutOfBoundsException("Card index out of range: " + cardIndex);
    }
    
    FlashcardCard cardEntity = FlashcardMapper.toCardEntity(card);
    cardEntity.validate();
    
    deck.getCards().set(cardIndex, cardEntity);
    deck = deckRepository.save(deck);
    
    return FlashcardMapper.toDeckDTO(deck);
}

@Override
@Transactional
public FlashcardDeckDTO removeCard(UUID userId, UUID deckId, int cardIndex) {
    FlashcardDeck deck = getDeckWithOwnerCheck(userId, deckId);
    
    deck.removeCard(cardIndex);
    deck = deckRepository.save(deck);
    
    // Also remove progress for this card and shift higher indexes
    progressRepository.deleteByUserIdAndDeckIdAndCardIndex(userId, deckId, cardIndex);
    progressRepository.decrementCardIndexesAbove(userId, deckId, cardIndex);
    
    return FlashcardMapper.toDeckDTO(deck);
}
```

### Step 9: Implement Unit Tests (90 min)

**File**: `backend/src/test/java/com/lexia/backend/service/ai/FlashcardServiceImplTest.java`

**Test Categories**:

| Category | Tests | Priority |
|----------|-------|----------|
| Deck CRUD | 8 tests | P0 |
| Generate from Lesson | 6 tests | P0 |
| Card Operations | 6 tests | P1 |
| Study Session | 6 tests | P1 |
| Review Submission | 4 tests | P1 |
| Error Handling | 5 tests | P0 |
| Fallback Behavior | 3 tests | P0 |

**Key Test Cases**:
```java
// Deck Generation
@Test void generateFromLesson_success_createsFlashcardDeck()
@Test void generateFromLesson_duplicateLesson_throwsException()
@Test void generateFromLesson_aiFailure_returnsFallbackCards()
@Test void generateFromLesson_quotaExceeded_returnsFallbackCards()
@Test void generateFromLesson_invalidLessonId_throwsNotFoundException()
@Test void generateFromLesson_extractsReadingContent()

// Deck CRUD
@Test void createDeck_success_returnsDeckWithCards()
@Test void getDeck_success_returnsDeckWithProgress()
@Test void getDeck_notOwned_throwsAccessDenied()
@Test void getUserDecks_returnsPaginatedDecks()
@Test void updateDeck_updatesMetadata()
@Test void deleteDeck_removesProgressRecords()

// Study Session
@Test void getStudySession_returnsDueAndNewCards()
@Test void getStudySession_respectsMaxCards()
@Test void submitReview_appliesSM2Algorithm()
@Test void submitReview_createsProgressIfMissing()

// Card Operations
@Test void addCard_addsToEndOfDeck()
@Test void updateCard_updatesExistingCard()
@Test void removeCard_shiftsProgressIndexes()
```

### Step 10: Integration Testing & Documentation (30 min)

**Actions**:
1. Run full test suite: `./gradlew test`
2. Verify coverage: `./gradlew jacocoTestReport`
3. Update API documentation if needed
4. Update `daily-log.md` with implementation details

---

## 4. Timeline Estimate

| Step | Task | Time | Cumulative |
|------|------|------|------------|
| 1 | Create FlashcardService interface | 30 min | 0:30 |
| 2 | Create prompt template migration | 15 min | 0:45 |
| 3 | Implement core structure | 45 min | 1:30 |
| 4 | Implement content extraction | 45 min | 2:15 |
| 5 | Implement generateFromLesson | 60 min | 3:15 |
| 6 | Implement AI generation with fallback | 45 min | 4:00 |
| 7 | Implement study session methods | 45 min | 4:45 |
| 8 | Implement card CRUD operations | 30 min | 5:15 |
| 9 | Write unit tests | 90 min | 6:45 |
| 10 | Integration testing & docs | 30 min | 7:15 |

**Total Estimated Time**: 7-8 hours (split across 2 sessions)

---

## 5. Risks and Mitigations

### 5.1 Technical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| AI response parsing failure | Medium | High | Robust JSON cleaning + fallback cards |
| Lesson content extraction fails | Low | High | Handle all 4 lesson types explicitly |
| SM-2 algorithm bugs | Low | Medium | Use existing tested entity method |
| Token quota exceeded | Medium | Low | Fallback content ready |
| Database constraint violations | Low | Medium | Validate before save |

### 5.2 Integration Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| LessonService not returning content | Low | High | Verify content field is populated |
| GeminiClientService unavailable | Medium | Medium | Circuit breaker + fallback |
| Concurrent deck creation race condition | Low | Medium | Use unique constraint (user_id, source_type, source_id) |

### 5.3 Mitigation Strategies

1. **Fallback Content Strategy**:
   - Pre-define 50+ generic business English vocabulary cards
   - Categorized by CEFR level (A2, B1, B2, C1)
   - Use when AI fails or quota exceeded

2. **Content Extraction Resilience**:
   - Use `path()` instead of `get()` in Jackson to avoid null pointers
   - Provide default empty content if parsing fails
   - Log extraction issues for debugging

3. **Testing Strategy**:
   - Mock all external dependencies
   - Test each lesson type extraction separately
   - Test AI failure scenarios explicitly

---

## 6. Test Strategy

### 6.1 Unit Test Coverage Requirements

| Component | Target Coverage | Focus Areas |
|-----------|-----------------|-------------|
| FlashcardServiceImpl | ≥80% | All public methods |
| Content extraction | ≥90% | All 4 lesson types |
| AI generation | ≥80% | Success + fallback paths |
| SM-2 integration | ≥70% | Already tested in entity |

### 6.2 Test Dependencies (Mocks)

```java
@Mock private FlashcardDeckRepository deckRepository;
@Mock private UserFlashcardProgressRepository progressRepository;
@Mock private GeminiClientService geminiClientService;
@Mock private AiUsageTracker aiUsageTracker;
@Mock private PromptTemplateService promptTemplateService;
@Mock private LessonService lessonService;
@Mock private ObjectMapper objectMapper;

@InjectMocks private FlashcardServiceImpl flashcardService;
```

### 6.3 Test Data Fixtures

```java
// Sample lesson with READING content
private static final String READING_CONTENT = """
{
  "passages": [{
    "title": "Business Communication",
    "content": "Effective communication is essential..."
  }],
  "vocabulary": [{
    "term": "stakeholder",
    "definition": "a person with an interest in a business"
  }]
}
""";

// Sample AI response
private static final String AI_RESPONSE = """
[
  {
    "front": "stakeholder",
    "back": {
      "definition": "A person with an interest in a business",
      "partOfSpeech": "noun",
      "pronunciation": "/ˈsteɪkˌhoʊldər/"
    },
    "difficulty": 3
  }
]
""";
```

### 6.4 Test Execution

```bash
# Run all flashcard tests
./gradlew test --tests "*FlashcardServiceImplTest*"

# Generate coverage report
./gradlew jacocoTestReport

# View report
open build/reports/jacoco/test/html/index.html
```

---

## 7. File Summary

| File | Type | Action |
|------|------|--------|
| `service/ai/FlashcardService.java` | Interface | Create |
| `service/ai/impl/FlashcardServiceImpl.java` | Class | Create |
| `db/migration/V30__insert_flashcard_prompt_template.sql` | SQL | Create |
| `test/.../FlashcardServiceImplTest.java` | Test | Create |
| `docs/implement/sprint-5/daily-log.md` | Doc | Update |

---

## 8. Definition of Done

- [ ] FlashcardService interface created with all 14 methods
- [ ] FlashcardServiceImpl fully implemented
- [ ] Prompt template migration created
- [ ] All 4 lesson types supported for content extraction
- [ ] AI generation with fallback working
- [ ] Study session and review submission working
- [ ] Unit tests passing with ≥80% coverage
- [ ] No compilation errors
- [ ] Integration test with real AI call verified
- [ ] Documentation updated
- [ ] Code reviewed (conventional commits)

---

## 9. Next Steps After D4

| Task | Description | Dependency |
|------|-------------|------------|
| D5 | FlashcardController REST endpoints | D4 complete |
| F4 | Frontend flashcard deck list | D5 complete |
| F5 | Frontend flashcard study UI | D5 complete |
| M4 | Mobile flashcard screens | D5 complete |

---

*This plan follows the LEXIA coding standards and Sprint 5 specification. Implementation should be done incrementally with tests written alongside each method.*
