# Sprint 4 Task Breakdown - Mobile App Development

**Sprint**: 4 / 8  
**Duration**: November 24 – December 11, 2025 (18 days)  
**Status**: 🟢 In Progress (Day 9) | **Progress**: 16.5/43 pts (38.4%)  
**Created**: November 24, 2025  
**Last Updated**: November 27, 2025

---

## 📱 Overview

**Goal**: Build foundation of Lexia Mobile App (React Native + Expo) with authentication, navigation, core features, and offline support.

**Total Points**: 43 points over 18 days = **2.4 pts/day**  
**Current Velocity**: 5.5 pts/day 🚀 (exceeding target!)

---

## 📅 Day-by-Day Task Breakdown

### **Week 1: Setup & Authentication (Day 1-6)**

#### **Day 1 (Nov 24) - Project Setup Part 1** [3.5 pts]

**Tasks**:

- [x] **A1**: Initialize Expo project with TypeScript (0.5 pt) ✅
  - `npx create-expo-app lexia-mobile --template expo-template-blank-typescript`
  - Test on iOS/Android simulator
  - Verify app runs with `npm start`
- [x] **A2**: Configure ESLint, Prettier, absolute imports (0.5 pt) ✅

  - Install ESLint + Prettier + TypeScript plugins
  - Create `eslint.config.js` (mirror web config)
  - Create `prettier.config.js`
  - Configure `tsconfig.json` with path aliases (`@/` → `./`)
  - Test: `npm run lint` passes

- [x] **A3**: Setup directory structure (0.5 pt) ✅

  - Create folders: `app/`, `components/`, `services/`, `store/`, `types/`, `hooks/`, `utils/`, `assets/`
  - Create `README.md` documenting structure
  - Create `.env.example` with `API_URL`

- [x] **A4**: Configure Axios client with interceptors (1 pt) ✅

  - Create `services/api.ts`
  - Setup base Axios instance (baseURL, timeout 30s)
  - Request interceptor: Add Authorization header from AsyncStorage
  - Response interceptor: Handle 401, network errors, timeout
  - **Copy logic from `lexia-web/lib/api.ts`** (offline detection, retry logic)

- [x] **A5**: Define base types (0.5 pt) ✅

  - Create `types/index.ts`
  - Define types: `User`, `Course`, `Lesson`, `TokenResponse`, `LoginRequest`, `RegisterRequest`
  - Match backend DTOs from `API-SPECIFICATION.md`
  - Create `types/api.ts` for API response types

- [x] **A6**: Install missing dependencies (0.5 pt) - **HIGH PRIORITY** ✅
  - Install packages (see below)
  - Verify no peer dependency conflicts
  - Test: `npm run build` passes

**Dependencies to Install**:

```bash
# Core
npm install @tanstack/react-query @tanstack/query-async-storage-persister
npm install zod react-hook-form
npm install @react-native-community/netinfo

# Charts & UI
npm install react-native-chart-kit react-native-svg
npm install react-native-markdown-display
npm install react-native-fast-image

# Audio
npm install expo-av

# Biometric (optional)
npm install expo-local-authentication

# Push Notifications
npm install expo-notifications

# Dev dependencies
npm install --save-dev @testing-library/react-native @testing-library/jest-native
npm install --save-dev jest-expo @types/jest
```

**End of Day Check**: ✅ Project compiles, ESLint passes, Axios client configured

---

#### **Day 2 (Nov 25) - Project Setup Part 2** [3 pts]

**Tasks**:

- [x] **A7**: Configure test environment (1 pt) ✅

  - Create `jest.config.js` for React Native
  - Create `jest.setup.js` with `@testing-library/jest-native`
  - Create sample test: `__tests__/App.test.tsx`
  - Configure coverage thresholds: 60% global, 80% services
  - Test: `npm test` passes

- [x] **A8**: Setup ESLint rules + Prettier (0.5 pt) ✅

  - Final ESLint config adjustments
  - Add pre-commit hook (optional: husky + lint-staged)
  - Document linting rules in README

- [x] **A9**: Setup React Query (1.5 pts) - **MOVED FROM E3** ✅
  - Create `lib/queryClient.ts`
  - Configure QueryClient with AsyncStorage persister
  - Setup QueryClientProvider in `App.tsx`
  - Configure defaults: `staleTime: 5 minutes`, `cacheTime: 24 hours`
  - Test: Query persists across app restarts

**End of Day Check**: ✅ Tests pass, React Query configured, dependencies installed

---

#### **Day 3 (Nov 26) - Auth Store & Token Storage** [2.5 pts]

**Tasks**:

- [x] **B1**: Implement Auth Store (Zustand) (1 pt) ✅

  - Create `store/authStore.ts`
  - State: `user`, `accessToken`, `refreshToken`, `isAuthenticated`, `isLoading`
  - Actions: `login`, `logout`, `register`, `setUser`, `setTokens`, `clearAuth`
  - Persist to AsyncStorage (use `zustand/middleware` persist)
  - Initial loading state prevents UI flash

- [x] **B4**: Implement Token Storage (1.5 pts) ✅
  - Create `lib/tokenStorage.ts`
  - Functions: `saveTokens()`, `getAccessToken()`, `getRefreshToken()`, `getTokenType()`, `clearTokens()`
  - Keys: `@lexia/access_token`, `@lexia/refresh_token`, `@lexia/token_type`
  - Auto-login logic: Check token on app launch → restore auth state
  - Handle app backgrounding: AppState listener → refresh if expired
  - Create `components/AuthProvider.tsx` (wraps app, handles auto-login)

**End of Day Check**: ✅ Auth store persists, tokens saved/retrieved from AsyncStorage

---

#### **Day 4 (Nov 27) - Axios Interceptor & Token Refresh** [1.5 pts]

**Tasks**:

- [x] **B6**: Implement Axios Interceptor with Token Refresh (1.5 pts) ✅
  - Update `services/api.ts`
  - Request interceptor: Attach `Authorization: Bearer ${token}` from AsyncStorage
  - Response interceptor: Detect 401 → trigger refresh
  - Refresh flow:
    - Call `POST /auth/refresh` with refresh token
    - Save new tokens to AsyncStorage
    - Retry original failed request
  - Promise lock: Prevent concurrent refresh calls (`isRefreshing` flag)
  - Queue failed requests during refresh
  - Logout on refresh failure: Clear AsyncStorage → navigate to Login
  - **Copy logic from `lexia-web/lib/api.ts`** (lines 90-180)

**End of Day Check**: ✅ 401 triggers refresh, original request retried, promise lock works

---

#### **Day 5 (Nov 28) - Login & Register Screens** [2 pts]

**Tasks**:

- [x] **B2**: Build Login Screen (1 pt) ✅

  - Create `app/auth/LoginScreen.tsx`
  - Form: Email (text input), Password (secure text input)
  - Validation: React Hook Form + Zod (email format, min 8 chars password)
  - Submit: Call `POST /auth/login` → save tokens → navigate to Home
  - Loading state: Disable button, show spinner
  - Error handling: Toast for invalid credentials, network errors

- [x] **B3**: Build Register Screen (1 pt) ✅
  - Create `app/auth/RegisterScreen.tsx`
  - Form: First Name, Last Name, Email, Password, Confirm Password
  - Validation: Zod (all required, email format, password match)
  - Submit: Call `POST /auth/register` → auto-login → navigate to Home
  - Loading/error states

**End of Day Check**: ✅ Login/Register screens functional, form validation works

---

#### **Day 6 (Nov 29) - Auth Error Handling & Navigation** [2.5 pts]

**Tasks**:

- [x] **B5**: Handle Auth Errors (1 pt) ✅

  - User-friendly error messages: 400/401/500/network/timeout
  - Retry logic: 3 attempts with exponential backoff (network errors only)
  - "No internet" banner: Use NetInfo, show banner at top of screen
  - Test: Airplane mode → banner appears

- [x] **C1**: Setup Tab Navigation (1 pt) ✅

  - Install `@react-navigation/bottom-tabs`
  - Create 4 tabs: Home, Courses, Progress, Profile
  - Icons: react-native-vector-icons (or Expo icons)
  - Active state styling: Blue accent

- [x] **C2**: Setup Stack Navigation + Auth Gate (0.5 pt) ✅
  - Install `@react-navigation/native-stack`
  - Create Auth Stack: Login, Register
  - Create Main Stack: Tabs, CourseDetail, LessonViewer
  - Splash screen: Check AsyncStorage → navigate to Auth/Main

**End of Day Check**: ✅ Auth errors handled, tab navigation works, auth gate functional

---

### **Week 2: Core Features (Day 7-12)**

#### **Day 7 (Nov 30) - Navigation Polish & Home Screen** [2 pts]

**Tasks**:

- [x] **C3**: Implement Custom Tab Bar (0.5 pt) ✅

  - Custom styling, smooth transitions
  - Badge count on tabs (e.g., notifications)

- [x] **C4**: Header Components (0.5 pt) ✅

  - Back button, title, optional actions (settings icon)

- [ ] **D1**: Home Screen (Dashboard) (1 pt)
  - Display user name (from auth store)
  - "Continue Learning" card (last accessed course from progress API)
  - Daily Streak card
  - Stats: Enrolled courses, Completed lessons, Study hours
  - Loading: Skeleton loaders (react-native-paper Skeleton)
  - Error: Retry button
  - Pull-to-refresh: RefreshControl

**End of Day Check**: ✅ Home screen displays stats, pull-to-refresh works

---

#### **Day 8 (Dec 1) - Courses Screen** [2 pts]

**Tasks**:

- [ ] **D2**: Courses Screen (List) (2 pts)
  - Fetch courses: `GET /api/v1/courses`
  - Search bar: Debounce 300ms
  - Filter: CEFR level chips (A1-C2)
  - Sort: 4 options (dropdown)
  - Course Card: Thumbnail, title, level badge, description (truncated)
  - Infinite scroll: FlatList `onEndReached`
  - Skeleton loading: 6 skeleton cards
  - Empty state: "No courses found"
  - Error state: Retry button
  - React Query: `useInfiniteQuery`

**End of Day Check**: ✅ Course list loads, search/filter works, infinite scroll works

---

#### **Day 9 (Dec 2) - Course Detail Screen** [1.5 pts]

**Tasks**:

- [ ] **D3**: Course Detail Screen (1.5 pts)
  - Fetch course: `GET /api/v1/courses/{id}`
  - Display: Title, Level, Description, Thumbnail
  - Section List: Collapsible accordions (react-native-paper Accordion)
  - Lesson List: Type icons (📖 Reading, 🎧 Listening, ❓ Quiz, 🗣️ Speaking), completion checkmarks
  - Enroll Button: `POST /api/v1/enrollments/courses/{id}`
  - Loading state, error handling (404)
  - Toast: Success message
  - Progress bar: If already enrolled
  - React Query: `useMutation` for enroll

**End of Day Check**: ✅ Course detail loads, enroll works, sections collapsible

---

#### **Day 10 (Dec 3) - Lesson Viewer Part 1 (Reading & Listening)** [1.5 pts]

**Tasks**:

- [ ] **D4.1**: Lesson Viewer - READING (0.5 pt)

  - Fetch lesson: `GET /api/v1/lessons/{id}`
  - Render markdown: `react-native-markdown-display`
  - Styling: Headings, lists, code blocks
  - Progress indicator: "Lesson X of Y"

- [ ] **D4.2**: Lesson Viewer - LISTENING (1 pt)
  - Audio player: `expo-av`
  - Controls: Play/Pause, Seek bar, Current time / Total duration
  - Transcript: Collapsible section
  - Vocabulary list: Word, definition, timestamp (tap to seek)
  - Loading state: Spinner while audio loads

**End of Day Check**: ✅ Reading lessons render markdown, Listening lessons play audio

---

#### **Day 11 (Dec 4) - Lesson Viewer Part 2 (Quiz & Speaking)** [1.5 pts]

**Tasks**:

- [ ] **D4.3**: Lesson Viewer - QUIZ (1 pt)

  - Parse quiz data: `content.questions[]`
  - Question types:
    - `multiple_choice`: Radio buttons
    - `true_false`: Toggle buttons
    - `fill_blank`: Text input
    - `matching`: Basic drag-drop (or simple list matching)
  - Timer: If `content.timeLimit` exists, show countdown
  - Submit: `POST /api/v1/progress/lessons/{lessonId}/submit` with `{ answers: [...], score: 85 }`
  - Results: Score, correct/incorrect indicators, explanations
  - Passing score: Green if `score >= passingScore`, red otherwise

- [ ] **D4.4**: Lesson Viewer - SPEAKING (0.5 pt)
  - Record button: `expo-av` Audio.Recording
  - Playback: Listen to recorded audio (local only, no submission)
  - Basic UI: Record icon, waveform placeholder

**End of Day Check**: ✅ Quiz interactive, submit works, Speaking records audio

---

#### **Day 12 (Dec 5) - Lesson Completion & Navigation** [1.5 pts]

**Tasks**:

- [ ] **D4.5**: Complete Lesson Button (0.5 pt)

  - Button: "Complete Lesson" at bottom of viewer
  - API: `POST /api/v1/progress/lessons/{lessonId}/complete`
  - Retry logic: 3 attempts with exponential backoff
  - Success: Toast + confetti animation (if quiz passed)
  - Navigate: Next lesson or back to course detail

- [ ] **D5**: Lesson Navigation (Prev/Next) (0.5 pt)

  - Prev/Next buttons: Navigate between lessons in same section
  - Cross-section navigation: Auto-advance to next section if at end
  - Disabled states: First lesson (no prev), last lesson (no next)
  - Progress indicator: Update "Lesson X of Y"

- [ ] **D6**: Push Notifications Setup (0.5 pt) - **PARTIAL**
  - Install `expo-notifications`
  - Request permissions on first launch
  - Store permission status in AsyncStorage
  - **Note**: Backend API `/notifications/register` not ready - defer device token registration to Sprint 5

**End of Day Check**: ✅ Lesson completion works, prev/next navigation functional

---

### **Week 3: Progress, Offline, Testing (Day 13-18)**

#### **Day 13 (Dec 6) - Progress Screen & Profile** [2.5 pts]

**Tasks**:

- [ ] **E1**: Progress Screen (1.5 pts)

  - Fetch progress: `GET /api/v1/progress/summary`
  - Weekly Activity Chart: `react-native-chart-kit` LineChart
  - Streak Calendar: GitHub-style heatmap (past 90 days)
  - Stats Cards: Total lessons, Current streak, Study hours, XP
  - Skeleton loading, error state, pull-to-refresh
  - React Query: `useQuery` with caching

- [ ] **D5**: Profile Screen (1 pt)
  - Display: Avatar, Name, Email, CEFR Level, Streak
  - Edit Profile button: Navigate to edit screen (basic form)
  - Logout button: Clear AsyncStorage → navigate to Auth
  - Loading state for profile fetch
  - React Query: `useQuery` for profile

**End of Day Check**: ✅ Progress screen displays charts, Profile screen shows user info

---

#### **Day 14 (Dec 7) - Offline Support Part 1** [2 pts]

**Tasks**:

- [ ] **E2**: Document Offline Strategy (0.5 pt)

  - Create `docs/implement/sprint-4/offline-strategy.md`
  - Decision: React Query + AsyncStorage persister
  - Conflict resolution: Last-write-wins (document in strategy)

- [ ] **E4**: Implement Offline Support (1.5 pts)
  - Network detection: NetInfo listener in App.tsx
  - "You're offline" banner: Show at top when disconnected
  - Queue mutations: Store in AsyncStorage when offline
  - Sync queue: Background task when back online
  - Graceful degradation: Hide "Enroll", "Complete Lesson" buttons when offline
  - "Syncing..." indicator when back online

**End of Day Check**: ✅ Offline banner appears, cached data shows, mutations queued

---

#### **Day 15 (Dec 8) - Offline Support Part 2** [2 pts]

**Tasks**:

- [ ] **E5**: Add Offline-First Features (2 pts)
  - Download lessons: `AsyncStorage` + `FileSystem.downloadAsync`
  - Download images: `react-native-fast-image` with cache
  - Download button: On lesson cards
  - Progress indicator: During download
  - "Downloaded" badge: On lesson cards
  - Delete download: Option in lesson viewer
  - Storage usage: Display in settings

**End of Day Check**: ✅ Lessons downloadable, viewable offline, images cached

---

#### **Day 16 (Dec 9) - Testing Part 1** [3 pts]

**Tasks**:

- [ ] **F1**: Unit tests for auth store (1 pt)

  - Test login/logout/register actions
  - Test token persistence
  - Test auto-login logic
  - Coverage ≥90%

- [ ] **F2**: Unit tests for API client interceptors (1 pt)

  - Mock AsyncStorage
  - Test Authorization header injection
  - Test 401 token refresh flow
  - Test network error retry logic
  - Test timeout handling
  - Coverage ≥90%

- [ ] **F3**: Integration tests for auth flow (1 pt)
  - Test full login flow (input → API → store → navigation)
  - Test register flow
  - Test logout flow
  - Use `@testing-library/react-native`

**End of Day Check**: ✅ Auth store tests pass, API client tests pass, integration tests pass

---

#### **Day 17 (Dec 10) - Testing Part 2** [1.5 pts]

**Tasks**:

- [ ] **F4**: Snapshot tests for core components (1 pt)

  - CourseCard, LessonItem, StatsCard, Header
  - Loading states
  - Error states
  - Generate snapshots: `npm test -- -u`

- [ ] **F5**: Coverage verification (0.5 pt)
  - Run: `npm test -- --coverage`
  - Verify: ≥60% global, ≥80% services
  - Document: Coverage report in `docs/implement/sprint-4/coverage-report.md`

**End of Day Check**: ✅ Snapshot tests pass, coverage thresholds met

---

#### **Day 18 (Dec 11) - Performance & QA** [1.5 pts]

**Tasks**:

- [ ] **F5**: Performance profiling (1 pt)

  - App launch time: <3s (iPhone 12)
  - Scrolling: 60fps on course list (100+ items)
  - Memory usage: <200MB (use Flipper or Xcode Instruments)
  - Bundle size: <50MB (release build)
  - Document: `docs/implement/sprint-4/performance-report.md`

- [ ] **B7**: Biometric authentication (OPTIONAL) (0.5 pt)

  - If time allows, implement TouchID/FaceID
  - Toggle in settings
  - Fallback to password

- [ ] **Bug fixes & polish** (remainder)
  - Fix any failing tests
  - Address performance bottlenecks
  - UI polish: Consistent spacing, colors

**End of Day Check**: ✅ Performance targets met, all tests pass, app ready for demo

---

## 📊 Progress Tracking

| Epic                      | Points | Status         | Completion Date |
| ------------------------- | ------ | -------------- | --------------- |
| A: Project Initialization | 7      | ✅ Complete    | Nov 25, 2025    |
| B: Authentication         | 5/8    | 🟢 Mostly Done | Nov 25-26, 2025 |
| C: Navigation & Layout    | 4      | ✅ Complete    | Nov 27, 2025    |
| D: Core Features          | 10     | 🔵 Not Started | -               |
| E: Progress & Offline     | 8      | 🔵 Not Started | -               |
| F: Testing & Performance  | 6      | 🔵 Not Started | -               |

**Total**: 43 points | **Completed**: 16.5 points (38.4%)

**Note**: B7 (Biometric Auth) deferred to Sprint 5 (1 pt)

---

## ✅ Daily Checklist

**Every Morning**:

- [ ] Read `current-sprint-status.md`
- [ ] Check `task-breakdown.md` for today's tasks
- [ ] Pull latest from `dev` branch

**Every Evening**:

- [ ] Update `daily-log.md` with progress
- [ ] Commit changes (conventional format)
- [ ] Push to `dev` branch
- [ ] Update task status in this file

**End of Day Check**:

- [ ] Code compiles: `npm run build`
- [ ] Tests pass: `npm test`
- [ ] Linting passes: `npm run lint`

---

## 🚨 Blockers & Notes

**Known Issues**:

- [ ] **Backend API `/notifications/register` not ready** → Defer D6 device token registration to Sprint 5
- [ ] **Quiz submission endpoint** → Verify `POST /api/v1/progress/lessons/{lessonId}/submit` exists

**Adjustments Made**:

- ✅ **Timeline extended**: Day 1-2 → Day 1-3 for setup (realistic)
- ✅ **Coverage target lowered**: 60% global → 50% global (Sprint 4), 60% in Sprint 5
- ✅ **Epic B clarified**: B4 (token storage) vs B6 (axios interceptor) - no overlap
- ✅ **Epic D4 detailed**: Quiz data structure, submission API, scoring logic documented

**Next Steps After Sprint 4**:

- Sprint 5: Admin panel, content management, push notifications backend
- Sprint 6: Security audit, httpOnly cookies migration, advanced features

---

## 📝 Session Documentation

Each development session should create a summary in `docs/implement/sprint-4/session-X-topic.md` with:

- What we accomplished
- Code generated (files, LOC)
- Key decisions
- Challenges faced
- Quality assessment (1-10)
- Best prompts used
- Next steps

---

**Last Updated**: November 27, 2025  
**Next Review**: December 11, 2025 (End of Sprint 4)
