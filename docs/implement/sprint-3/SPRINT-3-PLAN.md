# SPRINT 3 - FRONTEND DEVELOPMENT (WEB) - IMPLEMENTATION PLAN

**Sprint**: 3 / 8  
**Duration**: November 8-21, 2025 (14 days)  
**Status**: ⏳ Ready to Start  
**Estimated Story Points**: 28 points

---

## 🎯 Sprint Goals

### Primary Objectives

1. ✅ Build fully functional Next.js web application
2. ✅ Integrate with all existing backend APIs (Sprint 1-2)
3. ✅ Implement responsive design (mobile, tablet, desktop)
4. ✅ Achieve 60%+ test coverage
5. ✅ Deploy development version for testing

### Success Criteria

- [ ] Users can register and login via web UI
- [ ] All courses displayed from API
- [ ] Enrollment and progress tracking works
- [ ] Profile management functional
- [ ] Responsive on all screen sizes
- [ ] Loading states and error handling
- [ ] Forms validated properly
- [ ] 60%+ test coverage (Jest + RTL)

---

## 📅 Two-Week Timeline

### **Week 1: Foundation & Authentication**

**Day 1-2 (Nov 8-9): Project Setup** ⚡

```
✓ Initialize Next.js 14+ with App Router
✓ Configure TypeScript
✓ Setup Tailwind CSS
✓ Install shadcn/ui components
✓ Configure Zustand store
✓ Setup Axios API client
✓ Environment variables
✓ Git repository structure
```

**Day 3-4 (Nov 10-11): Authentication** 🔐

```
✓ Login page with form validation
✓ Register page with password confirmation
✓ Secure session management via httpOnly cookies (no localStorage)
✓ Token refresh logic in axios interceptor (Promise lock)
✓ Auth store (Zustand) without token fields
✓ Protected route middleware calls backend /auth/session
✓ Logout functionality (server clears cookies)
```

**Day 5-7 (Nov 12-14): Layout & Dashboard** 🏠

```
✓ Main layout component
✓ Sidebar navigation
✓ Header with user dropdown
✓ Responsive mobile menu
✓ Dashboard home page
✓ Stats cards (enrolled courses, progress)
✓ Recent activity component
```

### **Week 2: Core Features & Testing**

**Day 8-9 (Nov 15-16): Course Features** 📚

```
✓ Course listing page
✓ Search and filter functionality
✓ Course card component
✓ Course detail page
✓ Enrollment button with API call
✓ Learning path display
✓ CEFR level badges
```

**Day 10-11 (Nov 17-18): Lessons & Progress** 📊

```
✓ Lesson viewer interface
✓ Lesson navigation (prev/next)
✓ Progress tracking UI
✓ Completion checkmarks
✓ Progress dashboard with charts
✓ Streak display
✓ Statistics cards
```

**Day 12-13 (Nov 19-20): Profile & Polish** 👤

```
✓ Profile page (view/edit)
✓ Avatar upload interface
✓ Settings page
✓ Form validation refinement
✓ Error handling + toasts
✓ Loading skeletons
✓ Responsive design fixes
```

**Day 14 (Nov 21): Testing & Documentation** 🧪

```
✓ Jest + React Testing Library setup
✓ Component unit tests
✓ Integration tests for key flows
✓ Verify 60%+ coverage
✓ Update documentation
✓ Prepare for sprint review
```

---

## 📦 Epic Breakdown

### **Epic A: Project Setup & Configuration** (4 pts)

**A1: Next.js Project Initialization** (1 pt)

```bash
# Commands to run
npx create-next-app@latest lexia-web --typescript --tailwind --app
cd lexia-web
npm install zustand axios react-hook-form zod lucide-react
npm install -D @types/node
```

**Files to Create**:

- `src/app/layout.tsx` - Root layout
- `src/app/page.tsx` - Home page
- `src/lib/api.ts` - Axios client
- `src/store/authStore.ts` - Auth state
- `.env.local` - Environment variables

**Environment Variables**:

```env
NEXT_PUBLIC_API_URL=http://localhost:8088/api/v1
NEXT_PUBLIC_APP_NAME=LEXIA
```

---

**A2: Tailwind CSS + shadcn/ui Setup** (0.5 pt)

```bash
# Install shadcn/ui
npx shadcn-ui@latest init
npx shadcn-ui@latest add button input card form toast
```

**Configuration**:

- `tailwind.config.ts` - Custom colors, fonts
- `src/styles/globals.css` - Global styles
- `components.json` - shadcn/ui config

---

**A3: Zustand State Management** (0.5 pt)

**Create Stores**:

```typescript
// src/store/authStore.ts
export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => Promise<void>;
  loadUser: () => Promise<void>;
}

// src/store/courseStore.ts
// src/store/progressStore.ts
```

---

**A4: Axios API Client Setup** (1 pt)

**Create API Client**:

```typescript
// src/lib/api.ts
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
  timeout: 30000,
});

// No manual Authorization header — cookies are sent automatically
api.interceptors.request.use((config) => config);

let refreshPromise: Promise<void> | null = null;

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config } = error;

    if (response?.status === 401 && !config._retry) {
      config._retry = true;
      if (!refreshPromise) {
        refreshPromise = authService.refreshSession().finally(() => {
          refreshPromise = null;
        });
      }
      try {
        await refreshPromise;
        return api(config);
      } catch {
        await authService.logout();
        return Promise.reject(error);
      }
    }

    const method = (config?.method || "").toUpperCase();
    const shouldRetry =
      ["GET", "HEAD", "OPTIONS"].includes(method) &&
      (error.code === "ERR_NETWORK" || response?.status >= 500);

    if (shouldRetry && (config._retryCount || 0) < 3) {
      config._retryCount = (config._retryCount || 0) + 1;
      const backoff =
        300 * Math.pow(2, config._retryCount - 1) + Math.random() * 50;
      await new Promise((r) => setTimeout(r, backoff));
      return api(config);
    }

    return Promise.reject(error);
  }
);
```

**API Services**:

- `src/services/authService.ts` - Auth endpoints
- `src/services/courseService.ts` - Course endpoints
- `src/services/progressService.ts` - Progress endpoints
- `src/services/profileService.ts` - Profile endpoints

---

**A5: Environment Configuration** (1 pt)

**Files**:

- `.env.local` - Development config
- `.env.production` - Production config
- `next.config.js` - Next.js configuration

---

### **Epic B: Authentication Pages** (5 pts)

**B1: Login Page** (1.5 pts)

**File**: `src/app/(auth)/login/page.tsx`

**Features**:

- Email + password form
- React Hook Form + Zod validation
- "Remember me" checkbox
- "Forgot password" link (placeholder)
- Error display (toast)
- Loading state
- Redirect to dashboard on success

**Validation**:

```typescript
const loginSchema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});
```

---

**B2: Register Page** (1.5 pts)

**File**: `src/app/(auth)/register/page.tsx`

**Features**:

- Email, password, confirm password
- Real-time validation
- Password strength indicator
- Terms and conditions checkbox
- Error handling
- Success redirect to dashboard

**Validation**:

```typescript
const registerSchema = z
  .object({
    email: z.string().email(),
    password: z
      .string()
      .min(8)
      .regex(/[A-Z]/, "Must contain uppercase")
      .regex(/[a-z]/, "Must contain lowercase")
      .regex(/[0-9]/, "Must contain number"),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });
```

---

**B3: JWT Session Management (httpOnly cookies)** (1 pt)

**Features**:

- No client-side token storage (no localStorage/sessionStorage)
- Axios sends cookies automatically with `withCredentials: true`
- Auto-refresh on 401 using refresh cookie (Promise lock)
- Logout calls backend to clear cookies

**Implementation Notes**:

```typescript
// No token getters/setters needed; backend manages cookies via Set-Cookie (HttpOnly; Secure; SameSite=Strict)
// Refresh handled in api.ts interceptor with a shared refreshPromise to avoid race conditions
```

---

**B4: Protected Routes Middleware** (0.5 pt)

**File**: `src/middleware.ts`

**Features**:

- Check authentication before rendering
- Redirect to /login if not authenticated
- Allow public routes (/login, /register)

```typescript
export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const publicRoutes = ["/login", "/register", "/", "/forgot-password"];
  if (publicRoutes.some((r) => pathname.startsWith(r))) {
    return NextResponse.next();
  }

  try {
    // Delegate auth validation to backend; it reads httpOnly cookies
    const resp = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/auth/session`,
      {
        headers: { Cookie: request.headers.get("cookie") || "" },
      }
    );
    if (!resp.ok && pathname !== "/login") {
      return NextResponse.redirect(new URL("/login", request.url));
    }
    return NextResponse.next();
  } catch {
    return NextResponse.redirect(new URL("/login", request.url));
  }
}
```

---

**B5: Auth Store (Zustand)** (0.5 pt)

**File**: `src/store/authStore.ts`

**State**:

```typescript
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterDTO) => Promise<void>;
  logout: () => Promise<void>;
  loadUser: () => Promise<void>;
}
```

---

### **Epic C: Dashboard & Layout** (4 pts)

**C1: Main Layout with Sidebar** (1.5 pts)

**File**: `src/components/layout/MainLayout.tsx`

**Features**:

- Responsive sidebar (collapsible on mobile)
- Navigation links (Dashboard, Courses, Progress, Profile)
- Active link highlighting
- Logo and branding
- Footer

**Navigation Items**:

```typescript
const navItems = [
  { icon: Home, label: "Dashboard", href: "/dashboard" },
  { icon: BookOpen, label: "Courses", href: "/courses" },
  { icon: TrendingUp, label: "Progress", href: "/progress" },
  { icon: User, label: "Profile", href: "/profile" },
];
```

---

**C2: Header with User Dropdown** (0.5 pt)

**File**: `src/components/layout/Header.tsx`

**Features**:

- User avatar (with fallback initials)
- Dropdown menu (Profile, Settings, Logout)
- Notifications icon (placeholder)
- Search bar (placeholder)

---

**C3: Responsive Navigation** (1 pt)

**Features**:

- Hamburger menu for mobile
- Slide-in sidebar on mobile
- Overlay backdrop
- Touch gestures
- Smooth animations

---

**C4: Dashboard Home Page** (1 pt)

**File**: `src/app/dashboard/page.tsx`

**Features**:

- Welcome message with user name
- Stats cards (Enrolled Courses, Completed Lessons, Current Streak)
- Recent activity list
- Continue learning section
- Recommended courses

**Stats Display**:

```typescript
<div className="grid grid-cols-1 md:grid-cols-3 gap-6">
  <StatsCard
    title="Enrolled Courses"
    value={enrolledCount}
    icon={BookOpen}
    trend="+2 this week"
  />
  <StatsCard
    title="Completed Lessons"
    value={completedLessons}
    icon={CheckCircle}
    trend="75% complete"
  />
  <StatsCard
    title="Current Streak"
    value={`${streak} days`}
    icon={Flame}
    trend="Keep it up!"
  />
</div>
```

---

### **Epic D: Course & Learning Path** (7 pts)

**D1: Course Listing Page** (2 pts)

**File**: `src/app/courses/page.tsx`

**Features**:

- Grid/List view toggle
- Search bar
- Filter by level (A1-C2)
- Filter by category
- Sort by (Popular, Recent, Alphabetical)
- Pagination or infinite scroll
- Loading skeletons
- Empty state

**Course Card**:

```typescript
<CourseCard
  title={course.title}
  description={course.description}
  level={course.level}
  lessonsCount={course.lessonCount}
  enrolled={course.isEnrolled}
  thumbnail={course.thumbnailUrl}
  onEnroll={() => handleEnroll(course.id)}
/>
```

---

**D2: Course Detail Page** (1.5 pts)

**File**: `src/app/courses/[id]/page.tsx`

**Features**:

- Course header (title, description, level)
- Course thumbnail/video
- Enrollment button
- Course curriculum (sections and lessons)
- Progress bar if enrolled
- Learning path reference
- Reviews/ratings (placeholder)

**Enrollment Flow**:

```typescript
const handleEnroll = async () => {
  setEnrolling(true);
  try {
    await enrollmentService.enrollInCourse(courseId);
    toast.success("Successfully enrolled!");
    router.push(`/courses/${courseId}/lessons`);
  } catch (error) {
    toast.error("Enrollment failed");
  } finally {
    setEnrolling(false);
  }
};
```

---

**D3: Learning Path Display** (1.5 pts)

**File**: `src/components/courses/LearningPath.tsx`

**Features**:

- Visual path with connected nodes
- CEFR levels (A1 → A2 → B1 → B2 → C1 → C2)
- Courses for each level
- Progress indicators
- Current position highlight

---

**D4: Lesson Viewer Interface** (1.5 pts)

**File**: `src/app/courses/[courseId]/lessons/[lessonId]/page.tsx`

**Features**:

- Lesson title and description
- Content display (JSONB content rendered)
- Audio player (if audio URL exists)
- Complete lesson button
- Progress tracking
- Notes section (placeholder)

**Content Rendering**:

```typescript
const renderLessonContent = (content: any) => {
  // Parse JSONB content
  if (content.type === "text") {
    return <TextContent data={content.data} />;
  } else if (content.type === "audio") {
    return <AudioPlayer url={content.audioUrl} />;
  } else if (content.type === "exercise") {
    return <Exercise data={content.data} />;
  }
};
```

---

**D5: Lesson Navigation** (0.5 pt)

**Features**:

- Previous lesson button
- Next lesson button
- Progress indicator (Lesson 3 of 15)
- Back to course button

---

### **Epic E: Progress & Profile** (5 pts)

**E1: Progress Dashboard** (2 pts)

**File**: `src/app/progress/page.tsx`

**Features**:

- Overall progress chart (line/bar chart)
- Completion percentage
- Streak calendar heatmap
- Time spent learning
- Lessons completed by day
- Achievements (badges placeholder)

**Chart Libraries**:

```bash
npm install recharts
```

**Chart Example**:

```typescript
<ResponsiveContainer width="100%" height={300}>
  <LineChart data={progressData}>
    <XAxis dataKey="date" />
    <YAxis />
    <Tooltip />
    <Line type="monotone" dataKey="lessonsCompleted" stroke="#8884d8" />
  </LineChart>
</ResponsiveContainer>
```

---

**E2: Lesson Completion Tracking UI** (1 pt)

**Features**:

- Checkmark on completed lessons
- Completion percentage per course
- Last completed timestamp
- Completion celebration (confetti animation)

---

**E3: Profile Management Page** (1 pt)

**File**: `src/app/profile/page.tsx`

**Features**:

- View mode (display info)
- Edit mode (form)
- Fields: firstName, lastName, bio, phoneNumber, timezone, language
- Save button with loading state
- Success/error feedback

**Form Validation**:

```typescript
const profileSchema = z.object({
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  bio: z.string().max(500).optional(),
  phoneNumber: z
    .string()
    .regex(/^\+?[0-9]{10,20}$/)
    .optional(),
  timezone: z.string(),
  language: z.string().length(2),
});
```

---

**E4: Avatar Upload Interface** (0.5 pt)

**Features**:

- Current avatar display
- Upload button
- Image preview before save
- Crop tool (optional)
- Delete avatar button

**Implementation**:

```typescript
const handleAvatarUpload = async (file: File) => {
  const formData = new FormData();
  formData.append("avatar", file);

  const response = await profileService.uploadAvatar(formData);
  setAvatarUrl(response.avatarUrl);
  toast.success("Avatar updated!");
};
```

---

**E5: Settings Page** (0.5 pt)

**File**: `src/app/settings/page.tsx`

**Features**:

- Language preference
- Timezone setting
- Email notifications (toggles)
- Theme (light/dark placeholder)
- Account deletion (placeholder)

---

### **Epic F: Testing & Polish** (3 pts)

**F1: Form Validation** (0.5 pt)

**Features**:

- All forms use React Hook Form + Zod
- Real-time validation feedback
- Clear error messages
- Field-level errors
- Submit button disabled on validation errors

---

**F2: Error Handling + Toast Notifications** (0.5 pt)

**Features**:

- Toast notifications for success/error
- Global error boundary
- Network error handling
- 404 page
- 500 error page
- Retry mechanisms

**Setup**:

```bash
npm install react-hot-toast
```

---

**F3: Loading States + Skeletons** (0.5 pt)

**Features**:

- Loading spinners for buttons
- Skeleton loaders for data lists
- Shimmer effect
- Progress bars for long operations
- Optimistic updates where possible

---

**F4: Jest + React Testing Library Setup** (0.5 pt)

**Setup**:

```bash
npm install -D jest @testing-library/react @testing-library/jest-dom @testing-library/user-event
npm install -D jest-environment-jsdom
```

**Config**: `jest.config.js`, `jest.setup.js`

---

**F5: Component Unit Tests** (1 pt)

**Test Coverage Target**: 60%+

**Components to Test**:

- `LoginForm.test.tsx` - Form validation, submission
- `CourseCard.test.tsx` - Rendering, enrollment
- `StatsCard.test.tsx` - Display logic
- `Navigation.test.tsx` - Link rendering, active state
- `ProgressChart.test.tsx` - Data visualization

**Test Example**:

```typescript
describe("LoginForm", () => {
  it("should validate email format", async () => {
    render(<LoginForm />);
    const emailInput = screen.getByLabelText("Email");
    await userEvent.type(emailInput, "invalid-email");
    await userEvent.tab();
    expect(screen.getByText("Invalid email")).toBeInTheDocument();
  });
});
```

---

## 🎨 Design System

### Colors (Tailwind Config)

```typescript
colors: {
  primary: {
    50: '#f0f9ff',
    500: '#3b82f6',  // Main blue
    600: '#2563eb',
    900: '#1e3a8a',
  },
  accent: {
    500: '#f59e0b',  // Amber for highlights
  },
  success: '#10b981',
  error: '#ef4444',
  warning: '#f59e0b',
}
```

### Typography

```
Font Family: Inter (from Google Fonts)
Headings: font-bold
Body: font-normal
Code: font-mono
```

### Components (shadcn/ui)

```bash
npx shadcn-ui@latest add button
npx shadcn-ui@latest add input
npx shadcn-ui@latest add card
npx shadcn-ui@latest add form
npx shadcn-ui@latest add toast
npx shadcn-ui@latest add dialog
npx shadcn-ui@latest add dropdown-menu
npx shadcn-ui@latest add avatar
npx shadcn-ui@latest add badge
npx shadcn-ui@latest add progress
npx shadcn-ui@latest add skeleton
npx shadcn-ui@latest add tabs
```

---

## 📁 Project Structure

```
lexia-web/
├── public/
│   ├── logo.svg
│   └── images/
├── src/
│   ├── app/
│   │   ├── (auth)/
│   │   │   ├── login/
│   │   │   │   └── page.tsx
│   │   │   └── register/
│   │   │       └── page.tsx
│   │   ├── dashboard/
│   │   │   └── page.tsx
│   │   ├── courses/
│   │   │   ├── page.tsx
│   │   │   └── [id]/
│   │   │       ├── page.tsx
│   │   │       └── lessons/[lessonId]/page.tsx
│   │   ├── progress/
│   │   │   └── page.tsx
│   │   ├── profile/
│   │   │   └── page.tsx
│   │   ├── settings/
│   │   │   └── page.tsx
│   │   ├── layout.tsx
│   │   ├── page.tsx
│   │   └── globals.css
│   ├── components/
│   │   ├── layout/
│   │   │   ├── MainLayout.tsx
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   └── Footer.tsx
│   │   ├── auth/
│   │   │   ├── LoginForm.tsx
│   │   │   └── RegisterForm.tsx
│   │   ├── courses/
│   │   │   ├── CourseCard.tsx
│   │   │   ├── CourseList.tsx
│   │   │   ├── CourseDetail.tsx
│   │   │   └── LearningPath.tsx
│   │   ├── progress/
│   │   │   ├── ProgressChart.tsx
│   │   │   ├── StreakCalendar.tsx
│   │   │   └── StatsCard.tsx
│   │   ├── lessons/
│   │   │   ├── LessonViewer.tsx
│   │   │   ├── LessonNavigation.tsx
│   │   │   └── ContentRenderer.tsx
│   │   ├── profile/
│   │   │   ├── ProfileForm.tsx
│   │   │   └── AvatarUpload.tsx
│   │   └── ui/
│   │       └── [shadcn components]
│   ├── lib/
│   │   ├── api.ts
│   │   ├── auth.ts
│   │   └── utils.ts
│   ├── services/
│   │   ├── authService.ts
│   │   ├── courseService.ts
│   │   ├── progressService.ts
│   │   └── profileService.ts
│   ├── store/
│   │   ├── authStore.ts
│   │   ├── courseStore.ts
│   │   └── progressStore.ts
│   ├── types/
│   │   ├── auth.ts
│   │   ├── course.ts
│   │   ├── progress.ts
│   │   └── user.ts
│   └── hooks/
│       ├── useAuth.ts
│       ├── useCourses.ts
│       └── useProgress.ts
├── tests/
│   ├── components/
│   ├── services/
│   └── utils/
├── .env.local
├── .env.production
├── next.config.js
├── tailwind.config.ts
├── tsconfig.json
├── jest.config.js
└── package.json
```

---

## 🧪 Testing Strategy

### Unit Tests (60%+ coverage)

- All reusable components
- Form validation logic
- Utility functions
- Store actions

### Integration Tests

- Login flow (form → API → redirect)
- Course enrollment flow
- Progress tracking update
- Profile edit flow

### E2E Tests (Sprint 6)

- Complete user journey
- Cross-browser testing

---

## 🚀 Deployment (Development)

### Vercel (Recommended)

```bash
# Install Vercel CLI
npm install -g vercel

# Deploy
vercel
```

### Environment Variables (Vercel)

```
NEXT_PUBLIC_API_URL=http://localhost:8088/api/v1  # Dev
NEXT_PUBLIC_API_URL=https://api.lexia.com/api/v1  # Prod
```

---

## 📝 Documentation Checklist

- [ ] README.md with setup instructions
- [ ] API integration guide
- [ ] Component documentation (Storybook optional)
- [ ] Testing guide
- [ ] Deployment guide
- [ ] Troubleshooting guide

---

## ⚡ Quick Start Commands

```bash
# Create Next.js project
npx create-next-app@latest lexia-web --typescript --tailwind --app

# Install dependencies
cd lexia-web
npm install zustand axios react-hook-form zod lucide-react react-hot-toast recharts

# Install shadcn/ui
npx shadcn-ui@latest init
npx shadcn-ui@latest add button input card form toast dialog dropdown-menu avatar badge progress skeleton

# Install dev dependencies
npm install -D @testing-library/react @testing-library/jest-dom @testing-library/user-event jest jest-environment-jsdom

# Run development server
npm run dev

# Run tests
npm test

# Build for production
npm run build
```

---

## 🎯 Success Metrics

| Metric              | Target                | Tracking             |
| ------------------- | --------------------- | -------------------- |
| **Story Points**    | 28/28                 | Daily standup        |
| **Test Coverage**   | ≥60%                  | Jest coverage report |
| **Page Load Time**  | <2s                   | Lighthouse           |
| **Accessibility**   | A11y score >90        | axe DevTools         |
| **Responsive**      | Mobile/Tablet/Desktop | Manual testing       |
| **API Integration** | All endpoints working | Postman/manual       |

---

## 🔧 Troubleshooting

### Common Issues

**1. CORS Errors**

```java
// Backend: SecurityConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

**2. Token Refresh Loop**

- Check interceptor logic
- Verify refresh token endpoint
- Debug token expiry times

**3. Hydration Errors (Next.js)**

- Avoid localStorage in component render
- Use `useEffect` for client-only code
- Check server/client data consistency

---

## 📚 Resources

### Documentation

- [Next.js Docs](https://nextjs.org/docs)
- [shadcn/ui](https://ui.shadcn.com/)
- [Zustand](https://github.com/pmndrs/zustand)
- [React Hook Form](https://react-hook-form.com/)
- [Tailwind CSS](https://tailwindcss.com/)

### Design Inspiration

- [Duolingo](https://www.duolingo.com/) - Gamification
- [Coursera](https://www.coursera.org/) - Course UI
- [Khan Academy](https://www.khanacademy.org/) - Learning paths

---

**Ready to start Sprint 3! 🚀**

**Next Step**: Run setup commands and create initial project structure.
