# LEXIA - Current Sprint Status

## Sprint 5 — AI Integration (Gemini API)

**Sprint**: 5 / 8 | **Duration**: Dec 12 – Dec 31, 2025 (20 days)  
**Status**: 🟢 In Progress (Day 5) | **Progress**: 19.0/31.5 points (60.3%)  
**Last Updated**: December 13, 2025 (Epic A: 93.3% ✅, Epic B: 50.0% ✅, Epic C: 60% ✅, Epic D: 80% ✅)

---

## 🤖 Sprint 5 Overview

**Goal**: Integrate Google Gemini AI to deliver intelligent, personalized English learning features across all platforms.

**Key Deliverables**:

- 🔄 AI Infrastructure (Gemini client + tracking + rate limiting) - **26.7% DONE**
- 🔄 Role-play conversations with AI partner - **Database Ready**
- 🔄 Grammar exercise generator - **Database Ready**
- 🔄 Flashcard auto-generation from lessons - **Database Ready**
- ⬜ Web UI for AI features
- ⬜ Mobile UI for AI features
- 🎯 Admin dashboard for AI monitoring (optional)

**Velocity Target**: 1.45 pts/day (29 points / 20 days)

**Recent Updates** (Dec 13 - Sprint Day 5):

- ✅ **Task D5 Complete**: FlashcardController with 8 REST endpoints + 20 unit tests (0.5 pt)
- ✅ **Task C5 Complete**: GrammarController with 13 REST endpoints + 41 unit tests (0.5 pt)
- ✅ **Task C6 Complete**: Answer validation already in GrammarExerciseServiceImpl (0.5 pt)
- ✅ **Epic C**: 6/7 tasks complete (80%) - REST API + Service layer complete
- ✅ **Controller**: 13 endpoints (generate, topics, exercises, submit, stats, history, progress, fallback)
- ✅ **Tests**: 41 comprehensive unit tests with @WebMvcTest + GlobalExceptionHandler
- ✅ **Code Review**: Perfect 10/10 score - NO ISSUES FOUND (production ready)
- ✅ **Validation**: All gradle tests passing, FlashcardController pattern followed
- ✅ **Previous**: D5, C4, D4, B4, C2, C3, A5, A9, A3, A4, A8, B2, B3, D2, D3 complete
- ✅ **Epic A**: 7/9 tasks complete (93.3%)
- ✅ **Epic B**: 3/7 tasks complete (42.9%)
- ✅ **Epic D**: 4/7 tasks complete (57.1%)
- 🎯 **Next**: A6 (Rate limiting), B5 (RolePlay controller), C7 (Grammar integration tests)

---

## 📋 Sprint 5 Story Breakdown

### Epic A: AI Infrastructure (7.5 pts)

**Status**: 🔄 In Progress (7.0/7.5 pts - 93.3%)  
**Timeline**: Day 1-4 (December 11-16)

- [x] **A1**: Add Gemini SDK + Resilience4j dependencies (0.5 pt) ✅ **COMPLETE** (Dec 11)
  - ✅ Google Gemini AI SDK v1.30.0 added to build.gradle
  - ✅ Resilience4j Spring Boot 3 module v2.2.0 (circuit breaker, retry, rate limiter, time limiter)
  - ✅ spring-boot-starter-aop for aspect support
  - ✅ Build validated: `./gradlew clean build -x test` successful
  - ✅ Dependencies resolved from Maven Central
  
- [x] **A2**: Create GeminiConfig with environment configuration (0.5 pt) ✅ **COMPLETE** (Dec 11)
  - ✅ GeminiConfig.java with @Configuration annotation
  - ✅ Gemini client bean with API key injection (returns null if not configured)
  - ✅ 3 content config beans: default (0.7 temp), structured (0.3), creative (0.9)
  - ✅ Configuration properties: model names, max tokens, temperature
  - ✅ 16 unit tests covering all scenarios
  
- [x] **A3**: Implement GeminiClientService with retry/circuit breaker + SSE support (2 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ GeminiClientService interface with 7 methods
  - ✅ GeminiClientServiceImpl with @Retry, @CircuitBreaker, @RateLimiter annotations
  - ✅ SSE streaming via SseEmitter with 30s timeout
  - ✅ 4 custom exception classes (AiServiceException hierarchy)
  - ✅ 3 response DTOs (TokenUsageDTO, GeminiResponseDTO, SseEventDTO)
  - ✅ Fallback methods with graceful degradation
  - ✅ Token estimation + cost calculation
  - ✅ @PreDestroy shutdown for ExecutorService
  - ✅ 46 unit tests (GeminiClientServiceTest, AiExceptionTest, AiDtoTest)
  - ✅ Code review: 8.7/10 quality, high-priority fixes applied
  
- [x] **A4**: Create V23 migration for AI usage tracking tables (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ Enhanced `ai_usage_logs` table (content_type, model_id, total_tokens, response_time_ms, success, error_message, request_metadata)
  - ✅ `user_ai_quotas` table with daily/monthly limits, premium multiplier, suspension support
  - ✅ Feature-specific limits in JSONB (roleplay, grammar, flashcard)
  - ✅ 3 analytics views: daily summary, monthly summary, by-user summary
  - ✅ Helper functions: reset quotas (daily/monthly), increment usage with quota checks
  - ✅ 15+ indexes for performance optimization
  - ✅ Safe column rename with DO block, COALESCE for NULL handling
  
- [x] **A5**: Implement AiUsageTracker service (1 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ AiUsageTracker interface with trackUsage(), quota management methods
  - ✅ AiUsageTrackerImpl with Gemini pricing calculation (Gemini 2.0 Flash: $0.075/$0.30 per M tokens)
  - ✅ UserAiQuota entity with daily/monthly limits and feature-specific JSONB limits
  - ✅ UserAiQuotaRepository with atomic increment operations
  - ✅ Async tracking with dedicated aiUsageExecutor (2-5 threads, 200 queue)
  - ✅ Enhanced AIUsageLog entity with V23 columns
  - ✅ Unit tests: AiUsageTrackerImplTest with comprehensive coverage
  
- [ ] **A6**: Implement AiRateLimitService per user/feature (1 pt) ⬜ **TODO**
  - Per-user daily/monthly quotas
  - Per-feature limits (roleplay, grammar, flashcard)
  - Quota enforcement and reset logic
  
- [x] **A7**: Implement input sanitization + prompt injection filter (1 pt) ✅ **COMPLETE** (Dec 11)
  - ✅ PromptSanitizer.java utility class with security patterns
  - ✅ Prompt injection detection (ignore instructions, jailbreak, role override)
  - ✅ SQL injection detection (UNION SELECT, DROP TABLE, ' OR '1'='1)
  - ✅ XSS detection (<script>, javascript:, onerror=)
  - ✅ Length validation (500 roleplay, 200 grammar)
  - ✅ ValidPrompt annotation + ValidPromptValidator
  - ✅ 70 unit tests with 100% coverage

- [x] **A8**: Create V24 migration for ai_prompt_templates table (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ `ai_prompt_templates` table with versioning (template_key, version, UNIQUE constraint)
  - ✅ A/B testing support (traffic_percentage, experiment_id)
  - ✅ Performance metrics tracking (usage_count, success_count, avg_response_time_ms, success_rate)
  - ✅ `prompt_template_history` audit table with change tracking
  - ✅ 5 seed prompt templates: roleplay_scenario_v1, roleplay_conversation_v1, grammar_exercise_v1, flashcard_generation_v1, context_summarization_v1
  - ✅ Helper functions: get_active_prompt_template(), increment_template_usage() with accurate metrics calculation
  - ✅ Trigger for automatic change tracking
  - ✅ Fixed success rate calculation (separate success_count column)
  - Audit logging of blocked attempts
  
- [x] **A9**: Implement PromptTemplateService with caching (1 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ PromptTemplateService interface with getTemplate(), resolveVariables(), A/B testing
  - ✅ PromptTemplateServiceImpl with @Cacheable annotations (5-min TTL)
  - ✅ Caffeine cache configuration (promptTemplates, userQuotas)
  - ✅ Mustache-style variable substitution with regex {{variable}}
  - ✅ A/B testing via weighted random selection based on traffic percentage
  - ✅ PromptTemplate entity with versioning and metrics tracking
  - ✅ PromptTemplateRepository with metrics and A/B queries
  - ✅ Unit tests: PromptTemplateServiceImplTest with comprehensive coverage

**Progress**: 7/9 tasks complete (7.0/7.5 points - 93.3%)

---

### Epic B: Role-Play Feature (7 pts)

**Status**: 🔄 In Progress (3.0/7 pts - 42.9%)  
**Timeline**: Day 2-9 (December 12-21)  
**Dependencies**: Epic A (A2, A3)

- [x] **B1**: Create V25 migration for roleplay tables (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ roleplay_scenarios table (14 columns, UUID PK, CEFR checks, JSONB objectives/vocabulary)
  - ✅ roleplay_conversations table (10 columns, messages JSONB, mode enum, metrics)
  - ✅ user_ai_quotas table (8 columns, daily/monthly limits)
  - ✅ ai_prompt_templates table (9 columns, hot-reload support)
  - ✅ 12 fallback scenarios (A1: 2, A2: 2, B1: 2, B2: 2, C1: 2, C2: 2)
  - ✅ 2 prompt templates (scenario generation, conversation responses)
  - ✅ 8 indexes for performance optimization
- [x] **B2**: Create RolePlayScenario and RolePlayConversation entities (1 pt) ✅ **COMPLETE** (Dec 12)
- [x] **B3**: Create DTOs and mappers for role-play (0.5 pt) ✅ **COMPLETE** (Dec 12)
- [x] **B4**: Implement RolePlayService (scenario generation) (1.5 pt) ✅ **COMPLETE** (Dec 13)
  - ✅ RolePlayScenarioRepository and RolePlayConversationRepository
  - ✅ RolePlayService interface with 7 methods
  - ✅ RolePlayServiceImpl with Gemini integration (~199 lines)
  - ✅ Scenario generation with AI, conversation management, message handling
  - ✅ Context window optimization (last 10 messages)
  - ✅ Input sanitization, ownership validation, status checks
  - ✅ AI usage tracking integration
  - ✅ 3 unit tests in RolePlayServiceImplTest
  - ✅ Code review: 7/10 quality, all fixes applied
- [ ] **B5a**: Implement immersive mode (chat-only, fast) (0.5 pt)
- [ ] **B5b**: Implement learning mode (chat + feedback) (1 pt)
- [ ] **B5c**: Implement SSE streaming for AI responses (SseEmitter) (1.5 pt)
- [ ] **B6**: Create RolePlayController with endpoints (1 pt)
- [ ] **B7**: Implement FallbackContentService for scenarios (0.5 pt) 🔵 **P1**
- [ ] **B8**: Write unit + integration tests (≥70%) (1 pt)

---

### Epic C: Grammar Exercise Feature (5 pts)

**Status**: 🔄 In Progress (4.0/5 pts - 80%)  
**Timeline**: Day 2-12 (December 12-24)  
**Dependencies**: Epic A

- [x] **C1**: Create V26 migration for grammar tables (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ grammar_topics table (8 columns, 40+ topics with CEFR levels)
  - ✅ grammar_exercise_sets table (10 columns, JSONB content, fallback support)
  - ✅ user_grammar_progress table (10 columns, answers JSONB, scoring)
  - ✅ 40+ grammar topics (Tenses: 13, Modals: 7, Conditionals: 5, Voice: 6, Clauses: 4, Other: 10+)
  - ✅ 2 fallback exercise sets (Present Simple A2, First Conditional B1)
  - ✅ 2 prompt templates (exercise generation, flashcard generation)
  - ✅ 9 indexes for efficient queries
- [x] **C2**: Create GrammarExerciseSet entity and repository (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ GrammarTopic entity with category grouping and CEFR levels
  - ✅ GrammarExerciseSet entity with JSONB content storage
  - ✅ UserGrammarProgress entity with SM-2 scoring
  - ✅ GrammarTopicRepository with custom queries
  - ✅ GrammarExerciseSetRepository with filtering methods
  - ✅ UserGrammarProgressRepository with progress tracking
- [x] **C3**: Create DTOs and mappers (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ 7 Grammar DTOs with validation (GrammarTopicDTO, GrammarRequestDTO, GrammarExerciseDTO, GrammarExerciseSetDTO, GrammarAnswerDTO, GrammarResultDTO, GrammarProgressDTO)
  - ✅ GrammarExerciseMapper with content parsing
  - ✅ GrammarExerciseMapperTest with 20 passing tests
  - ✅ Code review: Typo fixed in GrammarTopicRepository
- [x] **C4**: Implement GrammarExerciseService (1.5 pt) ✅ **COMPLETE** (Dec 13)
  - ✅ GrammarExerciseService interface with 14 method signatures
  - ✅ GrammarExerciseServiceImpl implementation (~600 lines)
  - ✅ AI generation with retry + fallback mechanism
  - ✅ Daily quota enforcement (50 requests/day via AiUsageTracker)
  - ✅ Ownership verification (IDOR prevention)
  - ✅ Robust answer mapping (HashMap for out-of-order submissions)
  - ✅ Scoring logic (70% passing threshold, detailed feedback)
  - ✅ GrammarStatsDTO with nested TopicStats and LevelStats classes
  - ✅ GrammarExerciseSetRepository.findFallbackByCefrLevelAndGrammarPoint() added
  - ✅ 28 unit tests (100% pass rate): Generate (5), Topics (4), Retrieval (3), Submit (9), History (5), Fallback (2)
  - ✅ Code review: Fixed critical IDOR vulnerability, fixed major answer mapping flaw
  - ✅ Security tests: Ownership checks, public exercise access, out-of-order handling
- [x] **C5**: Create GrammarController with endpoints (0.5 pt) ✅ **COMPLETE** (Dec 13)
  - ✅ GrammarController.java with 13 REST endpoints (588 lines)
  - ✅ POST /generate - AI exercise generation with validation
  - ✅ GET /topics - List all, by level, by category, get categories
  - ✅ GET /exercises - Get by ID, list user's (paginated)
  - ✅ POST /submit - Submit answers with scoring
  - ✅ GET /history - User history (paginated)
  - ✅ GET /stats - User statistics (completion, accuracy, by level/topic)
  - ✅ GET /progress - Progress for specific exercise set
  - ✅ GET /fallback - Fallback exercises (CEFR level, grammar point)
  - ✅ Comprehensive Swagger/OpenAPI documentation
  - ✅ Input validation via validateCefrLevel()
  - ✅ Security via @AuthenticationPrincipal User
  - ✅ 41 unit tests in GrammarControllerTest (701 lines)
  - ✅ @WebMvcTest with @ContextConfiguration(GlobalExceptionHandler)
  - ✅ Code review: Perfect 10/10 score - NO ISSUES FOUND
  - ✅ All gradle tests passing (100% success rate)
  - ✅ Follows FlashcardController pattern consistently
- [x] **C6**: Implement answer validation and scoring (0.5 pt) ✅ **COMPLETE** (Dec 13)
  - ✅ Already implemented in GrammarExerciseServiceImpl.submitAnswers()
  - ✅ Scoring: (Correct / Total) * 100 with BigDecimal precision
  - ✅ Passing threshold: 70%
  - ✅ Detailed feedback per question with explanations
  - ✅ Out-of-order answer handling via HashMap
  - ✅ Duplicate submission prevention
  - ✅ IDOR prevention via ownership checks
- [ ] **C7**: Write unit + integration tests (≥70%) (1 pt)

---

### Epic D: Flashcard Feature (5 pts)

**Status**: 🔄 In Progress (4.0/5 pts - 80%)  
**Timeline**: Day 2-15 (December 12-27)  
**Dependencies**: Epic A

- [x] **D1**: Add flashcard tables to V26 migration (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ flashcard_decks table (11 columns, source_type enum, cards JSONB)
  - ✅ user_flashcard_progress table (12 columns, SM-2 algorithm support)
  - ✅ SM-2 spaced repetition: ease_factor (1.30+ unbounded), interval_days, mastery_level (0-5)
  - ✅ Source consistency CHECK constraint (lesson requires source_id)
  - ✅ 7 indexes including partial index for due cards
  - ✅ Code review fixes: ease_factor constraint corrected for SM-2 algorithm
- [x] **D2**: Create FlashcardDeck entity and repository (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ FlashcardDeck entity with JSONB cards storage (List<FlashcardCard>)
  - ✅ FlashcardCard POJO with front, back (FlashcardBack), tags, difficulty
  - ✅ FlashcardBack POJO with definition, partOfSpeech, pronunciation, synonyms, collocations
  - ✅ UserFlashcardProgress entity with full SM-2 algorithm implementation
  - ✅ FlashcardDeckRepository with custom queries (due cards, statistics, batch ops)
  - ✅ UserFlashcardProgressRepository with spaced repetition queries
  - ✅ SourceType enum (LESSON, AI_GENERATED, USER_CREATED)
  - ✅ 64 unit tests passing (FlashcardDeckTest, UserFlashcardProgressTest)
- [x] **D3**: Create DTOs and mappers (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ 9 DTOs: FlashcardBackDTO, FlashcardCardDTO, FlashcardDeckDTO, CreateFlashcardDeckDTO, UpdateFlashcardDeckDTO, FlashcardProgressDTO, FlashcardStudySessionDTO, FlashcardReviewResultDTO, GenerateFlashcardsDTO
  - ✅ FlashcardMapper with null-safe entity-DTO conversions
  - ✅ Swagger/OpenAPI @Schema annotations on all DTOs
  - ✅ Jakarta validation: @NotBlank, @NotNull, @Valid annotations
  - ✅ Code review 8.5/10, validation fixes applied
  - ✅ FlashcardMapperTest with comprehensive coverage
- [x] **D4**: Implement FlashcardService (generate from lesson) (1.5 pt) ✅ **COMPLETE** (Dec 13)
  - ✅ FlashcardService interface with full method signatures
  - ✅ FlashcardServiceImpl with AI generation + fallback
  - ✅ SM-2 spaced repetition algorithm integration
  - ✅ Quota enforcement via AiUsageTracker
- [x] **D5**: Create FlashcardController with endpoints (0.5 pt) ✅ **COMPLETE** (Dec 13)
  - ✅ FlashcardController.java (601 lines) in controller/ai/ package
  - ✅ 8 primary REST endpoints + 2 helper endpoints
  - ✅ POST /api/v1/ai/flashcards/generate - AI generation from lesson (201 Created)
  - ✅ POST /api/v1/ai/flashcards/decks - Create custom deck (201 Created)
  - ✅ GET /api/v1/ai/flashcards/decks - List user's decks with pagination (page size max 100)
  - ✅ GET /api/v1/ai/flashcards/decks/{id} - Get deck with all cards (200 OK)
  - ✅ PUT /api/v1/ai/flashcards/decks/{id} - Update deck metadata/cards (200 OK)
  - ✅ DELETE /api/v1/ai/flashcards/decks/{id} - Delete deck (204 No Content)
  - ✅ GET /api/v1/ai/flashcards/decks/{id}/study - Get study session (SM-2 ordered, max 50 cards)
  - ✅ POST /api/v1/ai/flashcards/decks/{id}/review - Submit review with quality ratings (200 OK)
  - ✅ GET /api/v1/ai/flashcards/due-count - Total due cards across all decks
  - ✅ GET /api/v1/ai/flashcards/lessons/{id}/deck - Check if lesson deck exists
  - ✅ FlashcardControllerTest.java (562 lines) with 20 unit tests
  - ✅ 9 nested @DisplayName test classes for organization
  - ✅ Mock-based tests: FlashcardService, JwtTokenProvider, CustomUserDetailsService
  - ✅ Comprehensive Swagger/OpenAPI documentation (summaries, descriptions, examples)
  - ✅ Defensive coding: Page size capped at 100, study session max 50 cards
  - ✅ Security: @AuthenticationPrincipal + ownership checks via service layer
  - ✅ Code review: **PASS** - High quality, well-documented, secure
  - ✅ All tests passing (100% pass rate)
- [x] **D6**: Implement spaced repetition algorithm (0.5 pt) ✅ **COMPLETE** (Dec 13)
  - ✅ SM-2 algorithm in UserFlashcardProgress.applyReview() method
  - ✅ Quality ratings 0-5 (blackout → perfect recall)
  - ✅ Ease factor adjustment (1.30 min, unbounded max)
  - ✅ Interval calculation with exponential growth
  - ✅ Mastery level progression (0=New → 5=Expert)
  - ✅ Next review date scheduling
- [ ] **D7**: Write unit + integration tests (≥70%) (1 pt)

---

### Epic E: Web Frontend (4 pts)

**Status**: ⬜ Not Started  
**Timeline**: Day 15-18 (December 27-30)  
**Dependencies**: Epics B, C, D

- [ ] **E1**: Create AI services (roleplay, grammar, flashcard) (0.5 pt)
- [ ] **E2**: Build role-play chat interface (1 pt)
- [ ] **E2a**: Add mode toggle button (Immersive/Learning) (0.5 pt) 🔵 **P1**
- [ ] **E3**: Build grammar sandbox UI (1 pt)
- [ ] **E4**: Build flashcard study interface with animations (1 pt)
- [ ] **E5**: Add AI loading states and error handling (0.5 pt)

---

### Epic F: Mobile Frontend (3 pts)

**Status**: ⬜ Not Started  
**Timeline**: Day 18-20 (December 30-31)  
**Dependencies**: Epics B, C, D

- [ ] **F1**: Create AI services matching web (0.5 pt)
- [ ] **F2**: Build role-play screen (mobile chat) (1 pt)
- [ ] **F3**: Build grammar practice screen (0.5 pt)
- [ ] **F4**: Build flashcard screen with gestures (1 pt)

---

### Epic G: Admin Dashboard (2.5 pts) - Optional/Sprint 6

**Status**: ⬜ Deferred  
**Timeline**: Spillover or Sprint 6  
**Dependencies**: Epic A

- [ ] **G1**: Create AI monitoring routes (0.5 pt) 🔵 **P2**
- [ ] **G2**: Build usage statistics dashboard (1 pt) 🔵 **P2**
- [ ] **G3**: Build cost breakdown charts (0.5 pt) 🔵 **P2**
- [ ] **G4**: Add quota management table (0.5 pt) 🔵 **P2**

---

## 📊 Sprint 5 Metrics

| Metric | Target | Current | Status |
|--------|--------|---------|--------|
| **Story Points** | 31.5 | 20.0 | 63.5% |
| **Tasks Complete** | 37 | 21 | 56.8% |
| **Test Coverage (Backend)** | ≥70% | TBD | ⬜ |
| **Test Coverage (Frontend)** | ≥60% | TBD | ⬜ |
| **P0 Bugs** | 0 | 0 | ✅ |
| **Days Remaining** | 20 | 15 | - |
| **Velocity** | 1.45 pts/day | 4.00 pts/day | Day 5 |

---

## 🎯 Immediate Next Steps

1. **A2** (Dec 12): Create GeminiConfig.java - Configuration bean for Gemini client
2. **A7** (Dec 12): Implement input sanitization + prompt injection filter
3. **A3** (Dec 13): Implement GeminiClientService with Resilience4j decorators
4. **Test** (Dec 13): Validate Gemini API connectivity with simple prompt

---

## 🔗 Documentation

- [Sprint 5 Plan](./sprint-5/SPRINT-5-PLAN.md) - Detailed schedule & tasks
- [Sprint 5 Specification](./sprint-5/SPRINT-5-SPECIFICATION.md) - Requirements & schemas
- [Daily Log](../implement/sprint-5/daily-log.md) - Daily progress tracking

---

## Previous Sprints Summary

### Sprint 4 — Mobile Development (React Native) ✅ COMPLETE

**Sprint**: 4 / 8 | **Duration**: Nov 22 – Dec 7, 2025 (16 days)  
**Status**: ✅ Complete (79.1%) | **Progress**: 34/43 points  

**Delivered**: Expo project setup, Authentication flow, Navigation & Layout, Course screens, Progress tracking, Testing infrastructure

### Sprint 3 — Frontend Development (Web) ✅ COMPLETE

**Sprint**: 3 / 8 | **Duration**: Nov 8 – Nov 21, 2025 (14 days)  
**Status**: ✅ Complete (100%) | **Progress**: 29.5/29 points  

**Delivered**: Course & Learning Path UI, Progress & Profile management, Testing & Polish (WCAG AA compliant)

### Sprint 2 — Completed ✅

**Sprint**: 2 / 8 | **Duration**: Oct 29 – Nov 7, 2025 (9 days)  
**Status**: ✅ Complete (100%) | **Progress**: 21/21 points  
**Coverage**: 87% overall, 93% services

**Delivered**: Course/Lesson APIs, Learning Paths, Progress Tracking, Actuator

### Sprint 1 — Completed ✅

**Sprint**: 1 / 8 | **Duration**: Oct 16-28, 2025 (12 days)  
**Status**: ✅ Complete (100%) | **Coverage**: 81%

**Delivered**: JWT Auth, User Management, Profile API, Swagger Docs, Token Rotation

---
