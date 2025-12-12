# LEXIA Sprint 5 - Parallel Execution Roadmap

**Created**: December 12, 2025  
**Purpose**: Task execution strategy without day-by-day constraints  
**Strategy**: Maximum parallelization with dependency-driven phases

---

## 📋 Quick Reference - Parallel Phases

```
[PHASE 1] → [PHASE 2] → [PHASE 3] → [PHASE 4] → [PHASE 5] → [PHASE 6]
Setup      Migrations   Backend    Testing    Frontend   Polish
(⚡ 2h)   (⚡ 4h)      (⚡ 16h)   (⚡ 8h)    (⚡ 12h)  (⚡ 4h)
```

---

## 🎯 PHASE 1: Setup & Configuration (Blocker Phase - 2h)
**Must complete before proceeding**

### Dependencies Installation & Config
```
A1   ─→ A2 ──┐
     └→ A7 ──┤ (Parallel after A1)
             ├─→ [PHASE 2]
          A6 ┘
```

| Task | Duration | Dependencies | Parallelizable |
|------|----------|--------------|-----------------|
| **A1** | 0.5h | None | ❌ Sequential |
| **A2** | 0.5h | A1 | ✅ After A1 |
| **A7** | 1h | A1 | ✅ After A1 |
| **A6** | 1h | A1 | ✅ After A1 |

**Execution**: 
1. Do A1 first
2. After A1 completes → Launch A2, A7, A6 **in parallel**

**Output**: 
- ✅ build.gradle updated
- ✅ GeminiConfig.java created
- ✅ InputSanitizer.java created
- ✅ RateLimitConfig.java created

---

## 🎯 PHASE 2: Database Migrations & Core Services (4h)
**Can start after Phase 1 → Don't wait for other Phase 1 tasks**

### 5 Independent Migrations + 2 Core Services
```
┌─ A3 (GeminiClientService) ──┐
│                             ├─→ [PHASE 3]
└─ A4, A8, B1, C1, D1 ────────┘
   (5 migrations - all parallel)

├─ A5 ──┐
└─ A9 ──┤ (Parallel, depend on A3)
        └─→ [PHASE 3]
```

### Parallel Tracks

**Track 1: Core AI Client (1h)**
```
A3: GeminiClientService
   - Retry logic (exponential backoff)
   - Circuit breaker pattern
   - SSE emitter for streaming
   - Error handling
   - Resilience4j integration
```

**Track 2: Database Migrations (2h) - ALL PARALLEL**
```
A4:  V20_add_ai_usage_tracking.sql
     └─ Tables: ai_usage_logs, user_ai_quotas
     └─ Indexes: user, date, type
     
A8:  V20_add_ai_prompt_templates.sql (same migration file)
     └─ Table: ai_prompt_templates
     
B1:  V21_add_roleplay_tables.sql
     └─ Tables: roleplay_scenarios, roleplay_conversations
     
C1:  V22_add_grammar_tables.sql
     └─ Table: grammar_exercise_sets
     
D1:  V22_add_flashcard_tables.sql (same migration file)
     └─ Tables: flashcard_decks, user_flashcard_progress
```

**Track 3: Core Tracking Services (1h) - Start after A3 + A4/A8 complete**
```
A5:  AiUsageTracker
     - Log API calls
     - Token counting
     - Cost calculation
     
A9:  PromptTemplateService
     - Load from DB
     - Caching layer
     - Template substitution
```

**Execution**:
1. Start **A3 + A4 + A8 + B1 + C1 + D1** immediately (5 tasks in parallel)
2. After **A3 + A4 + A8** done → Start **A5 + A9** (2 tasks in parallel)
3. Don't wait for B1, C1, D1 to start Phase 3

**Output**:
- ✅ GeminiClientService.java (circuit breaker, SSE ready)
- ✅ 5 migration files applied
- ✅ AiUsageTracker.java
- ✅ PromptTemplateService.java

---

## 🎯 PHASE 3: Feature Backend Implementation (16h)
**Can start immediately after A3 completes (don't wait for migrations)**

### 3 Independent Feature Implementations (RolePlay, Grammar, Flashcard)
```
FEATURE 1: ROLEPLAY          FEATURE 2: GRAMMAR           FEATURE 3: FLASHCARD
├─ B2 (Entities)            ├─ C2 (Entities)             ├─ D2 (Entities)
├─ B3 (DTOs)                ├─ C3 (DTOs)                 ├─ D3 (DTOs)
├─ B4 (Service)    ┐        ├─ C4 (Service)    ┐         ├─ D4 (Service)    ┐
├─ B5 (Conversation) ├──┐   ├─ C5 (Controller) ├──┐      ├─ D5 (Controller) ├──┐
├─ B6 (Controller)  ┤  │    └─ C6 (Scoring)   │  │       └─ D6 (Spaced Rep)│  │
├─ B7 (Fallback)    ├──┤                      └──┤                          └──┤
└─ B9 (Context Mgmt)│  │                         │                             │
                    │  ├─ [Phase 4: Testing] ───┴─ [Phase 5: Frontend]
                    │  │
                    └──┘
```

### Parallel Execution Strategy

**Wave 1: Entities + DTOs (2h) - ALL 3 FEATURES IN PARALLEL**
```
B2 ─┐
C2 ─┼─→ All parallel (no dependency on each other)
D2 ─┘

B3 ─┐
C3 ─┼─→ All parallel (no dependency on each other)
D3 ─┘
```

**Wave 2: Services (6h) - ALL 3 FEATURES IN PARALLEL**
```
B4 (RolePlayService) ────┐
C4 (GrammarService) ─────┼─→ All parallel (all depend on A3, which is done)
D4 (FlashcardService) ───┘
```

**Wave 3: Controllers + Supporting (5h) - ALL IN PARALLEL**
```
B5 (Conversation Logic) ─┐
B6 (Controller)          │
B7 (Fallback)            │
B9 (Context Mgmt)        ├─ RolePlay feature: ~5h
                         │
C5 (Controller)          ├─ Grammar feature: ~3h
C6 (Scoring)             │
                         │
D5 (Controller)          ├─ Flashcard feature: ~4h
D6 (Spaced Repetition)   │
                         │
     ALL 3 can run in parallel
```

**Execution Order**:
```
1. Do B2 + C2 + D2 in parallel (1h)
2. Do B3 + C3 + D3 in parallel (1h)
3. Do B4 + C4 + D4 in parallel (6h)
4. Do B5+B6+B7+B9 + C5+C6 + D5+D6 in parallel (5h)
5. → Ready for Phase 4
```

**Output**:
- ✅ 3 complete feature backends
- ✅ All entities, DTOs, services, controllers
- ✅ Role-play with SSE streaming support
- ✅ Grammar with detailed scoring
- ✅ Flashcard with spaced repetition

---

## 🎯 PHASE 4: Backend Testing & Quality (8h)
**Overlaps with Phase 5 - Start after Phase 3 entities**

### Testing for All 3 Features (Parallel)
```
B8 (RolePlay Tests) ──┐
C7 (Grammar Tests) ───┼─→ All parallel
D7 (Flashcard Tests) ─┘
```

| Feature | Unit Tests | Integration Tests | Coverage Target |
|---------|------------|-------------------|-----------------|
| **RolePlay** | 12 tests | 8 tests | 75% |
| **Grammar** | 10 tests | 6 tests | 75% |
| **Flashcard** | 10 tests | 6 tests | 75% |

**Test Categories (All Parallel)**:
```
Per Feature:
├─ Service logic tests (mocked AI)
├─ Controller endpoint tests (MockMvc)
├─ Entity repository tests
├─ Error handling tests
├─ Edge case tests
└─ AI response validation tests
```

**Execution**:
1. Write tests while implementing Phase 3 (TDD approach)
2. After Phase 3 code complete → Run all 3 test suites in parallel
3. Target: ≥70% coverage per feature

**Output**:
- ✅ 30+ unit tests
- ✅ 20+ integration tests
- ✅ Coverage reports
- ✅ All tests passing

---

## 🎯 PHASE 5: Frontend Implementation (12h)
**Can start after A3 + Phase 3 services complete**

### Web Frontend (E1-E5) vs Mobile Frontend (F1-F4) - PARALLEL

**Track 1: Web Frontend (7h)**
```
E1 (AI Services) ──┐
                   ├─→ All parallel
E2 (RolePlay UI) ──┤   - No dependencies between
E3 (Grammar UI) ───┤     components
E4 (Flashcard UI) ─┤   - Can build independently
E5 (Loading States)┘
```

**Track 2: Mobile Frontend (5h)**
```
F1 (AI Services) ────┐
                     ├─→ All parallel
F2 (RolePlay Screen) ├─ Similar structure
F3 (Grammar Screen) ─┤ as Web but with
F4 (Flashcard Screen)┘ mobile-specific patterns
```

**Parallel Strategy**:
```
WEB TEAM              MOBILE TEAM
├─ E1: Services      ├─ F1: Services
├─ E2: RolePlay UI   ├─ F2: RolePlay Screen
├─ E3: Grammar UI    ├─ F3: Grammar Screen
├─ E4: Flashcard UI  ├─ F4: Flashcard Screen
└─ E5: Error states  └─ (No explicit E5 equivalent, built-in)

Can be done simultaneously
No blocking dependencies
```

**Execution**:
1. Create AI service files: **E1 + F1 in parallel**
2. Create UI components: **E2+E3+E4+E5 in parallel AND F2+F3+F4 in parallel**
3. Both teams work independently

**Output**:
- ✅ Web AI services
- ✅ 5 web UI components/pages
- ✅ Mobile AI services
- ✅ 4 mobile screens
- ✅ Error handling + loading states

---

## 🎯 PHASE 6: Polish & Integration (4h)
**Final phase - Sequential validation**

```
├─ Run full test suite (all tests)
├─ Integration testing (E2E)
├─ Performance testing
├─ Error scenario testing
├─ Documentation finalization
└─ Deployment preparation
```

**Validation Checklist**:
- [ ] All backend tests pass
- [ ] All frontend tests pass
- [ ] No console errors
- [ ] API response times < 3s
- [ ] SSE streaming works
- [ ] Fallback works
- [ ] Documentation complete

---

## 🚀 Execution Summary

| Phase | Duration | Parallelization | Blocker |
|-------|----------|-----------------|---------|
| **1. Setup** | 2h | ❌ Some sequential | YES - Next phase blocker |
| **2. Migrations** | 4h | ✅ 5 migrations in parallel | Partially - A3 not blocked |
| **3. Features** | 16h | ✅ 3 features in parallel | YES - Needed for testing |
| **4. Testing** | 8h | ✅ 3 features in parallel | Can overlap with Phase 5 |
| **5. Frontend** | 12h | ✅ Web + Mobile in parallel | No - Independent |
| **6. Polish** | 4h | ❌ Sequential validation | Final blocker |
| **TOTAL** | **46h** | Parallelized to ~**20h wall time** | ⚡ 57% efficiency |

---

## 📊 Critical Path (Longest Sequence)

```
A1 (0.5h) → [A2+A7+A6 parallel: 1h]
         ↓
       A3 (1h) + [A4+A8+B1+C1+D1 parallel: 2h]
         ↓
     [B2+C2+D2 parallel: 1h]
         ↓
     [B3+C3+D3 parallel: 1h]
         ↓
     [B4+C4+D4 parallel: 6h]
         ↓
     [B5+B6+B7+B9 + C5+C6 + D5+D6 parallel: 5h]
         ↓
     [B8+C7+D7 parallel: 2h] + [E1+F1 + E2+E3+E4+E5 + F2+F3+F4 parallel: 12h]
         ↓
     [Final validation: 4h]

CRITICAL PATH: A1 → A3 → B2 → B3 → B4 → B5+B6+B7+B9 → Tests → Frontend → Validation
TOTAL: ~20 hours (assuming fast execution & no blockers)
```

---

## 🎯 How to Execute

### Start of Day (or session):
1. Check which phase you're in
2. Read the parallel tracks for that phase
3. **Start ALL tasks in the same wave simultaneously**
4. Don't move to next phase until all tasks in current phase complete

### Example Workflow:

```bash
# Day 1 - Phase 1 Setup
Task: Do A1
After A1 → Start [A2, A7, A6] all at once
Wait for all 3 to complete

# Day 2 - Phase 2 Migrations
Task: Start [A3, A4, A8, B1, C1, D1] all at once
After [A3, A4, A8] complete → Start [A5, A9]
Don't wait for B1, C1, D1 to start Phase 3

# Day 2-3 - Phase 3 Features
Task: Start [B2, C2, D2] all at once
After [B2, C2, D2] complete → Start [B3, C3, D3] all at once
After [B3, C3, D3] complete → Start [B4, C4, D4] all at once
After [B4, C4, D4] complete → Start [B5+B6+B7+B9, C5+C6, D5+D6] all at once

# Day 4 - Phase 4 & 5 (Overlap)
Task: Run tests [B8, C7, D7] in parallel
Task: Build frontend [E1, E2, E3, E4, E5, F1, F2, F3, F4] in parallel
Both can run simultaneously

# Day 5 - Phase 6 Polish
Task: Run full integration tests, fix issues, finalize
```

---

## 🔑 Key Rules for Parallel Execution

✅ **DO**:
- Start all tasks at same wave simultaneously
- Use separate branches/features per feature (B, C, D)
- Run tests in parallel (different test suites)
- Assign independent work to team members
- Monitor for blockers early

❌ **DON'T**:
- Wait for one feature to finish before starting another
- Run all tests sequentially
- Change shared code without coordination
- Skip migration testing before next phase
- Ignore dependency arrows

---

## 📈 Timeline Comparison

### Sequential Execution (Current Plan)
```
20 working days × 6-8 hours = 120-160 hours
```

### Parallel Execution (This Roadmap)
```
~20 wall-hours with 5+ concurrent streams
= 4-5 days with full parallelization
= 8-10 days with proper team coordination
```

**Time Saved: 50-60% reduction** ⚡

---

## 🤝 Team Assignment (If Available)

```
Developer 1: Phase 1 Setup (A1, A2, A7, A6)
Developer 2: Phase 2 Migrations (A4, A8, B1, C1, D1)
Developer 3: Phase 2 Core (A3, A5, A9)

Then rotate:
Developer 1 & 2 & 3: Phase 3 Features (assign per feature)
Developer 4 & 5: Phase 5 Frontend (Web + Mobile)

All: Phase 4 Testing + Phase 6 Polish
```

---

**Document Status**: ✅ Ready for execution  
**Last Updated**: December 12, 2025
