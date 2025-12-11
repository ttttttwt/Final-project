# LEXIA - Current Sprint Status

## Sprint 5 — AI Integration (Gemini API)

**Sprint**: 5 / 8 | **Duration**: Dec 12 – Dec 31, 2025 (20 days)  
**Status**: 🟢 In Progress (Day 1) | **Progress**: 0.5/29 points (1.7%)  
**Last Updated**: December 11, 2025 (Epic A: 6.7% - Task A1 Complete ✅)

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

**Recent Updates** (Dec 11 - Sprint Start):

- ✅ **Sprint Planning Complete**: Comprehensive plan & specification documents created
- ✅ **Task A1 Complete**: Gemini SDK + Resilience4j dependencies added
- ✅ **Dependencies**: Official Google Gemini SDK v1.30.0 + Resilience4j 2.2.0 for Spring Boot 3
- ✅ **Configuration**: Gemini API properties & Resilience4j (retry, circuit breaker, rate limiter) configured
- ✅ **Documentation**: .env.example created, README.md updated with Sprint 5 setup
- ✅ **Build Validated**: Gradle build successful with new dependencies
- 🎯 **Next**: A2 (GeminiConfig), A7 (Input sanitization)

---

## 📋 Sprint 5 Story Breakdown

### Epic A: AI Infrastructure (7.5 pts)

**Status**: 🔄 In Progress (0.5/7.5 pts - 6.7%)  
**Timeline**: Day 1-4 (December 11-16)

- [x] **A1**: Add Gemini SDK + Resilience4j dependencies (0.5 pt) ✅ **COMPLETE** (Dec 11)
  - ✅ Google Gemini AI SDK v1.30.0 added to build.gradle
  - ✅ Resilience4j Spring Boot 3 module v2.2.0 (circuit breaker, retry, rate limiter, time limiter)
  - ✅ spring-boot-starter-aop for aspect support
  - ✅ Build validated: `./gradlew clean build -x test` successful
  - ✅ Dependencies resolved from Maven Central
  
- [ ] **A2**: Create GeminiConfig with environment configuration (0.5 pt) ⬜ **TODO**
  - Gemini client bean with API key injection
  - Model configuration (default vs premium)
  - Token limits and temperature settings
  
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
  
- [ ] **A7**: Implement input sanitization + prompt injection filter (1 pt) ⬜ **TODO**
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

- [x] **A1**: Initialize Expo project with TypeScript (0.5 pt) ✅ **COMPLETE**
  - ✅ Expo TypeScript template created
  - ✅ React Native 0.76.5 + Expo SDK 52
- [x] **A2**: Configure ESLint, Prettier, and absolute imports (0.5 pt) ✅ **COMPLETE** (Nov 25)

  - ✅ ESLint v8.57.0 + React Native plugin
  - ✅ Prettier v3.1.0 matching web config
  - ✅ Path aliases configured in tsconfig.json

- [x] **A3**: Setup directory structure (0.5 pt) ✅ **COMPLETE**

  - ✅ Folders created: `app/`, `components/`, `services/`, `store/`, `types/`
  - ✅ Mirror structure from web app

- [x] **A4**: Configure Axios client with interceptors (1 pt) ✅ **COMPLETE** (Nov 25 Late)

  - ✅ Complete `services/api.ts` (345 lines)
  - ✅ Promise lock pattern (prevent concurrent refresh)
  - ✅ Failed request queue management
  - ✅ Token refresh with AsyncStorage
  - ✅ Network connectivity check (NetInfo)
  - ✅ Retry logic with exponential backoff
  - ✅ Comprehensive error handling (401, network, timeout, 5xx)
  - ✅ Token storage helpers (save, get, clear)

- [x] **A5**: Define base types (0.5 pt) ✅ **COMPLETE** (Nov 25 Late)

  - ✅ Complete `types/index.ts` (200+ lines)
  - ✅ User, Auth types (LoginRequest, RegisterRequest, TokenResponse)
  - ✅ Course, Section, Enrollment types
  - ✅ Lesson types (READING, LISTENING, QUIZ, SPEAKING)
  - ✅ Lesson content types (ReadingContent, ListeningContent, etc.)
  - ✅ Progress and Learning Path types

- [x] **A6**: Install missing dependencies (0.5 pt) ✅ **COMPLETE** (Nov 25)
  - ✅ React Query v5.56.0 + AsyncStorage persister
  - ✅ Zod v3.22.4 + React Hook Form v7.49.0
  - ✅ NetInfo v11.3.0
  - ✅ Charts (Chart Kit + SVG)
  - ✅ Markdown Display v7.0.0
  - ✅ Fast Image v8.6.3
- [x] **A7**: Configure test environment (1 pt) ✅ **COMPLETE** (Nov 25)
  - ✅ Jest v29.7.0 configured
  - ✅ Testing Library v12.4.0
  - ✅ Coverage thresholds: 60% global, 80% services
  - ✅ Sample tests passing (4/4)
- [x] **A8**: Setup ESLint + Prettier (1 pt) ✅ **COMPLETE** (Nov 25)
  - ✅ ESLint v8.57.0 + React Native plugin
  - ✅ Prettier v3.1.0
  - ✅ 0 errors, 20 warnings (acceptable)
  - ✅ All files formatted

**🎊 Epic A: 7/7 points (100%) - COMPLETE ✅**

---

### Epic B: Authentication (8 pts)

**Status**: ✅ Mostly Complete (5/8 pts - 62.5%)  
**Timeline**: Day 3-6 (November 25-29)

- [x] **B1**: Auth Store + AuthProvider (1 pt) ✅ **COMPLETE** (Nov 25 Evening)
  - ✅ Complete auth store with login, register, logout, loadUser actions
  - ✅ Error handling and loading states
  - ✅ Integration with token storage helpers from api.ts
  - ✅ Auto-login on app launch via loadUser()
  - ✅ AppState listener for token validation on app resume
  - ✅ Splash screen during session initialization
  - ✅ AuthProvider component (85 lines)
  - ✅ Auth store (150+ lines)
- [x] **Auth Service**: Created authService.ts (45 lines)
  - ✅ login() - POST /auth/login
  - ✅ register() - POST /auth/register
  - ✅ logout() - POST /auth/logout
  - ✅ getProfile() - GET /users/profile
- [x] **B2**: Login Screen with form validation (1 pt) ✅ **COMPLETE** (Nov 25 Night)
  - ✅ React Hook Form + Zod validation schema
  - ✅ Email format validation + password min 8 chars
  - ✅ Real-time error feedback on blur
  - ✅ Loading states + disabled inputs during submission
  - ✅ HelperText error messages
  - ✅ ScrollView for keyboard handling
  - ✅ Success/Error alerts
- [x] **B3**: Register Screen with form validation (1 pt) ✅ **COMPLETE** (Nov 25 Late Night)
  - ✅ React Hook Form + comprehensive Zod schema
  - ✅ Full name validation (2-50 chars, letters only with regex)
  - ✅ Strong password requirements (uppercase, lowercase, number)
  - ✅ Password strength indicator with visual bar
  - ✅ Confirm password matching with Zod `.refine()`
  - ✅ Real-time validation on blur
  - ✅ HelperText error messages for all fields
  - ✅ Loading states + disabled inputs
  - ✅ ScrollView for keyboard handling
- [x] **B4**: Token Storage & Auto-login (1 pt) ✅ **VERIFIED** (Nov 25)
  - ✅ saveTokens() - AsyncStorage.multiSet (4 keys)
  - ✅ getAccessToken() - Retrieve access token
  - ✅ getRefreshToken() - Retrieve refresh token
  - ✅ getTokenType() - Returns "Bearer" by default
  - ✅ clearTokens() - AsyncStorage.multiRemove all tokens
  - ✅ All helpers working in api.ts (lines 57-103)
  - ✅ Auto-login on app launch (AuthProvider)
  - ✅ AppState listener for token validation on resume
- [x] **B5**: Auth Error Handling (1 pt) ✅ **COMPLETE** (Nov 25 Late Night Part 2)
  - ✅ NetworkStatusBanner component (auto-detects offline with NetInfo)
  - ✅ Slide animation (300ms) for smooth UX
  - ✅ User-friendly error message utility (maps all error types)
  - ✅ Error title utility for Alert dialogs
  - ✅ Retry logic already in api.ts (exponential backoff, max 3 attempts)
  - ✅ Updated auth store + LoginScreen + RegisterScreen
  - ✅ 24 unit tests passing
- [x] **B6**: Axios Interceptor + Token Refresh (1.5 pts) ✅ **COMPLETE** (Already done in A4)
  - ✅ Request interceptor attaches Authorization header
  - ✅ Response interceptor detects 401
  - ✅ Promise lock prevents concurrent refresh
  - ✅ Failed request queue management
  - ✅ Token refresh with /auth/refresh endpoint
  - ✅ Auto-logout on refresh failure
- [ ] **B7**: Biometric Auth (TouchID/FaceID) (1 pt) - **OPTIONAL** (Defer to Sprint 5)

**🎊 Epic B Achievements (Nov 25)**:

**Evening Session**:

- ✅ Complete auth store foundation (login, register, logout, loadUser)
- ✅ Token storage verified (5 helpers working)
- ✅ Session management (auto-login, AppState listener)
- ✅ Auth service layer created
- ✅ Screens integrated with store
- ✅ Path aliases configured (@/ imports)

**Night Session** (B2):

- ✅ Login Screen with React Hook Form + Zod validation
- ✅ Email format validation + password min 8 chars
- ✅ Real-time validation on blur (better UX)
- ✅ HelperText error messages
- ✅ Loading states + disabled inputs
- ✅ ScrollView for keyboard handling

**Quality Metrics**:

- ✅ TypeScript: 0 errors
- ✅ ESLint: 0 errors (19 warnings acceptable)
- ✅ Tests: 4/4 passing
- ✅ Package: @hookform/resolvers installed

**Files Modified (B2 - Nov 25 Night)**:

- Modified: `app/auth/LoginScreen.tsx` (170 lines - complete rewrite with React Hook Form)
- Installed: `@hookform/resolvers@3.9.1`
- **Total**: ~170 lines of production code

**Next Steps** (Day 11 - Nov 28):

- D3: Course Detail Screen with sections/lessons
- D4: Lesson Viewer (4 types: READING, LISTENING, QUIZ, SPEAKING)
- D5: Lesson Navigation (prev/next)
- D6: Complete Lesson API Integration

---

### Epic C: Navigation & Layout (4 pts)

**Status**: ✅ Complete (4/4 pts - 100%)  
**Timeline**: Day 7-9 (November 25-27)

- [x] **C1**: Setup Tab Navigation (Home, Courses, Progress, Profile) (1 pt) ✅ **COMPLETE** (Nov 25)
  - ✅ Bottom Tab Navigator configured in App.tsx
  - ✅ Material Community Icons for all 4 tabs:
    - Home: `home` icon
    - Courses: `book-open-page-variant` icon
    - Progress: `chart-line` icon
    - Profile: `account-circle` icon
  - ✅ Active state styling (purple #6200ee active, grey inactive)
  - ✅ Tab bar styling (white background, 60px height, elevation shadow)
  - ✅ Header styling (purple background, white text, bold title)
  - ✅ Installed @types/react-native-vector-icons for TypeScript
  - ✅ TypeScript: 0 errors, ESLint: 0 errors
- [x] **C2**: Stack Navigation + Auth Gate (1 pt) ✅ **COMPLETE** (Nov 25)
  - ✅ Created navigation types (`types/navigation.ts`)
  - ✅ Typed RootStackParamList with all screens (Auth + Main + nested)
  - ✅ Global type augmentation for autocomplete
  - ✅ Created branded SplashScreen component (LEXIA branding)
  - ✅ Auth gate switches between Auth/Main stacks
  - ✅ Fade animation between stacks
  - ✅ CourseDetail + LessonViewer with purple headers
  - ✅ Screen titles configured ("Welcome Back", "Create Account", etc.)
  - ✅ TypeScript: 0 errors, ESLint: 0 errors
- [x] **C3**: Custom Tab Bar (1 pt) ✅ **COMPLETE** (Nov 27)
  - ✅ Created `CustomTabBar` component (230+ lines)
  - ✅ Animated scale + opacity transitions on tab press
  - ✅ Badge support for notifications (with 99+ overflow)
  - ✅ Haptic feedback on Android (Vibration API)
  - ✅ Active indicator line under selected tab
  - ✅ Outline/filled icon variants for active/inactive states
  - ✅ Safe area insets handling for notches
  - ✅ Accessibility labels with badge counts
- [x] **C4**: Header Components (1 pt) ✅ **COMPLETE** (Nov 27)
  - ✅ Created `CustomHeader` component (280+ lines)
  - ✅ User greeting with time-based messages
  - ✅ Avatar display with initials fallback
  - ✅ Search bar for Courses screen
  - ✅ Notification bell with badge count
  - ✅ Back button support for nested screens
  - ✅ Right action button support (settings icon)
  - ✅ Safe area insets for status bar

**🎊 Epic C Achievements (Nov 25-27)**:

**C1 - Tab Navigation**:

- ✅ Professional tab navigation with Material Design principles
- ✅ 4 tabs functional with intuitive icons
- ✅ Active state clearly indicates current tab (purple color)
- ✅ Proper spacing and touch targets (60px height > 44px minimum)
- ✅ Consistent styling across all tabs (header + tab bar)
- ✅ TypeScript support for vector icons

**C2 - Stack Navigation + Auth Gate**:

- ✅ Type-safe navigation with full TypeScript support
- ✅ Auth gate seamlessly switches between Auth/Main stacks
- ✅ Branded SplashScreen prevents UI flash during initialization
- ✅ Fade animation for smooth transitions
- ✅ Purple headers consistent across nested screens
- ✅ Navigation params typed for CourseDetail + LessonViewer

**C3 - Custom Tab Bar**:

- ✅ Spring animations for press feedback
- ✅ Badge support with overflow (99+)
- ✅ Haptic feedback for Android
- ✅ Active indicator line
- ✅ Icon state changes (outline/filled)

**C4 - Custom Header**:

- ✅ 4 header variants in one flexible component
- ✅ Time-based personalized greetings
- ✅ Integrated search with clear button
- ✅ Notification badge support
- ✅ All tab screens updated with new headers

**Quality Metrics**:

- ✅ TypeScript: 0 errors
- ✅ ESLint: 0 errors (16 warnings acceptable)
- ✅ Navigation: All flows work correctly
- ✅ Tests: 28/28 passing
- ✅ Accessibility: Labels on all interactive elements

**Files Created/Modified (C3-C4 - Nov 27)**:

- Created: `components/CustomTabBar.tsx` (230 lines)
- Created: `components/CustomHeader.tsx` (280 lines)
- Created: `hooks/useDebounce.ts` (60 lines)
- Modified: `App.tsx` (use CustomTabBar)
- Modified: `app/tabs/HomeScreen.tsx` (dashboard UI)
- Modified: `app/tabs/CoursesScreen.tsx` (search + cards)
- Modified: `app/tabs/ProgressScreen.tsx` (stats + progress)
- Modified: `app/tabs/ProfileScreen.tsx` (profile card + menu)

**Total**: 3 files created, 5 files modified, ~1,000 lines of production code

---

### Epic D: Core Features (10 pts)

**Status**: ✅ Complete (10/10 pts - 100%) 🎊  
**Timeline**: Day 9-13 (November 27 - December 2)

- [x] **D1**: Home Screen Dashboard with API (1.5 pt) ✅ **COMPLETE** (Nov 27)
  - ✅ Created `services/progressService.ts` (65 lines)
    - getStreak(), completeLesson(), getCourseProgress()
    - getDashboardOverview(), getProgressSummary()
  - ✅ Created `services/enrollmentService.ts` (55 lines)
    - enroll(), getMyEnrollments(), isEnrolled(), getEnrollmentByCourse()
  - ✅ Rewrote `HomeScreen.tsx` (370 lines) with API integration
    - StatsCard component with loading states
    - ContinueLearningCard with progress bars
    - Pull-to-refresh, error handling
    - Recommendations section
  - ✅ Added dashboard types to `types/index.ts`
    - StreakData, DashboardStats, DashboardOverview
    - Goal, Activity, Recommendation
- [x] **D2**: Courses Screen with Search/Filter (2 pt) ✅ **COMPLETE** (Nov 27)
  - ✅ Created `services/courseService.ts` (100 lines)
    - getCourses(), getCourseById(), getCourseWithSections()
    - searchCourses() with title, level filters
  - ✅ Rewrote `CoursesScreen.tsx` (350 lines) with API integration
    - CEFR level filter chips (All, A1-C2)
    - Debounced search (300ms) using useDebounce hook
    - Infinite scroll pagination
    - Enrollment status badges on course cards
  - ✅ Added progress types to `types/index.ts`
    - DailyActivity, ProgressSummary, LessonProgressDTO
    - CourseProgressDTO, LessonProgressSummary
  - ✅ Created unit tests (24 tests for 3 services)
    - `__tests__/services/courseService.test.ts` (7 tests)
    - `__tests__/services/progressService.test.ts` (8 tests)
    - `__tests__/services/enrollmentService.test.ts` (9 tests)
- [x] **D3**: Course Detail Screen (1.5 pt) ✅ **COMPLETE** (Nov 27)
  - ✅ Created `components/courses/SectionCard.tsx` (170 lines)
    - Animated expand/collapse with LayoutAnimation
    - Chevron rotation animation
    - Progress bar for enrolled users
    - Completed lessons count display
  - ✅ Created `components/courses/LessonListItem.tsx` (200 lines)
    - Type-specific icons (book, headphones, help-circle, microphone)
    - Type-specific colors (blue, purple, orange, green)
    - Completion checkmark indicator
    - Lock icon for non-enrolled users
    - Duration display with clock icon
  - ✅ Rewrote `CourseDetailScreen.tsx` (380 lines) with full API integration
    - Course thumbnail/banner image
    - CEFR level badge
    - Course stats (lessons, hours, sections)
    - Description section
    - Enroll button with loading state
    - Curriculum section with collapsible sections
    - Pull-to-refresh
    - Error handling with retry
    - Snackbar notifications
    - Navigation to LessonViewer
  - ✅ Unit tests for components (28 tests)
    - `__tests__/components/LessonListItem.test.tsx` (14 tests)
    - `__tests__/components/SectionCard.test.tsx` (14 tests)
- [x] **D4**: Lesson Viewer (3 pts) ✅ **COMPLETE** (Dec 1)
  - ✅ D4.1: Reading lesson type (Markdown rendering)
    - Created `ReadingLesson` component (350 lines)
    - Markdown rendering with react-native-markdown-display
    - Collapsible vocabulary section
    - Comprehension questions with validation
  - ✅ D4.2: Listening lesson type (Audio player)
    - Created `ListeningLesson` component (450 lines)
    - Audio player with expo-av
    - Playback speed control (0.5x-2x)
    - Transcript with timestamps
  - ✅ D4.3: Quiz lesson type (Interactive quiz)
    - Created `QuizLesson` component (550 lines)
    - Timer countdown with visual indicator
    - Question navigation dots
    - Hints, review mode, retry functionality
  - ✅ D4.4: Speaking lesson type (Voice recording)
    - Created `SpeakingLesson` component (500 lines)
    - Voice recording with expo-av
    - Playback and re-record options
    - Sample answers and prompts
  - ✅ Created `services/lessonService.ts` (180 lines)
  - ✅ Rewrote `LessonViewerScreen.tsx` (390 lines)
  - ✅ Installed `expo-av` for audio functionality
- [x] **D5**: Lesson Navigation (0.5 pt) ✅ **INTEGRATED IN D4**
  - ✅ Previous/Next lesson buttons in LessonViewerScreen
  - ✅ Progress indicator (Lesson X of Y) in header
  - ✅ Navigation footer appears after completion
- [x] **D6**: Push Notifications Setup (1.5 pt) ✅ **COMPLETE** (Dec 2)
  - ✅ Created `services/pushNotificationService.ts` (420+ lines)
    - Request notification permissions with user-friendly flow
    - Get Expo Push Token for remote notifications
    - Create Android notification channels (4 channels)
    - Register/unregister device token (backend API placeholder)
    - Schedule local notifications for study reminders
    - Cancel notifications (single/all)
    - Badge management (iOS)
  - ✅ Created `hooks/usePushNotifications.ts` (210+ lines)
    - State management for permission, token, loading
    - Notification listeners (foreground received, tap response)
    - Deep linking on notification tap
  - ✅ Created `components/PushNotificationProvider.tsx` (230+ lines)
    - Initialize push notifications when user logs in
    - Handle cold start from notification tap
    - Deep link navigation to relevant screens
    - Cleanup listeners on logout
  - ✅ Updated `App.tsx` with navigationRef for deep linking
  - ✅ Updated `app.json` with notification plugins and permissions
  - ✅ Added push notification types to `types/index.ts`
  - ✅ Created 22 unit tests for pushNotificationService
  - ⚠️ Note: Backend device token API not ready - tokens stored locally

**🎊 Epic D Achievements (Nov 27 - Dec 2)**:

**D1 - Home Dashboard**:

- ✅ 2 new services created (progressService, enrollmentService)
- ✅ Dashboard fetches real data from backend
- ✅ Stats cards show enrolled courses, completed lessons, streak
- ✅ Continue learning section with progress bars
- ✅ Pull-to-refresh for manual data refresh

**D2 - Courses Screen**:

- ✅ Full search functionality with debounce
- ✅ CEFR level filtering (7 levels: All + A1-C2)
- ✅ Infinite scroll pagination
- ✅ Enrollment status badges
- ✅ Loading states and error handling

**Quality Metrics (Day 10)**:

- ✅ TypeScript: 0 errors
- ✅ ESLint: 0 errors, 2 warnings
- ✅ Tests: 52/52 passing (28 existing + 24 new)
- ✅ Services: 3 new files, ~220 lines
- ✅ Screens: 2 rewrites, ~720 lines

**Files Created/Modified (D1-D2 - Nov 27)**:

- Created: `services/courseService.ts` (100 lines)
- Created: `services/progressService.ts` (65 lines)
- Created: `services/enrollmentService.ts` (55 lines)
- Created: `__tests__/services/courseService.test.ts` (7 tests)
- Created: `__tests__/services/progressService.test.ts` (8 tests)
- Created: `__tests__/services/enrollmentService.test.ts` (9 tests)
- Modified: `types/index.ts` (+90 lines - progress/dashboard types)
- Modified: `app/tabs/HomeScreen.tsx` (370 lines - complete rewrite)
- Modified: `app/tabs/CoursesScreen.tsx` (350 lines - complete rewrite)

**Total**: 6 files created, 3 files modified, ~1,200 lines of production code + tests

---

### Epic E: Progress & Offline (8 pts)

**Status**: ✅ Complete (8/8 pts - 100%) 🎊  
**Timeline**: Day 14-15 (December 2-8)

- [x] **E1**: Progress Screen with Charts (1.5 pt) ✅ **COMPLETE** (Dec 2)

  - ✅ Created `ProgressChart` component (~180 lines)
    - LineChart with react-native-chart-kit (7-day activity)
    - Bezier curves, purple theme (#6200ee)
    - Stats row (total, average, max values)
    - Empty state handling
    - showTimeSpent toggle prop
  - ✅ Created `StreakCalendar` component (~340 lines)
    - GitHub-style heatmap (90 days default)
    - 5 intensity levels (grey→green gradient)
    - Interactive cells with tooltip on press
    - Streak stats section (current, best, total active days)
    - "Active Today" badge
    - Legend (Less→More)
  - ✅ Rewrote `ProgressScreen.tsx` (~600 lines)
    - API integration (getProgressSummary, getStreak, getMyEnrollments)
    - 4 Stats Cards (Lessons, Hours, Current Streak, Best Streak)
    - Weekly Activity Chart with toggle (lessons/time)
    - 90-day Streak Calendar
    - Course Progress list with CEFR badges
    - Pull-to-refresh with RefreshControl
    - Skeleton loading states
    - Error handling with Snackbar
  - ✅ Unit Tests: 29 tests (13 ProgressChart + 16 StreakCalendar)
  - ✅ TypeScript: 0 errors
  - ✅ All 161 tests passing

- [x] **E2**: Profile Screen Completion (0.5 pt) ✅ **COMPLETE** (Dec 2)

  - ✅ Display: Avatar, Name, Email, CEFR Level, Streak
  - ✅ Edit Profile navigation
  - ✅ Logout button
  - ✅ Loading state, React Query
  - ✅ Unit tests passing

- [x] **E3**: Network Detection Component (0.5 pt) ✅ **COMPLETE** (Dec 2)

  - ✅ NetworkProvider component with NetInfo listener
  - ✅ "You're offline" banner with NetworkStatusBanner
  - ✅ useNetwork, useIsOffline, useConnectionType hooks
  - ✅ 13 unit tests passing

- [x] **E4**: Offline Queue Implementation (1.5 pts) ✅ **COMPLETE** (Dec 2)

  - ✅ Created `offlineQueueService.ts` (~370 lines)
    - Queue mutations in AsyncStorage (`@lexia/offline_queue`)
    - Exponential backoff retry (max 3 attempts)
    - Last-write-wins conflict resolution
    - Queue size limit (100 items)
    - Progress callback for sync operations
  - ✅ Created `useOfflineSync` hook (~200 lines)
    - Auto-sync when connection restored
    - Manual sync with `syncNow()`
    - Queue stats tracking
    - Callbacks: onSyncStart, onSyncComplete, onSyncError
  - ✅ Created `SyncIndicator` component (~170 lines)
    - Compact mode (icon button with badge)
    - Expanded mode (full status display)
    - Syncing animation, success/error states
  - ✅ Integrated with progressService.ts
    - `completeLesson()` queues offline when disconnected
    - Returns optimistic response with `_offline: true` flag
  - ✅ UI Integration
    - LessonViewerScreen shows offline completion message
    - ProfileScreen displays SyncIndicator
  - ✅ Unit Tests: 27 tests (14 offlineQueueService + 13 useOfflineSync)
  - ✅ **219 Total Tests Passing** 🎊

- [x] **E5**: Offline-First Features (2 pts) ✅ **COMPLETE** (Dec 2)

  - ✅ Created `offlineDownloadService.ts` (~670 lines)
    - Download lessons with audio files to FileSystem
    - Download entire courses (all sections and lessons)
    - Track downloaded lessons/courses in AsyncStorage
    - Storage management (get storage info, clear downloads)
    - Metadata tracking (downloadedAt, fileSize, duration)
    - Progress callback for download operations
  - ✅ Created `useDownloads` hook (~250 lines)
    - Download state management (downloadedLessonIds, downloadedCourseIds)
    - Download progress tracking (downloadingLessons Map)
    - Storage info (total, used, available, itemCount)
    - Functions: downloadLesson, downloadCourse, deleteLesson, deleteCourse
  - ✅ Created `DownloadButton` component (~280 lines)
    - Download/delete toggle with progress indicator
    - ActivityIndicator during download
    - Confirmation dialog for delete
    - Multiple size variants (small, medium, large)
  - ✅ Created `DownloadedBadge` component (~120 lines)
    - Visual indicator for downloaded lessons
    - Positionable as overlay (top-left, top-right, etc.)
    - Multiple sizes (small, medium, large)
  - ✅ Created `StorageUsage` component (~290 lines)
    - Storage progress bar (used/total)
    - Downloaded lesson/course counts
    - Clear all downloads option with confirmation
    - Loading and error states
  - ✅ Created `utils/formatters.ts` (~80 lines)
    - formatBytes() - human-readable file sizes
    - formatDuration() - HH:MM:SS format
    - formatRelativeTime() - "2 hours ago" format
    - formatPercentage() - percentage formatting
  - ✅ Unit Tests: 22 tests for offlineDownloadService
  - ✅ **241 Total Tests Passing** 🎊

- [x] **E6**: React Query Offline Persister (1.5 pts) ✅ **COMPLETE** (Dec 2)
  - ✅ Created `lib/queryClient.ts` (~150 lines)
    - QueryClient configuration with stale times
    - AsyncStorage persister with createAsyncStoragePersister
    - QUERY_KEYS constants for all queries
    - CACHE_TIME constants (SHORT, DEFAULT, LONG stale times)
    - persistOptions for PersistQueryClientProvider
    - clearQueryCache() helper function
  - ✅ Created `components/QueryProvider.tsx` (~110 lines)
    - PersistQueryClientProvider wrapper
    - Network state listener (pause/resume queries)
    - AppState listener (refetch on app resume)
    - Cache cleanup on logout
  - ✅ Created `hooks/useCourses.ts` (~150 lines)
    - useCourses() - paginated course list
    - useSearchCourses() - search with filters
    - useCourse() - single course by ID
    - useCourseWithSections() - course with lessons
    - useMyEnrollments() - user's enrollments
    - useEnrollmentStatus() - check if enrolled
    - useEnrollCourse() - enroll mutation
    - usePrefetchCourse() - prefetch helpers
  - ✅ Created `hooks/useProgress.ts` (~140 lines)
    - useDashboard() - dashboard overview
    - useStreak() - streak data
    - useProgressSummary() - progress summary
    - useCourseProgress() - course progress
    - useCompleteLesson() - complete lesson mutation
    - useRefreshProgress() - refresh all progress data
  - ✅ Updated `App.tsx` with QueryProvider
  - ✅ Unit Tests: 22 tests (11 useCourses + 11 useProgress)
  - ✅ **263 Total Tests Passing** 🎊

**🎊 Epic E Achievements (Dec 2)**:

**E1 - Progress Screen**:

- ✅ Professional progress visualization with LineChart
- ✅ GitHub-style streak calendar (90 days)
- ✅ 4 stat cards with icons and subtext
- ✅ Course progress list with CEFR badges
- ✅ Pull-to-refresh for manual data refresh
- ✅ Skeleton loading prevents layout shift
- ✅ Error handling with retry capability

**E4 - Offline Queue**:

- ✅ Complete offline mutation queueing system
- ✅ Auto-sync when reconnected to network
- ✅ Visual sync indicator with progress
- ✅ Integrated with lesson completion flow
- ✅ Last-write-wins conflict resolution
- ✅ Exponential backoff for retries

**E5 - Offline-First Features**:

- ✅ Complete lesson download service with FileSystem storage
- ✅ Course download support (all sections/lessons)
- ✅ Download progress tracking with callbacks
- ✅ Storage usage display component
- ✅ Download/delete UI components
- ✅ Formatter utilities for sizes and durations

**Quality Metrics (E1-E5)**:

- ✅ TypeScript: 0 errors
- ✅ ESLint: 0 errors (warnings only)
- ✅ Tests: 241/241 passing (80 new tests for E1-E5)
- ✅ Coverage: Services well tested

**Files Created/Modified (E5 - Dec 2)**:

- Created: `services/offlineDownloadService.ts` (~670 lines)
- Created: `hooks/useDownloads.ts` (~250 lines)
- Created: `components/lessons/DownloadButton.tsx` (~280 lines)
- Created: `components/lessons/DownloadedBadge.tsx` (~120 lines)
- Created: `components/lessons/StorageUsage.tsx` (~290 lines)
- Created: `components/lessons/index.ts` (export file)
- Created: `utils/formatters.ts` (~80 lines)
- Created: `hooks/index.ts` (export file)
- Created: `__tests__/services/offlineDownloadService.test.ts` (~500 lines, 22 tests)

**Total (E5)**: 9 files created, ~2,200 lines of production code + tests

---

### Admin Site Tasks (Parallel Track)

- [x] **Admin-1**: Project Initialization (2 pts) ✅ **COMPLETE** (Nov 24)
  - ✅ Initialize Vite + React + TypeScript
  - ✅ Configure Tailwind CSS v3 & shadcn/ui
  - ✅ Setup directory structure & routing
  - ✅ Verify build

**End of Day Check (Day 3)**: ✅ B1+B4 complete, TypeScript ✅, ESLint ✅, Tests ✅

**Timeline**: Day 1-3 (November 24-25) - ✅ **AHEAD OF SCHEDULE** (B1+B4 done early!)

**Resources**:

- 📄 Sprint Plan: `docs/plan/sprint-4-plan.md`
- 📄 Task Breakdown: `docs/implement/sprint-4/task-breakdown.md` ✨ **NEW**
- 📄 Web API Client: `lexia-web/lib/api.ts` (reference for token refresh logic)
- 📄 Web Auth Service: `lexia-web/services/authService.ts` (reference for auth flow)

**Key Decisions Made** (Nov 24):

- ✅ React Query (TanStack Query v5) confirmed for offline support
- ✅ Timeline adjusted: Day 1-3 for setup (realistic)
- ✅ Epic B clarified: B4 focuses on AsyncStorage, B6 focuses on Axios interceptor
- ✅ Epic D4 detailed: Quiz data structure from `DATABASE-SCHEMA.md`
- ✅ Coverage target: 50% global (Sprint 4) → 60% (Sprint 5)
- ✅ Performance targets: launch <3s, 60fps, <200MB memory, bundle <50MB
- ⚠️ Push notifications backend API not ready - defer D6 device token registration to Sprint 5

---

## Sprint 3 — Frontend Development (Web) ✅ COMPLETE

**Sprint**: 3 / 8 | **Duration**: Nov 8 – Nov 21, 2025 (14 days)  
**Status**: ✅ Complete (100%) | **Progress**: 29.5/29 points (101.7%)  
**Completed**: November 20, 2025

### Achievements

### Story Breakdown

| Epic                      | Status      | Progress  | Notes                        |
| ------------------------- | ----------- | --------- | ---------------------------- |
| D: Course & Learning Path | ✅ Complete | 7/7 pts   | All D1-D5 tasks complete     |
| E: Progress & Profile     | ✅ Complete | 5/5 pts   | E1-E5 complete               |
| F: Testing & Polish       | ✅ Complete | 4.5/4 pts | All F1-F6 complete + WCAG AA |

**Total**: 29.5 points (Updated from 29 for accessibility audit)

### Current Tasks

**Epic A: Project Setup & Configuration** ✅ **COMPLETE** (4 pts)

**📊 Velocity Alert**: Currently at 1.8 pts/day (target: 2.1 pts/day). Epic B complete! On track for C-D.

- ✅ B3.3: Auto-Logout - AuthProvider, ProtectedRoute, session initialization

> **🔐 Security Update (Nov 18)**: Frontend token handling temporarily reverted to localStorage + Authorization header to reduce implementation friction for Sprint 3. Middleware now fails open because tokens are not accessible on the edge. Backend APIs + ProtectedRoute enforce authentication until Sprint 6 reintroduces httpOnly cookies.

- ✅ Session persists across page reloads
- [x] B4: Protected routes middleware (0.5 pt) ✅ **COMPLETE** (Nov 12)
  - ✅ Next.js middleware calling backend /users/profile for validation
  - ✅ Redirects with returnUrl parameter
  - ✅ Public routes configuration
  - ✅ Asset exclusion for performance
- [x] B5: Auth store refinement (0.5 pt) ✅ **COMPLETE** (Nov 12)
  - ✅ Fixed initial loading state (false → true)
  - ✅ Created LoadingScreen component
  - ✅ Simplified AuthProvider logic

**🎊 Epic B Achievements**:

- ✅ Complete authentication flow (login → register → logout)
- ✅ Configurable token strategy (currently localStorage + Authorization header; httpOnly cookies scheduled for Sprint 6)
- ✅ Server-side middleware implementation (temporarily fail-open while tokens live in localStorage)
- ✅ Client-side ProtectedRoute component
- ✅ Promise lock prevents concurrent token refresh
- ✅ Comprehensive error handling
- ✅ Session persistence across page reloads
- ✅ Loading states prevent UI flashing
- ✅ Responsive design (320px - 1920px)
- ✅ Accessibility (ARIA labels, keyboard nav)

> ⚠️ Until Sprint 6, rely on backend APIs + ProtectedRoute for enforcement while middleware remains fail-open.

**Epic C: Dashboard & Layout** ✅ **COMPLETE** (4 pts - 100%)

- [x] C1: Main layout with sidebar navigation (1.5 pts) ✅ **COMPLETE** (Nov 13)
  - ✅ Sidebar component with collapse/expand (desktop: 256px → 80px)
  - ✅ Mobile slide-in menu with backdrop overlay
  - ✅ Active route highlighting (blue accent)
  - ✅ Real auth integration (user name, logout)
  - ✅ 4 protected pages (dashboard, courses, progress, profile)
  - ✅ Dark mode support, accessibility (ARIA, keyboard nav)
  - ✅ Quality: 9.5/10 ⭐⭐⭐⭐⭐
- [x] C2: Header with user profile dropdown (0.5 pt) ✅ **COMPLETE** (Nov 13)
  - ✅ Enhanced Header with dynamic page title (desktop)
  - ✅ Search bar placeholder (center, desktop only)
  - ✅ Notifications icon with badge count
  - ✅ User dropdown menu (Profile, Settings, Logout)
  - ✅ Avatar with initials fallback
  - ✅ Responsive design (search hidden on mobile)
  - ✅ Quality: 9/10 ⭐⭐⭐⭐⭐
- [x] C3: Responsive navigation (mobile menu) (1 pt) ✅ **COMPLETE** (Nov 13)
  - ✅ Smooth slide-in/out animations (300ms transitions)
  - ✅ Touch gesture support (swipe-right to open, swipe-left to close)
  - ✅ Edge swipe detection (50px from left edge)
  - ✅ Tap outside to close (backdrop overlay)
  - ✅ Responsive across all breakpoints (320px - 1920px)
  - ✅ Native implementation (no external library)
  - ✅ Quality: 9/10 ⭐⭐⭐⭐⭐
- [x] C4: Dashboard home page with stats (1 pt) ✅ **COMPLETE** (Nov 13)
  - ✅ Created progressService.ts (API integration for enrollments + streak)
  - ✅ Created StatsCard component (reusable, 4 color themes, loading states)
  - ✅ Dashboard page with live stats (enrolled courses, completed lessons, study hours, streak)
  - ✅ Recent activity section with enrollment data
  - ✅ Loading states with skeleton components
  - ✅ Error handling with toast notifications
  - ✅ "Continue Learning" CTA button
  - ✅ Empty state handling
  - ✅ Quality: 9/10 ⭐⭐⭐⭐⭐

**🎊 Epic C Achievements**:

- ✅ Complete dashboard layout (sidebar, header, main content)
- ✅ Responsive navigation (desktop collapse, mobile slide-in)
- ✅ Touch gesture support (swipe interactions)
- ✅ Live API integration (progressService aggregates multiple endpoints)
- ✅ Reusable StatsCard component (4 color themes)
- ✅ Loading states prevent UI flashing
- ✅ Error handling with user-friendly messages
- ✅ Dark mode support
- ✅ Accessibility (ARIA, keyboard nav)
- ✅ Responsive design (320px - 1920px)

**Epic D: Course & Learning Path** ✅ **COMPLETE** (7/7 pts - 100%)

- [x] D1.1: Course list page (0.5 pt) ✅ **COMPLETE** (Nov 13)
  - ✅ Search bar with debounce (300ms)
  - ✅ CEFR level filter (A1-C2 badges)
  - ✅ Sort options (4 choices)
  - ✅ Grid/List view toggle
  - ✅ Pagination with URL sync
  - ✅ Loading skeletons & empty states
- [x] D1.2: Course card component (0.5 pt) ✅ **COMPLETE** (Nov 13)
  - ✅ Thumbnail with CEFR badge
  - ✅ Truncated title & description
  - ✅ Hover animations
  - ✅ Responsive design
- [x] D1.3: Search and filter API integration (0.5 pt) ✅ **Already implemented in D1.1**
- [x] D1.4: Pagination (0.5 pt) ✅ **Already implemented in D1.1**
- [x] D2: Course detail page with enrollment (1.5 pts) ✅ **COMPLETE** (Nov 13)
  - ✅ Course detail page at /courses/[id] with sections & lessons
  - ✅ CourseSection component (collapsible)
  - ✅ LessonItem component with type icons
  - ✅ Enrollment button with loading states
  - ✅ enrollmentService API client
  - ✅ Toast notifications
- [x] D3: Learning path display (1.5 pts) ✅ **COMPLETE** (Nov 13)
  - ✅ Learning paths page at /learning-paths
  - ✅ LearningPathCard component with CEFR badges
  - ✅ Recommended path highlighting
  - ✅ Start path button with 409 conflict handling
  - ✅ Progress tracking for started paths
  - ✅ learningPathService with 6 methods
  - ✅ TypeScript types (LearningPath, UserPathProgress, CEFR_LEVELS)
- [x] D4: Lesson viewer interface (1.5 pts) ✅ **COMPLETE** (Nov 13)
  - ✅ D4.1: Create Lesson Viewer Page (0.5 pt)
  - ✅ D4.2: Create Content Renderer (0.5 pt)
  - ✅ D4.3: Add Complete Lesson Button (0.5 pt)
- [x] D5: Lesson navigation (prev/next) (0.5 pt) ✅ **COMPLETE** (Nov 14)
  - ✅ LessonNavigation component with prev/next buttons
  - ✅ useLessonNavigation hook for navigation logic
  - ✅ Progress indicator (Lesson X of Y)
  - ✅ Cross-section navigation support
  - ✅ Disabled states for first/last lessons

**Epic E: Progress & Profile** (5 pts - 80% complete)

- [x] E1: Progress dashboard with charts - **Dependencies: D1-D5, D4** (2 pts) ✅ **COMPLETE** (Nov 14)
  - ✅ E1.1: Create Progress Page (0.5 pt) - Stats grid, API integration
  - ✅ E1.2: Create Progress Chart (0.8 pt) - Recharts area chart with tooltips
  - ✅ E1.3: Create Streak Calendar (0.7 pt) - GitHub-style heatmap (365 days)
  - ✅ Enhanced progressService with getProgressSummary()
  - ✅ Updated progress types (StreakData, DailyActivity, ProgressSummary)
  - ✅ Responsive design, loading states, dark mode
- [x] E2: Lesson completion tracking UI (1 pt) ✅ **COMPLETE** (Nov 14)
  - ✅ E2.1: CourseCard progress bars and completion badges
  - ✅ E2.2: LessonItem checkmarks (UI ready, **backend endpoint implemented Nov 20**)
  - ✅ E2.3: Confetti animation (verified working)
  - ✅ Enrollment integration in courses page
  - ✅ Progress indicators on course cards
  - ✅ **UPDATE (Nov 20)**: Backend endpoint `GET /api/v1/progress/courses/{courseId}/lessons` now implemented. Frontend can now display individual lesson checkmarks using `CourseProgressDTO` response.
- [x] E3: Profile management page (1 pt) ✅ **COMPLETE** (Nov 14)
  - ✅ E3.1: Extended User type with full profile fields (bio, phone, timezone, language, learningGoal)
  - ✅ E3.2: Created ProfileForm component (250+ lines) with React Hook Form + Zod validation
  - ✅ E3.3: Updated profile page with avatar display, profile overview card, and edit form
  - ✅ E3.4: Tested profile update flow (TypeScript compilation successful)
  - ✅ Timezone selector (100+ options), language selector (10 languages), CEFR levels (A1-C2)
  - ✅ Phone validation, loading states, toast notifications
  - ✅ Fixed Next.js 16 async params issue (lesson viewer, courses page)
  - ✅ Installed shadcn/ui components: textarea, select
  - ✅ Responsive design, dark mode support
- [x] E4: Avatar upload interface (0.5 pt) ✅ **COMPLETE** (Nov 15)
  - ✅ E4.1: Created AvatarUpload component (301 lines) with file selection, preview, upload, delete
  - ✅ E4.2: Implemented upload logic with userService.uploadAvatar() integration, progress tracking
  - ✅ E4.3: Integrated with profile page, replaced static Avatar with interactive AvatarUpload
  - ✅ E4.4: Added deleteAvatar() method to userService
  - ✅ File validation (max 5MB, JPG/PNG/GIF/WebP only)
  - ✅ Preview dialog with upload progress bar
  - ✅ Camera icon hover overlay on avatar
  - ✅ Toast notifications for success/error
  - ✅ Responsive design, loading states, accessibility (ARIA labels)
  - ✅ Production build successful
- [x] E5: Settings page (0.5 pt) ✅ **COMPLETE** (Nov 15)
  - ✅ E5.1: Created Settings page (565 lines) with language, timezone, notifications, theme toggle
  - ✅ E5.2: Integrated next-themes for Light/Dark/System theme switching
  - ✅ E5.3: Language selector (10 languages) and Timezone selector (14 zones)
  - ✅ E5.4: Email notification toggles (3 types: email, reminders, reports)
  - ✅ E5.5: Save/Reset functionality with userService integration
  - ✅ Visual theme cards with checkmark indicators
  - ✅ Toast notifications for all actions
  - ✅ Responsive design, dark mode support, accessibility (ARIA labels)
  - ✅ Installed shadcn/ui components: switch, separator
  - ✅ Production build successful

**Epic F: Testing & Polish** ✅ **COMPLETE** (4.5/4 pts - 112.5%)

- [x] F1: Form validation for all inputs (0.5 pt) ✅ **COMPLETE** (Nov 15)
  - ✅ Comprehensive audit of 4 forms (Login, Register, Profile, Settings)
  - ✅ React Hook Form + Zod validation
  - ✅ Real-time feedback, password strength indicators
  - ✅ ARIA labels, keyboard navigation
  - ✅ Documentation: FORM-VALIDATION-AUDIT.md (600+ lines)
  - ✅ Quality: 9.5/10 ⭐⭐⭐⭐⭐
- [x] F2: Error handling + toast notifications + **Error Boundary** (0.7 pt) ✅ **COMPLETE** (Nov 15)
  - ✅ 404 Not Found page (app/not-found.tsx - 77 lines)
  - ✅ 500 Server Error page (app/error.tsx - 93 lines)
  - ✅ ErrorBoundary component (components/ErrorBoundary.tsx - 180 lines)
  - ✅ Sonner toast notifications (already configured)
  - ✅ Comprehensive API error handling (network, timeout, retry)
  - ✅ Quality: 10/10 ⭐⭐⭐⭐⭐
- [x] F3: Loading states + skeletons + **Responsive testing** (0.8 pt) ✅ **COMPLETE** (Nov 15)
  - ✅ Loading states audit (LOADING-STATES-AUDIT.md - 800+ lines)
  - ✅ Fixed Dashboard skeleton loaders (replaced animate-pulse)
  - ✅ Added Loader2 spinners to all action buttons
  - ✅ Responsive design testing (RESPONSIVE-TESTING-RESULTS.md - 700+ lines)
  - ✅ Tested 10 pages × 6 breakpoints (320px - 1920px)
  - ✅ 100% pass rate, touch targets ≥40px, no horizontal scroll
  - ✅ Overall scores: Loading 9.1/10, Responsive 99.3% ⭐⭐⭐⭐⭐
- [x] F4: Jest + React Testing Library Setup (0.5 pt) ✅ **COMPLETE** (Nov 17)
  - ✅ Jest v30.2.0 configured with Next.js integration
  - ✅ Coverage thresholds: 60% global, 80% services, 70% branches
  - ✅ React Testing Library v16.3.0 with jest-dom matchers
  - ✅ Test utils with ThemeProvider wrapper
  - ✅ Comprehensive mock data (users, courses, progress, errors)
  - ✅ Sample tests (8/8 passing)
  - ✅ Test scripts: test, test:watch, test:coverage
  - ✅ Documentation: tests/README.md (400+ lines)
  - ✅ Files created: 8 files (1,073+ lines)
  - ✅ Quality: 10/10 ⭐⭐⭐⭐⭐
- [x] F5: Component Unit Tests (1.5 pt) ✅ **COMPLETE** (Nov 17)
  - ✅ F5.1: Authentication components tested (Login + Register flows, error handling, success navigation)
  - ✅ F5.2: Course components tested (CourseCard coverage + Courses page search/filter)
  - ✅ F5.3: Dashboard components tested (StatsCard, ProgressChart formatting and loading states)
  - ✅ F5.4: Navigation components tested (Sidebar, Header interactions & logout flow)
  - ✅ F5.5: Coverage verification complete (24.69% global, 87-100% on tested components)
  - ✅ Test Results: 9 suites / 34 tests passed (100% pass rate)
  - ✅ Critical paths covered: Auth (91-93%), Courses (90%), Dashboard (100%), Navigation (94%)
  - ⚠️ Note: Global thresholds not met (24.69% vs 60% target) - defer remaining 2.3 pts to Sprint 4
  - ✅ Quality: 8/10 ⭐⭐⭐⭐⭐
- [x] F6: Accessibility Audit (0.5 pt) ✅ **COMPLETE** (Nov 17)
  - ✅ F6.1: ARIA labels & semantic HTML review (25+ locations audited)
  - ✅ F6.2: Keyboard navigation testing (Tab/Shift+Tab/Enter/Escape/Arrow keys)
  - ✅ F6.3: Color contrast analysis (all text 4.5:1+, most AAA)
  - ✅ F6.4: Screen reader compatibility check (announcements, landmarks)
  - ✅ F6.5: Comprehensive audit report (ACCESSIBILITY-AUDIT.md - 500+ lines)
  - ✅ WCAG 2.1 Level AA: **49/50 criteria passed (98%)**
  - ✅ Overall Grade: **A+ (96/100)** - Approved for production
  - ✅ Quality: 10/10 ⭐⭐⭐⭐⭐

**🎊 Epic F Achievements**:

- ✅ Complete test infrastructure (Jest + RTL + mocks)
- ✅ 9 test suites, 34 tests, 100% pass rate
- ✅ Critical user paths tested (auth, courses, dashboard, navigation)
- ✅ Tested components: 87-100% coverage
- ✅ Form validation audit (4 forms)
- ✅ Error handling + Error Boundary
- ✅ Loading states + responsive design tested
- ✅ **WCAG AA compliant** (49/50 criteria, 98%)
- ✅ Comprehensive documentation (tests/README.md, 4 audit reports)

**Quality Improvements** (Nov 11):

- ✅ Security: httpOnly cookies for JWT
- ✅ Error Boundary for React errors
- ✅ Comprehensive API error handling (retry logic)
- ✅ Coverage thresholds defined (60% global, 80% services)
- ✅ Responsive design testing checklist
- ✅ Accessibility audit (ARIA, keyboard nav, WCAG AA)

**Next Up** 📋

- **Epic D: Course & Learning Path** (7 points)
  - D1: Course Listing Page with Search/Filter (2 pts)
  - D2: Course Detail Page with Enrollment (1.5 pts)
  - D3: Learning Path Display Component (1.5 pts)
  - D4: Lesson Viewer Interface (1.5 pts)
  - D5: Lesson Navigation (0.5 pt)

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
