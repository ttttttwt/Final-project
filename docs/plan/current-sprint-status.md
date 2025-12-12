# LEXIA - Current Sprint Status

## Sprint 5 — AI Integration (Gemini API)

**Sprint**: 5 / 8 | **Duration**: Dec 12 – Dec 31, 2025 (20 days)  
**Status**: 🟢 In Progress (Day 1) | **Progress**: 2.0/29 points (6.9%)  
**Last Updated**: December 11, 2025 (Epic A: 26.7% - Tasks A1, A2, A7 Complete ✅)

---

## 🤖 Sprint 5 Overview

**Goal**: Integrate Google Gemini AI to deliver intelligent, personalized English learning features across all platforms.

**Key Deliverables**:

- 🔄 AI Infrastructure (Gemini client + tracking + rate limiting) - **6.7% DONE**
- ⬜ Role-play conversations with AI partner
- ⬜ Grammar exercise generator
- ⬜ Flashcard auto-generation from lessons
- ⬜ Web UI for AI features
- ⬜ Mobile UI for AI features
- 🎯 Admin dashboard for AI monitoring (optional)

**Velocity Target**: 1.45 pts/day (29 points / 20 days)

**Recent Updates** (Dec 11 - Sprint Day 1):

- ✅ **Sprint Planning Complete**: Comprehensive plan & specification documents created
- ✅ **Task A1 Complete**: Gemini SDK + Resilience4j dependencies added
- ✅ **Task A2 Complete**: GeminiConfig.java with 3 content configuration beans created
- ✅ **Task A7 Complete**: PromptSanitizer + ValidPrompt annotation for security
- ✅ **Security**: Prompt injection, SQL injection, XSS detection patterns implemented
- ✅ **Tests**: 86 tests passing (16 config tests + 70 sanitizer tests)
- ✅ **Quality**: All files compile, no errors, tests at 100%
- 🎯 **Next Day 2**: A3 (GeminiClientService), A4 (Database migrations)

---

## 📋 Sprint 5 Story Breakdown

### Epic A: AI Infrastructure (7.5 pts)

**Status**: 🔄 In Progress (2.0/7.5 pts - 26.7%)  
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
  
- [ ] **A3**: Implement GeminiClientService with retry/circuit breaker + SSE support (2 pt) ⬜ **TODO**
  - Core service for Gemini API calls
  - @Retry, @CircuitBreaker, @RateLimiter annotations
  - Support for streaming responses (Server-Sent Events)
  - Fallback handling
  
- [ ] **A4**: Create V20 migration for AI usage tracking tables (0.5 pt) ⬜ **TODO**
  - `ai_usage_logs` table
  - `user_ai_quotas` table
  - Indexes for performance
  - Daily summary view
  
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
  - Input validation (max length, allowed characters)
  - Prompt injection detection (regex patterns)
  - XSS prevention
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

**Status**: ⬜ Not Started  
**Timeline**: Day 4-9 (December 16-21)  
**Dependencies**: Epic A (A2, A3)

- [ ] **B1**: Create V21 migration for roleplay tables (0.5 pt)
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

**Status**: ⬜ Not Started  
**Timeline**: Day 9-12 (December 21-24)  
**Dependencies**: Epic A

- [ ] **C1**: Create V22 migration for grammar tables (0.5 pt)
- [ ] **C2**: Create GrammarExerciseSet entity and repository (0.5 pt)
- [ ] **C3**: Create DTOs and mappers (0.5 pt)
- [ ] **C4**: Implement GrammarExerciseService (1.5 pt)
- [ ] **C5**: Create GrammarController with endpoints (0.5 pt)
- [ ] **C6**: Implement answer validation and scoring (0.5 pt) 🔵 **P1**
- [ ] **C7**: Write unit + integration tests (≥70%) (1 pt)

---

### Epic D: Flashcard Feature (5 pts)

**Status**: ⬜ Not Started  
**Timeline**: Day 12-15 (December 24-27)  
**Dependencies**: Epic A

- [ ] **D1**: Add flashcard tables to V22 migration (0.5 pt)
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
