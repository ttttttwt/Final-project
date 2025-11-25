# Sprint 4 Plan Updates Summary

**Date**: November 24, 2025  
**Sprint**: 4 / 8  
**Status**: ✅ Ready to Start

---

## 📋 Changes Made

### 1. ✅ Epic B: Authentication - Clarified & Reorganized

**Problem**: Tasks B4 (Token Management) and B6 (Token Refresh) had overlap and unclear separation of concerns.

**Solution**: Clarified focus areas for each task:

#### **B4: Token Storage & Auto-login** (1.5 pts)

**Focus**: AsyncStorage operations, app launch behavior

**Key Changes**:

- ✅ Added specific AsyncStorage keys: `@lexia/access_token`, `@lexia/refresh_token`, `@lexia/token_type`
- ✅ Helper functions defined: `saveTokens()`, `getAccessToken()`, `getRefreshToken()`, `clearTokens()`
- ✅ Auto-login logic detailed: Check token on launch → restore auth state
- ✅ App backgrounding handling: AppState listener → refresh if expired
- ✅ Splash screen during token validation (prevents UI flash)
- ✅ Logout clears all tokens with `AsyncStorage.multiRemove()`

#### **B6: Axios Interceptor with Token Refresh** (1.5 pts)

**Focus**: 401 detection, refresh flow, request retry

**Key Changes**:

- ✅ Request interceptor: Attach `Authorization: Bearer ${token}` from AsyncStorage
- ✅ Response interceptor: Detect 401 status code
- ✅ Refresh flow: Call `POST /auth/refresh` with refresh token
- ✅ Promise lock: Prevent concurrent refresh calls (`isRefreshing` flag + `refreshPromise`)
- ✅ Queue failed requests: Store during refresh, retry all after success
- ✅ Token update: Save new tokens to AsyncStorage
- ✅ Retry original request: Re-execute failed request with new token
- ✅ Logout on refresh failure: 401/403 → clear AsyncStorage → navigate to Login
- ✅ **Copy reference**: Use logic from `lexia-web/lib/api.ts` (lines 90-180)

**Impact**: No overlap, clear separation of concerns, easier to implement and test.

---

### 2. ✅ Epic D4: Lesson Viewer - Detailed Quiz Implementation

**Problem**: Task D4 mentioned "Interactive quiz UI" but lacked details about:

- Quiz data structure (backend format)
- Answer submission (API endpoint)
- Scoring logic (client vs server)

**Solution**: Added comprehensive quiz implementation details:

#### **Quiz Data Structure** (from `DATABASE-SCHEMA.md`)

```json
{
  "title": "Present Perfect Tense Quiz",
  "instructions": "Choose the correct form of the verb.",
  "timeLimit": 600,
  "passingScore": 80,
  "questions": [
    {
      "question": "I ___ to Paris three times.",
      "type": "multiple_choice",
      "options": ["have been", "was", "have gone", "went"],
      "correctAnswer": 0,
      "points": 2,
      "explanation": "Use 'have been' for completed actions.",
      "hint": "Think about present perfect structure"
    }
  ]
}
```

#### **Quiz Features Added**:

- ✅ Parse quiz data from backend (see `DATABASE-SCHEMA.md` QUIZ lesson schema)
- ✅ Question types:
  - `multiple_choice`: Radio buttons
  - `true_false`: Toggle buttons
  - `fill_blank`: Text input
  - `matching`: Drag-drop (basic implementation)
- ✅ Timer display: If `timeLimit` exists, show countdown
- ✅ Answer submission: `POST /api/v1/progress/lessons/{lessonId}/submit` with:
  ```json
  {
    "answers": [{ "questionId": 1, "answer": 0 }],
    "score": 85
  }
  ```
- ✅ Results display: Score, correct/incorrect feedback, explanations
- ✅ Passing score indicator: Green if `score >= passingScore`, red otherwise
- ✅ Success feedback: Toast + confetti animation if quiz passed

**Impact**: Clear implementation roadmap, no ambiguity about quiz flow.

---

### 3. ✅ Task Breakdown Created

**File**: `docs/implement/sprint-4/task-breakdown.md`

**Contents**:

- 📅 Day-by-day breakdown (18 days)
- 🎯 Daily goals with specific point targets
- ✅ Detailed acceptance criteria for each day
- 📦 Dependencies list with installation commands
- 🔍 End-of-day checks for each day
- 📊 Progress tracking table
- 🚨 Known blockers documented

**Highlights**:

#### **Week 1: Setup & Authentication** (Day 1-6)

- Day 1: Project setup + dependencies (3.5 pts)
- Day 2: Testing + React Query setup (3 pts)
- Day 3: Auth store + token storage (2.5 pts)
- Day 4: Axios interceptor + token refresh (1.5 pts)
- Day 5: Login/Register screens (2 pts)
- Day 6: Auth errors + navigation (2.5 pts)

#### **Week 2: Core Features** (Day 7-12)

- Day 7: Home screen + navigation polish (2 pts)
- Day 8: Courses screen with search/filter (2 pts)
- Day 9: Course detail screen (1.5 pts)
- Day 10: Lesson viewer - Reading/Listening (1.5 pts)
- Day 11: Lesson viewer - Quiz/Speaking (1.5 pts)
- Day 12: Lesson completion + navigation (1.5 pts)

#### **Week 3: Progress, Offline, Testing** (Day 13-18)

- Day 13: Progress screen + profile (2.5 pts)
- Day 14: Offline support part 1 (2 pts)
- Day 15: Offline support part 2 (2 pts)
- Day 16: Testing part 1 (3 pts)
- Day 17: Testing part 2 (1.5 pts)
- Day 18: Performance profiling + polish (1.5 pts)

**Impact**: Clear roadmap, daily targets, realistic timeline.

---

### 4. ✅ Daily Log Created

**File**: `docs/implement/sprint-4/daily-log.md`

**Contents**:

- 📅 Daily entries for all 18 days
- 🎯 Daily goals, completed tasks, notes
- 🚧 Blockers tracking
- 📊 Sprint summary with epic status
- 🎓 Key learnings section

**Impact**: Progress tracking, decision documentation, blocker resolution history.

---

### 5. ✅ Current Sprint Status Updated

**File**: `docs/plan/current-sprint-status.md`

**Changes**:

- ✅ Sprint dates adjusted: Nov 24 – Dec 11 (was Nov 23 – Dec 10)
- ✅ Status: 🟢 In Progress (Day 1)
- ✅ Recent updates section added (Nov 24)
- ✅ Current tasks updated with Day 1 focus
- ✅ Timeline adjusted: Day 1-3 for setup (was Day 1-2)
- ✅ Resources added: Task breakdown, API reference files
- ✅ Key decisions documented
- ✅ Coverage target clarified: 50% global (Sprint 4) → 60% (Sprint 5)
- ⚠️ Blocker noted: Push notifications backend API not ready

**Impact**: Single source of truth for sprint status.

---

### 6. ✅ Offline Strategy Verified

**File**: `docs/implement/sprint-4/offline-strategy.md` (already existed)

**Status**: Already comprehensive, no changes needed

**Contents**:

- ✅ Decision: React Query (TanStack Query v5) + AsyncStorage persister
- ✅ Decision matrix comparing 4 options
- ✅ Implementation plan (4 phases)
- ✅ Query keys strategy
- ✅ Cache configuration
- ✅ Performance benchmarks
- ✅ Testing strategy
- ✅ Conflict resolution: Last-Write-Wins (documented)

**Impact**: Clear offline strategy, conflict resolution documented.

---

## 📊 Summary of Improvements

| Area                | Before                      | After                                              | Impact                    |
| ------------------- | --------------------------- | -------------------------------------------------- | ------------------------- |
| **Epic B**          | Overlapping tasks (B4 + B6) | Clear separation: B4 (storage) vs B6 (interceptor) | ✅ Easier to implement    |
| **Epic D4**         | Vague "interactive quiz UI" | Detailed quiz structure, API, scoring              | ✅ Clear implementation   |
| **Task Breakdown**  | None                        | Day-by-day plan (18 days)                          | ✅ Clear roadmap          |
| **Daily Log**       | None                        | All 18 days templated                              | ✅ Progress tracking      |
| **Sprint Status**   | Day 0                       | Day 1, updated with decisions                      | ✅ Single source of truth |
| **Timeline**        | Day 1-2 setup               | Day 1-3 setup (realistic)                          | ✅ Realistic velocity     |
| **Coverage Target** | 60% global                  | 50% global (Sprint 4) → 60% (Sprint 5)             | ✅ Realistic goal         |

---

## 🚨 Known Issues & Blockers

### 1. **Push Notifications Backend API Not Ready**

- **Issue**: Task D6 requires `POST /api/v1/notifications/register` endpoint
- **Impact**: Cannot register device tokens with backend
- **Resolution**: Defer device token registration to Sprint 5
- **Workaround**: Implement permissions request only in Sprint 4

### 2. **Dependencies Not Installed**

- **Issue**: `lexia-mobile/package.json` missing many dependencies
- **Impact**: Cannot start development without packages
- **Resolution**: Task A6 (Install dependencies) must be Day 1 priority
- **Action**: Run dependency installation commands from task breakdown

### 3. **Quiz Submission Endpoint Verification**

- **Issue**: Need to verify `POST /api/v1/progress/lessons/{lessonId}/submit` exists
- **Impact**: Quiz results cannot be submitted if endpoint missing
- **Resolution**: Check backend API during Day 11 (Quiz implementation)
- **Fallback**: Store quiz results locally, submit when endpoint ready

---

## ✅ Ready to Start Checklist

- [x] Sprint 4 Plan reviewed and approved
- [x] Epic B clarified (B4 vs B6)
- [x] Epic D4 detailed (quiz implementation)
- [x] Task breakdown created (18 days, day-by-day)
- [x] Daily log templated
- [x] Current sprint status updated
- [x] Offline strategy verified
- [x] Known blockers documented
- [ ] Dependencies installed (Task A6 - Day 1 priority)
- [ ] Project initialized (Task A1 - Day 1)

---

## 🎯 Next Steps

### Immediate (Day 1 - Nov 24)

1. **Install dependencies** (Task A6) - HIGH PRIORITY

   ```bash
   cd lexia-mobile
   npm install @tanstack/react-query @tanstack/query-async-storage-persister
   npm install zod react-hook-form @react-native-community/netinfo
   npm install react-native-chart-kit react-native-svg
   npm install react-native-markdown-display react-native-fast-image
   npm install expo-av expo-local-authentication expo-notifications
   npm install --save-dev @testing-library/react-native @testing-library/jest-native
   ```

2. **Initialize Expo project** (Task A1)

   ```bash
   npx create-expo-app lexia-mobile --template expo-template-blank-typescript
   ```

3. **Configure ESLint/Prettier** (Task A2)
4. **Setup directory structure** (Task A3)
5. **Configure Axios client** (Task A4)
6. **Define base types** (Task A5)

### Week 1 (Day 1-6)

- Complete Epic A (Project Initialization) - 7 pts
- Complete Epic B (Authentication) - 8 pts

### Week 2 (Day 7-12)

- Complete Epic C (Navigation) - 4 pts
- Complete Epic D (Core Features) - 10 pts

### Week 3 (Day 13-18)

- Complete Epic E (Offline) - 8 pts
- Complete Epic F (Testing) - 6 pts

---

## 📚 Reference Files

**Main Documents**:

- `docs/plan/sprint-4-plan.md` - Sprint overview
- `docs/implement/sprint-4/task-breakdown.md` - Day-by-day tasks
- `docs/implement/sprint-4/daily-log.md` - Progress tracking
- `docs/implement/sprint-4/offline-strategy.md` - Offline decision
- `docs/plan/current-sprint-status.md` - Current status

**Reference for Implementation**:

- `lexia-web/lib/api.ts` - Token refresh logic (lines 90-180)
- `lexia-web/services/authService.ts` - Auth flow reference
- `backend/docs/context/DATABASE-SCHEMA.md` - Quiz data structure (lines 270-320)
- `backend/docs/context/API-SPECIFICATION.md` - API endpoints

---

**Created**: November 24, 2025  
**Last Updated**: November 24, 2025  
**Status**: ✅ Ready to Start Sprint 4
