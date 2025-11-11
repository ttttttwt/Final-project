# LEXIA - Current Sprint Status

## Sprint 3 — Frontend Development (Web)

**Sprint**: 3 / 8 | **Duration**: Nov 8 – Nov 21, 2025 (14 days)  
**Status**: ⏳ In Progress (Day 4) | **Progress**: 4/29 points (14%)  
**Last Updated**: November 11, 2025 (Security & Quality Updates Applied)

### Focus

- Next.js 14+ web application setup with TypeScript
- Authentication UI (Login/Register) with **secure JWT (httpOnly cookies)**
- Dashboard and navigation layout
- Course browsing and enrollment UI
- Learning path display
- Progress tracking visualization
- Profile management interface
- Responsive design and form validation
- Testing: 60%+ coverage (Jest + React Testing Library)
- **Security**: httpOnly cookies, Error Boundary, comprehensive error handling
- **Accessibility**: WCAG AA compliance (ARIA, keyboard nav, contrast)

### Story Breakdown

| Epic                      | Status         | Progress | Notes                                    |
| ------------------------- | -------------- | -------- | ---------------------------------------- |
| A: Project Setup & Config | ✅ Complete    | 4/4 pts  | Setup complete                           |
| B: Authentication Pages   | 🔵 Next        | 0/5 pts  | ⚠️ Using httpOnly cookies (security fix) |
| C: Dashboard & Layout     | 🔵 Not Started | 0/4 pts  | Depends on B                             |
| D: Course & Learning Path | 🔵 Not Started | 0/7 pts  | Depends on C                             |
| E: Progress & Profile     | 🔵 Not Started | 0/5 pts  | Depends on D                             |
| F: Testing & Polish       | 🔵 Not Started | 0/4 pts  | +1 pt for quality improvements           |

**Total**: 29 points (Updated from 28 for security + quality)

### Current Tasks

**Epic A: Project Setup & Configuration** ✅ **COMPLETE** (4 pts)

- [x] A1: Next.js 14+ project initialization with TypeScript (1 pt)
- [x] A2: Tailwind CSS + shadcn/ui setup (0.5 pt)
- [x] A3: Zustand state management configuration (0.5 pt)
- [x] A4: Axios client + API integration setup (1 pt)
- [x] A5: Environment variables + build configuration (1 pt)

**📊 Velocity Alert**: Currently at 1 pt/day (target: 2.1 pts/day). Need to accelerate in Epic B-C.

**🔐 Security Update (Nov 11)**: Epic B updated to use httpOnly cookies instead of localStorage for JWT tokens (OWASP compliance).

**Epic B: Authentication Pages** 🔵 **NEXT UP** (5 pts)

- [ ] B1: Login page design + form validation (1.5 pts)
- [ ] B2: Register page with password confirmation (1.5 pts)
- [ ] B3: JWT token management (httpOnly cookies + refresh) - **⚠️ Security-critical** (1 pt)
  - Use httpOnly cookies (not localStorage)
  - Backend sets cookies with HttpOnly, Secure, SameSite flags
  - Axios sends cookies automatically with `withCredentials: true`
  - Enhanced error handling (network, timeout, retry logic)
- [ ] B4: Protected routes middleware (0.5 pt)
- [ ] B5: Auth context/store (Zustand) (0.5 pt)

**Epic C: Dashboard & Layout** (4 pts)

- [ ] C1: Main layout with sidebar navigation (1.5 pts)
- [ ] C2: Header with user profile dropdown (0.5 pt)
- [ ] C3: Responsive navigation (mobile menu) (1 pt)
- [ ] C4: Dashboard home page with stats (1 pt)

**Epic D: Course & Learning Path** (7 pts)

- [ ] D1: Course listing page with search/filter (2 pts)
- [ ] D2: Course detail page with enrollment (1.5 pts)
- [ ] D3: Learning path display component (1.5 pts)
- [ ] D4: Lesson viewer interface (1.5 pts)
- [ ] D5: Lesson navigation (prev/next) (0.5 pt)

**Epic E: Progress & Profile** (5 pts)

- [ ] E1: Progress dashboard with charts - **Dependencies: D1-D5, D4** (2 pts)
- [ ] E2: Lesson completion tracking UI (1 pt)
- [ ] E3: Profile management page (1 pt)
- [ ] E4: Avatar upload interface (0.5 pt)
- [ ] E5: Settings page (0.5 pt)

**Epic F: Testing & Polish** (4 pts - Updated from 3 pts)

- [ ] F1: Form validation for all inputs (0.5 pt)
- [ ] F2: Error handling + toast notifications + **Error Boundary** (0.7 pt)
- [ ] F3: Loading states + skeletons + **Responsive testing** (0.8 pt)
- [ ] F4: Jest + RTL setup + **Coverage thresholds** (0.5 pt)
- [ ] F5: Component unit tests (60%+ coverage verified) (1.5 pt)
- [ ] F6: **Accessibility audit** (WCAG AA) - 🆕 (0.5 pt)

**Quality Improvements** (Nov 11):

- ✅ Security: httpOnly cookies for JWT
- ✅ Error Boundary for React errors
- ✅ Comprehensive API error handling (retry logic)
- ✅ Coverage thresholds defined (60% global, 80% services)
- ✅ Responsive design testing checklist
- ✅ Accessibility audit (ARIA, keyboard nav, WCAG AA)

**Next Up** 📋

- Start with Epic A: Project Setup (Day 1-2)

---

## Previous Sprints Summary

### Sprint 2 — Completed ✅

**Sprint**: 2 / 8 | **Duration**: Oct 29 – Nov 7, 2025 (9 days)  
**Status**: ✅ Complete (100%) | **Progress**: 21/21 points  
**Coverage**: 87% overall, 93% services

**Delivered**: Course/Lesson APIs, Learning Paths, Progress Tracking, Actuator

### Sprint 1 — Completed ✅

**Sprint**: 1 / 8 | **Duration**: Oct 16-28, 2025 (12 days)  
**Status**: ✅ Complete (100%) | **Coverage**: 81%

**Delivered**: JWT Auth, User Management, Profile API, Swagger Docs, Token Rotation
