# Sprint 3 - Detailed Task Breakdown

**Sprint**: 3 / 8  
**Duration**: November 8 – November 21, 2025 (14 days)  
**Total Story Points**: 29 points (Updated from 28)  
**Status**: ⏳ In Progress (Day 4)  
**Completed**: 4/29 points (14%)  
**Last Updated**: November 11, 2025 (Security & Quality Updates Applied)

---

## 📋 Task Breakdown Overview

| Epic                      | Tasks  | Subtasks | Completed | Total Points | Progress |
| ------------------------- | ------ | -------- | --------- | ------------ | -------- |
| A: Project Setup & Config | 5      | 12       | 12/12     | 4            | 100%     |
| B: Authentication Pages   | 5      | 16       | 0/16      | 5            | 0%       |
| C: Dashboard & Layout     | 4      | 12       | 0/12      | 4            | 0%       |
| D: Course & Learning Path | 5      | 18       | 0/18      | 7            | 0%       |
| E: Progress & Profile     | 5      | 14       | 0/14      | 5            | 0%       |
| F: Testing & Polish       | 6      | 18       | 0/18      | 4            | 0%       |
| **TOTAL**                 | **30** | **90**   | **12/90** | **29**       | **14%**  |

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
  - [x] accessToken: string | null
  - [x] refreshToken: string | null
  - [x] isAuthenticated: boolean
- [x] Implement actions:
  - [x] login(tokens, user)
  - [x] logout()
  - [x] setUser(user)
  - [x] refreshAccessToken()
- [x] Add localStorage persistence
- [x] Test store with sample data

**Deliverables**:

- ✅ src/store/authStore.ts (100+ lines)
- ✅ Full authentication state management
- ✅ localStorage integration
- ✅ TypeScript types defined

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
  - [x] Add JWT Bearer token from cookie (axios sends automatically)
  - [x] Set withCredentials: true for cookie support
  - [x] Log requests in development
- [x] Add response interceptor:
  - [x] Handle 401 (refresh token or logout)
  - [x] Handle 403 (insufficient permissions)
  - [x] Handle network errors (ERR_NETWORK, no internet)
  - [x] Handle timeout errors (ECONNABORTED)
  - [x] Handle server errors (500, 502, 503)
  - [x] Add retry logic for network errors (3 attempts)
  - [x] Return proper error format

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

**Status**: 🔵 Next Up | **Progress**: 0/5 points (0%)

---

### Task B1: Login Page (1.5 points)

**Priority**: P0 | **Dependencies**: A1-A5 | **Estimated**: 1 day  
**Status**: 🔵 Not Started | **Progress**: 0/1.5 points (0%)

#### Subtasks:

#### B1.1: Create Login Page Layout (0.5 points)

- [ ] Create `src/app/(auth)/login/page.tsx`
- [ ] Design page layout:
  - [ ] Logo and branding
  - [ ] Login form container
  - [ ] Link to register page
  - [ ] "Forgot password" link (placeholder)
- [ ] Style with Tailwind CSS
- [ ] Make responsive (mobile, tablet, desktop)

#### B1.2: Create Login Form Component (0.5 points)

- [ ] Create `src/components/auth/LoginForm.tsx`
- [ ] Add form fields:
  - [ ] Email input with validation
  - [ ] Password input with show/hide toggle
  - [ ] "Remember me" checkbox
- [ ] Use React Hook Form + Zod validation:
  - [ ] Email: required, valid format
  - [ ] Password: required, min 8 chars
- [ ] Display validation errors
- [ ] Add loading state on submit button

#### B1.3: Implement Login Logic (0.5 points)

- [ ] Integrate with authService.login()
- [ ] Handle successful login:
  - [ ] Store tokens in authStore
  - [ ] Store user data in authStore
  - [ ] Redirect to dashboard
- [ ] Handle errors:
  - [ ] Display error toast
  - [ ] Show inline form errors
  - [ ] Handle 401 (invalid credentials)
- [ ] Add loading spinner during request
- [ ] Test login flow end-to-end

**Deliverables**:

- [ ] Login page at /login
- [ ] Full form validation
- [ ] API integration working
- [ ] Responsive design
- [ ] Error handling

---

### Task B2: Register Page (1.5 points)

**Priority**: P0 | **Dependencies**: B1 | **Estimated**: 1 day  
**Status**: 🔵 Not Started | **Progress**: 0/1.5 points (0%)

#### Subtasks:

#### B2.1: Create Register Page Layout (0.5 points)

- [ ] Create `src/app/(auth)/register/page.tsx`
- [ ] Design page layout:
  - [ ] Logo and branding
  - [ ] Registration form container
  - [ ] Link to login page
  - [ ] Terms and conditions
- [ ] Style with Tailwind CSS
- [ ] Make responsive

#### B2.2: Create Register Form Component (0.5 points)

- [ ] Create `src/components/auth/RegisterForm.tsx`
- [ ] Add form fields:
  - [ ] Email input
  - [ ] Password input with strength indicator
  - [ ] Confirm password input
  - [ ] Terms checkbox
- [ ] Use React Hook Form + Zod validation:
  - [ ] Email: required, valid format
  - [ ] Password: required, min 8, uppercase, lowercase, number
  - [ ] Confirm password: must match password
  - [ ] Terms: must be accepted
- [ ] Display validation errors in real-time

#### B2.3: Implement Register Logic (0.5 points)

- [ ] Integrate with authService.register()
- [ ] Handle successful registration:
  - [ ] Auto-login after registration
  - [ ] Redirect to dashboard
  - [ ] Show success toast
- [ ] Handle errors:
  - [ ] Display error toast
  - [ ] Handle 409 (email already exists)
  - [ ] Show inline form errors
- [ ] Add loading spinner
- [ ] Test registration flow

**Deliverables**:

- [ ] Register page at /register
- [ ] Password strength indicator
- [ ] Full validation
- [ ] API integration
- [ ] Responsive design

---

### Task B3: JWT Token Management (1 point)

**Priority**: P0 | **Dependencies**: A4, B1, B2 | **Estimated**: 0.5 days  
**Status**: 🔵 Not Started | **Progress**: 0/1 points (0%)

#### Subtasks:

#### B3.1: Implement Token Storage (0.4 points)

**⚠️ SECURITY UPDATE**: Using httpOnly cookies instead of localStorage to prevent XSS attacks

- [ ] Create `src/lib/auth.ts`
- [ ] Implement token functions:
  - [ ] getAccessToken() → read from cookie (axios sends automatically)
  - [ ] getRefreshToken() → read from cookie
  - [ ] clearTokens() → call logout API to clear httpOnly cookies
  - [ ] Note: Backend sets cookies via `Set-Cookie` header with `HttpOnly; Secure; SameSite=Strict`
- [ ] Add token expiry check:
  - [ ] isTokenExpired(token) → decode JWT, check exp
- [ ] Document security decision in session notes
- [ ] Test token storage works

**Security Rationale**:

- ✅ httpOnly cookies cannot be accessed by JavaScript (XSS protection)
- ✅ Secure flag ensures HTTPS-only transmission
- ✅ SameSite=Strict prevents CSRF attacks
- ✅ Complies with OWASP best practices

#### B3.2: Implement Token Refresh (0.4 points)

- [ ] Update axios interceptor in api.ts
- [ ] On 401 response:
  - [ ] Call authService.refreshToken() (backend will refresh via httpOnly cookie)
  - [ ] Backend returns new access token in cookie
  - [ ] Retry original request automatically
  - [ ] If refresh fails (403/401) → logout user and redirect to /login
- [ ] Prevent multiple concurrent refresh requests (use mutex/lock pattern)
- [ ] Queue failed requests during refresh, replay after success
- [ ] Add offline detection (navigator.onLine)
- [ ] Test token refresh flow comprehensively

#### B3.3: Implement Auto-Logout (0.2 points)

- [ ] Create useAuth hook in `src/hooks/useAuth.ts`
- [ ] Add token expiry timer:
  - [ ] Check token expiry on mount
  - [ ] Set timeout to refresh before expiry
  - [ ] Auto-logout if refresh fails
- [ ] Test auto-logout works

**Deliverables**:

- [ ] src/lib/auth.ts with token functions
- [ ] Token refresh in axios interceptor
- [ ] useAuth hook with auto-logout
- [ ] All flows tested

---

### Task B4: Protected Routes Middleware (0.5 points)

**Priority**: P0 | **Dependencies**: B3 | **Estimated**: 0.25 days  
**Status**: 🔵 Not Started | **Progress**: 0/0.5 points (0%)

#### Subtasks:

#### B4.1: Create Middleware (0.3 points)

- [ ] Create `src/middleware.ts`
- [ ] Check authentication:
  - [ ] Get token from cookies/localStorage
  - [ ] If no token → redirect to /login
  - [ ] If token expired → redirect to /login
- [ ] Define public routes:
  - [ ] /login, /register (allow unauthenticated)
- [ ] Define protected routes:
  - [ ] /dashboard, /courses, /progress, /profile
- [ ] Test middleware redirects work

#### B4.2: Create Protected Route Component (0.2 points)

- [ ] Create `src/components/auth/ProtectedRoute.tsx`
- [ ] Check authentication on client:
  - [ ] Use authStore
  - [ ] Show loading spinner while checking
  - [ ] Redirect to login if not authenticated
- [ ] Wrap protected pages with component
- [ ] Test protection works

**Deliverables**:

- [ ] src/middleware.ts (Next.js middleware)
- [ ] ProtectedRoute component
- [ ] All routes protected correctly

---

### Task B5: Auth Store Refinement (0.5 points)

**Priority**: P0 | **Dependencies**: B1-B4 | **Estimated**: 0.25 days  
**Status**: 🔵 Not Started | **Progress**: 0/0.5 points (0%)

#### Subtasks:

#### B5.1: Add User Loading State (0.3 points)

- [ ] Update authStore with:
  - [ ] isLoading: boolean
  - [ ] loadUser() action
- [ ] Implement loadUser():
  - [ ] Check localStorage for tokens
  - [ ] Fetch user profile from API
  - [ ] Update store with user data
- [ ] Call loadUser() on app mount
- [ ] Show loading screen while loading

#### B5.2: Test Full Auth Flow (0.2 points)

- [ ] Test login → dashboard
- [ ] Test register → dashboard
- [ ] Test logout → login
- [ ] Test protected route access
- [ ] Test token refresh
- [ ] Test auto-logout
- [ ] Document any issues

**Deliverables**:

- [ ] Updated authStore with loading
- [ ] Full auth flow tested
- [ ] All edge cases handled

---

## 🎯 EPIC C: Dashboard & Layout (4 points)

**Status**: 🔵 Not Started | **Progress**: 0/4 points (0%)

---

### Task C1: Main Layout with Sidebar (1.5 points)

**Priority**: P0 | **Dependencies**: B1-B5 | **Estimated**: 1 day  
**Status**: 🔵 Not Started

#### Subtasks:

#### C1.1: Create Layout Component (0.5 points)

- [ ] Create `src/components/layout/MainLayout.tsx`
- [ ] Design layout structure:
  - [ ] Sidebar (fixed left)
  - [ ] Main content area
  - [ ] Mobile: collapsible sidebar
- [ ] Style with Tailwind CSS
- [ ] Make responsive

#### C1.2: Create Sidebar Component (0.5 points)

- [ ] Create `src/components/layout/Sidebar.tsx`
- [ ] Add navigation items:
  - [ ] Dashboard (Home icon)
  - [ ] Courses (BookOpen icon)
  - [ ] Progress (TrendingUp icon)
  - [ ] Profile (User icon)
- [ ] Highlight active route
- [ ] Add collapse toggle (desktop)
- [ ] Add logo/branding

#### C1.3: Add Mobile Navigation (0.5 points)

- [ ] Add hamburger menu button (mobile)
- [ ] Create slide-in sidebar (mobile)
- [ ] Add backdrop overlay
- [ ] Add touch gestures (swipe to open/close)
- [ ] Test on mobile devices

**Deliverables**:

- [ ] MainLayout component
- [ ] Sidebar component
- [ ] Responsive navigation
- [ ] Mobile menu working

---

### Task C2: Header with User Dropdown (0.5 points)

**Priority**: P0 | **Dependencies**: C1 | **Estimated**: 0.25 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### C2.1: Create Header Component (0.3 points)

- [ ] Create `src/components/layout/Header.tsx`
- [ ] Add elements:
  - [ ] Page title (dynamic)
  - [ ] Search bar (placeholder)
  - [ ] Notifications icon
  - [ ] User avatar/menu
- [ ] Style with Tailwind CSS

#### C2.2: Create User Dropdown (0.2 points)

- [ ] Use shadcn/ui DropdownMenu
- [ ] Add menu items:
  - [ ] Profile
  - [ ] Settings
  - [ ] Logout
- [ ] Add user name and email
- [ ] Add avatar with initials fallback
- [ ] Handle logout click

**Deliverables**:

- [ ] Header component
- [ ] User dropdown menu
- [ ] Logout functionality

---

### Task C3: Responsive Navigation (1 point)

**Priority**: P0 | **Dependencies**: C1, C2 | **Estimated**: 0.5 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### C3.1: Add Mobile Menu Animations (0.4 points)

- [ ] Add slide-in animation for sidebar
- [ ] Add fade-in animation for backdrop
- [ ] Add smooth transitions
- [ ] Test animations smooth on mobile

#### C3.2: Add Touch Gestures (0.3 points)

- [ ] Install swipe library (if needed)
- [ ] Add swipe-right to open sidebar
- [ ] Add swipe-left to close sidebar
- [ ] Add tap outside to close
- [ ] Test gestures work

#### C3.3: Test Responsive Design (0.3 points)

- [ ] Test on mobile (320px - 767px)
- [ ] Test on tablet (768px - 1023px)
- [ ] Test on desktop (1024px+)
- [ ] Fix any layout issues
- [ ] Verify navigation works on all sizes

**Deliverables**:

- [ ] Smooth animations
- [ ] Touch gestures working
- [ ] Responsive on all devices

---

### Task C4: Dashboard Home Page (1 point)

**Priority**: P0 | **Dependencies**: C1-C3 | **Estimated**: 0.5 days  
**Status**: 🔵 Not Started

#### Subtasks:

#### C4.1: Create Dashboard Page (0.4 points)

- [ ] Create `src/app/dashboard/page.tsx`
- [ ] Add welcome message with user name
- [ ] Create stats cards grid:
  - [ ] Enrolled Courses
  - [ ] Completed Lessons
  - [ ] Current Streak
- [ ] Fetch data from API
- [ ] Style with Tailwind CSS

#### C4.2: Create Stats Card Component (0.3 points)

- [ ] Create `src/components/dashboard/StatsCard.tsx`
- [ ] Props: title, value, icon, color
- [ ] Add loading skeleton
- [ ] Add hover effects
- [ ] Make reusable

#### C4.3: Add Recent Activity (0.3 points)

- [ ] Create recent activity section
- [ ] Show last 5 lessons completed
- [ ] Show enrollment history
- [ ] Add "Continue Learning" button
- [ ] Link to course pages

**Deliverables**:

- [ ] Dashboard page at /dashboard
- [ ] Stats cards with live data
- [ ] Recent activity section
- [ ] Loading states

---

## 🎯 EPIC D: Course & Learning Path (7 points)

**Status**: 🔵 Not Started | **Progress**: 0/7 points (0%)

---

### Task D1: Course Listing Page (2 points)

**Priority**: P0 | **Dependencies**: C1-C4 | **Estimated**: 1 day  
**Status**: 🔵 Not Started

#### Subtasks:

#### D1.1: Create Course List Page (0.5 points)

- [ ] Create `src/app/courses/page.tsx`
- [ ] Add page header with title
- [ ] Add search bar
- [ ] Add filter controls (CEFR level)
- [ ] Add sort dropdown
- [ ] Add grid/list view toggle
- [ ] Style layout

#### D1.2: Create Course Card Component (0.5 points)

- [ ] Create `src/components/courses/CourseCard.tsx`
- [ ] Display:
  - [ ] Course thumbnail
  - [ ] Title and description (truncated)
  - [ ] CEFR level badge
  - [ ] Enroll button
- [ ] Add hover effects
- [ ] Handle enroll click
- [ ] Make responsive

#### D1.3: Implement Search and Filter (0.5 points)

- [ ] Connect search bar to API
- [ ] Debounce search input (300ms)
- [ ] Connect filters to API (CEFR level)
- [ ] Update URL query params
- [ ] Show loading state while fetching
- [ ] Show empty state if no results

#### D1.4: Add Pagination (0.5 points)

- [ ] Fetch courses with pagination
- [ ] Add page controls (prev/next)
- [ ] Display current page info
- [ ] Update URL on page change
- [ ] Scroll to top on page change
- [ ] Test pagination works

**Deliverables**:

- [ ] Course listing page at /courses
- [ ] Search and filters working
- [ ] Pagination implemented
- [ ] Responsive design

---

### Task D2: Course Detail Page (1.5 points)

**Priority**: P0 | **Dependencies**: D1 | **Estimated**: 1 day  
**Status**: 🔵 Not Started

#### Subtasks:

#### D2.1: Create Course Detail Page (0.5 points)

- [ ] Create `src/app/courses/[id]/page.tsx`
- [ ] Fetch course by ID
- [ ] Display course header:
  - [ ] Title, description
  - [ ] Thumbnail/video
  - [ ] CEFR level badge
- [ ] Add enroll button
- [ ] Show loading skeleton
- [ ] Handle not found (404)

#### D2.2: Create Course Curriculum Section (0.5 points)

- [ ] Display sections and lessons
- [ ] Create Section component:
  - [ ] Section title
  - [ ] Collapsible lesson list
- [ ] Create Lesson list item:
  - [ ] Lesson title, type icon
  - [ ] Duration
  - [ ] Completion status (if enrolled)
- [ ] Make curriculum expandable/collapsible

#### D2.3: Implement Enrollment Flow (0.5 points)

- [ ] Handle enroll button click
- [ ] Call enrollmentService.enroll()
- [ ] Show success toast
- [ ] Update UI (show progress, access lessons)
- [ ] Handle already enrolled (409)
- [ ] Handle errors
- [ ] Test enrollment flow

**Deliverables**:

- [ ] Course detail page at /courses/[id]
- [ ] Curriculum displayed
- [ ] Enrollment working
- [ ] Error handling

---

### Task D3: Learning Path Display (1.5 points)

**Priority**: P1 | **Dependencies**: D2 | **Estimated**: 1 day  
**Status**: 🔵 Not Started

#### Subtasks:

#### D3.1: Create Learning Path Page (0.5 points)

- [ ] Create `src/app/learning-paths/page.tsx`
- [ ] Fetch all learning paths
- [ ] Display paths as cards:
  - [ ] Path name, description
  - [ ] CEFR level
  - [ ] Course count
  - [ ] Start button
- [ ] Show recommended path first

#### D3.2: Create Path Detail View (0.5 points)

- [ ] Create `src/components/courses/LearningPath.tsx`
- [ ] Visual path display:
  - [ ] Connected nodes (A1 → A2 → B1...)
  - [ ] Course cards in each level
  - [ ] Progress indicators
- [ ] Highlight current position
- [ ] Show completed courses (checkmarks)

#### D3.3: Implement Start Path Flow (0.5 points)

- [ ] Handle start path button
- [ ] Call pathService.startPath()
- [ ] Redirect to first course
- [ ] Show success message
- [ ] Handle already started
- [ ] Test flow

**Deliverables**:

- [ ] Learning paths page
- [ ] Visual path component
- [ ] Start path working
- [ ] Progress tracking

---

### Task D4: Lesson Viewer Interface (1.5 points)

**Priority**: P0 | **Dependencies**: D2 | **Estimated**: 1 day  
**Status**: 🔵 Not Started

#### Subtasks:

#### D4.1: Create Lesson Viewer Page (0.5 points)

- [ ] Create `src/app/courses/[courseId]/lessons/[lessonId]/page.tsx`
- [ ] Fetch lesson by ID
- [ ] Display lesson header:
  - [ ] Title, type, duration
  - [ ] Progress status
- [ ] Show loading skeleton

#### D4.2: Create Content Renderer (0.5 points)

- [ ] Create `src/components/lessons/ContentRenderer.tsx`
- [ ] Render based on lesson type:
  - [ ] READING: passages, questions
  - [ ] LISTENING: audio player, transcript
  - [ ] QUIZ: questions, options
  - [ ] SPEAKING: prompts, recording
- [ ] Parse JSONB content
- [ ] Style each type appropriately

#### D4.3: Add Complete Lesson Button (0.5 points)

- [ ] Add complete button at bottom
- [ ] Call progressService.completeLesson()
- [ ] Show success animation (confetti)
- [ ] Update progress UI
- [ ] Navigate to next lesson
- [ ] Handle errors

**Deliverables**:

- [ ] Lesson viewer at /courses/[courseId]/lessons/[lessonId]
- [ ] Content renders correctly for all types
- [ ] Completion working
- [ ] Navigation to next lesson

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

## � Sprint 3 Risks & Mitigations

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

- **Completed**: 12/83 subtasks (14%)
- **Story Points**: 4.0/28 points (14%)
- **Days Elapsed**: 4/14 days (29%)
- **Status**: 🔵 In Progress - On Track

### Completed Tasks

1. ✅ **A1** - Next.js Project Initialization (1 point) - Nov 8
2. ✅ **A2** - Tailwind CSS + shadcn/ui Setup (0.5 points) - Nov 8
3. ✅ **A3** - Zustand State Management (0.5 points) - Nov 8
4. ✅ **A4** - Axios API Client Setup (1 point) - Nov 8
5. ✅ **A5** - Environment Configuration (1 point) - Nov 8

### Current Sprint

- 📋 **Epic B** - Authentication Pages (5 points) - Starting Nov 11-12

### Upcoming Next

- 📋 **B1** - Login Page (1.5 points)
- 📋 **B2** - Register Page (1.5 points)
- 📋 **B3** - JWT Token Management (1 point)

### Sprint Health Indicators

- ✅ No blockers
- ✅ Project setup complete
- ✅ On schedule (14% done in 29% of time)
- ✅ Epic A complete (4.0/4.0 points)
- 🔵 Epic B ready to start (0/5 points)
- ✅ Backend API stable and available
- ✅ Test coverage infrastructure ready (Jest/RTL)
- ⚠️ Security update required: Implement httpOnly cookies for JWT (B3.1)
- ✅ Documentation updated with quality improvements

### Sprint Velocity Tracking

| Metric          | Target       | Current  | Status                            |
| --------------- | ------------ | -------- | --------------------------------- |
| Story Points    | 29 (updated) | 4        | 🔵 14%                            |
| Days Elapsed    | 14           | 4        | 🔵 29%                            |
| Velocity        | 2.1 pts/day  | 1 pt/day | ⚠️ Below target (catch-up needed) |
| Test Coverage   | 60%+         | 0%       | 🔵 Not started (Epic F)           |
| Tasks Completed | 90 (updated) | 12       | 🔵 13%                            |

**Velocity Analysis**:

- Expected at Day 4: ~8.4 points (29 × 29% ≈ 8.4)
- Actual at Day 4: 4 points
- **Gap**: -4.4 points (need to accelerate in Epic B-C)
- **Recommendation**: Focus on P0 tasks, consider pair programming for complex components

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
