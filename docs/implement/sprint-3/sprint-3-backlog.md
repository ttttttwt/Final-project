# 🎯 SPRINT 3 - LEXIA Frontend (Web)

**Duration**: November 8 – November 21, 2025 (14 days)  
**Target Story Points**: 29 points (Updated from 28)  
**Focus**: Next.js 14+ Web Application with Full Backend Integration  
**Status**: ⏳ In Progress (14% complete)  
**Last Updated**: November 11, 2025 (Security & Quality Updates Applied)

---

## 📊 Sprint Overview

### Objectives

1. ✅ Build fully functional Next.js web application
2. ✅ Integrate with all existing backend APIs (Sprint 1-2)
3. ✅ Implement responsive design (mobile, tablet, desktop)
4. ✅ Achieve 60%+ test coverage (Jest + React Testing Library)
5. ✅ Deploy development version for testing
6. 🔐 Implement secure JWT token management (httpOnly cookies)
7. ♿ Ensure WCAG AA accessibility compliance

### Success Criteria

- [ ] Users can register and login via web UI
- [ ] All courses displayed from API
- [ ] Enrollment and progress tracking works
- [ ] Profile management functional
- [ ] Responsive on all screen sizes (320px - 1920px)
- [ ] Loading states and error handling with retry logic
- [ ] Forms validated properly (Zod + React Hook Form)
- [ ] 60%+ test coverage (Jest + RTL)
- [ ] JWT tokens secure (httpOnly cookies, not localStorage)
- [ ] Accessibility audit passed (ARIA, keyboard nav, contrast)

### Quality Updates Applied (Nov 11, 2025)

**🔴 CRITICAL Security Fix**:

- ✅ Changed JWT storage from localStorage to httpOnly cookies
- ✅ Prevents XSS attacks (OWASP compliance)
- ✅ Backend sets cookies with HttpOnly, Secure, SameSite=Strict flags

**🟡 MAJOR Improvements**:

- ✅ Added Error Boundary component for React errors
- ✅ Enhanced API error handling (network, timeout, server errors)
- ✅ Added retry logic (3 attempts with exponential backoff)
- ✅ Defined coverage thresholds (60% global, 80% services)
- ✅ Added responsive design testing checklist

**🟢 Additional Features**:

- ✅ Added Accessibility Audit (Task F6 - 0.5 pts)
- ✅ WCAG AA compliance requirements
- ✅ Keyboard navigation testing
- ✅ Screen reader compatibility

### Velocity Constraints

- Sprint 1 velocity: 21 points (100% completion)
- Sprint 2 velocity: 21 points (100% completion)
- Sprint 3 capacity: 29 points (increased from 28 for quality improvements)
- Team size: 1 developer
- Working days: 14 days (November 8-21)

**Sprint 3 Adjustments**:

- Added +1 story point for security, testing, and accessibility improvements
- Focus on quality over speed
- Security-first approach (httpOnly cookies)
- Comprehensive error handling and testing

---

## 🎯 EPIC A: Project Setup & Configuration (4 points) ✅ COMPLETE

### Task A1: Next.js Project Initialization (1 point) ✅ COMPLETE

**Priority**: P0 (Must Have) | **Dependencies**: None

**Description**: Initialize Next.js 14+ project with TypeScript, Tailwind CSS, and App Router.

**Technical Details**:

- Use Next.js 14+ with App Router
- TypeScript for type safety
- Tailwind CSS for styling
- ESLint for code quality
- Folder structure: src/app, src/components, src/lib, src/services, src/store

**Acceptance Criteria**:

- [x] Project created with `create-next-app`
- [x] TypeScript configured
- [x] Tailwind CSS working
- [x] ESLint configured
- [x] Dev server runs on localhost:3000
- [x] Folder structure created

**Definition of Done**:

- [x] Next.js project initialized
- [x] All core dependencies installed
- [x] Project structure documented
- [x] Initial commit to git

---

### Task A2: Tailwind CSS + shadcn/ui Setup (0.5 points) ✅ COMPLETE

**Priority**: P0 (Must Have) | **Dependencies**: A1

**Description**: Configure Tailwind CSS and install shadcn/ui component library for consistent UI design.

**Technical Details**:

```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button input card form toast dropdown-menu badge
```

**Components Required**:

- Button, Input, Card, Form
- Toast (for notifications)
- DropdownMenu (for user menu)
- Badge (for CEFR levels)

**Acceptance Criteria**:

- [x] shadcn/ui initialized
- [x] 7 base components installed
- [x] components.json configured
- [x] Tailwind config customized
- [x] All components render correctly

**Definition of Done**:

- [x] shadcn/ui configured
- [x] All base components installed
- [x] Test page verifies components work
- [x] Tailwind customization applied

---

### Task A3: Zustand State Management (0.5 points) ✅ COMPLETE

**Priority**: P0 (Must Have) | **Dependencies**: A1

**Description**: Set up Zustand for global state management (auth, courses, progress).

**Technical Details**:

```typescript
// src/store/authStore.ts
export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  login: (tokens: TokenResponse, user: User) => void;
  logout: () => void;
  setUser: (user: User) => void;
  refreshAccessToken: () => Promise<void>;
}
```

**Stores Required**:

1. **authStore**: Authentication state and tokens
2. **courseStore**: Course filters and state
3. **progressStore**: Progress and streak data

**Acceptance Criteria**:

- [x] authStore created with localStorage persistence
- [x] courseStore created with filter state
- [x] progressStore created with progress state
- [x] All stores with TypeScript types
- [x] Test stores work independently

**Definition of Done**:

- [x] 3 Zustand stores created
- [x] localStorage integration for auth
- [x] TypeScript types defined
- [x] Store actions tested

---

### Task A4: Axios API Client Setup (1 point) ✅ COMPLETE

**Priority**: P0 (Must Have) | **Dependencies**: A1, A3

**Description**: Create Axios client with interceptors for JWT authentication and error handling.

**Technical Details**:

```typescript
// src/lib/api.ts
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 30000,
});

// Request interceptor - add JWT token
api.interceptors.request.use((config) => {
  const token = authStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle 401, refresh token
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Attempt token refresh
      // If refresh fails, logout user
    }
    return Promise.reject(error);
  }
);
```

**API Services Required**:

1. **authService**: login, register, refreshToken, logout
2. **courseService**: getCourses, getCourseById, searchCourses
3. **progressService**: getProgress, completeLesson, getStreak
4. **profileService**: getProfile, updateProfile, uploadAvatar

**Acceptance Criteria**:

- [x] Axios instance configured with baseURL
- [x] Request interceptor adds JWT token
- [x] Response interceptor handles 401/403
- [x] Token refresh logic implemented
- [x] 4 API service files created
- [x] TypeScript types for all API responses
- [x] Error handling with proper format

**Definition of Done**:

- [x] api.ts with interceptors
- [x] 4 service files with API functions
- [x] Type definitions for requests/responses
- [x] JSDoc comments on all functions

---

### Task A5: Environment Configuration (1 point) ✅ COMPLETE

**Priority**: P0 (Must Have) | **Dependencies**: A1, A4

**Description**: Configure environment variables and utility functions for the application.

**Technical Details**:

**Environment Variables**:

```env
# .env.local
NEXT_PUBLIC_API_URL=http://localhost:8088/api/v1
NEXT_PUBLIC_APP_NAME=LEXIA
NEXT_PUBLIC_APP_VERSION=1.0.0
```

**Utility Functions**:

```typescript
// src/lib/utils.ts
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs)); // Tailwind class merging
}

export function formatDate(date: Date): string {
  return format(date, "PPP"); // Format: Jan 1, 2025
}

export function formatDuration(minutes: number): string {
  return `${minutes} min`; // Format: 15 min
}

export function getInitials(name: string): string {
  return name
    .split(" ")
    .map((n) => n[0])
    .join(""); // Format: JD
}
```

**Constants**:

```typescript
// src/lib/constants.ts
export const CEFR_LEVELS = ["A1", "A2", "B1", "B2", "C1", "C2"];
export const LESSON_TYPES = ["READING", "LISTENING", "QUIZ", "SPEAKING"];
```

**Acceptance Criteria**:

- [x] .env.local created with API URL
- [x] .env.production template
- [x] .env.local.example for documentation
- [x] utils.ts with helper functions
- [x] constants.ts with app constants
- [x] next.config.js configured
- [x] tsconfig.json with path aliases

**Definition of Done**:

- [x] Environment files configured
- [x] Utility functions created
- [x] Constants defined
- [x] Next.js config updated
- [x] Documentation in README

---

## 🎯 EPIC B: Authentication Pages (5 points)

### Task B1: Login Page (1.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: A1-A5

**Description**: Create login page with form validation and backend integration.

**Technical Details**:

```typescript
// src/app/(auth)/login/page.tsx
const loginSchema = z.object({
  email: z.string().email("Invalid email address"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

type LoginFormData = z.infer<typeof loginSchema>;
```

**Features**:

- Email + password form
- React Hook Form + Zod validation
- "Remember me" checkbox
- "Forgot password" link (placeholder)
- Error display via toast
- Loading state during API call
- Redirect to dashboard on success

**Acceptance Criteria**:

- [ ] Login page at /login route
- [ ] Form validation with real-time feedback
- [ ] API integration with authService.login()
- [ ] JWT tokens stored in authStore
- [ ] Error handling (401, network errors)
- [ ] Loading spinner during submission
- [ ] Redirect to /dashboard on success
- [ ] Responsive design (mobile, tablet, desktop)

**Definition of Done**:

- [ ] Login page component created
- [ ] Form validation working
- [ ] API integration tested
- [ ] Error handling comprehensive
- [ ] Responsive on all devices
- [ ] Tests written (if applicable)

---

### Task B2: Register Page (1.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: B1

**Description**: Create registration page with password confirmation and validation.

**Technical Details**:

```typescript
// src/app/(auth)/register/page.tsx
const registerSchema = z
  .object({
    email: z.string().email("Invalid email"),
    password: z
      .string()
      .min(8, "At least 8 characters")
      .regex(/[A-Z]/, "At least one uppercase")
      .regex(/[a-z]/, "At least one lowercase")
      .regex(/[0-9]/, "At least one number"),
    confirmPassword: z.string(),
    acceptTerms: z.boolean().refine((val) => val === true, {
      message: "You must accept terms and conditions",
    }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });
```

**Features**:

- Email, password, confirm password fields
- Password strength indicator (visual)
- Terms and conditions checkbox
- Real-time validation feedback
- API integration with authService.register()
- Auto-login after successful registration
- Error handling (409 duplicate email)

**Acceptance Criteria**:

- [ ] Register page at /register route
- [ ] All fields validated (email, password, confirm)
- [ ] Password strength indicator visual
- [ ] Terms checkbox required
- [ ] API integration with authService.register()
- [ ] Auto-login and redirect to dashboard
- [ ] Error handling (409, validation errors)
- [ ] Responsive design

**Definition of Done**:

- [ ] Register page created
- [ ] Password strength indicator working
- [ ] Form validation comprehensive
- [ ] API integration tested
- [ ] Auto-login after registration
- [ ] Tests written

---

### Task B3: JWT Token Management (1 point)

**Priority**: P0 (Must Have) | **Dependencies**: A4, B1, B2

**Description**: Implement JWT token storage, refresh, and expiry handling.

**Technical Details**:

**⚠️ SECURITY UPDATE**: Using httpOnly cookies instead of localStorage

```typescript
// ❌ OLD APPROACH (Insecure - XSS vulnerable)
localStorage.setItem("accessToken", access);

// ✅ NEW APPROACH (Secure - XSS protected)
// Backend sets cookie:
Set-Cookie: accessToken=...; HttpOnly; Secure; SameSite=Strict

// Frontend axios config:
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  withCredentials: true, // Send cookies automatically
});

// No manual token storage needed!
// Cookies sent automatically with every request
```

**Token Refresh Logic**:

- On 401 response, call refresh endpoint (backend uses httpOnly cookie)
- Backend returns new access token in cookie
- Retry original request automatically
- If refresh fails, logout and redirect to /login
- Use mutex to prevent concurrent refresh requests
- Queue failed requests during refresh

**Why httpOnly Cookies?**:

- ✅ XSS Protection: JavaScript cannot access cookies
- ✅ CSRF Protection: SameSite=Strict prevents cross-site requests
- ✅ Secure: Transmitted only over HTTPS
- ✅ OWASP Compliance: Follows security best practices
- ✅ Auto-sent: Axios sends cookies automatically with `withCredentials: true`

**Acceptance Criteria**:

- [ ] Token storage functions (get from cookie, clear via logout API)
- [ ] Token expiry check function (decode JWT)
- [ ] Token refresh in axios interceptor (automatic)
- [ ] Mutex to prevent concurrent refreshes
- [ ] Auto-logout on refresh failure
- [ ] useAuth hook for token management
- [ ] Token refresh tested comprehensively
- [ ] Network error handling (retry 3 times)
- [ ] Offline detection (navigator.onLine)
- [ ] Security documented in session notes

**Definition of Done**:

- [ ] src/lib/auth.ts created
- [ ] Token refresh logic in api.ts
- [ ] useAuth hook created
- [ ] All flows tested (login, refresh, logout)
- [ ] Edge cases handled

---

### Task B4: Protected Routes Middleware (0.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: B3

**Description**: Create middleware to protect routes requiring authentication.

**Technical Details**:

```typescript
// src/middleware.ts
export function middleware(request: NextRequest) {
  const token = request.cookies.get("accessToken");
  const { pathname } = request.nextUrl;

  // Public routes
  if (pathname.startsWith("/login") || pathname.startsWith("/register")) {
    return NextResponse.next();
  }

  // Protected routes
  if (!token) {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico).*)"],
};
```

**Protected Routes**:

- /dashboard
- /courses (detail pages)
- /progress
- /profile
- /settings

**Public Routes**:

- /login
- /register
- / (home)

**Acceptance Criteria**:

- [ ] Middleware created in src/middleware.ts
- [ ] Authentication check on protected routes
- [ ] Redirect to /login if not authenticated
- [ ] Public routes accessible without auth
- [ ] ProtectedRoute component for client-side
- [ ] Loading state while checking auth

**Definition of Done**:

- [ ] Middleware configured
- [ ] Protected routes enforced
- [ ] Public routes accessible
- [ ] Client-side protection component
- [ ] All routes tested

---

### Task B5: Auth Store Refinement (0.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: B1-B4

**Description**: Add user loading state and initialization to auth store.

**Technical Details**:

```typescript
// src/store/authStore.ts
interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean; // NEW
  login: (tokens: TokenResponse, user: User) => void;
  logout: () => void;
  setUser: (user: User) => void;
  loadUser: () => Promise<void>; // NEW
}

const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true,

  loadUser: async () => {
    const token = getAccessToken();
    if (token && !isTokenExpired(token)) {
      try {
        const user = await profileService.getProfile();
        set({ user, accessToken: token, isAuthenticated: true });
      } catch (error) {
        clearTokens();
      }
    }
    set({ isLoading: false });
  },
}));
```

**User Initialization Flow**:

1. On app mount, check localStorage for tokens
2. If token exists and not expired, fetch user profile
3. Update store with user data
4. Set isLoading to false
5. If token missing or expired, set isLoading to false

**Acceptance Criteria**:

- [ ] isLoading state added to store
- [ ] loadUser() action implemented
- [ ] loadUser() called on app mount
- [ ] Loading screen shown while checking auth
- [ ] User data loaded from API if token valid
- [ ] Full auth flow tested (login → refresh → logout)

**Definition of Done**:

- [ ] Auth store updated
- [ ] User initialization working
- [ ] Loading state handled
- [ ] All auth flows tested
- [ ] Documentation updated

---

## 🎯 EPIC C: Dashboard & Layout (4 points)

### Task C1: Main Layout with Sidebar (1.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: B1-B5

**Description**: Create main application layout with responsive sidebar navigation.

**Technical Details**:

```typescript
// src/components/layout/MainLayout.tsx
const navItems = [
  { icon: Home, label: "Dashboard", href: "/dashboard" },
  { icon: BookOpen, label: "Courses", href: "/courses" },
  { icon: TrendingUp, label: "Progress", href: "/progress" },
  { icon: User, label: "Profile", href: "/profile" },
];
```

**Layout Structure**:

- Fixed sidebar on desktop (240px width)
- Collapsible sidebar toggle
- Main content area (flexible width)
- Mobile: slide-in sidebar with backdrop

**Acceptance Criteria**:

- [ ] MainLayout component created
- [ ] Sidebar component with navigation links
- [ ] Active link highlighting
- [ ] Collapse toggle (desktop)
- [ ] Logo and branding
- [ ] Responsive (mobile sidebar slides in)
- [ ] Smooth transitions

**Definition of Done**:

- [ ] Layout components created
- [ ] Navigation working
- [ ] Responsive on all devices
- [ ] Active link styling
- [ ] Tests written

---

### Task C2: Header with User Dropdown (0.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: C1

**Description**: Create header component with user profile dropdown menu.

**Technical Details**:

```typescript
// src/components/layout/Header.tsx
<DropdownMenu>
  <DropdownMenuTrigger>
    <Avatar>
      <AvatarImage src={user.avatarUrl} />
      <AvatarFallback>{getInitials(user.name)}</AvatarFallback>
    </Avatar>
  </DropdownMenuTrigger>
  <DropdownMenuContent>
    <DropdownMenuItem onClick={() => router.push("/profile")}>
      Profile
    </DropdownMenuItem>
    <DropdownMenuItem onClick={() => router.push("/settings")}>
      Settings
    </DropdownMenuItem>
    <DropdownMenuSeparator />
    <DropdownMenuItem onClick={handleLogout}>Logout</DropdownMenuItem>
  </DropdownMenuContent>
</DropdownMenu>
```

**Features**:

- User avatar with fallback (initials)
- User name and email display
- Dropdown menu (Profile, Settings, Logout)
- Notifications icon (placeholder)
- Search bar (placeholder)

**Acceptance Criteria**:

- [ ] Header component created
- [ ] User avatar with initials fallback
- [ ] Dropdown menu with 3 items
- [ ] Logout functionality working
- [ ] Responsive design

**Definition of Done**:

- [ ] Header component
- [ ] Dropdown menu working
- [ ] Logout tested
- [ ] Responsive

---

### Task C3: Responsive Navigation (1 point)

**Priority**: P0 (Must Have) | **Dependencies**: C1, C2

**Description**: Add mobile menu animations and touch gestures for sidebar.

**Technical Details**:

```typescript
// Mobile menu toggle
const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

// Framer Motion animations
<motion.div
  initial={{ x: "-100%" }}
  animate={{ x: isMobileMenuOpen ? 0 : "-100%" }}
  transition={{ type: "spring", stiffness: 300, damping: 30 }}
>
  {/* Sidebar content */}
</motion.div>;
```

**Features**:

- Hamburger menu button (mobile)
- Slide-in animation for sidebar
- Fade-in animation for backdrop
- Touch gestures (swipe right to open, left to close)
- Tap outside to close

**Acceptance Criteria**:

- [ ] Hamburger menu button
- [ ] Sidebar slides in smoothly
- [ ] Backdrop with fade animation
- [ ] Swipe gestures working
- [ ] Tap outside closes menu
- [ ] Tested on mobile devices (320px - 767px)
- [ ] Tested on tablet (768px - 1023px)
- [ ] Tested on desktop (1024px+)

**Definition of Done**:

- [ ] Animations smooth
- [ ] Touch gestures working
- [ ] Responsive on all devices
- [ ] No layout issues

---

### Task C4: Dashboard Home Page (1 point)

**Priority**: P0 (Must Have) | **Dependencies**: C1-C3

**Description**: Create dashboard home page with stats and recent activity.

**Technical Details**:

```typescript
// src/app/dashboard/page.tsx
<div className="grid grid-cols-1 md:grid-cols-3 gap-6">
  <StatsCard
    title="Enrolled Courses"
    value={enrollments.length}
    icon={BookOpen}
    color="blue"
  />
  <StatsCard
    title="Completed Lessons"
    value={completedLessons}
    icon={CheckCircle}
    color="green"
  />
  <StatsCard
    title="Current Streak"
    value={`${streak.currentStreak} days`}
    icon={Flame}
    color="orange"
  />
</div>
```

**Features**:

- Welcome message with user name
- Stats cards grid (3 cards):
  - Enrolled Courses
  - Completed Lessons
  - Current Streak
- Recent activity section:
  - Last 5 lessons completed
  - Enrollment history
- Continue learning section:
  - Next lesson to complete
  - Course progress bars

**Acceptance Criteria**:

- [ ] Dashboard page at /dashboard
- [ ] 3 stats cards with live data from API
- [ ] Recent activity list (last 5 items)
- [ ] Continue learning section
- [ ] Loading skeletons during data fetch
- [ ] Error handling
- [ ] Responsive design

**Definition of Done**:

- [ ] Dashboard page created
- [ ] StatsCard component reusable
- [ ] API integration working
- [ ] Loading states
- [ ] Responsive
- [ ] Tests written

---

## 🎯 EPIC D: Course & Learning Path (7 points)

### Task D1: Course Listing Page (2 points)

**Priority**: P0 (Must Have) | **Dependencies**: C1-C4

**Description**: Create course listing page with search, filters, and pagination.

**Technical Details**:

```typescript
// src/app/courses/page.tsx
const [courses, setCourses] = useState<Course[]>([]);
const [filters, setFilters] = useState<CourseFilters>({
  search: "",
  cefrLevel: null,
  sort: "popular",
});
const [page, setPage] = useState(0);
const [totalPages, setTotalPages] = useState(0);

useEffect(() => {
  fetchCourses(filters, page);
}, [filters, page]);
```

**Features**:

- Course grid/list view toggle
- Search bar with debounce (300ms)
- CEFR level filter (A1-C2)
- Sort dropdown (Popular, Recent, A-Z)
- Pagination controls
- Loading skeletons
- Empty state if no results

**Acceptance Criteria**:

- [ ] Course listing page at /courses
- [ ] Search bar with debounce working
- [ ] CEFR level filter working
- [ ] Sort functionality working
- [ ] Pagination working (prev/next + page numbers)
- [ ] Grid and list view toggle
- [ ] Loading skeletons during fetch
- [ ] Empty state displayed correctly
- [ ] URL query params updated on filter/page change
- [ ] Responsive design

**Definition of Done**:

- [ ] Course listing page created
- [ ] CourseCard component reusable
- [ ] All filters working
- [ ] Pagination tested
- [ ] API integration complete
- [ ] Responsive
- [ ] Tests written

---

### Task D2: Course Detail Page (1.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: D1

**Description**: Create course detail page with curriculum and enrollment functionality.

**Technical Details**:

```typescript
// src/app/courses/[id]/page.tsx
const { data: course, isLoading } = useCourse(params.id);
const { mutate: enroll, isLoading: isEnrolling } = useEnroll();

const handleEnroll = async () => {
  await enroll(course.id);
  toast.success("Enrolled successfully!");
  router.push(`/courses/${course.id}/lessons/${firstLessonId}`);
};
```

**Features**:

- Course header (title, description, thumbnail, CEFR badge)
- Enroll button (if not enrolled)
- Course curriculum (sections and lessons):
  - Collapsible sections
  - Lesson items with type icon, duration
  - Completion checkmarks (if enrolled)
- Progress bar (if enrolled)
- Learning path reference
- Reviews/ratings section (placeholder)

**Acceptance Criteria**:

- [ ] Course detail page at /courses/[id]
- [ ] Course data fetched and displayed
- [ ] Curriculum sections collapsible
- [ ] Lesson items show type, duration
- [ ] Enroll button works (API call)
- [ ] Success toast on enrollment
- [ ] Redirect to first lesson after enrollment
- [ ] Handle 409 (already enrolled)
- [ ] Progress bar if enrolled
- [ ] Loading skeleton
- [ ] 404 handling if course not found
- [ ] Responsive design

**Definition of Done**:

- [ ] Course detail page created
- [ ] Enrollment flow tested
- [ ] Curriculum display working
- [ ] API integration complete
- [ ] Error handling
- [ ] Responsive
- [ ] Tests written

---

### Task D3: Learning Path Display (1.5 points)

**Priority**: P1 (Should Have) | **Dependencies**: D2

**Description**: Create learning path display with visual nodes and progress indicators.

**Technical Details**:

```typescript
// src/components/courses/LearningPath.tsx
const levels = ["A1", "A2", "B1", "B2", "C1", "C2"];

return (
  <div className="flex items-center gap-4">
    {levels.map((level, index) => (
      <div key={level} className="flex items-center">
        <LevelNode
          level={level}
          courses={getCoursesByLevel(level)}
          isCompleted={isLevelCompleted(level)}
          isCurrent={currentLevel === level}
        />
        {index < levels.length - 1 && <Arrow />}
      </div>
    ))}
  </div>
);
```

**Features**:

- Visual path with connected nodes (A1 → A2 → B1 → B2 → C1 → C2)
- Each node shows CEFR level
- Course cards in each level
- Progress indicators (completed, in-progress, not-started)
- Current position highlight
- Start path button
- Recommended path badge

**Acceptance Criteria**:

- [ ] Learning paths page at /learning-paths
- [ ] Visual path with 6 nodes (A1-C2)
- [ ] Connected arrows between nodes
- [ ] Course cards in each level
- [ ] Progress indicators (colors/icons)
- [ ] Current position highlighted
- [ ] Start path button working (API call)
- [ ] Handle already started (show progress)
- [ ] Responsive design (scroll horizontally on mobile)

**Definition of Done**:

- [ ] Learning path page created
- [ ] Visual path component
- [ ] Start path flow tested
- [ ] API integration complete
- [ ] Responsive
- [ ] Tests written

---

### Task D4: Lesson Viewer Interface (1.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: D2

**Description**: Create lesson viewer with content rendering for all lesson types.

**Technical Details**:

```typescript
// src/components/lessons/ContentRenderer.tsx
const renderContent = (lesson: Lesson) => {
  switch (lesson.lessonType) {
    case "READING":
      return <ReadingContent content={lesson.content} />;
    case "LISTENING":
      return <ListeningContent content={lesson.content} />;
    case "QUIZ":
      return <QuizContent content={lesson.content} />;
    case "SPEAKING":
      return <SpeakingContent content={lesson.content} />;
  }
};
```

**Lesson Types Rendering**:

1. **READING**: Passages, vocabulary, comprehension questions
2. **LISTENING**: Audio player, transcript, questions
3. **QUIZ**: Multiple choice questions, submit button
4. **SPEAKING**: Role-play prompts, recording interface (placeholder)

**Acceptance Criteria**:

- [ ] Lesson viewer at /courses/[courseId]/lessons/[lessonId]
- [ ] Lesson header (title, type, duration)
- [ ] Content renders correctly for all 4 types
- [ ] READING: passages and questions displayed
- [ ] LISTENING: audio player works, transcript shown
- [ ] QUIZ: questions and options interactive
- [ ] SPEAKING: prompts shown (recording placeholder)
- [ ] Complete lesson button at bottom
- [ ] Confetti animation on completion
- [ ] Progress updated in backend
- [ ] Navigate to next lesson after completion
- [ ] Loading skeleton
- [ ] Responsive design

**Definition of Done**:

- [ ] Lesson viewer page created
- [ ] ContentRenderer component handles all types
- [ ] Complete lesson flow tested
- [ ] API integration complete
- [ ] Confetti animation working
- [ ] Responsive
- [ ] Tests written

---

### Task D5: Lesson Navigation (0.5 points)

**Priority**: P1 (Should Have) | **Dependencies**: D4

**Description**: Add previous/next lesson navigation controls.

**Technical Details**:

```typescript
// src/components/lessons/LessonNavigation.tsx
const { prevLessonId, nextLessonId, currentIndex, totalLessons } = useLessonNav(
  courseId,
  lessonId
);

<div className="flex justify-between items-center">
  <Button
    disabled={!prevLessonId}
    onClick={() => router.push(`/courses/${courseId}/lessons/${prevLessonId}`)}
  >
    <ChevronLeft /> Previous
  </Button>
  <span>
    Lesson {currentIndex + 1} of {totalLessons}
  </span>
  <Button
    disabled={!nextLessonId}
    onClick={() => router.push(`/courses/${courseId}/lessons/${nextLessonId}`)}
  >
    Next <ChevronRight />
  </Button>
</div>;
```

**Features**:

- Previous lesson button
- Next lesson button
- Progress indicator: "Lesson X of Y"
- Back to course button
- Disable prev on first lesson
- Disable next on last lesson
- Handle cross-section navigation

**Acceptance Criteria**:

- [ ] Navigation component created
- [ ] Previous/next buttons working
- [ ] Progress indicator shows correct count
- [ ] Buttons disabled appropriately
- [ ] Cross-section navigation works
- [ ] Back to course button works

**Definition of Done**:

- [ ] Navigation component
- [ ] Navigation logic tested
- [ ] All edge cases handled
- [ ] Responsive

---

## 🎯 EPIC E: Progress & Profile (5 points)

### Task E1: Progress Dashboard with Charts (2 points)

**Priority**: P0 (Must Have) | **Dependencies**: D1-D5, D4

**Description**: Create progress dashboard with charts and streak visualization.

**Technical Details**:

```typescript
// src/app/progress/page.tsx
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

<ResponsiveContainer width="100%" height={300}>
  <LineChart data={progressData}>
    <XAxis dataKey="date" />
    <YAxis />
    <Tooltip />
    <Line type="monotone" dataKey="lessonsCompleted" stroke="#3b82f6" />
  </LineChart>
</ResponsiveContainer>;
```

**Features**:

- Stats grid (4 cards):
  - Total Lessons Completed
  - Total Time Spent
  - Current Streak
  - Longest Streak
- Progress line chart (last 30 days)
- Streak calendar heatmap (last 365 days)
- Achievements section (placeholder)

**Acceptance Criteria**:

- [ ] Progress page at /progress
- [ ] 4 stats cards with live data
- [ ] Line chart showing progress over time
- [ ] Streak calendar heatmap (365 days)
- [ ] Chart tooltips working
- [ ] Responsive design (chart scales)
- [ ] Loading skeletons
- [ ] API integration complete

**Definition of Done**:

- [ ] Progress page created
- [ ] Charts rendering with real data
- [ ] Streak calendar working
- [ ] API integration complete
- [ ] Responsive
- [ ] Tests written

---

### Task E2: Lesson Completion Tracking UI (1 point)

**Priority**: P0 (Must Have) | **Dependencies**: D4, E1

**Description**: Add completion checkmarks and celebration animations.

**Technical Details**:

```typescript
// Confetti animation
import confetti from "canvas-confetti";

const celebrateCompletion = () => {
  confetti({
    particleCount: 100,
    spread: 70,
    origin: { y: 0.6 },
  });
};
```

**Features**:

- Checkmarks on completed lessons (course curriculum)
- Completion percentage per course
- Last completed timestamp
- Confetti animation on lesson complete
- Success modal/toast with stats
- "Next Lesson" button in modal

**Acceptance Criteria**:

- [ ] Checkmarks show on completed lessons
- [ ] Completion percentage displayed
- [ ] Confetti animation triggers on complete
- [ ] Success modal shows completion stats
- [ ] "Next Lesson" button works
- [ ] Updates in real-time after completion

**Definition of Done**:

- [ ] Checkmarks working
- [ ] Confetti animation
- [ ] Success modal
- [ ] Real-time updates
- [ ] Tests written

---

### Task E3: Profile Management Page (1 point)

**Priority**: P0 (Must Have) | **Dependencies**: C4

**Description**: Create profile page with view and edit modes.

**Technical Details**:

```typescript
// src/app/profile/page.tsx
const profileSchema = z.object({
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  bio: z.string().max(500).optional(),
  phoneNumber: z
    .string()
    .regex(/^\+?[1-9]\d{1,14}$/)
    .optional(),
  timezone: z.string(),
  language: z.string().length(2),
});
```

**Features**:

- View mode (display profile info)
- Edit mode (form with all fields)
- Fields: firstName, lastName, bio, phoneNumber, timezone, language
- Save button with loading state
- Cancel button to discard changes
- Success toast on save
- Validation errors displayed

**Acceptance Criteria**:

- [ ] Profile page at /profile
- [ ] View mode displays all info
- [ ] Edit button switches to edit mode
- [ ] All fields editable
- [ ] Timezone dropdown with all timezones
- [ ] Language dropdown (EN, VI, etc.)
- [ ] Form validation working
- [ ] Save updates profile via API
- [ ] Success toast on save
- [ ] Cancel discards changes
- [ ] Responsive design

**Definition of Done**:

- [ ] Profile page created
- [ ] View/edit modes working
- [ ] Form validation complete
- [ ] API integration tested
- [ ] Responsive
- [ ] Tests written

---

### Task E4: Avatar Upload Interface (0.5 points)

**Priority**: P1 (Should Have) | **Dependencies**: E3

**Description**: Add avatar upload functionality with preview.

**Technical Details**:

```typescript
const handleAvatarUpload = async (file: File) => {
  const formData = new FormData();
  formData.append("avatar", file);

  await profileService.uploadAvatar(formData);
  toast.success("Avatar updated!");
  refreshProfile();
};
```

**Features**:

- Current avatar display (or initials)
- Upload button
- File selection (accepts: .jpg, .png, .gif)
- Image preview before upload
- Crop tool (optional)
- Delete avatar button
- Upload progress indicator

**Acceptance Criteria**:

- [ ] Avatar upload component in profile
- [ ] Upload button triggers file selection
- [ ] Image preview before upload
- [ ] Upload sends multipart/form-data to API
- [ ] Progress indicator during upload
- [ ] Delete avatar button works
- [ ] Avatar updates in UI after upload
- [ ] File size validation (max 5MB)
- [ ] File type validation

**Definition of Done**:

- [ ] Avatar upload component
- [ ] Upload flow tested
- [ ] Preview working
- [ ] API integration complete
- [ ] Error handling
- [ ] Tests written

---

### Task E5: Settings Page (0.5 points)

**Priority**: P1 (Should Have) | **Dependencies**: E3

**Description**: Create settings page for preferences and notifications.

**Technical Details**:

```typescript
// src/app/settings/page.tsx
const [settings, setSettings] = useState({
  language: "en",
  timezone: "UTC",
  emailNotifications: true,
  pushNotifications: false,
  theme: "light", // placeholder
});
```

**Features**:

- Language preference dropdown
- Timezone setting dropdown
- Email notifications toggle
- Push notifications toggle (placeholder)
- Theme selector (light/dark - placeholder)
- Account deletion button (placeholder)
- Save button

**Acceptance Criteria**:

- [ ] Settings page at /settings
- [ ] All settings displayed
- [ ] Toggles work (Switch component)
- [ ] Dropdowns populated with options
- [ ] Save updates settings via API
- [ ] Success toast on save
- [ ] Theme toggle (placeholder - no functionality yet)
- [ ] Responsive design

**Definition of Done**:

- [ ] Settings page created
- [ ] All settings working
- [ ] API integration complete
- [ ] Responsive
- [ ] Tests written

---

## 🎯 EPIC F: Testing & Polish (3 points)

### Task F1: Form Validation Refinement (0.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: B1-B2, E3

**Description**: Review and refine all form validations across the application.

**Acceptance Criteria**:

- [ ] All forms use React Hook Form + Zod
- [ ] Real-time validation feedback
- [ ] Clear, user-friendly error messages
- [ ] Field-level errors displayed
- [ ] Form-level errors displayed
- [ ] Submit button disabled on validation errors
- [ ] Custom validation rules tested

**Definition of Done**:

- [ ] All forms validated
- [ ] Error messages consistent
- [ ] Edge cases tested
- [ ] User feedback clear

---

### Task F2: Error Handling + Toast Notifications (0.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: All

**Description**: Implement global error handling and toast notifications.

**Technical Details**:

```typescript
// src/components/ErrorBoundary.tsx
class ErrorBoundary extends React.Component {
  componentDidCatch(error, errorInfo) {
    logError(error, errorInfo);
    toast.error("Something went wrong. Please try again.");
  }

  render() {
    if (this.state.hasError) {
      return <ErrorFallback />;
    }
    return this.props.children;
  }
}
```

**Features**:

- react-hot-toast configured
- Success toasts for actions (login, enroll, complete, etc.)
- Error toasts for failures (network, validation, etc.)
- Error boundary component
- 404 page (course/lesson not found)
- 500 error page (server error)
- Network error handling
- Retry mechanisms for failed requests

**Acceptance Criteria**:

- [ ] react-hot-toast configured
- [ ] Toaster component in layout
- [ ] Success toasts on actions
- [ ] Error toasts on failures
- [ ] Error boundary catches errors
- [ ] 404 page created
- [ ] 500 error page created
- [ ] Network errors handled gracefully
- [ ] Retry button on errors

**Definition of Done**:

- [ ] Toast notifications working
- [ ] Error boundary in place
- [ ] Error pages created
- [ ] All error scenarios handled
- [ ] Tests written

---

### Task F3: Loading States + Skeletons (0.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: All

**Description**: Add loading states and skeleton loaders throughout the application.

**Technical Details**:

```typescript
// src/components/skeletons/CourseCardSkeleton.tsx
<Card>
  <Skeleton className="h-48 w-full" />
  <CardContent>
    <Skeleton className="h-6 w-3/4 mb-2" />
    <Skeleton className="h-4 w-full mb-1" />
    <Skeleton className="h-4 w-2/3" />
  </CardContent>
</Card>
```

**Features**:

- Button loading spinners (during actions)
- Page-level loading indicators
- Skeleton loaders for:
  - Course cards
  - Stats cards
  - Lesson content
  - Profile info
- Shimmer animation effect
- Optimistic updates where possible

**Acceptance Criteria**:

- [ ] Loading spinners on all action buttons
- [ ] Skeleton loaders for all data lists
- [ ] Skeleton loaders match content layout
- [ ] Shimmer effect smooth
- [ ] Optimistic updates for key actions
- [ ] No jarring transitions
- [ ] Loading states consistent

**Definition of Done**:

- [ ] Skeleton components created
- [ ] Loading states on all pages
- [ ] Shimmer animation working
- [ ] User experience smooth
- [ ] Tests written

---

### Task F4: Jest + React Testing Library Setup (0.5 points)

**Priority**: P0 (Must Have) | **Dependencies**: None

**Description**: Configure Jest and React Testing Library for component testing.

**Technical Details**:

```bash
npm install -D jest @types/jest @testing-library/react @testing-library/jest-dom @testing-library/user-event jest-environment-jsdom
```

```javascript
// jest.config.js
module.exports = {
  testEnvironment: "jsdom",
  setupFilesAfterEnv: ["<rootDir>/jest.setup.js"],
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
  },
  collectCoverageFrom: [
    "src/**/*.{ts,tsx}",
    "!src/**/*.d.ts",
    "!src/**/*.stories.tsx",
  ],
  coverageThreshold: {
    global: {
      branches: 60,
      functions: 60,
      lines: 60,
      statements: 60,
    },
  },
};
```

**Acceptance Criteria**:

- [ ] Jest installed and configured
- [ ] RTL installed
- [ ] jest.config.js created
- [ ] jest.setup.js created
- [ ] Test scripts in package.json (test, test:watch, test:coverage)
- [ ] Test utils created (render with providers)
- [ ] Mock API responses setup
- [ ] Sample test passes

**Definition of Done**:

- [ ] Jest configured
- [ ] RTL configured
- [ ] Test scripts working
- [ ] Test utils created
- [ ] Documentation updated

---

### Task F5: Component Unit Tests (1 point)

**Priority**: P0 (Must Have) | **Dependencies**: F4

**Description**: Write comprehensive unit tests for key components.

**Test Coverage Target**: 60%+ overall

**Components to Test**:

1. **Authentication**:

   - LoginForm: validation, submission, error display
   - RegisterForm: validation, password strength, submission

2. **Courses**:

   - CourseCard: rendering, enroll button, hover
   - CourseList: rendering grid, search, filter

3. **Dashboard**:

   - StatsCard: data display, loading state
   - ProgressChart: chart renders, data visualization

4. **Navigation**:
   - Sidebar: links render, active state
   - Header: dropdown menu, logout

**Test Example**:

```typescript
// src/components/auth/__tests__/LoginForm.test.tsx
describe("LoginForm", () => {
  it("should validate email format", async () => {
    render(<LoginForm />);
    const emailInput = screen.getByLabelText(/email/i);
    await userEvent.type(emailInput, "invalid-email");
    await userEvent.tab();
    expect(screen.getByText(/invalid email/i)).toBeInTheDocument();
  });

  it("should submit form with valid data", async () => {
    const onSubmit = jest.fn();
    render(<LoginForm onSubmit={onSubmit} />);

    await userEvent.type(screen.getByLabelText(/email/i), "test@example.com");
    await userEvent.type(screen.getByLabelText(/password/i), "password123");
    await userEvent.click(screen.getByRole("button", { name: /login/i }));

    expect(onSubmit).toHaveBeenCalledWith({
      email: "test@example.com",
      password: "password123",
    });
  });
});
```

**Acceptance Criteria**:

- [ ] 20+ component tests written
- [ ] All critical components tested
- [ ] User interactions tested (click, type, submit)
- [ ] API integration tested (mocked)
- [ ] Edge cases tested
- [ ] 60%+ code coverage achieved
- [ ] All tests passing
- [ ] Coverage report generated

**Definition of Done**:

- [ ] Component tests written
- [ ] Coverage target met (60%+)
- [ ] All tests passing
- [ ] Coverage report generated
- [ ] Documentation updated

---

## � Sprint 3 Risks & Mitigations

### High-Impact Risks

| Risk                                  | Probability  | Impact | Mitigation Strategy                                                                                                                           | Owner        |
| ------------------------------------- | ------------ | ------ | --------------------------------------------------------------------------------------------------------------------------------------------- | ------------ |
| **Backend API contract changes**      | Low (10%)    | High   | • API versioning enforced (v1)<br>• Contract tests before integration<br>• Mock API for development<br>• Regular sync with backend team       | Frontend Dev |
| **JWT token refresh bugs**            | Medium (30%) | High   | • Comprehensive token lifecycle testing<br>• Fallback logout mechanism<br>• Token expiry monitoring<br>• Retry logic with exponential backoff | Frontend Dev |
| **Test coverage below 60% target**    | Medium (40%) | High   | • TDD: write tests alongside code<br>• Daily coverage monitoring<br>• Block PR if coverage drops<br>• Prioritize critical path testing        | Frontend Dev |
| **Responsive design fails on mobile** | Medium (30%) | Medium | • Mobile-first CSS methodology<br>• Test on real devices early<br>• Use Chrome DevTools device emulation<br>• Breakpoint testing checklist    | Frontend Dev |

### Medium-Impact Risks

| Risk                                  | Probability  | Impact | Mitigation Strategy                                                                        | Owner        |
| ------------------------------------- | ------------ | ------ | ------------------------------------------------------------------------------------------ | ------------ |
| **shadcn/ui component conflicts**     | Low (15%)    | Medium | • Pin exact versions in package.json<br>• Test after updates<br>• Custom wrapper if needed | Frontend Dev |
| **Axios interceptor race conditions** | Low (20%)    | Medium | • Mutex for token refresh<br>• Queue failed requests<br>• Handle concurrent requests       | Frontend Dev |
| **Zustand state complexity**          | Medium (25%) | Low    | • Keep stores focused<br>• Document state flow<br>• Avoid deeply nested updates            | Frontend Dev |
| **Performance issues (large lists)**  | Low (15%)    | Medium | • Pagination everywhere<br>• Virtual scrolling<br>• Lazy load images                       | Frontend Dev |

### Dependencies & External Blockers

| Dependency                   | Status           | Required By | Contingency Plan              | Last Checked |
| ---------------------------- | ---------------- | ----------- | ----------------------------- | ------------ |
| **Backend API (Sprint 1-2)** | ✅ Stable        | All tasks   | Mock API server (json-server) | Nov 11, 2025 |
| **Design assets**            | ⚠️ Partial       | UI polish   | Lucide icons + placeholders   | Nov 11, 2025 |
| **Test environment**         | 🔵 Setup pending | F4-F5       | Local Jest first              | Nov 11, 2025 |
| **Production hosting**       | 🔵 Not started   | Deployment  | Vercel free tier              | TBD          |

### Risk Monitoring Schedule

**Daily Standup Checks** (9:00 AM):

- [ ] Any new blockers emerged?
- [ ] Test coverage still on track?
- [ ] API integration issues?
- [ ] Team member blocked?

**Mid-Sprint Review** (Day 7 - Nov 15):

- [ ] Re-assess risk probabilities
- [ ] Update mitigation strategies
- [ ] Escalate critical risks
- [ ] Adjust sprint scope if needed

**End-Sprint Retrospective** (Day 14 - Nov 21):

- [ ] Document what risks materialized
- [ ] Lessons learned
- [ ] Update risk register for Sprint 4
- [ ] Share best practices

### Escalation Path

```
Level 1 (Minor): Self-resolve within 2 hours
       ↓ (unresolved)
Level 2 (Moderate): Team discussion, 1 day
       ↓ (unresolved)
Level 3 (Critical): Scope adjustment, re-planning
```

---

## �📊 Sprint 3 Summary

### Total Story Points: 28

| Epic                      | Story Points | Priority | Status         |
| ------------------------- | ------------ | -------- | -------------- |
| A: Project Setup & Config | 4            | P0       | ✅ Complete    |
| B: Authentication Pages   | 5            | P0       | 🔵 Next Up     |
| C: Dashboard & Layout     | 4            | P0       | 🔵 Not Started |
| D: Course & Learning Path | 7            | P0       | 🔵 Not Started |
| E: Progress & Profile     | 5            | P0       | 🔵 Not Started |
| F: Testing & Polish       | 3            | P0       | 🔵 Not Started |

### Technology Stack

**Framework & Language**:

- Next.js 14+ (App Router)
- TypeScript
- React 18

**Styling**:

- Tailwind CSS
- shadcn/ui components
- Lucide React (icons)

**State Management**:

- Zustand (global state)

**Forms & Validation**:

- React Hook Form
- Zod schema validation

**API & Data**:

- Axios (HTTP client)
- SWR (optional - data fetching)

**Charts**:

- Recharts

**Notifications**:

- react-hot-toast

**Testing**:

- Jest
- React Testing Library
- @testing-library/user-event

### Testing Strategy

**Unit Testing** (60%+ coverage target):

- Component testing with React Testing Library
- Hook testing with renderHook
- Utility function testing
- Store testing (Zustand)

**Integration Testing** (Key flows):

- Auth flow: Register → Login → Dashboard
- Enrollment flow: Browse → Detail → Enroll
- Lesson flow: View → Complete → Next
- Profile flow: View → Edit → Save

**E2E Testing** (Future - Sprint 4+):

- Cypress or Playwright
- Critical user journeys
- Cross-browser testing

**Testing Approach**:

```typescript
// TDD Approach - Write tests FIRST
describe("LoginForm", () => {
  it("should validate email format", async () => {
    // Write test first
  });
});

// Then implement component
export function LoginForm() {
  // Implement to pass test
}
```

**Coverage Targets**:

- Overall: 60%+
- Critical components (Auth, Course): 80%+
- Utility functions: 90%+
- Stores: 70%+

**Mocking Strategy**:

```typescript
// Mock API responses
jest.mock("@/lib/api", () => ({
  api: {
    post: jest.fn(),
    get: jest.fn(),
  },
}));

// Mock Next.js router
jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
    back: jest.fn(),
  }),
}));
```

### Success Criteria

At the end of Sprint 3 (November 21, 2025):

- [x] ✅ Next.js web application running
- [ ] ✅ Users can register and login
- [ ] ✅ All courses displayed from backend API
- [ ] ✅ Enrollment and progress tracking works
- [ ] ✅ Profile management functional
- [ ] ✅ Responsive design (mobile, tablet, desktop)
- [ ] ✅ 60%+ test coverage achieved
- [ ] ✅ All 28 story points delivered

### Sprint Health Metrics

**Track Daily**:

- Story points completed (target: 2 pts/day)
- Test coverage percentage (target: 60%+)
- Open bugs/issues (target: < 3)
- Blocked tasks (target: 0)

**Current Status** (as of Nov 11, Day 4):

- ✅ Points completed: 4/28 (14%)
- ✅ Days elapsed: 4/14 (29%)
- ⚠️ Velocity: 1 pt/day (below 2 pt/day target)
- ✅ Blockers: 0
- 🔵 Test coverage: Not started (Epic F)

**Velocity Alert**: Currently behind schedule by ~4 points. Need to accelerate in Epic B-C.

### Definition of Done (Sprint Level)

- [ ] All 28 story points completed
- [ ] All acceptance criteria met
- [ ] 60%+ test coverage (Jest + RTL)
- [ ] All tests passing
- [ ] Responsive on all device sizes
- [ ] No console errors in browser
- [ ] Code reviewed and committed
- [ ] Documentation updated
- [ ] Sprint retrospective completed

---

**Last Updated**: November 11, 2025  
**Next Review**: November 14, 2025 (Mid-Sprint Check-In)
