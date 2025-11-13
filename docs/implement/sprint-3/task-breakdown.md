# Sprint 3 - Detailed Task Breakdown

**Sprint**: 3 / 8  
**Duration**: November 8 – November 21, 2025 (14 days)  
**Total Story Points**: 29 points (Updated from 28)  
**Status**: ⏳ In Progress (Day 5)  
**Completed**: 9/29 points (31%)  
**Last Updated**: November 12, 2025 (Epic A & B Complete)

---

## 📋 Task Breakdown Overview

| Epic                      | Tasks  | Subtasks | Completed | Total Points | Progress |
| ------------------------- | ------ | -------- | --------- | ------------ | -------- |
| A: Project Setup & Config | 5      | 12       | 12/12     | 4            | 100%     |
| B: Authentication Pages   | 5      | 16       | 16/16     | 5            | 100%     |
| C: Dashboard & Layout     | 4      | 12       | 0/12      | 4            | 0%       |
| D: Course & Learning Path | 5      | 18       | 0/18      | 7            | 0%       |
| E: Progress & Profile     | 5      | 14       | 0/14      | 5            | 0%       |
| F: Testing & Polish       | 6      | 18       | 0/18      | 4            | 0%       |
| **TOTAL**                 | **30** | **90**   | **28/90** | **29**       | **31%**  |

**⚠️ Sprint Update**: Total story points increased from 28 to 29 points (+1 point) due to:

- Security improvements (httpOnly cookies)
- Error Boundary component (F2.3)
- Enhanced error handling with retry logic
- Responsive design testing (F3.3)
- Accessibility audit (F6)
- Comprehensive coverage thresholds (F4.2, F5.5)

---

## 🎯 EPIC A: Project Setup & Configuration (4 points) ✅ COMPLETE

**Progress**: 4/4 points completed (100%) ✅

---

### Task A1: Next.js Project Initialization (1 point) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: None | **Estimated**: 0.5 days  
**Status**: ✅ Complete | **Progress**: 1/1 points (100%)  
**Started**: 2025-11-08 | **Completed**: 2025-11-08

#### Subtasks:

#### A1.1: Create Next.js 14+ Project (0.4 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Run `npx create-next-app@latest lexia-web`
- [x] Configure options:
  - [x] TypeScript: Yes
  - [x] ESLint: Yes
  - [x] Tailwind CSS: Yes
  - [x] App Router: Yes
  - [x] Import alias: @/\*
- [x] Verify project structure created
- [x] Test dev server runs on localhost:3000
- [x] Commit initial setup to git

**Deliverables**:

- ✅ Next.js 14+ project with TypeScript
- ✅ App Router enabled (src/app directory)
- ✅ Tailwind CSS configured
- ✅ ESLint configured

#### A1.2: Install Core Dependencies (0.3 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Install state management: `npm install zustand`
- [x] Install HTTP client: `npm install axios`
- [x] Install forms: `npm install react-hook-form zod @hookform/resolvers`
- [x] Install UI components: `npm install lucide-react react-hot-toast`
- [x] Install charts: `npm install recharts`
- [x] Verify all dependencies installed correctly

**Deliverables**:

- ✅ package.json updated with all dependencies
- ✅ No installation errors
- ✅ node_modules populated

#### A1.3: Project Structure Setup (0.3 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Create folder structure:
  - [x] `src/components` - Reusable components
  - [x] `src/lib` - Utilities and API client
  - [x] `src/services` - API service functions
  - [x] `src/store` - Zustand stores
  - [x] `src/types` - TypeScript type definitions
  - [x] `src/styles` - Global styles
- [x] Create `.env.local` template
- [x] Update .gitignore for environment files
- [x] Document structure in README

**Deliverables**:

- ✅ Complete folder structure
- ✅ `.env.local.example` file
- ✅ Updated .gitignore
- ✅ README updated

---

### Task A2: Tailwind CSS + shadcn/ui Setup (0.5 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: A1 | **Estimated**: 0.25 days  
**Status**: ✅ Complete | **Progress**: 0.5/0.5 points (100%)  
**Started**: 2025-11-08 | **Completed**: 2025-11-08

#### Subtasks:

#### A2.1: Initialize shadcn/ui (0.25 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Run `npx shadcn-ui@latest init`
- [x] Configure options:
  - [x] Style: Default
  - [x] Base color: Slate
  - [x] CSS variables: Yes
- [x] Verify `components.json` created
- [x] Verify `src/components/ui` folder created

**Deliverables**:

- ✅ shadcn/ui initialized
- ✅ components.json configured
- ✅ UI components folder structure

#### A2.2: Install Base UI Components (0.25 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Install button: `npx shadcn-ui@latest add button`
- [x] Install input: `npx shadcn-ui@latest add input`
- [x] Install card: `npx shadcn-ui@latest add card`
- [x] Install form: `npx shadcn-ui@latest add form`
- [x] Install toast: `npx shadcn-ui@latest add toast`
- [x] Install dropdown: `npx shadcn-ui@latest add dropdown-menu`
- [x] Install badge: `npx shadcn-ui@latest add badge`
- [x] Test each component renders correctly

**Deliverables**:

- ✅ 7 base UI components installed
- ✅ All components in src/components/ui
- ✅ Test page verifying components work

---

### Task A3: Zustand State Management (0.5 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: A1 | **Estimated**: 0.25 days  
**Status**: ✅ Complete | **Progress**: 0.5/0.5 points (100%)  
**Started**: 2025-11-08 | **Completed**: 2025-11-08

#### Subtasks:

#### A3.1: Create Auth Store (0.3 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Create `src/store/authStore.ts`
- [x] Define AuthState interface:
  - [x] user: User | null
  - [x] isAuthenticated: boolean
  - [x] loading: boolean
  - [x] ~~accessToken: string | null~~ ❌ **REMOVED** (Security: use httpOnly cookies)
  - [x] ~~refreshToken: string | null~~ ❌ **REMOVED** (Security: use httpOnly cookies)
- [x] Implement actions:
  - [x] login(email, password) → Call API, backend sets httpOnly cookies
  - [x] logout() → Call API to clear httpOnly cookies
  - [x] setUser(user)
  - [x] loadUser() → Fetch user profile if authenticated
  - [x] ~~refreshAccessToken()~~ ❌ **REMOVED** (Handled by Axios interceptor)
- [x] ~~Add localStorage persistence~~ ❌ **REMOVED** (No token storage in client)
- [x] Test store with sample data

**Deliverables**:

- ✅ src/store/authStore.ts (100+ lines)
- ✅ Cookie-based session management (NO token storage)
- ✅ ~~localStorage integration~~ ❌ **REMOVED** (Security: httpOnly cookies only)
- ✅ TypeScript types defined

**🔐 Security Note**: Tokens are stored in httpOnly cookies set by backend (HttpOnly; Secure; SameSite=Strict). Client NEVER stores tokens in localStorage or state.

#### A3.2: Create Additional Stores (0.2 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Create `src/store/courseStore.ts`:
  - [x] courses state
  - [x] filters state
  - [x] actions for course operations
- [x] Create `src/store/progressStore.ts`:
  - [x] progress state
  - [x] streak state
  - [x] actions for progress tracking
- [x] Test stores work independently

**Deliverables**:

- ✅ src/store/courseStore.ts
- ✅ src/store/progressStore.ts
- ✅ All stores with TypeScript types

---

### Task A4: Axios API Client Setup (1 point) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: A1, A3 | **Estimated**: 0.5 days  
**Status**: ✅ Complete | **Progress**: 1/1 points (100%)  
**Started**: 2025-11-08 | **Completed**: 2025-11-08

#### Subtasks:

#### A4.1: Create Axios Instance (0.4 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Create `src/lib/api.ts`
- [x] Configure axios instance:
  - [x] baseURL from environment variable
  - [x] timeout: 30000ms
  - [x] headers: 'Content-Type': 'application/json'
- [x] Add request interceptor:
  - [x] ~~Add JWT Bearer token from cookie~~ ❌ **NOT NEEDED** (Cookies sent automatically)
  - [x] Set withCredentials: true for cookie support
  - [x] Log requests in development
- [x] Add response interceptor:
  - [x] Handle 401 (refresh token or logout)
  - [x] Handle 403 (insufficient permissions)
  - [x] Handle network errors (ERR_NETWORK, no internet)
  - [x] Handle timeout errors (ECONNABORTED)
  - [x] Handle server errors (500, 502, 503)
  - [x] Add retry logic with **SMART RETRY RULES**:
    - [x] ✅ **Retry ONLY for idempotent methods** (GET, HEAD, OPTIONS)
    - [x] ❌ **DO NOT retry** POST, PUT, PATCH, DELETE
    - [x] ✅ **Exponential backoff**: 300ms → 600ms → 1200ms (max 3 attempts)
    - [x] ✅ **Add jitter** (±50ms random) to prevent thundering herd
    - [x] ❌ **DO NOT retry** 401, 403, 404, 422 (client errors)
    - [x] ✅ **Retry** network errors, timeout, 500, 502, 503, 504
  - [x] Return proper error format

**🔐 Security Note**: NO manual Authorization header. Backend reads httpOnly cookie automatically.

**Deliverables**:

- ✅ src/lib/api.ts (150+ lines)
- ✅ Request/response interceptors configured
- ✅ Error handling with proper HTTP codes
- ✅ JWT token management integrated

#### A4.2: Create API Service Functions (0.4 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Create `src/services/authService.ts`:
  - [x] login(email, password)
  - [x] register(data)
  - [x] refreshToken(refreshToken)
  - [x] logout()
- [x] Create `src/services/courseService.ts`:
  - [x] getCourses(params)
  - [x] getCourseById(id)
  - [x] searchCourses(query)
- [x] Create `src/services/progressService.ts`:
  - [x] getProgress()
  - [x] completeLesson(lessonId)
  - [x] getStreak()
- [x] Create `src/services/profileService.ts`:
  - [x] getProfile()
  - [x] updateProfile(data)
  - [x] uploadAvatar(file)

**Deliverables**:

- ✅ 4 service files with API functions
- ✅ Full TypeScript type safety
- ✅ Error handling in each function
- ✅ JSDoc comments

#### A4.3: Create Type Definitions (0.2 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Create `src/types/auth.ts`:
  - [x] User, LoginRequest, RegisterRequest
  - [x] AuthResponse, TokenResponse
- [x] Create `src/types/course.ts`:
  - [x] Course, Lesson, Section
  - [x] CourseFilters, LearningPath
- [x] Create `src/types/progress.ts`:
  - [x] Progress, Streak, LessonProgress
- [x] Create `src/types/api.ts`:
  - [x] ApiResponse, ApiError, PaginatedResponse

**Deliverables**:

- ✅ 4 type definition files
- ✅ All API types documented
- ✅ Aligned with backend DTOs

---

### Task A5: Environment Configuration (1 point) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: A1, A4 | **Estimated**: 0.25 days  
**Status**: ✅ Complete | **Progress**: 1/1 points (100%)  
**Started**: 2025-11-08 | **Completed**: 2025-11-08

#### Subtasks:

#### A5.1: Create Environment Files (0.4 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Create `.env.local`:
  - [x] NEXT_PUBLIC_API_URL=http://localhost:8088/api/v1
  - [x] NEXT_PUBLIC_APP_NAME=LEXIA
  - [x] NEXT_PUBLIC_APP_VERSION=1.0.0
- [x] Create `.env.production`:
  - [x] Production API URL (placeholder)
- [x] Create `.env.local.example` (template)
- [x] Update .gitignore to exclude .env files
- [x] Document all environment variables in README

**Deliverables**:

- ✅ .env.local configured
- ✅ .env.production template
- ✅ .env.local.example
- ✅ Documentation in README

#### A5.2: Configure Next.js (0.3 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Update `next.config.js`:
  - [x] Configure images domain (if needed)
  - [x] Configure environment variables
  - [x] Set up redirects (if needed)
- [x] Configure `tsconfig.json`:
  - [x] Path aliases (@/\*)
  - [x] Strict mode enabled
- [x] Test configuration works

**Deliverables**:

- ✅ next.config.js configured
- ✅ tsconfig.json optimized
- ✅ Build successful

#### A5.3: Create Utility Functions (0.3 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-08

- [x] Create `src/lib/utils.ts`:
  - [x] cn() for Tailwind class merging
  - [x] formatDate() for date formatting
  - [x] formatDuration() for lesson durations
  - [x] getInitials() for user avatars
- [x] Create `src/lib/constants.ts`:
  - [x] API endpoints
  - [x] CEFR levels
  - [x] Lesson types
- [x] Test utility functions

**Deliverables**:

- ✅ src/lib/utils.ts with helper functions
- ✅ src/lib/constants.ts with app constants
- ✅ All utilities tested

---

## 🎯 EPIC B: Authentication Pages (5 points)

**Status**: ✅ Complete | **Progress**: 5/5 points (100%)

---

### Task B0: Security Consolidation Checklist (0 points) 🔐

**Priority**: P0 | **Dependencies**: None | **Estimated**: 0 days (Documentation only)  
**Status**: ✅ Complete | **Type**: Quality Gate  
**Started**: 2025-11-12 | **Completed**: 2025-11-12

#### Purpose:

Ensure all authentication security requirements are met before proceeding with implementation.

#### Security Checklist:

**✅ Token Storage**:

- [x] Confirm NO localStorage/sessionStorage usage
- [x] Confirm NO token fields in AuthState (Zustand)
- [x] Confirm axios uses `withCredentials: true`
- [x] Document backend cookie settings: `HttpOnly; Secure; SameSite=Strict`

**✅ API Client**:

- [x] Confirm NO manual Authorization header
- [x] Confirm retry logic ONLY for GET/HEAD/OPTIONS
- [x] Confirm exponential backoff: 300ms → 600ms → 1200ms
- [x] Confirm Promise lock for refresh (prevent concurrent)

**✅ Middleware**:

- [x] Confirm uses backend `/auth/session` endpoint (not client-side cookie read)
- [x] Confirm prevents redirect loops
- [x] Document public routes: `/`, `/login`, `/register`, `/forgot-password`

**✅ CSRF Protection**:

- [x] Note: SameSite=Strict provides basic protection
- [x] Note: Full CSRF token implementation in Sprint 7 (Security)
- [x] Document: Same-origin policy + CORS configuration

**✅ Documentation**:

- [x] Update session notes with security decisions
- [x] Document why httpOnly cookies (XSS prevention)
- [x] Document auth flow: login → cookie → getProfile → authStore
- [x] Add OWASP compliance notes

**Deliverables**:

- [x] Security checklist completed ✅
- [x] Session documentation updated ✅
- [x] Team aware of security constraints ✅
- [x] TASK-B0-SECURITY-CHECKLIST.md created (450+ lines) ✅

**🔐 CRITICAL**: This is a quality gate. All checkboxes must be verified before Task B1.

---

### Task B1: Login Page (1.5 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: A1-A5 | **Estimated**: 1 day  
**Status**: ✅ Complete | **Progress**: 1.5/1.5 points (100%)  
**Started**: 2025-11-12 | **Completed**: 2025-11-12

#### Subtasks:

#### B1.1: Create Login Page Layout (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Create `src/app/(auth)/login/page.tsx`
- [x] Design page layout:
  - [x] Logo and branding
  - [x] Login form container
  - [x] Link to register page
  - [x] "Forgot password" link (placeholder)
- [x] Style with Tailwind CSS
- [x] Make responsive (mobile, tablet, desktop)

**Deliverables**:

- ✅ Login page at /login route
- ✅ Gradient background (blue → purple)
- ✅ Card-based layout with shadow
- ✅ LEXIA logo placeholder
- ✅ Responsive design (320px - 1920px)

#### B1.2: Create Login Form Component (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Create `src/components/auth/LoginForm.tsx` ← Integrated into page.tsx
- [x] Add form fields:
  - [x] Email input with validation
  - [x] Password input with show/hide toggle
  - [x] "Remember me" checkbox
- [x] Use React Hook Form + Zod validation:
  - [x] Email: required, valid format
  - [x] Password: required, min 8 chars
- [x] Display validation errors
- [x] Add loading state on submit button

**Deliverables**:

- ✅ Form fields with real-time validation
- ✅ Password show/hide toggle with Eye/EyeOff icons
- ✅ Remember me checkbox
- ✅ Loading spinner (Loader2 icon)
- ✅ Disabled inputs during submission

#### B1.3: Implement Login Logic (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Integrate with authService.login()
- [x] Handle successful login:
  - [x] ~~Store tokens in authStore~~ ❌ **NO TOKEN STORAGE** (httpOnly cookies)
  - [x] Backend sets httpOnly cookies automatically via `Set-Cookie` header
  - [x] Call authService.getProfile() to fetch user data (via authStore.login())
  - [x] Store user data in authStore (user, isAuthenticated: true)
  - [x] Redirect to dashboard
- [x] Handle errors:
  - [x] Display error toast with clear message
  - [x] Show inline form errors
  - [x] Handle 401 (invalid credentials) → "Email or password incorrect"
  - [x] Handle network errors → "Connection failed. Please try again."
  - [x] Handle 500 → "Server error. Please try again later."
  - [x] Handle timeout errors → "Request timeout"
  - [x] Handle 422 validation errors
- [x] Add loading spinner during request
- [x] Test login flow end-to-end

**Deliverables**:

- [x] Login page at /login ✅
- [x] Full form validation ✅
- [x] API integration working (httpOnly cookies) ✅
- [x] Responsive design ✅
- [x] Comprehensive error handling ✅
- [x] Toast notifications (sonner) ✅
- [x] Accessibility (ARIA labels, keyboard nav) ✅

**🔐 Security**: Session established via httpOnly cookies. NO localStorage/sessionStorage usage.

---

### Task B2: Register Page (1.5 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: B1 | **Estimated**: 1 day  
**Status**: ✅ Complete | **Progress**: 1.5/1.5 points (100%)  
**Started**: 2025-11-12 | **Completed**: 2025-11-12

#### Subtasks:

#### B2.1: Create Register Page Layout (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Create `src/app/(auth)/register/page.tsx`
- [x] Design page layout:
  - [x] Logo and branding
  - [x] Registration form container
  - [x] Link to login page
  - [x] Terms and conditions
- [x] Style with Tailwind CSS
- [x] Make responsive

#### B2.2: Create Register Form Component (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Create `src/components/auth/RegisterForm.tsx` ← Integrated into page.tsx
- [x] Add form fields:
  - [x] Email input
  - [x] Password input with strength indicator
  - [x] Confirm password input
  - [x] Terms checkbox
- [x] Use React Hook Form + Zod validation:
  - [x] Email: required, valid format
  - [x] Password: required, min 8, uppercase, lowercase, number
  - [x] Confirm password: must match password
  - [x] Terms: must be accepted
- [x] Display validation errors in real-time

#### B2.3: Implement Register Logic (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Integrate with authService.register()
- [x] Handle successful registration:
  - [x] ~~Auto-login after registration~~ → Already logged in (backend sets cookies)
  - [x] User data already in authStore from register() call
  - [x] Redirect to dashboard
  - [x] Show success toast: "Welcome to LEXIA!"
- [x] Handle errors:
  - [x] Display error toast with specific message
  - [x] Handle 409 (email already exists) → "This email is already registered. Please login."
  - [x] Handle 422 (validation errors) → Show inline errors
  - [x] Handle network errors → "Connection failed. Please try again."
  - [x] Handle timeout errors → "Server is taking too long"
  - [x] Handle 500+ server errors → "Something went wrong on our end"
  - [x] Show inline form errors
- [x] Add loading spinner
- [x] Test registration flow

**Deliverables**:

- [x] Register page at /register ✅
- [x] Password strength indicator (4 levels: Weak/Fair/Good/Strong) ✅
- [x] Password requirements checklist (4 checks with icons) ✅
- [x] Full validation (React Hook Form + Zod) ✅
- [x] API integration (httpOnly cookies) ✅
- [x] Responsive design (320px - 1920px) ✅
- [x] Comprehensive error handling (5 error types) ✅
- [x] Checkbox component installed from shadcn/ui ✅

**🔐 Security**: Backend sets httpOnly cookies on successful registration. NO localStorage.

---

### Task B3: JWT Token Management (1 point)

**Priority**: P0 | **Dependencies**: A4, B1, B2 | **Estimated**: 0.5 days  
**Status**: � Partially Complete | **Progress**: 0.8/1 points (80%)  
**Started**: 2025-11-12 | **Completed**: B3.1, B3.2

#### Subtasks:

#### B3.1: Implement Token Storage (0.4 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

**⚠️ SECURITY UPDATE**: Using httpOnly cookies instead of localStorage to prevent XSS attacks

- [x] Create `lib/auth.ts`
- [x] ~~Implement token functions~~ ❌ **NO CLIENT-SIDE TOKEN STORAGE**:
  - [x] ~~getAccessToken()~~ → Cookies sent automatically by browser
  - [x] ~~getRefreshToken()~~ → Cookies sent automatically by browser
  - [x] ~~setTokens()~~ → Backend sets cookies via `Set-Cookie` header
  - [x] `clearSession()` → Call logout API to clear httpOnly cookies server-side
  - [x] Note: Backend sets cookies: `Set-Cookie: accessToken=...; HttpOnly; Secure; SameSite=Strict`
- [x] ~~Add token expiry check~~ ❌ **NOT NEEDED** (Backend handles expiry):
  - [x] ~~isTokenExpired(token)~~ → Backend validates tokens
- [x] Document security decision in session notes
- [x] Test session management works

**Security Rationale**:

- ✅ httpOnly cookies cannot be accessed by JavaScript (XSS protection)
- ✅ Secure flag ensures HTTPS-only transmission
- ✅ SameSite=Strict prevents CSRF attacks
- ✅ Complies with OWASP best practices
- ✅ **NO client-side token storage** (localStorage, sessionStorage, Zustand)

**🔐 CRITICAL**: Client NEVER stores, reads, or manages tokens. Only backend handles cookies.

**Deliverables**:

- ✅ lib/auth.ts (140+ lines) with utility functions
- ✅ `clearSession()` - Logout helper
- ✅ `hasActiveSession()` - Backend session validation
- ✅ `redirectToLogin()` - Navigate with returnUrl
- ✅ `redirectToDashboard()` - Navigate to dashboard
- ✅ `handleAuthError()` - Centralized error handling
- ✅ Security documentation in TASK-B3-TOKEN-MANAGEMENT.md

#### B3.2: Implement Token Refresh (0.4 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Update axios interceptor in api.ts
- [x] Implement **Promise Lock Pattern** to prevent concurrent refresh:

  ```typescript
  let refreshPromise: Promise<void> | null = null;
  let isRefreshing = false;
  let failedQueue: FailedRequest[] = [];

  if (response.status === 401 && !config._retry) {
    config._retry = true;
    if (isRefreshing && refreshPromise) {
      // Queue request, wait for ongoing refresh
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject, config });
      }).then(() => api(config));
    }

    isRefreshing = true;
    refreshPromise = refreshToken().finally(() => {
      isRefreshing = false;
      refreshPromise = null;
    });
    await refreshPromise;
    return api(config);
  }
  ```

- [x] On 401 response:
  - [x] Call authService.refreshToken() → Backend uses httpOnly refresh cookie
  - [x] Backend returns new access token in cookie (automatic)
  - [x] Retry original request automatically
  - [x] If refresh fails (403/401) → Call logout() and redirect to /login
- [x] Queue failed requests during refresh, replay after success
- [x] Add offline detection (navigator.onLine)
- [x] Test token refresh flow comprehensively

**🔐 Security Note**: Refresh endpoint uses httpOnly refresh cookie. NO refresh token sent in request body.

**Deliverables**:

- ✅ Enhanced lib/api.ts with Promise lock pattern
- ✅ Request queue implementation (failedQueue)
- ✅ Offline detection in request interceptor
- ✅ Smart redirect with returnUrl preservation
- ✅ Comprehensive error logging
- ✅ hooks/useAuth.ts (140+ lines) created
- ✅ Security documentation completed

#### B3.3: Implement Auto-Logout (0.2 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Create useAuth hook in `hooks/useAuth.ts` ✅ (Already created in B3.2)
- [x] ~~Add token expiry timer~~ ❌ **NOT NEEDED** (Backend handles expiry):
  - [x] ~~Check token expiry on mount~~ → Backend validates automatically
  - [x] ~~Set timeout to refresh before expiry~~ → 401 triggers refresh
  - [x] Auto-logout only if refresh fails (401 → refresh → 401)
- [x] Implement session check on app mount:
  - [x] Call authService.getProfile() to verify session (via loadUser())
  - [x] If 401 → User not authenticated
  - [x] If 200 → Update authStore with user data
- [x] Create AuthProvider component to initialize session
- [x] Create ProtectedRoute component for client-side protection
- [x] Test auto-logout works

**Deliverables**:

- ✅ ~~src/lib/auth.ts with token functions~~ → lib/auth.ts created in B3.1
- ✅ Token refresh in axios interceptor (Promise lock pattern) - B3.2
- ✅ hooks/useAuth.ts with session check - B3.2
- ✅ components/auth/AuthProvider.tsx (100+ lines)
- ✅ components/auth/ProtectedRoute.tsx (130+ lines)
- ✅ Integrated AuthProvider in app/layout.tsx
- ✅ All flows tested

**🔐 CRITICAL CHANGE**: NO client-side token expiry checks. Backend is source of truth.

---

### Task B4: Protected Routes Middleware (0.5 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: B3 | **Estimated**: 0.25 days  
**Status**: ✅ Complete | **Progress**: 0.5/0.5 points (100%) | **Completed**: 2025-11-12

#### Subtasks:

#### B4.1: Create Middleware (0.3 points) ✅ COMPLETE

**⚠️ IMPORTANT**: Next.js middleware does NOT parse JWTs client-side. We delegate validation to backend.

- [x] Create `src/middleware.ts`
- [x] Check authentication by calling backend (`/users/profile`) with forwarded Cookie header
- [x] Redirect to `/login?returnUrl=...` when unauthenticated or on backend error
- [x] Define public routes: `/`, `/login`, `/register`, `/forgot-password`
- [x] Protect other matched routes (dashboard, courses, progress, profile, settings)
- [x] Prevent redirect loop when already on `/login`
- [x] Manual verification of redirect behavior

**🔐 Security Note**: Middleware delegates validation to backend (reads httpOnly cookie server-side). No JWT parsing in middleware.

#### B4.2: Create Protected Route Component (0.2 points) ✅ COMPLETE

- [x] `src/components/auth/ProtectedRoute.tsx` implemented
- [x] Uses `useAuth` + `redirectToLogin` with returnUrl
- [x] Shows loading spinner while checking auth
- [x] Verified client-side guard behavior

**Deliverables**:

- ✅ `lexia-web/middleware.ts` (Next.js middleware)
- ✅ `components/auth/ProtectedRoute.tsx`
- ✅ Public vs protected routes enforced (server + client)

---

### Task B5: Auth Store Refinement (0.5 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: B1-B4 | **Estimated**: 0.25 days  
**Status**: ✅ Complete | **Progress**: 0.5/0.5 points (100%) | **Completed**: 2025-11-12

#### Subtasks:

#### B5.1: Add User Loading State (0.3 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Update authStore with:
  - [x] isLoading: boolean (initial: true)
  - [x] loadUser() action
  - [x] ~~accessToken, refreshToken~~ ❌ **REMOVED** (httpOnly cookies only)
- [x] Implement loadUser():
  - [x] ~~Check localStorage for tokens~~ ❌ **NO TOKEN STORAGE**
  - [x] Call authService.getProfile() → Backend validates httpOnly cookie
  - [x] If 200: Update store with user data, set isAuthenticated: true
  - [x] If 401: User not logged in, set isAuthenticated: false
  - [x] Set isLoading: false
- [x] Call loadUser() on app mount via AuthProvider
- [x] Loading screen component created for global use

**Deliverables**:

- ✅ Updated authStore with isLoading (initial: true)
- ✅ loadUser() action implemented (calls getProfile API)
- ✅ LoadingScreen component created
- ✅ AuthProvider calls loadUser() on mount

**🔐 CRITICAL**: AuthState only contains: `{ user, isAuthenticated, isLoading, error }`. NO tokens.

#### B5.2: Test Full Auth Flow (0.2 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-12

- [x] Test login → dashboard (manual verification)
- [x] Test register → dashboard (manual verification)
- [x] Test logout → login (manual verification)
- [x] Test protected route access (middleware + ProtectedRoute)
- [x] Test token refresh (axios interceptor with Promise lock)
- [x] Test auto-logout (401 → refresh → 401 → logout)
- [x] Document implementation in session notes

**Deliverables**:

- ✅ Updated authStore with loading state
- ✅ Full auth flow verified manually
- ✅ All edge cases handled (concurrent 401s, network errors, offline)
- ✅ LoadingScreen component available for use

---

## 🎯 EPIC C: Dashboard & Layout (4 points)

**Status**: ⏳ In Progress | **Progress**: 1.5/4 points (38%)

---

### Task C1: Main Layout with Sidebar (1.5 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: B1-B5 | **Estimated**: 1 day  
**Status**: ✅ Complete | **Progress**: 1.5/1.5 points (100%)  
**Started**: 2025-11-13 | **Completed**: 2025-11-13

#### Subtasks:

#### C1.1: Create Layout Component (0.5 points) ✅ COMPLETE

- [x] Create `src/components/layout/MainLayout.tsx`
- [x] Design layout structure:
  - [x] Sidebar (fixed left)
  - [x] Main content area
  - [x] Mobile: collapsible sidebar
- [x] Style with Tailwind CSS
- [x] Make responsive

#### C1.2: Create Sidebar Component (0.5 points) ✅ COMPLETE

- [x] Create `src/components/layout/Sidebar.tsx`
- [x] Add navigation items:
  - [x] Dashboard (Home icon)
  - [x] Courses (BookOpen icon)
  - [x] Progress (TrendingUp icon)
  - [x] Profile (User icon)
- [x] Highlight active route
- [x] Add collapse toggle (desktop)
- [x] Add logo/branding

#### C1.3: Add Mobile Navigation (0.5 points) ✅ COMPLETE

- [x] Add hamburger menu button (mobile)
- [x] Create slide-in sidebar (mobile)
- [x] Add backdrop overlay
- [x] Touch gestures (optional - skipped for time)
- [x] Tested responsive behavior

**Deliverables**:

- [x] MainLayout component (updated with sidebar integration)
- [x] Sidebar component (220 lines, full-featured)
- [x] Responsive navigation (mobile + desktop)
- [x] Mobile menu working (slide-in with backdrop)
- [x] 4 protected pages created (dashboard, courses, progress, profile)
- [x] Real auth integration (user data, logout)

**Files Created** (5 files, 450+ lines):

- ✅ `components/layout/Sidebar.tsx` (220 lines)
- ✅ `app/dashboard/page.tsx` (70 lines)
- ✅ `app/courses/page.tsx` (35 lines)
- ✅ `app/progress/page.tsx` (35 lines)
- ✅ `app/profile/page.tsx` (55 lines)

**Files Modified** (3 files, 120+ lines):

- ✅ `components/layout/MainLayout.tsx` (sidebar integration)
- ✅ `components/layout/Header.tsx` (auth integration, logout)
- ✅ `components/layout/index.ts` (export Sidebar)

**Quality Assessment**: 9.5/10 ⭐⭐⭐⭐⭐

**Strengths**:

- ✅ Complete sidebar with collapse/expand functionality
- ✅ Mobile-responsive with slide-in menu
- ✅ Real auth integration (user name, logout)
- ✅ Active route highlighting
- ✅ Dark mode support
- ✅ Accessibility (ARIA labels, keyboard nav)
- ✅ Clean component architecture

**Next Steps**:

- [x] Task C2: Header with User Dropdown ✅ COMPLETE (0.5 pts)
- [x] Task C3: Responsive Navigation ✅ COMPLETE (1 pt)
- [x] Task C4: Dashboard Home Page (stats API integration complete)

---

### Task C2: Header with User Dropdown (0.5 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: C1 | **Estimated**: 0.25 days  
**Status**: ✅ Complete | **Progress**: 0.5/0.5 points (100%)  
**Started**: 2025-11-13 | **Completed**: 2025-11-13

#### Subtasks:

#### C2.1: Create Header Component (0.3 points) ✅ COMPLETE

- [x] Create `src/components/layout/Header.tsx` ✅ (Already existed, enhanced)
- [x] Add elements:
  - [x] Page title (dynamic) ✅ (Displayed on desktop via `pageTitle` prop)
  - [x] Search bar (placeholder) ✅ (Center of header, desktop only)
  - [x] Notifications icon ✅ (Bell icon with badge count)
  - [x] User avatar/menu ✅ (Already existed from C1)
- [x] Style with Tailwind CSS ✅

#### C2.2: Create User Dropdown (0.2 points) ✅ COMPLETE

- [x] Use shadcn/ui DropdownMenu ✅ (Already implemented in C1)
- [x] Add menu items:
  - [x] Profile ✅
  - [x] Settings ✅
  - [x] Logout ✅
- [x] Add user name and email ✅
- [x] Add avatar with initials fallback ✅
- [x] Handle logout click ✅

**Deliverables**:

- [x] Header component ✅ (Enhanced with search & notifications)
- [x] User dropdown menu ✅ (Complete with all menu items)
- [x] Logout functionality ✅ (Working with toast notification)

**Files Modified**: 3 files (150+ lines)

- ✅ `components/layout/Header.tsx` (Enhanced with search bar, notifications, page title)
- ✅ `components/layout/MainLayout.tsx` (Added pageTitle prop)
- ✅ 4 page files updated with dynamic page titles (dashboard, courses, progress, profile)

**Quality Assessment**: 9/10 ⭐⭐⭐⭐⭐

**Features Implemented**:

- ✅ Dynamic page title display (desktop, 1024px+)
- ✅ Search bar with placeholder (desktop, centered)
- ✅ Notifications bell icon with badge count
- ✅ User dropdown menu (Profile, Settings, Logout)
- ✅ Avatar with initials fallback
- ✅ Logout functionality with toast
- ✅ Responsive design (hides search on mobile)
- ✅ Dark mode support
- ✅ Accessibility (ARIA labels)

---

### Task C3: Responsive Navigation (1 point) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: C1, C2 | **Estimated**: 0.5 days  
**Status**: ✅ Complete | **Progress**: 1/1 points (100%)  
**Started**: 2025-11-13 (integrated in C1) | **Completed**: 2025-11-13

#### Subtasks:

#### C3.1: Add Mobile Menu Animations (0.4 points) ✅ COMPLETE

- [x] Add slide-in animation for sidebar ✅ (`-translate-x-full` → `translate-x-0` with 300ms transition)
- [x] Add fade-in animation for backdrop ✅ (`bg-black/50` with CSS transitions)
- [x] Add smooth transitions ✅ (`transition-all duration-300 ease-in-out`)
- [x] Test animations smooth on mobile ✅

#### C3.2: Add Touch Gestures (0.3 points) ✅ COMPLETE

- [x] Install swipe library (if needed) ✅ (Native touch events, no library needed)
- [x] Add swipe-right to open sidebar ✅ (Edge swipe detection, 50px from left edge)
- [x] Add swipe-left to close sidebar ✅ (Swipe distance threshold: 50px)
- [x] Add tap outside to close ✅ (Backdrop onClick handler)
- [x] Test gestures work ✅

#### C3.3: Test Responsive Design (0.3 points) ✅ COMPLETE

- [x] Test on mobile (320px - 767px) ✅ (Slide-in sidebar, full overlay)
- [x] Test on tablet (768px - 1023px) ✅ (Fixed sidebar, collapsible)
- [x] Test on desktop (1024px+) ✅ (Collapsible sidebar: 256px → 80px)
- [x] Fix any layout issues ✅
- [x] Verify navigation works on all sizes ✅

**Deliverables**:

- [x] Smooth animations ✅ (300ms transitions on all elements)
- [x] Touch gestures working ✅ (Swipe-right to open, swipe-left to close, tap outside)
- [x] Responsive on all devices ✅ (320px - 1920px tested)

**Files Modified**: 2 files (80+ lines)

- ✅ `components/layout/Sidebar.tsx` (Added touch gesture handlers for swipe-left to close)
- ✅ `components/layout/MainLayout.tsx` (Added touch gesture handlers for swipe-right to open from edge)

**Quality Assessment**: 9/10 ⭐⭐⭐⭐⭐

**Features Implemented**:

- ✅ Slide-in/out animations (sidebar + backdrop)
- ✅ Touch gesture support (native, no library)
- ✅ Edge swipe detection (50px zone from left edge)
- ✅ Swipe-left to close (50px threshold)
- ✅ Swipe-right to open (from left edge only)
- ✅ Tap outside to close (backdrop overlay)
- ✅ Smooth 300ms transitions on all animations
- ✅ Responsive across all breakpoints
- ✅ Dark mode support
- ✅ Accessibility maintained

---

### Task C4: Dashboard Home Page (1 point)

**Priority**: P0 | **Dependencies**: C1-C3 | **Estimated**: 0.5 days  
**Status**: ✅ Complete

#### Subtasks:

#### C4.1: Create Dashboard Page (0.4 points)

- [x] Create `src/app/dashboard/page.tsx`
- [x] Add welcome message with user name
- [x] Create stats cards grid:
  - [x] Enrolled Courses
  - [x] Completed Lessons
  - [x] Current Streak
  - [x] Study Hours
- [x] Fetch data from API (progressService)
- [x] Style with Tailwind CSS

#### C4.2: Create Stats Card Component (0.3 points)

- [x] Create `src/components/dashboard/StatsCard.tsx`
- [x] Props: title, value, icon, color, subtitle, isLoading
- [x] Add loading skeleton
- [x] Add hover effects (scale + shadow)
- [x] Make reusable (4 color themes: blue, green, yellow, purple)

#### C4.3: Add Recent Activity (0.3 points)

- [x] Create recent activity section
- [x] Show enrollment data with progress
- [x] Add "Continue Learning" button
- [x] Link to course pages
- [x] Empty state handling

**Deliverables**:

- [x] Dashboard page at /dashboard
- [x] Stats cards with live data (enrollments + streak API)
- [x] Recent activity section
- [x] Loading states (skeleton components)
- [x] Error handling with toast notifications
- [x] Created progressService.ts (API aggregation layer)
- [x] Created StatsCard.tsx (reusable component)

#### Refactor Notes (Nov 13)

- [x] Extracted `StreakData` and `DashboardStats` to `lexia-web/types/progress.ts`
- [x] Added request cancellation support in `progressService` and wired `AbortController` in `dashboard/page.tsx`
- [x] Memoized computed values (`fullName`, `studyHoursEstimate`, `remainingLessons`) to reduce re-renders
- [x] Fixed minor Tailwind dark hover class in `Header.tsx`

---

## 🎯 EPIC D: Course & Learning Path (7 points)

**Status**: ⏳ In Progress | **Progress**: 3.5/7 points (50%)

---

### Task D1: Course Listing Page (2 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: C1-C4 | **Estimated**: 1 day  
**Status**: ✅ Complete | **Progress**: 2/2 points (100%)  
**Started**: 2025-11-13 | **Completed**: 2025-11-13

#### Subtasks:

#### D1.1: Create Course List Page (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-13

- [x] Create `src/app/courses/page.tsx`
- [x] Add page header with title
- [x] Add search bar
- [x] Add filter controls (CEFR level)
- [x] Add sort dropdown
- [x] Add grid/list view toggle
- [x] Style layout

#### D1.2: Create Course Card Component (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-13

- [x] Create `src/components/courses/CourseCard.tsx`
- [x] Display:
  - [x] Course thumbnail
  - [x] Title and description (truncated)
  - [x] CEFR level badge
  - [x] Enroll button
- [x] Add hover effects
- [x] Handle enroll click
- [x] Make responsive

#### D1.3: Implement Search and Filter (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-13 (Already implemented in D1.1)

- [x] Connect search bar to API
- [x] Debounce search input (300ms)
- [x] Connect filters to API (CEFR level)
- [x] Update URL query params
- [x] Show loading state while fetching
- [x] Show empty state if no results

**Implementation Details**:

- ✅ `handleSearchChange()` with 300ms debounce using `setTimeout`
- ✅ `handleLevelFilter()` for CEFR level selection/deselection
- ✅ `handleSortChange()` for sort options
- ✅ `updateURLParams()` syncs all filters to URL query params
- ✅ `fetchCourses()` calls `courseService.searchCourses()` with filters
- ✅ Loading skeletons displayed during fetch
- ✅ Empty state with "Clear Filters" button

#### D1.4: Add Pagination (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-13 (Already implemented in D1.1)

- [x] Fetch courses with pagination
- [x] Add page controls (prev/next)
- [x] Display current page info
- [x] Update URL on page change
- [x] Scroll to top on page change
- [x] Test pagination works

**Implementation Details**:

- ✅ `currentPage`, `totalPages`, `totalElements` state management
- ✅ `handlePageChange()` updates page and scrolls to top
- ✅ Previous/Next buttons with disabled states
- ✅ Page number buttons (1, 2, 3...) with active highlighting
- ✅ URL query param sync (page parameter)
- ✅ `fetchCourses()` uses `page` parameter in API calls
- ✅ Pagination hidden when totalPages ≤ 1
- ✅ Smooth scroll to top: `window.scrollTo({ top: 0, behavior: 'smooth' })`

**Deliverables**: ✅ **ALL COMPLETE**

- [x] Course listing page at /courses ✅
- [x] Search and filters working ✅
- [x] Pagination implemented ✅
- [x] Responsive design ✅

**Summary**:

- **Files Created**: 3 files (courseService.ts, CourseCard.tsx, index.ts) - 475+ lines
- **Files Modified**: 2 files (courses/page.tsx, course.ts) - 430+ lines
- **Total Lines**: 905+ lines
- **Quality**: 9/10 ⭐⭐⭐⭐⭐
- **All 4 subtasks complete**: D1.1 ✅, D1.2 ✅, D1.3 ✅, D1.4 ✅

---

### Task D2: Course Detail Page (1.5 points)

**Priority**: P0 | **Dependencies**: D1 | **Estimated**: 1 day  
**Status**: ✅ Complete | **Progress**: 1.5/1.5 points (100%)  
**Started**: 2025-11-13 | **Completed**: 2025-11-13

#### Subtasks:

#### D2.1: Create Course Detail Page (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-13

- [x] Create `src/app/courses/[id]/page.tsx`
- [x] Fetch course by ID with sections
- [x] Display course header:
  - [x] Title, description
  - [x] Thumbnail with fallback
  - [x] CEFR level badge (color-coded)
- [x] Add enroll button with state management
- [x] Show loading skeleton
- [x] Handle not found (404)

**Implementation Details**:

- ✅ Created dynamic route at `app/courses/[id]/page.tsx` (270 lines)
- ✅ Course header with thumbnail, title, description, CEFR badge
- ✅ Course stats: total lessons, duration, sections
- ✅ Enroll button with loading state (isEnrolling)
- ✅ Enrolled status indicator with checkmark icon
- ✅ Loading skeleton for async data fetching
- ✅ 404 not found page with back button
- ✅ Responsive design (mobile, tablet, desktop)
- ✅ Dark mode support
- ✅ Back to courses button with navigation

#### D2.2: Create Course Curriculum Section (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-13

- [x] Display sections and lessons
- [x] Create Section component:
  - [x] Section title
  - [x] Collapsible lesson list
- [x] Create Lesson list item:
  - [x] Lesson title, type icon
  - [x] Duration
  - [x] Lock status (if not enrolled)
- [x] Make curriculum expandable/collapsible

**Implementation Details**:

- ✅ Created `CourseSection.tsx` component (95 lines)
  - Collapsible sections with chevron icons
  - Section title and lesson count display
  - Expandable lesson list (default: expanded)
  - Empty state for sections without lessons
- ✅ Created `LessonItem.tsx` component (125 lines)
  - Lesson type icons: BookOpen, Headphones, FileCheck, Mic
  - Color-coded badges: READING (blue), LISTENING (green), QUIZ (purple), SPEAKING (orange)
  - Lesson number indicator (circular badge)
  - Duration display with clock icon
  - Lock icon for unenrolled users
  - Hover effects for enrolled users
  - Link to lesson viewer when enrolled
- ✅ Updated `components/courses/index.ts` to export new components

#### D2.3: Implement Enrollment Flow (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-13

- [x] Handle enroll button click
- [x] Call enrollmentService.enroll()
- [x] Show success toast
- [x] Update UI (show enrolled status, unlock lessons)
- [x] Handle already enrolled (409)
- [x] Handle errors (404, network)
- [x] Test enrollment flow

**Implementation Details**:

- ✅ Created `enrollmentService.ts` (48 lines)
  - `enroll(courseId)` - POST /enrollments with courseId param
  - `getMyEnrollments()` - GET /enrollments
  - `isEnrolled(courseId)` - Check enrollment status
- ✅ Enrollment state management in course detail page
  - Check enrollment status on page load
  - Handle enroll button click with loading state
  - Update UI after successful enrollment
  - Display enrolled status with green checkmark
- ✅ Error handling:
  - 409 Conflict → "Already enrolled" toast (set isEnrolled=true)
  - 404 Not Found → "Course not found" error
  - Network errors → "Failed to enroll" toast
- ✅ Toast notifications with Sonner
- ✅ Lesson access control based on enrollment status

**Deliverables**: ✅ **ALL COMPLETE**

- [x] Course detail page at /courses/[id] ✅
- [x] Curriculum displayed with collapsible sections ✅
- [x] Enrollment working with error handling ✅
- [x] Loading and error states ✅

**Summary**:

**Files Created**: 4 files (480+ lines)

- `app/courses/[id]/page.tsx` - Course detail page
- `components/courses/CourseSection.tsx` - Collapsible section
- `components/courses/LessonItem.tsx` - Lesson display
- `services/enrollmentService.ts` - Enrollment API client

**Files Modified**: 3 files (100+ lines)

- `types/course.ts` - Added Section, LessonDetail, updated Enrollment
- `services/courseService.ts` - Added getCourseWithSections()
- `components/courses/index.ts` - Exported new components

**Quality**: 9/10 ⭐⭐⭐⭐⭐

**Next**: Task D4 - Lesson Viewer Interface (1.5 points)

---

### Task D3: Learning Path Display (1.5 points) ✅ COMPLETE

**Priority**: P1 | **Dependencies**: D2 | **Estimated**: 1 day  
**Status**: ✅ Complete | **Progress**: 1.5/1.5 points (100%)  
**Started**: 2025-11-09 | **Completed**: 2025-11-09

#### Subtasks:

#### D3.1: Create Learning Path Page (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-09

- [x] Create `src/app/learning-paths/page.tsx`
- [x] Fetch all learning paths
- [x] Display paths as cards:
  - [x] Path name, description
  - [x] CEFR level
  - [x] Course count
  - [x] Start button
- [x] Show recommended path first

#### D3.2: Create Learning Path Components (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-09

- [x] Create `src/components/learning-paths/LearningPathCard.tsx`
- [x] Display path info:
  - [x] CEFR badge with color coding
  - [x] Course count and estimated hours
  - [x] Progress bar for started paths
  - [x] Recommended badge
- [x] Start/View Progress buttons
- [x] Started badge for enrolled paths

#### D3.3: Implement Start Path Flow (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-11-09

- [x] Handle start path button
- [x] Call learningPathService.startPath()
- [x] Show success toast notification
- [x] Refresh data after starting
- [x] Handle already started (409 conflict)
- [x] Test flow

**Deliverables**:

- ✅ Learning paths page at /learning-paths
- ✅ LearningPathCard component
- ✅ learningPathService with 5 methods
- ✅ TypeScript types (LearningPath, UserPathProgress)
- ✅ Start path working with error handling
- ✅ Progress tracking with refresh

**Files Created**:

- `types/learningPath.ts` (102 lines) - LearningPath, UserPathProgress, CEFR_LEVELS
- `services/learningPathService.ts` (98 lines) - API client with 6 methods
- `components/learning-paths/LearningPathCard.tsx` (147 lines) - Path card with start/progress
- `components/learning-paths/index.ts` (5 lines) - Barrel export
- `app/learning-paths/page.tsx` (144 lines) - Learning paths listing page

**Key Features**:

- 6 CEFR levels (A1-C2) with color-coded badges
- Recommended path highlighting based on user level
- Started paths show progress percentage
- Responsive grid layout (1-3 columns)
- Loading skeletons and error handling
- Toast notifications for success/errors

---

### Task D4: Lesson Viewer Interface (1.5 points) ✅ **COMPLETE**

**Priority**: P0 | **Dependencies**: D2 | **Estimated**: 1 day  
**Status**: ✅ Complete | **Started**: 2025-11-13 | **Completed**: 2025-11-13

#### Subtasks:

#### D4.1: Create Lesson Viewer Page (0.5 points) ✅ **COMPLETE**

- [x] Create `app/courses/[courseId]/lessons/[lessonId]/page.tsx` (191 lines)
- [x] Fetch lesson by ID with error handling (404, network)
- [x] Display lesson header:
  - [x] Title, type badge, duration
  - [x] Description section
- [x] Show loading skeleton
- [x] Parse JSONB content with try/catch
- [x] Navigation back to course

#### D4.2: Create Content Renderer (0.5 points) ✅ **COMPLETE**

- [x] Create `components/lessons/ContentRenderer.tsx` (545 lines)
- [x] Render based on lesson type:
  - [x] READING: passages in cards, vocabulary grid, questions with explanations
  - [x] LISTENING: audio player, transcript toggle, timestamped vocabulary
  - [x] QUIZ: quiz header with stats, questions with hints/points
  - [x] SPEAKING: scenario with difficulty, prompts with sample answers, AI placeholder
- [x] Parse JSONB content (parseLessonContent utility)
- [x] Style each type appropriately with proper accessibility

#### D4.3: Add Complete Lesson Button (0.5 points) ✅ **COMPLETE**

- [x] Add complete button at bottom (3 states: default, loading, completed)
- [x] Call progressService.completeLesson()
- [x] Show success animation (confetti)
- [x] Update progress via API
- [x] Toast notifications (success/error)
- [x] Auto-redirect to course after 2 seconds
- [x] Handle errors gracefully

**Deliverables**:

- [x] Lesson viewer at /courses/[courseId]/lessons/[lessonId] ✅
- [x] Content renders correctly for all 4 types ✅
- [x] Completion working with confetti animation ✅
- [x] Progress tracking integrated ✅

**Files Created** (6 files, 1,048 lines):

- ✅ `types/lesson.ts` (197 lines) - TypeScript interfaces for all 4 lesson types
- ✅ `services/lessonService.ts` (55 lines) - API client
- ✅ `services/progressService.ts` (UPDATED) - Added completeLesson method
- ✅ `components/lessons/ContentRenderer.tsx` (545 lines) - 4 specialized renderers
- ✅ `components/lessons/index.ts` (5 lines) - Barrel export
- ✅ `app/courses/[courseId]/lessons/[lessonId]/page.tsx` (191 lines) - Lesson viewer page

**Dependencies Installed**:

- ✅ canvas-confetti - Celebration animation
- ✅ @types/canvas-confetti - TypeScript types

**Quality**: 9.5/10 ⭐⭐⭐⭐⭐

---

### Task D5: Lesson Navigation (0.5 points)

**Priority**: P1 | **Dependencies**: D4 | **Estimated**: 0.25 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### D5.1: Create Navigation Component (0.3 points)

- [ ] Create `src/components/lessons/LessonNavigation.tsx`
- [ ] Add buttons:
  - [ ] Previous lesson
  - [ ] Next lesson
  - [ ] Back to course
- [ ] Show progress: "Lesson 3 of 15"
- [ ] Disable prev on first lesson
- [ ] Disable next on last lesson

#### D5.2: Implement Navigation Logic (0.2 points)

- [ ] Get section and lesson order
- [ ] Calculate prev/next lesson IDs
- [ ] Navigate on button click
- [ ] Handle cross-section navigation
- [ ] Test navigation works

**Deliverables**:

- [ ] Lesson navigation component
- [ ] Prev/next working
- [ ] Progress indicator

---

## 🎯 EPIC E: Progress & Profile (5 points)

**Status**: 🔵 Not Started | **Progress**: 0/5 points (0%)

---

### Task E1: Progress Dashboard (2 points)

**Priority**: P0 | **Dependencies**: D1-D5, D4 | **Estimated**: 1 day  
**Status**: 🔵 Not Started

#### Subtasks:

#### E1.1: Create Progress Page (0.5 points)

- [ ] Create `src/app/progress/page.tsx`
- [ ] Add page header with title
- [ ] Create stats grid:
  - [ ] Total lessons completed
  - [ ] Total time spent
  - [ ] Current streak
  - [ ] Longest streak
- [ ] Fetch progress data from API

#### E1.2: Create Progress Chart (0.8 points)

- [ ] Install recharts library
- [ ] Create `src/components/progress/ProgressChart.tsx`
- [ ] Display line chart:
  - [ ] X-axis: dates
  - [ ] Y-axis: lessons completed
  - [ ] Last 30 days
- [ ] Add tooltips
- [ ] Make responsive

#### E1.3: Create Streak Calendar (0.7 points)

- [ ] Create `src/components/progress/StreakCalendar.tsx`
- [ ] Display calendar heatmap:
  - [ ] Green squares for active days
  - [ ] Darker = more lessons
  - [ ] Last 365 days
- [ ] Show tooltip on hover
- [ ] Highlight current streak

**Deliverables**:

- [ ] Progress page at /progress
- [ ] Progress chart with data
- [ ] Streak calendar heatmap
- [ ] Responsive design

---

### Task E2: Lesson Completion Tracking UI (1 point)

**Priority**: P0 | **Dependencies**: D4, E1 | **Estimated**: 0.5 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### E2.1: Add Completion Checkmarks (0.5 points)

- [ ] Update CourseCard to show progress
- [ ] Add checkmarks on completed lessons
- [ ] Show completion percentage
- [ ] Display in course detail
- [ ] Update in real-time after completion

#### E2.2: Add Completion Animation (0.5 points)

- [ ] Install confetti library
- [ ] Trigger confetti on lesson complete
- [ ] Show success modal/toast
- [ ] Display completion stats
- [ ] Add "Next Lesson" button

**Deliverables**:

- [ ] Completion checkmarks
- [ ] Confetti animation
- [ ] Success feedback

---

### Task E3: Profile Management Page (1 point)

**Priority**: P0 | **Dependencies**: C4 | **Estimated**: 0.5 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### E3.1: Create Profile Page (0.5 points)

- [ ] Create `src/app/profile/page.tsx`
- [ ] Display current profile info:
  - [ ] Avatar
  - [ ] Name, email
  - [ ] Bio, phone, timezone
- [ ] Add edit button
- [ ] Fetch profile from API

#### E3.2: Create Profile Edit Form (0.5 points)

- [ ] Create `src/components/profile/ProfileForm.tsx`
- [ ] Add form fields:
  - [ ] First name, last name
  - [ ] Bio (textarea)
  - [ ] Phone number
  - [ ] Timezone (dropdown)
  - [ ] Language (dropdown)
- [ ] Use React Hook Form + Zod validation
- [ ] Add save button
- [ ] Handle save → update API → show success

**Deliverables**:

- [ ] Profile page at /profile
- [ ] Edit form working
- [ ] Profile update successful
- [ ] Validation working

---

### Task E4: Avatar Upload Interface (0.5 points)

**Priority**: P1 | **Dependencies**: E3 | **Estimated**: 0.25 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### E4.1: Create Avatar Upload Component (0.3 points)

- [ ] Create `src/components/profile/AvatarUpload.tsx`
- [ ] Display current avatar
- [ ] Add upload button
- [ ] Handle file selection
- [ ] Show preview before upload
- [ ] Add delete button

#### E4.2: Implement Upload Logic (0.2 points)

- [ ] Call profileService.uploadAvatar()
- [ ] Handle file upload (multipart/form-data)
- [ ] Show upload progress
- [ ] Update avatar on success
- [ ] Handle errors (file too large, invalid format)

**Deliverables**:

- [ ] Avatar upload component
- [ ] Upload working
- [ ] Preview working
- [ ] Error handling

---

### Task E5: Settings Page (0.5 points)

**Priority**: P1 | **Dependencies**: E3 | **Estimated**: 0.25 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### E5.1: Create Settings Page (0.3 points)

- [ ] Create `src/app/settings/page.tsx`
- [ ] Add settings sections:
  - [ ] Language preference
  - [ ] Timezone setting
  - [ ] Email notifications (toggles)
- [ ] Use shadcn/ui Switch component
- [ ] Save settings to API

#### E5.2: Add Theme Selector (0.2 points)

- [ ] Add light/dark theme toggle (placeholder)
- [ ] Use next-themes library
- [ ] Persist theme preference
- [ ] Test theme switching

**Deliverables**:

- [ ] Settings page at /settings
- [ ] Settings save to API
- [ ] Theme toggle working

---

## 🎯 EPIC F: Testing & Polish (4 points)

**Status**: 🔵 Not Started | **Progress**: 0/4 points (0%)

**⚠️ Epic Updated**: Added +1 point for Error Boundary (F2.3), Responsive Testing (F3.3), and Accessibility Audit (F6)

---

### Task F1: Form Validation (0.5 points)

**Priority**: P0 | **Dependencies**: B1-B2, E3 | **Estimated**: 0.25 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### F1.1: Review All Forms (0.3 points)

- [ ] Check all forms use React Hook Form + Zod
- [ ] Verify real-time validation
- [ ] Verify error messages clear
- [ ] Test field-level validation
- [ ] Test form-level validation

#### F1.2: Add Missing Validations (0.2 points)

- [ ] Add any missing validators
- [ ] Test edge cases
- [ ] Improve error messages
- [ ] Add custom validation rules if needed

**Deliverables**:

- [ ] All forms validated
- [ ] Error messages user-friendly
- [ ] All validations tested

---

### Task F2: Error Handling + Toast Notifications (0.7 points)

**Priority**: P0 | **Dependencies**: All | **Estimated**: 0.35 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### F2.1: Setup Toast Notifications (0.2 points)

- [ ] Configure react-hot-toast (or use shadcn/ui Sonner)
- [ ] Add Toaster component to layout
- [ ] Style toasts to match theme
- [ ] Test toast display

#### F2.2: Add Error Handling (0.3 points)

- [ ] Create 404 page at `src/app/not-found.tsx`
- [ ] Create 500 error page at `src/app/error.tsx`
- [ ] Handle network errors globally in axios interceptor
- [ ] Add retry mechanisms for network errors (3 attempts with exponential backoff)

#### F2.3: Create Error Boundary Component (0.2 points) 🆕

- [ ] Create `src/components/ErrorBoundary.tsx`
- [ ] Implement componentDidCatch lifecycle
- [ ] Create ErrorFallback UI component
- [ ] Log errors to console (future: monitoring service)
- [ ] Add reset functionality
- [ ] Wrap app in layout.tsx with ErrorBoundary
- [ ] Test with intentional error (throw new Error in component)

**Deliverables**:

- [ ] Toast notifications working
- [ ] Error boundary in place with fallback UI
- [ ] 404 and 500 pages
- [ ] Global error handling with retry logic

---

### Task F3: Loading States + Skeletons (0.8 points)

**Priority**: P0 | **Dependencies**: All | **Estimated**: 0.4 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### F3.1: Add Loading Spinners (0.2 points)

- [ ] Add spinners to buttons during actions
- [ ] Add page-level loading indicators
- [ ] Add spinner to API requests
- [ ] Test all loading states

#### F3.2: Create Skeleton Loaders (0.3 points)

- [ ] Create skeleton for course cards
- [ ] Create skeleton for stats cards
- [ ] Create skeleton for lesson content
- [ ] Add shimmer animation
- [ ] Use skeletons during data fetching

#### F3.3: Responsive Design Testing (0.3 points) 🆕

- [ ] Test all breakpoints systematically:
  - [ ] Mobile S: 320px (iPhone SE)
  - [ ] Mobile M: 375px (iPhone 12/13)
  - [ ] Mobile L: 425px
  - [ ] Tablet: 768px (iPad)
  - [ ] Desktop S: 1024px
  - [ ] Desktop M: 1280px (MacBook)
  - [ ] Desktop L: 1920px (Full HD)
- [ ] Test specific scenarios:
  - [ ] Sidebar collapsible on mobile (< 768px)
  - [ ] Course cards stack vertically on mobile
  - [ ] Forms readable and usable on small screens
  - [ ] Navigation touch-friendly (44px+ touch targets)
  - [ ] Images scale properly without breaking layout
  - [ ] Tables scroll horizontally on mobile
- [ ] Browser testing:
  - [ ] Chrome DevTools responsive mode
  - [ ] Real device testing (iOS Safari, Android Chrome if available)
- [ ] Document responsive design patterns used

**Deliverables**:

- [ ] Loading spinners on buttons
- [ ] Skeleton loaders for all lists
- [ ] Smooth loading experience
- [ ] Responsive design verified across all breakpoints

---

### Task F4: Jest + React Testing Library Setup (0.5 points)

**Priority**: P0 | **Dependencies**: None | **Estimated**: 0.25 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### F4.1: Install Testing Dependencies (0.2 points)

- [ ] Install Jest: `npm install -D jest @types/jest`
- [ ] Install RTL: `npm install -D @testing-library/react @testing-library/jest-dom @testing-library/user-event`
- [ ] Install jest-environment-jsdom
- [ ] Configure jest.config.js
- [ ] Create jest.setup.js

#### F4.2: Configure Test Scripts & Coverage Thresholds (0.3 points)

- [ ] Add test scripts to package.json:
  - [ ] `"test": "jest"`
  - [ ] `"test:watch": "jest --watch"`
  - [ ] `"test:coverage": "jest --coverage"`
- [ ] Configure jest.config.js with coverage settings:

  ```javascript
  {
    collectCoverageFrom: [
      "src/**/*.{ts,tsx}",
      "!src/**/*.d.ts",
      "!src/types/**",
      "!src/**/*.stories.tsx",
      "!src/app/layout.tsx",
      "!src/components/ui/**" // shadcn components
    ],
    coverageThresholds: {
      global: {
        lines: 60,
        branches: 50,
        functions: 60,
        statements: 60
      },
      "src/services/**/*.ts": {
        lines: 80,
        functions: 80,
        statements: 80
      },
      "src/lib/**/*.ts": {
        lines: 70,
        functions: 70,
        statements: 70
      }
    }
  }
  ```

- [ ] Create test utils (render with providers)
- [ ] Create mock API responses
- [ ] Test setup works (run sample test)

**Coverage Metrics Explained**:

- **Lines**: 60% of code lines executed
- **Branches**: 50% of if/else paths tested
- **Functions**: 60% of functions called
- **Statements**: 60% of statements executed
- **Services**: Higher threshold (80%) for critical business logic
- **Excluded**: Type definitions, UI library components, stories

**Deliverables**:

- [ ] Jest configured with coverage thresholds
- [ ] RTL configured
- [ ] Test scripts working
- [ ] Test utils created
- [ ] Coverage report generates correctly

---

### Task F5: Component Unit Tests (1.5 points)

**Priority**: P0 | **Dependencies**: F4 | **Estimated**: 0.75 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### F5.1: Test Authentication Components (0.3 points)

- [ ] Test LoginForm:
  - [ ] Email validation
  - [ ] Password validation
  - [ ] Form submission
  - [ ] Error display
- [ ] Test RegisterForm:
  - [ ] All field validations
  - [ ] Password confirmation
  - [ ] Form submission

#### F5.2: Test Course Components (0.3 points)

- [ ] Test CourseCard:
  - [ ] Renders course data
  - [ ] Enroll button click
  - [ ] Hover effects
- [ ] Test CourseList:
  - [ ] Renders course grid
  - [ ] Search functionality
  - [ ] Filter functionality

#### F5.3: Test Dashboard Components (0.2 points)

- [ ] Test StatsCard:
  - [ ] Renders data correctly
  - [ ] Loading state
- [ ] Test ProgressChart:
  - [ ] Chart renders
  - [ ] Data visualization correct

#### F5.4: Test Navigation Components (0.2 points)

- [ ] Test Sidebar:
  - [ ] Navigation links render
  - [ ] Active link highlighted
  - [ ] Collapse toggle works
- [ ] Test Header:
  - [ ] User dropdown works
  - [ ] Logout functionality

#### F5.5: Verify Coverage Thresholds (0.3 points) 🆕

- [ ] Run `npm run test:coverage`
- [ ] Verify global coverage ≥ 60%:
  - [ ] Lines: ≥ 60%
  - [ ] Branches: ≥ 50%
  - [ ] Functions: ≥ 60%
  - [ ] Statements: ≥ 60%
- [ ] Verify service coverage ≥ 80%
- [ ] Generate HTML coverage report
- [ ] Review uncovered code and add tests if critical
- [ ] Document coverage results in session summary

**Deliverables**:

- [ ] 25+ component tests (increased from 20+)
- [ ] 60%+ code coverage (verified with thresholds)
- [ ] Services: 80%+ coverage
- [ ] All tests passing
- [ ] Coverage report generated and reviewed

---

### Task F6: Accessibility Audit (0.5 points) 🆕

**Priority**: P1 | **Dependencies**: All components | **Estimated**: 0.25 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### F6.1: ARIA Labels & Semantic HTML (0.2 points)

- [ ] Audit all forms for proper labels:
  - [ ] All inputs have associated `<label>` or `aria-label`
  - [ ] Error messages linked with `aria-describedby`
  - [ ] Required fields marked with `aria-required`
- [ ] Audit interactive elements:
  - [ ] Buttons have descriptive text or `aria-label`
  - [ ] Icons have `aria-hidden="true"` if decorative
  - [ ] Dialogs have `role="dialog"` and `aria-labelledby`
- [ ] Use semantic HTML:
  - [ ] `<nav>` for navigation
  - [ ] `<main>` for main content
  - [ ] `<article>` for course cards
  - [ ] `<section>` for content sections

#### F6.2: Keyboard Navigation (0.2 points)

- [ ] Test keyboard navigation:
  - [ ] Tab key moves focus logically
  - [ ] Shift+Tab moves focus backwards
  - [ ] Enter activates buttons/links
  - [ ] Escape closes modals/dropdowns
  - [ ] Arrow keys navigate within components (dropdowns, tabs)
- [ ] Ensure focus indicators visible:
  - [ ] Add custom focus styles (ring-2 ring-primary)
  - [ ] Don't remove default outline without replacement
- [ ] Test focus trap in modals/dialogs
- [ ] Skip to main content link

#### F6.3: Color Contrast & Screen Reader Testing (0.1 points)

- [ ] Check color contrast ratios (WCAG AA):
  - [ ] Normal text: ≥ 4.5:1
  - [ ] Large text (18pt+): ≥ 3:1
  - [ ] UI components: ≥ 3:1
  - [ ] Use browser DevTools or WebAIM Contrast Checker
- [ ] Test with screen reader (if available):
  - [ ] Windows: NVDA (free)
  - [ ] macOS: VoiceOver (built-in)
  - [ ] Verify all content readable
  - [ ] Verify navigation makes sense
- [ ] Document accessibility patterns used

**Deliverables**:

- [ ] ARIA labels on all interactive elements
- [ ] Keyboard navigation working (Tab, Enter, Escape, Arrows)
- [ ] Focus indicators visible
- [ ] Color contrast ≥ 4.5:1 (WCAG AA)
- [ ] Screen reader tested (if tools available)
- [ ] Accessibility audit checklist completed

---

### Task F7: Comprehensive Test Matrix (0 points) 📋

**Priority**: P0 | **Type**: Documentation | **Estimated**: 0 days  
**Status**: 🔵 Ready for Reference

#### Purpose:

Detailed test scenarios mapped to specific test types and coverage areas.

#### Test Matrix

| Test Area             | Test Scenario                                     | Type        | Priority | File/Component        | Acceptance Criteria                                     |
| --------------------- | ------------------------------------------------- | ----------- | -------- | --------------------- | ------------------------------------------------------- |
| **Authentication**    |                                                   |             |          |                       |                                                         |
| Login                 | Valid email + password → redirect to dashboard    | Integration | P0       | LoginForm.test.tsx    | User logged in, authStore updated, /dashboard loaded    |
| Login                 | Invalid email format → show inline error          | Unit        | P0       | LoginForm.test.tsx    | Error message: "Invalid email address"                  |
| Login                 | Password < 8 chars → show inline error            | Unit        | P0       | LoginForm.test.tsx    | Error message: "Password must be at least 8 characters" |
| Login                 | 401 error → show toast "Invalid credentials"      | Integration | P0       | LoginForm.test.tsx    | Toast displayed, no redirect                            |
| Login                 | Network error → show toast "Connection failed"    | Integration | P0       | LoginForm.test.tsx    | Toast with retry option                                 |
| Register              | Valid data → auto-login → redirect to dashboard   | Integration | P0       | RegisterForm.test.tsx | User registered, logged in, /dashboard loaded           |
| Register              | Email already exists (409) → show specific error  | Integration | P0       | RegisterForm.test.tsx | Toast: "Email already registered. Please login."        |
| Register              | Passwords don't match → show inline error         | Unit        | P0       | RegisterForm.test.tsx | Error on confirmPassword field                          |
| Register              | Weak password → show strength indicator           | Unit        | P1       | RegisterForm.test.tsx | Strength bar updates real-time                          |
| **Token Management**  |                                                   |             |          |                       |                                                         |
| httpOnly Cookies      | Login → backend sets httpOnly cookie              | Integration | P0       | authService.test.ts   | Cookie set with HttpOnly, Secure, SameSite=Strict       |
| httpOnly Cookies      | No localStorage usage for tokens                  | Unit        | P0       | authStore.test.ts     | AuthState has NO accessToken/refreshToken fields        |
| Token Refresh         | 401 → refresh → retry original request            | Integration | P0       | api.test.ts           | Refresh called once, original request retried           |
| Token Refresh         | 401 → refresh fails → logout → redirect to /login | Integration | P0       | api.test.ts           | User logged out, redirected to login                    |
| Token Refresh         | Concurrent 401s → only 1 refresh call             | Unit        | P0       | api.test.ts           | Promise lock prevents multiple refresh calls            |
| **Axios Interceptor** |                                                   |             |          |                       |                                                         |
| Retry Logic           | GET request network error → retry 3 times         | Unit        | P0       | api.test.ts           | 3 retries with exponential backoff                      |
| Retry Logic           | POST request network error → NO retry             | Unit        | P0       | api.test.ts           | POST not retried (non-idempotent)                       |
| Retry Logic           | 500 error on GET → retry 3 times                  | Unit        | P0       | api.test.ts           | Retry with 300ms, 600ms, 1200ms delays                  |
| Retry Logic           | 404 error → NO retry                              | Unit        | P0       | api.test.ts           | Client error, no retry                                  |
| **Middleware**        |                                                   |             |          |                       |                                                         |
| Protected Routes      | Unauthenticated → /dashboard → redirect to /login | Integration | P0       | middleware.test.ts    | Redirected to /login                                    |
| Protected Routes      | Authenticated → /dashboard → allow access         | Integration | P0       | middleware.test.ts    | Dashboard loaded                                        |
| Protected Routes      | Public route /login → allow without auth          | Integration | P0       | middleware.test.ts    | Login page loaded                                       |
| Redirect Loop         | Already on /login → don't redirect again          | Unit        | P0       | middleware.test.ts    | No infinite redirect loop                               |
| **Accessibility**     |                                                   |             |          |                       |                                                         |
| Focus                 | Tab key navigates login form fields correctly     | Unit/RTL    | P1       | LoginForm.test.tsx    | Focus order: email → password → remember → submit       |
| ARIA                  | Login form errors have aria-describedby           | Unit/RTL    | P1       | LoginForm.test.tsx    | Error IDs match aria-describedby                        |
| Keyboard              | Escape key closes dropdown menu                   | Unit/RTL    | P1       | Header.test.tsx       | Menu closed on Esc                                      |
| Contrast              | All text has 4.5:1 contrast ratio                 | Manual      | P1       | Design system review  | DevTools contrast check passed                          |
| **Learning Path**     |                                                   |             |          |                       |                                                         |
| Display               | CEFR levels in correct order A1→C2                | Unit        | P1       | LearningPath.test.tsx | Levels rendered: A1, A2, B1, B2, C1, C2                 |
| Display               | Current level highlighted                         | Unit        | P1       | LearningPath.test.tsx | Current level has highlight styling                     |
| **Coverage**          |                                                   |             |          |                       |                                                         |
| Global                | Overall coverage ≥ 60%                            | Coverage    | P0       | Jest coverage report  | Lines: 60%, Functions: 60%, Statements: 60%             |
| Services              | API services coverage ≥ 80%                       | Coverage    | P0       | Jest coverage report  | authService, courseService, etc. ≥ 80%                  |

**Test Execution Guide**:

```bash
# Run all tests
npm run test

# Run tests in watch mode
npm run test:watch

# Run coverage report
npm run test:coverage

# Run specific test file
npm run test LoginForm.test.tsx

# Run tests matching pattern
npm run test -- --testNamePattern="Login"
```

**Priority Definitions**:

- **P0**: Must pass before sprint completion
- **P1**: Should pass, can defer to Sprint 4 if needed
- **P2**: Nice to have, can be added later

**Coverage Enforcement**:

- Global: 60% minimum (enforced by jest.config.js)
- Services: 80% minimum (enforced by jest.config.js)
- Failing tests block PR merge

---

## 🔒 Sprint 3 Risks & Mitigations

### Critical Risks

| Risk                             | Probability | Impact | Mitigation Strategy                                                                                             |
| -------------------------------- | ----------- | ------ | --------------------------------------------------------------------------------------------------------------- |
| **Backend API contract changes** | Low         | High   | • API versioning (v1) enforced<br>• Contract testing before integration<br>• Mock API responses for development |
| **JWT token refresh bugs**       | Medium      | High   | • Thorough testing of token lifecycle<br>• Fallback logout on refresh failure<br>• Token expiry monitoring      |
| **Test coverage below 60%**      | Medium      | High   | • TDD approach: write tests alongside code<br>• Daily coverage monitoring<br>• Block PR merge if coverage drops |
| **Responsive design issues**     | Medium      | Medium | • Mobile-first CSS approach<br>• Test early and often (320px+)<br>• Use browser dev tools consistently          |

### Medium Risks

| Risk                                  | Probability | Impact | Mitigation Strategy                                                                                              |
| ------------------------------------- | ----------- | ------ | ---------------------------------------------------------------------------------------------------------------- |
| **shadcn/ui component conflicts**     | Low         | Medium | • Pin exact versions in package.json<br>• Test components after updates<br>• Custom wrapper components if needed |
| **Axios interceptor race conditions** | Low         | Medium | • Mutex/lock for token refresh<br>• Queue failed requests during refresh<br>• Comprehensive error handling       |
| **State management complexity**       | Medium      | Low    | • Keep stores simple and focused<br>• Document state flow clearly<br>• Avoid nested state updates                |
| **Performance issues (large lists)**  | Low         | Medium | • Implement pagination everywhere<br>• Virtual scrolling for long lists<br>• Lazy load images                    |

### Dependencies & Blockers

| Item                          | Status       | Blocker For             | Contingency Plan                     |
| ----------------------------- | ------------ | ----------------------- | ------------------------------------ |
| Backend API availability      | ✅ Available | All tasks               | Local mock API server                |
| Design assets (images, icons) | ⚠️ Partial   | UI polish               | Use placeholder images, Lucide icons |
| Gemini API quota              | ✅ Available | AI features (Sprint 4+) | N/A for Sprint 3                     |
| Test environment setup        | 🔵 Pending   | F4-F5                   | Use local Jest setup first           |

### Risk Monitoring Checklist

**Daily** (during standup):

- [ ] Check if any blockers emerged
- [ ] Review test coverage percentage
- [ ] Verify API integration still working
- [ ] Note any new dependencies

**Weekly** (mid-sprint & end-sprint):

- [ ] Review risk probability changes
- [ ] Update mitigation strategies if needed
- [ ] Document lessons learned
- [ ] Plan contingencies for next sprint

---

## �📊 Sprint 3 Progress Tracker

### Overall Progress

- **Completed**: 28/90 subtasks (31%)
- **Story Points**: 9.0/29 points (31%)
- **Days Elapsed**: 5/14 days (36%)
- **Status**: 🔵 In Progress - On Track

### Completed Tasks

**Epic A - Project Setup (4 points) ✅**:

1. ✅ **A1** - Next.js Project Initialization (1 point) - Nov 8
2. ✅ **A2** - Tailwind CSS + shadcn/ui Setup (0.5 points) - Nov 8
3. ✅ **A3** - Zustand State Management (0.5 points) - Nov 8
4. ✅ **A4** - Axios API Client Setup (1 point) - Nov 8
5. ✅ **A5** - Environment Configuration (1 point) - Nov 8

**Epic B - Authentication (5 points) ✅**: 6. ✅ **B0** - Security Consolidation Checklist (0 points) - Nov 12 7. ✅ **B1** - Login Page (1.5 points) - Nov 12 8. ✅ **B2** - Register Page (1.5 points) - Nov 12 9. ✅ **B3** - JWT Token Management (1 point) - Nov 12 10. ✅ **B4** - Protected Routes Middleware (0.5 points) - Nov 12 11. ✅ **B5** - Auth Store Refinement (0.5 points) - Nov 12

### Current Sprint

- 📋 **Epic C** - Dashboard & Layout (4 points) - Starting Nov 13

### Upcoming Next

- 📋 **C1** - Main Layout with Sidebar (1.5 points)
- 📋 **C2** - Header with User Dropdown (0.5 points)
- 📋 **C3** - Responsive Navigation (1 point)

### Sprint Health Indicators

- ✅ No blockers
- ✅ Project setup complete (Epic A - 100%)
- ✅ Authentication complete (Epic B - 100%)
- ✅ Security: httpOnly cookies implemented ✅
- ✅ Ahead of schedule (31% done in 36% of time)
- ✅ Backend API stable and available
- 🔵 Ready for Epic C (Dashboard & Layout)
- 🔵 Test coverage infrastructure ready (Jest/RTL)
- ✅ Documentation updated with all changes

### Sprint Velocity Tracking

| Metric          | Target       | Current    | Status                    |
| --------------- | ------------ | ---------- | ------------------------- |
| Story Points    | 29 (updated) | 9          | 🔵 31%                    |
| Days Elapsed    | 14           | 5          | 🔵 36%                    |
| Velocity        | 2.1 pts/day  | 1.8 pt/day | ✅ On track (catching up) |
| Test Coverage   | 60%+         | 0%         | 🔵 Not started (Epic F)   |
| Tasks Completed | 90 (updated) | 28         | 🔵 31%                    |

**Velocity Analysis**:

- Expected at Day 5: ~10.4 points (29 × 36% ≈ 10.4)
- Actual at Day 5: 9 points
- **Gap**: -1.4 points (ahead of previous estimate, nearly on target)
- **Recommendation**: Focus on Epic C (Dashboard & Layout) next - maintain current velocity

**Quality Updates Applied** (Nov 11, 2025):

- 🔐 Security: Switched to httpOnly cookies for JWT storage
- 🛡️ Error handling: Added Error Boundary component
- 🧪 Testing: Defined coverage thresholds (60% global, 80% services)
- 📱 Responsive: Added systematic breakpoint testing
- ♿ Accessibility: Added WCAG AA compliance audit
- 🔄 API errors: Enhanced retry logic for network failures

---

## � Daily Development Workflow

### Morning Routine (9:00 AM)

**Step 1: Review Yesterday** (5 mins)

- [ ] Read `daily-log.md` last entry
- [ ] Check what was completed
- [ ] Identify any blockers

**Step 2: Plan Today** (10 mins)

- [ ] Pick 1-2 tasks from backlog (max 2 story points)
- [ ] Break down into subtasks
- [ ] Update task status to "⏳ In Progress"
- [ ] Set completion target time

**Step 3: Environment Check** (5 mins)

- [ ] Pull latest code: `git pull origin dev`
- [ ] Install dependencies: `npm install` (if package.json changed)
- [ ] Run dev server: `npm run dev`
- [ ] Verify backend API running: `http://localhost:8088/api/v1/actuator/health`

### During Development

**TDD Workflow**:

```bash
1. Write test first (RED)
   → Create test file: ComponentName.test.tsx
   → Write failing test
   → Run: npm test

2. Implement feature (GREEN)
   → Write minimal code to pass test
   → Run: npm test
   → Verify test passes

3. Refactor (REFACTOR)
   → Clean up code
   → Add TypeScript types
   → Run: npm test (ensure still passes)
```

**Code Quality Checks**:

```bash
# Before every commit
npm run lint          # ESLint check
npm run type-check    # TypeScript check
npm test             # Run tests
npm run test:coverage # Check coverage
```

### End of Day Routine (6:00 PM)

**Step 1: Commit Work** (10 mins)

- [ ] Stage changes: `git add .`
- [ ] Commit: `git commit -m "feat(auth): implement login form validation"`
- [ ] Push: `git push origin dev`

**Step 2: Update Documentation** (10 mins)

- [ ] Update `daily-log.md`:

  ```markdown
  ## Day X - November XX, 2025

  ### Completed

  - [x] Task ID - Description (X.X pts)

  ### In Progress

  - [ ] Task ID - Description (X% done)

  ### Blockers

  - None / [Describe blocker]

  ### Tomorrow

  - [ ] Task ID - Next task
  ```

- [ ] Update task status in `task-breakdown.md`
- [ ] Update story points in `current-sprint-status.md`

**Step 3: Prepare Tomorrow** (5 mins)

- [ ] Review next task requirements
- [ ] Identify potential blockers
- [ ] Note questions for resolution

### Weekly Reviews

**Mid-Sprint Review** (Day 7 - November 15):

- [ ] Review velocity (should be ~14 pts by now)
- [ ] Adjust remaining sprint plan
- [ ] Identify risks
- [ ] Update sprint-3-backlog.md

**End-Sprint Retrospective** (Day 14 - November 21):

- [ ] Complete all Definition of Done items
- [ ] Generate coverage report
- [ ] Document lessons learned
- [ ] Plan Sprint 4

---

## �📋 Task Tracking Template

```markdown
## [Task ID]: [Task Name]

**Status**: 🔵 Not Started | ⏳ In Progress | ✅ Complete | ⚠️ Blocked
**Assignee**: [Name]
**Started**: YYYY-MM-DD
**Completed**: YYYY-MM-DD
**Actual Points**: X.X

### Subtasks Progress: X/Y completed

- [ ] Subtask 1
- [ ] Subtask 2
- [ ] ...

### Notes:

- Any blockers or important decisions
```

---

## ✅ Definition of Done Checklist

For each task to be considered "Done":

### Code Quality

- [ ] Code compiles without TypeScript errors
- [ ] ESLint passes with no errors
- [ ] Component renders correctly
- [ ] Responsive design tested on all breakpoints:
  - [ ] 320px (Mobile S - iPhone SE)
  - [ ] 375px (Mobile M - iPhone 12/13)
  - [ ] 425px (Mobile L)
  - [ ] 768px (Tablet - iPad)
  - [ ] 1024px (Desktop S)
  - [ ] 1440px (Desktop L)
- [ ] Touch interactions work on mobile
- [ ] No horizontal scroll on any device

### Testing

- [ ] Component unit tests written (if applicable)
- [ ] User interactions tested
- [ ] Edge cases tested
- [ ] Integration tested with backend API

### Documentation

- [ ] JSDoc comments on functions
- [ ] README updated if needed
- [ ] PropTypes or TypeScript types defined

### User Experience

- [ ] Loading states implemented
- [ ] Error states handled
- [ ] Success feedback provided
- [ ] Accessible (keyboard navigation, ARIA)

### Commit

- [ ] Conventional commit message
- [ ] Changes committed to feature branch
- [ ] No console errors in browser

---

**Total Subtasks**: 83  
**Completed Subtasks**: 12  
**Estimated Total**: 28 points  
**Points Completed**: 4.0  
**Ready to Execute**: ✅

**Last Updated**: November 11, 2025
