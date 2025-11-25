# Sprint 4 Plan: Mobile App Development

**Sprint**: 4 / 8
**Duration**: November 23 – December 10, 2025 (18 days)
**Status**: 🔵 Not Started
**Goal**: Build the foundation of the Lexia Mobile App using React Native (Expo) and integrate with the Backend API.

## 📱 Technical Stack

- **Framework**: React Native (Expo SDK 50+)
- **Language**: TypeScript
- **Navigation**: React Navigation (Stack + Tabs)
- **State Management**: Zustand
- **API Client**: Axios (with interceptors for auth + retry logic)
- **Storage**: AsyncStorage (for JWT access tokens)
- **Data Fetching/Caching**: React Query (TanStack Query v5) - **DECISION CONFIRMED**
- **Form Validation**: Zod + React Hook Form
- **Network Detection**: @react-native-community/netinfo
- **UI Library**: React Native Paper (Material Design consistency)
- **Charts**: react-native-chart-kit + react-native-svg
- **Markdown**: react-native-markdown-display
- **Images**: react-native-fast-image (lazy loading)
- **Testing**: Jest + React Native Testing Library (≥60% global, ≥80% services)

## 📋 Task Breakdown

### Epic A: Project Initialization (7 pts)

- [ ] **A1**: Initialize Expo project with TypeScript (0.5 pt)
  - **Acceptance**: Expo SDK 50+ installed, app runs on iOS/Android simulator
- [ ] **A2**: Configure ESLint, Prettier, and absolute imports (0.5 pt)
  - **Acceptance**: ESLint rules match web app, imports use `@/` prefix
- [ ] **A3**: Setup directory structure (`app`, `components`, `services`, `store`, `types`, `hooks`, `utils`) (0.5 pt)
  - **Acceptance**: Clear separation of concerns, README.md documents structure
- [ ] **A4**: Configure Axios client with interceptors (Auth header, token refresh on 401) (1 pt)
  - **Acceptance**:
    - Authorization header automatically added from AsyncStorage
    - 401 triggers refresh token flow (copy logic from web `authService.ts`)
    - Network errors retry 3 times with exponential backoff
    - Timeout set to 30 seconds
- [ ] **A5**: Define base types (User, Course, Lesson, TokenResponse) + API base config (0.5 pt)
  - **Acceptance**: Types match backend DTOs from `API-SPECIFICATION.md`, env config for API_URL
- [ ] **A6**: Install missing dependencies (React Query, Zod, NetInfo, Charts, Markdown) (1 pt)
  - **Acceptance**: All packages installed and working:
    - `@tanstack/react-query` ^5.56.0
    - `zod` ^3.22.4
    - `react-hook-form` ^7.49.0
    - `@react-native-community/netinfo` ^11.3.0
    - `react-native-chart-kit` ^6.12.0
    - `react-native-svg` ^14.1.0
    - `react-native-markdown-display` ^7.0.0
    - `react-native-fast-image` ^8.6.3
- [ ] **A7**: Configure test environment (Jest + React Native Testing Library) (1 pt)
  - **Acceptance**:
    - `jest.config.js` configured for React Native
    - `@testing-library/react-native` ^12.4.0 installed
    - `@testing-library/jest-native` ^5.4.3 installed
    - Sample test passes
    - Coverage thresholds: 60% global, 80% services
- [ ] **A8**: Setup ESLint rules matching web app + Prettier (0.5 pt)
  - **Acceptance**: `eslint.config.js` mirrors web config, `prettier.config.js` created, no warnings on `npm run lint`

### Epic B: Authentication (8 pts)

- [ ] **B1**: Implement `AuthProvider` and Auth Store (Zustand) (1 pt)
  - **Acceptance**:
    - Store manages user state, token state, login/logout/register actions
    - Persisted to AsyncStorage (use `zustand/middleware` persist)
    - Initial loading state prevents UI flash
- [ ] **B2**: Build **Login Screen** (Email/Password) (1 pt)
  - **Acceptance**:
    - Form validation (React Hook Form + Zod: email format, min 8 chars password)
    - Loading state, disabled button during submission
    - Success toast + navigate to Home
    - Error toast for invalid credentials
- [ ] **B3**: Build **Register Screen** (Name, Email, Password) (1 pt)
  - **Acceptance**:
    - Form validation (Zod: firstName/lastName required, email format, password min 8 chars + confirmation match)
    - Loading/error states
    - Success → auto-login → navigate to Home
- [ ] **B4**: Implement Token Storage & Auto-login (1.5 pts)
  - **Focus**: AsyncStorage operations, app launch behavior
  - **Acceptance**:
    - Access token + refresh token stored in AsyncStorage with keys: `@lexia/access_token`, `@lexia/refresh_token`, `@lexia/token_type`
    - Helper functions: `saveTokens()`, `getAccessToken()`, `getRefreshToken()`, `clearTokens()`
    - Auto-login on app launch: Check AsyncStorage → if token exists and not expired → restore auth state → navigate to Home
    - Logout clears AsyncStorage completely (`AsyncStorage.multiRemove(['@lexia/access_token', '@lexia/refresh_token', '@lexia/token_type'])`)
    - Handle app backgrounding: On app resume (AppState listener), check token expiry → refresh if needed
    - Show splash screen during token validation (prevents UI flash)
- [ ] **B5**: Handle Auth Errors (Invalid credentials, Network error, Server error) (1 pt)
  - **Acceptance**:
    - User-friendly error messages for 400/401/500/network timeout
    - Retry logic for network errors (3 attempts with exponential backoff)
    - "No internet" banner when offline (use NetInfo)
    - Error handling in login/register flows
- [ ] **B6**: Implement Axios Interceptor with Token Refresh (1.5 pts)
  - **Focus**: 401 detection, refresh flow, request retry
  - **Acceptance**:
    - **Request interceptor**: Attach Authorization header from AsyncStorage (`Bearer ${accessToken}`)
    - **Response interceptor**: Detect 401 status code
    - **Refresh flow**: Call `POST /auth/refresh` with refresh token from AsyncStorage
    - **Promise lock**: Prevent concurrent refresh calls (use `isRefreshing` flag + `refreshPromise`)
    - **Queue failed requests**: Store failed requests during refresh, retry all after success
    - **Update tokens**: Save new access token + optional rotated refresh token to AsyncStorage
    - **Retry original request**: Re-execute failed request with new token
    - **Logout on refresh failure**: If refresh returns 401/403 → clear AsyncStorage → navigate to Login
    - **Copy token refresh logic from `lexia-web/lib/api.ts`** (lines 90-180)
    - Handle edge cases: Missing refresh token, network error during refresh
- [ ] **B7**: Add biometric authentication (TouchID/FaceID) - **OPTIONAL** (1 pt)
  - **Acceptance**:
    - Use `expo-local-authentication`
    - "Enable biometric login" toggle in settings
    - Store flag in AsyncStorage (`@lexia/biometric_enabled`)
    - On app launch, if enabled, prompt for biometric
    - Fallback to password login if biometric fails

### Epic C: Navigation & Layout (4 pts)

- [ ] **C1**: Setup **Tab Navigation** (Home, Courses, Progress, Profile)
  - **Acceptance**: 4 tabs functional, icons visible, active state styling
- [ ] **C2**: Setup **Stack Navigation** + Auth Gate (Splash → Auth/Main based on token)
  - **Acceptance**: On app launch, check AsyncStorage token → if valid go to Main, else Auth. Splash screen shows loading indicator
- [ ] **C3**: Implement Custom Tab Bar
  - **Acceptance**: Custom styling, smooth transitions, proper icons
- [ ] **C4**: Specific Header components
  - **Acceptance**: Back button, title, optional actions (e.g., settings icon)

### Epic D: Core Features (10 pts)

- [ ] **D1**: **Home Screen** (Dashboard) (1.5 pts)
  - Display User Name (from auth store)
  - Show "Continue Learning" (Last accessed course from progress API)
  - Show Daily Streak (from progress API)
  - Stats cards (Enrolled courses, Completed lessons, Study hours)
  - **Acceptance**:
    - Skeleton loading state (react-native-paper Skeleton)
    - Error handling (retry button)
    - Pull-to-refresh (RefreshControl)
    - Uses React Query for caching
- [ ] **D2**: **Courses Screen** (List) (2 pts)
  - Fetch courses from API (`GET /api/v1/courses`)
  - Search/Filter UI (CEFR level filter)
  - Course Card component (thumbnail, title, level badge, description)
  - **Acceptance**:
    - Skeleton loading (6 skeleton cards)
    - Empty state ("No courses found")
    - Error state with retry
    - Infinite scroll pagination (react-native FlatList with `onEndReached`)
    - Search debounce (300ms)
    - Uses React Query with `useInfiniteQuery`
- [ ] **D3**: **Course Detail Screen** (1.5 pts)
  - Course Info (Title, Level, Description, thumbnail)
  - Section List (collapsible accordions)
  - Lesson List (with type icons, completion checkmarks)
  - Enroll Button
  - **Acceptance**:
    - Loading state
    - Error handling (404 if course not found)
    - Enroll success toast
    - Disable button during API call
    - Show progress bar if already enrolled
    - Uses React Query `useMutation` for enroll
- [ ] **D4**: **Lesson Viewer** (2.5 pts)
  - Display Lesson Content based on type:
    - **READING**: Markdown rendering (`react-native-markdown-display`)
    - **LISTENING**: Audio player (expo-av)
    - **QUIZ**: Interactive quiz UI (radio buttons, checkboxes, true/false)
    - **SPEAKING**: Record audio button (expo-av) - basic UI only
  - "Complete Lesson" button
  - Progress indicator (Lesson X of Y)
  - **Acceptance**:
    - **READING**: Markdown content rendered with proper styling (headings, lists, code blocks)
    - **LISTENING**: Audio playback controls (play/pause/seek), display transcript, vocabulary list with timestamps
    - **QUIZ**:
      - Parse quiz data from backend (see `DATABASE-SCHEMA.md` QUIZ lesson schema)
      - Question types: `multiple_choice` (radio buttons), `true_false` (toggle), `fill_blank` (text input), `matching` (drag-drop - basic)
      - Display timer if `timeLimit` exists
      - Submit answers to backend: `POST /api/v1/progress/lessons/{lessonId}/submit` with body: `{ "answers": [{ "questionId": 1, "answer": 0 }], "score": 85 }`
      - Show results: Score, correct/incorrect feedback, explanations
      - Passing score indicator (green if score >= `passingScore`, red otherwise)
    - **SPEAKING**: Record button, playback recorded audio (local only, no submission in Sprint 4)
    - Complete lesson API call: `POST /api/v1/progress/lessons/{lessonId}/complete` with retry logic (3 attempts)
    - Loading state during API calls, success feedback (toast + confetti animation if quiz passed)
    - Error handling: Network errors, timeout, server errors
- [ ] **D5**: **Profile Screen** (1 pt)
  - View Profile Details (Avatar, Name, Email, CEFR Level, Streak)
  - Edit Profile button (navigate to edit screen)
  - Logout Button
  - **Acceptance**:
    - Logout clears AsyncStorage + navigates to Auth
    - Loading state for profile fetch
    - Uses React Query for profile data
- [ ] **D6**: **Push Notification Setup** (1.5 pts) - **NEW**
  - Configure Expo Notifications
  - Request permissions on first launch
  - Register device token with backend
  - Handle notification tap (deep linking)
  - **Acceptance**:
    - Notifications work on both iOS/Android
    - Deep link to lesson viewer when tapping notification
    - Store permission status in AsyncStorage
    - Graceful degradation if permission denied

### Epic E: Progress & Offline (8 pts)

- [ ] **E1**: **Progress Screen** (2 pts)
  - Display Weekly Activity Chart (react-native-chart-kit LineChart)
  - Streak Calendar (GitHub-style heatmap, past 90 days)
  - Statistics Cards (Total lessons completed, Current streak, Total study hours, XP earned)
  - **Acceptance**:
    - Skeleton loading (Skeleton components)
    - Error state with retry button
    - Empty state for new users ("Start your first lesson to see progress")
    - Pull-to-refresh
    - Uses React Query for caching
- [ ] **E2**: Document Offline Strategy Decision (0.5 pt)
  - **Decision**: Use **React Query (TanStack Query v5)** with AsyncStorage persistence
  - **Rationale**:
    - ✅ Built-in caching, automatic refetch, stale-while-revalidate
    - ✅ Less boilerplate than manual AsyncStorage
    - ✅ Better offline UX (shows cached data instantly)
    - ✅ Industry standard, 40k+ stars on GitHub
    - ✅ Integrates with `@tanstack/query-async-storage-persister`
    - ❌ Adds ~400KB to bundle (acceptable tradeoff)
  - **Acceptance**: Document in `docs/implement/sprint-4/offline-strategy.md` with decision matrix
- [ ] **E3**: Implement React Query Setup (1.5 pts)
  - Install `@tanstack/react-query` and `@tanstack/query-async-storage-persister`
  - Configure QueryClient with AsyncStorage persister
  - Setup QueryClientProvider in App.tsx
  - Configure default options (staleTime: 5 minutes, cacheTime: 24 hours)
  - **Acceptance**:
    - Queries persist across app restarts
    - Cached data shows instantly on app launch
    - Background refetch when online
- [ ] **E4**: Implement Offline Support (2 pts)
  - Network status detection (NetInfo listener)
  - Show "You're offline" banner when disconnected
  - Queue mutations when offline (store in AsyncStorage)
  - Sync queue when back online (background task)
  - **Acceptance**:
    - When offline, app shows cached data + banner
    - Retry button on error
    - Graceful degradation (hide "Enroll" button, "Complete Lesson" button when offline)
    - Show "Syncing..." indicator when back online
    - Handle conflicts (server changed data while offline)
- [ ] **E5**: Add Offline-First Features (2 pts)
  - Download lessons for offline viewing (AsyncStorage + FileSystem)
  - Download images with react-native-fast-image
  - Show download button on lesson cards
  - Progress indicator during download
  - **Acceptance**:
    - Lessons viewable offline after download
    - Images cached and load instantly
    - "Downloaded" badge on lesson cards
    - Delete download option
    - Storage usage display in settings

### Epic F: Testing & Performance (6 pts) - **NEW**

- [ ] **F1**: Unit tests for auth store (Zustand) (1 pt)
  - **Acceptance**:
    - Test login/logout/register actions
    - Test token persistence
    - Test auto-login logic
    - Coverage ≥90% for auth store
- [ ] **F2**: Unit tests for API client interceptors (1 pt)
  - **Acceptance**:
    - Test Authorization header injection
    - Test 401 token refresh flow
    - Test network error retry logic
    - Test timeout handling
    - Coverage ≥90% for API client
- [ ] **F3**: Integration tests for auth flow (1.5 pts)
  - **Acceptance**:
    - Test full login flow (input → API → store → navigation)
    - Test register flow
    - Test logout flow
    - Test token expiry scenario
    - Use `@testing-library/react-native`
- [ ] **F4**: Snapshot tests for core components (1 pt)
  - **Acceptance**:
    - Snapshots for CourseCard, LessonItem, StatsCard, Header
    - Snapshots for loading states
    - Snapshots for error states
- [ ] **F5**: Coverage verification + Performance profiling (1.5 pts)
  - **Acceptance**:
    - Coverage ≥60% global, ≥80% services
    - App launches in <3 seconds (iPhone 12)
    - 60fps scrolling on course list (100+ items)
    - Memory usage <200MB (profiled with Flipper)
    - Bundle size <50MB (release build)
    - Document results in `docs/implement/sprint-4/performance-report.md`

## 📅 Timeline (18 Days)

| Day           | Focus                   | Tasks                                     | Points  |
| :------------ | :---------------------- | :---------------------------------------- | :------ |
| **Day 1-2**   | Project Setup           | A1-A8 (Setup complete)                    | 7 pts   |
| **Day 3-4**   | Auth Screens            | B1-B3 (Login + Register)                  | 3 pts   |
| **Day 5-6**   | Token Management        | B4-B7 (Token refresh + biometric)         | 5 pts   |
| **Day 7-8**   | Navigation              | C1-C4 (Navigation complete)               | 4 pts   |
| **Day 9-10**  | Home & Courses          | D1-D2 (Dashboard + Course list)           | 3.5 pts |
| **Day 11-12** | Course Details          | D3-D4 (Detail + Lesson viewer)            | 4 pts   |
| **Day 13-14** | Profile & Notifications | D5-D6 (Profile + Push notifications)      | 2.5 pts |
| **Day 15-16** | Progress & Offline      | E1-E5 (Progress screen + offline support) | 8 pts   |
| **Day 17-18** | Testing & QA            | F1-F5 (Tests + performance + bug fixes)   | 6 pts   |

**Total**: 43 points over 18 days = **2.4 pts/day** (sustainable velocity)

## ✅ Definition of Done

### Code Quality

- [ ] Code compiles without errors (`npm run build` passes)
- [ ] ESLint passes with no warnings (`npm run lint`)
- [ ] Prettier formatting applied to all files
- [ ] TypeScript strict mode enabled, no `any` types
- [ ] No console.log statements in production code

### Testing

- [ ] **Unit tests** written for critical utilities/components:
  - Coverage ≥ 60% global
  - Coverage ≥ 80% for services/hooks (auth, API client, stores)
  - Tests for: `AuthProvider`, API client interceptors, token management, Login/Register form validation
- [ ] **Integration tests** for critical user flows:
  - Auth flow (login, register, logout, token expiry)
  - Course enrollment flow
  - Lesson completion flow
- [ ] **Snapshot tests** for core components
- [ ] All tests pass (`npm test`)

### Manual Testing Checklist

- [ ] **Authentication**:
  - Login with valid credentials → Success
  - Login with invalid credentials → Error message
  - Register with valid data → Auto-login → Navigate to Home
  - Register with invalid data → Validation errors
  - Token expiry → Refresh token → Continue session
  - Refresh token expiry → Logout → Redirect to Login
  - Logout → Clear AsyncStorage → Redirect to Login
  - Biometric login (if enabled) → Success/Fallback
- [ ] **Navigation**:
  - Splash screen → Auth gate → Tabs
  - Tab navigation (Home, Courses, Progress, Profile)
  - Stack navigation (Course detail → Lesson viewer)
  - Back button behavior
  - Deep linking from notifications
- [ ] **Offline Mode**:
  - Disconnect network → Show "You're offline" banner
  - Cached data displays instantly
  - Retry button on error
  - Reconnect network → Sync queue → Remove banner
  - Downloaded lessons viewable offline
- [ ] **Loading/Error States**:
  - Skeleton loading for all screens
  - Error states with retry button
  - Empty states ("No courses found")
  - Pull-to-refresh works
- [ ] **Form Validation**:
  - Email format validation
  - Password rules (min 8 chars)
  - Password confirmation match
  - Real-time validation feedback
- [ ] **Responsive Design**:
  - Test on multiple screen sizes (iPhone SE, iPhone 12, iPad)
  - Landscape mode support
  - Safe area insets respected

### Performance

- [ ] App launches in <3 seconds (iPhone 12)
- [ ] 60fps scrolling on course list (100+ items)
- [ ] Images lazy-load with react-native-fast-image
- [ ] Bundle size <50MB (release build)
- [ ] Memory usage <200MB (profiled with Flipper)
- [ ] No memory leaks (tested with Flipper)
- [ ] Smooth animations (tab transitions, modal slides)

### Documentation

- [ ] README.md updated with setup instructions
- [ ] API integration documented
- [ ] Offline strategy documented in `docs/implement/sprint-4/offline-strategy.md`
- [ ] Performance report in `docs/implement/sprint-4/performance-report.md`
- [ ] Session summaries created for each development session

### Platform Support

- [ ] App runs on iOS Simulator (tested on iOS 15+)
- [ ] App runs on Android Emulator (tested on Android 10+)
- [ ] No platform-specific crashes
- [ ] Push notifications work on both platforms
