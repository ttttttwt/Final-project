# LEXIA - Current Sprint Status

## Sprint 5 — AI Integration (Gemini API)

**Sprint**: 5 / 8 | **Duration**: Dec 12 – Dec 31, 2025 (20 days)  
**Status**: 🟢 In Progress (Day 2) | **Progress**: 6.5/31.5 points (20.6%)  
**Last Updated**: December 12, 2025 - Afternoon (Epic A: 66.7%, Epic B: 100% migrations, Epic C: 100% migrations, Epic D: 100% migrations ✅)

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

**Recent Updates** (Dec 12 - Sprint Day 2 - Afternoon):

- ✅ **Task A3 Complete**: GeminiClientService with Resilience4j + SSE streaming (2 pts)
- ✅ **Task A4 Complete**: V23 migration for AI usage tracking + user quotas (0.5 pts)
- ✅ **Task A8 Complete**: V24 migration for AI prompt templates with versioning (0.5 pts)
- ✅ **Task B1 Complete**: V25 migration for roleplay tables (scenarios, conversations, quotas, prompts)
- ✅ **Task C1 Complete**: V26 migration for grammar tables (topics, exercises, progress)
- ✅ **Task D1 Complete**: Flashcard tables added to V26 (decks, SM-2 spaced repetition)
- ✅ **AI Infrastructure**: 10 new files (exceptions, DTOs, service, tests) - 1,750+ lines
- ✅ **Database Schema**: 12 new tables created with comprehensive indexes and constraints
- ✅ **Code Review**: Critical fixes applied (safe column rename, COALESCE, success rate calculation)
- ✅ **Tests**: 46 unit tests passing, comprehensive coverage
- 🎯 **Next Day 3**: A5-A6 (Usage tracker + Rate limiting), B2-B3 (Entities & DTOs)

---

## 📋 Sprint 5 Story Breakdown

### Epic A: AI Infrastructure (7.5 pts)

**Status**: 🔄 In Progress (5.0/7.5 pts - 66.7%)  
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
  
- [ ] **A5**: Implement AiUsageTracker service (1 pt) ⬜ **TODO**
  - Log all AI API calls
  - Calculate token usage and costs
  - Track success/failure rates
  
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
  
- [ ] **A8**: Create V20 migration for ai_prompt_templates table (0.5 pt) 🔵 **P1** (Optional)
  - Table for reusable prompt templates
  - Version control for prompts
  
- [ ] **A9**: Implement PromptTemplateService with caching (1 pt) 🔵 **P1** (Optional)
  - Load prompts from database
  - Cache with Spring @Cacheable
  - Template variable substitution

**Progress**: 1/9 tasks complete (0.5/7.5 points)

---

### Epic B: Role-Play Feature (7 pts)

**Status**: 🔄 In Progress (0.5/7 pts - 7.1%)  
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
- [ ] **B2**: Create RolePlayScenario and RolePlayConversation entities (1 pt)
- [ ] **B3**: Create DTOs and mappers for role-play (0.5 pt)
- [ ] **B4**: Implement RolePlayService (scenario generation) (1.5 pt)
- [ ] **B5a**: Implement immersive mode (chat-only, fast) (0.5 pt)
- [ ] **B5b**: Implement learning mode (chat + feedback) (1 pt)
- [ ] **B5c**: Implement SSE streaming for AI responses (SseEmitter) (1.5 pt)
- [ ] **B6**: Create RolePlayController with endpoints (1 pt)
- [ ] **B7**: Implement FallbackContentService for scenarios (0.5 pt) 🔵 **P1**
- [ ] **B8**: Write unit + integration tests (≥70%) (1 pt)

---

### Epic C: Grammar Exercise Feature (5 pts)

**Status**: 🔄 In Progress (0.5/5 pts - 10%)  
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
- [ ] **C2**: Create GrammarExerciseSet entity and repository (0.5 pt)
- [ ] **C3**: Create DTOs and mappers (0.5 pt)
- [ ] **C4**: Implement GrammarExerciseService (1.5 pt)
- [ ] **C5**: Create GrammarController with endpoints (0.5 pt)
- [ ] **C6**: Implement answer validation and scoring (0.5 pt) 🔵 **P1**
- [ ] **C7**: Write unit + integration tests (≥70%) (1 pt)

---

### Epic D: Flashcard Feature (5 pts)

**Status**: 🔄 In Progress (0.5/5 pts - 10%)  
**Timeline**: Day 2-15 (December 12-27)  
**Dependencies**: Epic A

- [x] **D1**: Add flashcard tables to V26 migration (0.5 pt) ✅ **COMPLETE** (Dec 12)
  - ✅ flashcard_decks table (11 columns, source_type enum, cards JSONB)
  - ✅ user_flashcard_progress table (12 columns, SM-2 algorithm support)
  - ✅ SM-2 spaced repetition: ease_factor (1.30+ unbounded), interval_days, mastery_level (0-5)
  - ✅ Source consistency CHECK constraint (lesson requires source_id)
  - ✅ 7 indexes including partial index for due cards
  - ✅ Code review fixes: ease_factor constraint corrected for SM-2 algorithm
- [ ] **D2**: Create FlashcardDeck entity and repository (0.5 pt)
- [ ] **D3**: Create DTOs and mappers (0.5 pt)
- [ ] **D4**: Implement FlashcardService (generate from lesson) (1.5 pt)
- [ ] **D5**: Create FlashcardController with endpoints (0.5 pt)
- [ ] **D6**: Implement spaced repetition algorithm (0.5 pt) 🔵 **P1**
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
| **Story Points** | 29 | 0.5 | 1.7% |
| **Tasks Complete** | 37 | 1 | 2.7% |
| **Test Coverage (Backend)** | ≥70% | TBD | ⬜ |
| **Test Coverage (Frontend)** | ≥60% | TBD | ⬜ |
| **P0 Bugs** | 0 | 0 | ✅ |
| **Days Remaining** | 20 | 19 | - |
| **Velocity** | 1.45 pts/day | 0.5 pts/day | Day 1 |

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
