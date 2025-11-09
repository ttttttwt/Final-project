# LEXIA - Sprint Definitions

## Sprint Length

- Duration: 2 weeks (Tuesday to Monday)
- Sprint Planning: Tuesday 10:00 AM
- Daily Standup: Async via Slack
- Sprint Review: Monday 5:00 PM
- Sprint Retrospective: Monday 5:30 PM

## Roles (Solo Project)

- **Developer**: You (feature development, testing)
- **AI Assistant**: Claude (code generation, problem-solving)
- **Reviewer**: Self-review before "shipping"

## Sprint Structure

### Sprint 1: Backend & Authentication

**Status**: ✅ Complete (100%)  
**Duration**: October 16-28, 2025 (12 days)  
**Focus**: Foundation, security, database validation  
**Completion Date**: October 28, 2025

**Key Features**:

- ✅ Spring Boot 3.x project setup with Gradle
- ✅ PostgreSQL database with Flyway migrations
- ✅ JPA entities (User, UserProfile, Role, UserRole, RefreshToken, AuditLog)
- ✅ JWT authentication (access 15min, refresh 7days)
- ✅ User registration API with BCrypt (cost 12)
- ✅ User login API with token generation
- ✅ Token refresh API with rotation & theft detection
- ✅ Global exception handling
- ✅ Input validation with comprehensive error responses
- ✅ User profile CRUD operations
- ✅ Swagger/OpenAPI documentation

**Completed Tasks (13/13)**:

1. ✅ Project setup + Git infrastructure
2. ✅ Database schema migration (V1, V2, V3, V4)
3. ✅ JPA entities with relationships & validation
4. ✅ Repository interfaces with custom queries
5. ✅ JWT token provider implementation
6. ✅ AuthService with BCrypt password hashing
7. ✅ POST /api/v1/auth/register endpoint
8. ✅ POST /api/v1/auth/login endpoint
9. ✅ POST /api/v1/auth/refresh endpoint
10. ✅ Global exception handler (@ControllerAdvice)
11. ✅ Comprehensive testing (81% coverage ✅)
12. ✅ User Profile Management (Service, Controller, Security, Audit)
13. ✅ API Documentation (Swagger UI, OpenAPI 3.0, Testing Guide)

**Acceptance Criteria**: ALL MET ✅

- [x] User can register with email/password
- [x] User can login and get JWT token
- [x] JWT token refreshes properly (with rotation)
- [x] Unit tests: 70%+ coverage (81% achieved ✅)
- [x] User can view/update profile
- [x] Swagger docs accessible at /swagger-ui.html
- [x] All endpoints documented with examples

**Test Coverage**: 81% (Target: 70% ✅)

- com.lexia.backend.controller: 100%
- com.lexia.backend.service.impl: 100%
- com.lexia.backend.validation: 91%
- com.lexia.backend.mapper: 100%
- com.lexia.backend.common: 76%
- com.lexia.backend.auth: 67%

**Final Metrics**:

- Story Points: 21/21 (100% delivered)
- Total Tests: 112 (all passing)
- API Endpoints: 7 (all documented)
- Documentation: 2,600+ lines
- Performance: <200ms avg response time

**Lessons Learned**:

- ✅ Systematic task breakdown worked perfectly
- ✅ Testing early maintained quality
- ✅ Comprehensive documentation enhanced developer experience
- 🔧 Could document APIs earlier during development
- 🔧 Should add more integration tests
- 🔧 Need API versioning strategy for Sprint 2

---

### Sprint 2: Core APIs

**Status**: ✅ Complete (100%)  
**Duration**: October 29 – November 11, 2025 (delivered Day 8)  
**Focus**: Course and lesson delivery, learning paths, progress tracking  
**Completion Date**: November 7, 2025

**Key Features**:

- ✅ Course, section, and lesson domain with Flyway migrations and JSONB schemas
- ✅ Course & lesson services with filtering, DTOs, mappers, and controller coverage
- ✅ CEFR-aligned learning path API with seeded defaults and end-to-end tests
- ✅ Enrollment and lesson progress services with streak calculations and progress analytics
- ✅ Auth hardening (logout flow, principal wrapping) and Actuator operational monitoring

**Completed Tasks (21/21 story points)**:

- Epic A – Course & Lesson Management: migrations (V5/V6), entities, repositories, specs, DTOs/mappers, services, controllers, validation, Swagger, and full test suite
- Epic B – Learning Path: schema migrations (V7), seed data, domain layer, service/controller, contract tests
- Epic C – Progress Tracking: migrations (V9/V10), enrollment/progress entities, DTOs/mappers, services, controllers, service tests, streak logic
- Epic D – Technical Improvements: Spring Boot Actuator enablement with security and test coverage

**Acceptance Criteria**:

- [x] User can browse courses
- [x] User can enroll in course
- [x] User can access lessons
- [ ] Placement test calculates level correctly (A1-C2) — Deferred to Sprint 3 backlog refinement
- [x] Progress saved and retrieved accurately
- [x] All endpoints documented in Swagger
- [x] Unit tests: 70%+ coverage maintained (87% overall / 93% services)
- [x] Integration tests cover main flows

**Final Metrics**:

- Story Points: 21/21 delivered (100%)
- Total Tests: 414+ (Gradle suite, all passing)
- Code Coverage: 87% overall, 93% service layer (JaCoCo)
- API Endpoints: 25+ documented in Swagger/OpenAPI
- Database: 6 new Flyway migrations (V5–V10) applied successfully
- Performance: Controller and service tests green; recommendation latency resolved via security principal fix

**Lessons Learned**:

- Early Flyway validation plus verification scripts prevented regression in complex migrations
- Injecting domain `User` instances via a dedicated principal wrapper keeps controllers clean and avoids repeated repository calls
- Enforcing actuator security policies during implementation simplified compliance for future ops work

**Action Items for Sprint 3**:

1. Design and implement the CEFR placement assessment API deferred from Sprint 2 scope
2. Extend progress analytics to surface streak data in forthcoming AI-driven coaching features
3. Align API docs/Postman with new logout and actuator endpoints before frontend integration

---

### Sprint 3: Frontend Development (Web)

**Status**: ⏳ In Progress  
**Duration**: November 8-21, 2025 (14 days)  
**Focus**: Next.js web application with existing backend APIs

**Key Features**:

- Next.js 14+ with TypeScript and App Router
- Authentication UI (Login/Register) with JWT integration
- Dashboard layout with sidebar navigation
- Course browsing with search and filters
- Learning path display
- Progress tracking visualization
- Profile management with avatar upload
- Responsive design (mobile, tablet, desktop)
- Form validation and error handling
- State management with Zustand

**Task Breakdown**:

**Epic A: Project Setup** (4 pts)

1. A1: Next.js project initialization (1 pt)
2. A2: Tailwind CSS + shadcn/ui setup (0.5 pt)
3. A3: Zustand state management (0.5 pt)
4. A4: Axios API client setup (1 pt)
5. A5: Environment configuration (1 pt)

**Epic B: Authentication** (5 pts)

1. B1: Login page (1.5 pts)
2. B2: Register page (1.5 pts)
3. B3: JWT token management (1 pt)
4. B4: Protected routes middleware (0.5 pt)
5. B5: Auth store (0.5 pt)

**Epic C: Dashboard & Layout** (4 pts)

1. C1: Main layout with sidebar (1.5 pts)
2. C2: Header with user dropdown (0.5 pt)
3. C3: Responsive navigation (1 pt)
4. C4: Dashboard home page (1 pt)

**Epic D: Course Features** (7 pts)

1. D1: Course listing page (2 pts)
2. D2: Course detail + enrollment (1.5 pts)
3. D3: Learning path display (1.5 pts)
4. D4: Lesson viewer (1.5 pts)
5. D5: Lesson navigation (0.5 pt)

**Epic E: Progress & Profile** (5 pts)

1. E1: Progress dashboard with charts (2 pts)
2. E2: Completion tracking UI (1 pt)
3. E3: Profile page (1 pt)
4. E4: Avatar upload (0.5 pt)
5. E5: Settings page (0.5 pt)

**Epic F: Testing & Polish** (3 pts)

1. F1: Form validation (0.5 pt)
2. F2: Error handling + toasts (0.5 pt)
3. F3: Loading states (0.5 pt)
4. F4: Testing setup (0.5 pt)
5. F5: Component tests (1 pt)

**Acceptance Criteria**:

- [x] Backend APIs ready (Sprint 1-2 complete) ✅
- [ ] Next.js app runs locally with no errors
- [ ] Users can register and login
- [ ] Protected routes redirect to login
- [ ] Course listing displays all courses from API
- [ ] Users can enroll in courses
- [ ] Progress is tracked and displayed
- [ ] Profile can be viewed and edited
- [ ] Responsive on mobile, tablet, desktop
- [ ] Forms have validation with clear error messages
- [ ] Loading states for all async operations
- [ ] Unit tests: 60%+ coverage (Jest + RTL)

**Dependencies**: Sprint 1 & 2 must be 100% complete ✅

**Estimated Story Points**: 28 points

**Technical Stack**:

- Next.js 14+ (App Router)
- TypeScript
- Tailwind CSS + shadcn/ui
- Zustand (state management)
- Axios (API client)
- React Hook Form + Zod (form validation)
- Chart.js or Recharts (progress visualization)
- Jest + React Testing Library

---

### Sprint 4: Mobile App Development

**Status**: 🔵 Not Started  
**Duration**: November 22 - December 5, 2025 (14 days planned)  
**Focus**: React Native mobile app with backend integration

**Key Features**:

- React Native app setup with Expo
- Mobile authentication flow
- Tab navigation structure
- Course browsing screens
- Lesson viewer optimized for mobile
- Progress tracking screens
- Profile management
- Offline support basics
- Push notifications setup
- Mobile-specific UI/UX

**Planned Tasks**:

1. Expo project initialization with TypeScript
2. React Navigation setup (Stack + Tab)
3. Mobile authentication screens (Login/Register)
4. Auth token management (AsyncStorage)
5. Tab navigation layout (Home, Courses, Progress, Profile)
6. Course listing screen with pull-to-refresh
7. Course detail screen with enrollment
8. Lesson viewer optimized for mobile
9. Progress dashboard with native charts
10. Profile screen with image picker
11. Settings screen
12. Basic offline support (cache)
13. Push notification configuration
14. Mobile testing setup (Jest + RNTL)
15. Component tests (50%+ coverage)

**Acceptance Criteria**:

- [ ] Mobile app runs on iOS and Android
- [ ] Authentication flow works seamlessly
- [ ] All core features accessible (courses, lessons, progress)
- [ ] Smooth navigation with proper transitions
- [ ] Offline mode shows cached content
- [ ] Forms optimized for mobile input
- [ ] Image upload works (avatar, etc.)
- [ ] Push notifications registered
- [ ] Performance: 60fps maintained
- [ ] Tests: 50%+ coverage
- [ ] No memory leaks in navigation

**Dependencies**: Sprint 3 must be 100% complete

**Estimated Story Points**: 24 points

**Technical Stack**:

- React Native with Expo
- TypeScript
- React Navigation (Stack + Tab + Drawer)
- Axios (API client)
- AsyncStorage (local storage)
- React Native Paper or NativeBase (UI components)
- Victory Native or React Native Chart Kit (charts)
- Expo Image Picker
- Expo Notifications
- Jest + React Native Testing Library

---

### Sprint 5: AI Integration

**Status**: 🔵 Not Started  
**Duration**: December 6-19, 2025 (14 days planned)  
**Focus**: Gemini API integration + AI features in Web & Mobile

**Key Features**:

**Backend (AI Services)**:

- Gemini API client setup with retry logic
- Role-play scenario generator
- Grammar exercise generator
- Flashcard generator from content
- AI usage tracking and cost monitoring
- Fallback content for API failures

**Frontend (Web + Mobile)**:

- Role-play practice interface
- Grammar sandbox UI
- Flashcard study interface
- AI loading states and animations
- Error handling for AI failures
- AI feature integration in existing flows

**Planned Tasks**:

**Backend** (10 tasks):

1. Gemini API client service
2. AIUsageLog entity + repository (V11 migration)
3. RolePlayScenario entity + service
4. Grammar exercise generator service
5. Flashcard generator service
6. AI controllers (RolePlay, Grammar, Flashcard)
7. Fallback content service
8. Rate limiting for AI endpoints
9. Cost calculation and monitoring
10. Comprehensive tests with mock AI

**Frontend Web** (5 tasks):

1. Role-play UI (conversation interface)
2. Grammar sandbox (interactive exercises)
3. Flashcard study interface (swipe cards)
4. AI loading animations
5. Error handling + retry UI

**Frontend Mobile** (3 tasks):

1. Role-play screen (mobile-optimized)
2. Grammar practice screen
3. Flashcard screen with gestures

**Acceptance Criteria**:

- [ ] Gemini API successfully integrated
- [ ] Role-play generates unique scenarios (<3s)
- [ ] Grammar exercises with explanations
- [ ] Flashcards extracted from lessons
- [ ] AI usage tracked with cost
- [ ] Graceful fallback on API errors
- [ ] Web UI responsive and interactive
- [ ] Mobile gestures work smoothly
- [ ] Backend tests: 70%+ coverage
- [ ] Frontend tests: 60%+ coverage
- [ ] All endpoints documented

**Dependencies**: Sprint 3 & 4 must be complete

**Estimated Story Points**: 26 points

**Technical Stack**:

- Backend: Gemini SDK, Spring Boot
- Web: React components, animations
- Mobile: React Native gestures
- Testing: Mock AI responses

---

### Sprint 6: Testing & QA

**Status**: 🔵 Not Started  
**Duration**: December 20, 2025 - January 2, 2026 (14 days planned)  
**Focus**: End-to-end testing, performance optimization, bug fixes

**Key Features**:

- End-to-end testing (Web + Mobile)
- Performance optimization
- Bug fixes from testing phase
- Cross-browser/device testing
- Security audit
- Load testing
- Accessibility improvements
- UI/UX polish

**Planned Tasks**:

**Testing**:

1. E2E test suite setup (Cypress/Playwright)
2. Critical user flows (10+ scenarios)
3. Cross-browser testing (Chrome, Firefox, Safari)
4. Mobile testing (iOS + Android real devices)
5. Performance benchmarking
6. Load testing (100+ concurrent users)
7. Security audit (OWASP Top 10)
8. Accessibility testing (WCAG 2.1 AA)

**Optimization**:

1. Frontend bundle optimization
2. Image optimization (WebP, lazy loading)
3. API response caching
4. Database query optimization
5. Mobile app performance tuning
6. SEO optimization (Web)

**Bug Fixes**:

1. Fix all P0 (critical) bugs
2. Fix all P1 (major) bugs
3. Address P2 (minor) bugs
4. UI/UX improvements
5. Edge case handling

**Acceptance Criteria**:

- [ ] E2E tests: 80%+ critical flow coverage
- [ ] Zero P0 bugs, ≤2 P1 bugs remaining
- [ ] Performance: p99 <500ms (API), LCP <2.5s (Web)
- [ ] Load test: 100+ concurrent users
- [ ] Security audit passed
- [ ] Mobile: 60fps maintained
- [ ] Accessibility: WCAG 2.1 AA compliant
- [ ] Cross-browser: works on latest 2 versions
- [ ] Mobile: works on iOS 14+ and Android 8+

**Dependencies**: Sprint 5 must be complete

**Estimated Story Points**: 20 points

**Tools**:

- E2E: Cypress/Playwright
- Performance: Lighthouse, WebPageTest
- Load: K6, Artillery
- Security: OWASP ZAP, Snyk
- Mobile: Detox, Maestro

---

### Sprint 7: Security & Performance

**Status**: 🔵 Not Started  
**Duration**: January 3-16, 2026 (14 days planned)  
**Focus**: Security hardening, performance optimization, scaling

**Key Features**:

- Security audit and penetration testing
- Performance optimization (backend + frontend)
- Database scaling preparation
- Rate limiting and throttling
- Advanced caching strategies
- API optimization
- Mobile app optimization

**Planned Tasks**:

**Security**:

1. OWASP Top 10 security audit
2. Penetration testing (automated + manual)
3. SQL injection prevention verification
4. XSS/CSRF protection verification
5. JWT security review
6. Secrets management review
7. HTTPS enforcement
8. Security headers configuration

**Performance**:

1. Database query optimization (indexes, N+1)
2. API response caching (Redis)
3. CDN integration for static assets
4. Image optimization pipeline
5. Frontend bundle splitting
6. Lazy loading implementation
7. Service worker for offline support
8. Database connection pooling

**Scaling**:

1. Load balancer setup
2. Database replication strategy
3. API rate limiting per user
4. Background job processing
5. File upload optimization
6. Search optimization (Elasticsearch?)

**Acceptance Criteria**:

- [ ] Security audit: 0 critical, 0 high vulnerabilities
- [ ] Penetration test passed
- [ ] API p99 <300ms (improved from <500ms)
- [ ] Web LCP <2s (improved from <2.5s)
- [ ] Database queries all indexed
- [ ] Redis caching implemented
- [ ] Rate limiting active (100 req/min per user)
- [ ] CDN serving static assets
- [ ] Mobile app size <50MB

**Dependencies**: Sprint 6 complete

**Estimated Story Points**: 18 points

---

### Sprint 8: Deployment & Launch

**Status**: 🔵 Not Started  
**Duration**: January 17-30, 2026 (14 days planned)  
**Focus**: Production deployment, monitoring, launch preparation

**Key Features**:

- Production infrastructure setup
- CI/CD pipeline implementation
- Monitoring and logging
- Database migration to production
- Domain and SSL configuration
- App store submission (iOS + Android)
- Documentation finalization
- Soft launch and monitoring

**Planned Tasks**:

**Infrastructure**:

1. Cloud provider setup (AWS/GCP/Azure)
2. Production database (PostgreSQL managed)
3. Environment variables management
4. SSL/TLS certificates
5. Domain setup + DNS
6. CDN configuration
7. Backup strategy
8. Disaster recovery plan

**Deployment**:

1. Docker containerization (all services)
2. Kubernetes cluster setup (or Cloud Run/App Service)
3. CI/CD pipeline (GitHub Actions)
4. Automated testing in pipeline
5. Blue-green deployment
6. Rollback procedures
7. Database migration scripts

**Monitoring**:

1. Application monitoring (Datadog/New Relic)
2. Error tracking (Sentry)
3. Log aggregation (CloudWatch/ELK)
4. Uptime monitoring (UptimeRobot)
5. Alert configuration (PagerDuty/Slack)
6. Performance dashboards

**App Store**:

1. iOS App Store submission
2. Google Play Store submission
3. App screenshots and descriptions
4. Privacy policy and terms
5. App store optimization (ASO)

**Documentation**:

1. User guide (Web + Mobile)
2. Admin documentation
3. API documentation review
4. Deployment runbook
5. Troubleshooting guide
6. Release notes
7. Marketing materials

**Launch**:

1. Soft launch (limited users)
2. Monitor metrics and errors
3. Bug fixes if needed
4. Full public launch
5. Social media announcement
6. User onboarding flow
7. Analytics tracking

**Acceptance Criteria**:

- [ ] All services deployed to production
- [ ] CI/CD pipeline working (auto-deploy)
- [ ] Monitoring dashboards live
- [ ] Alerts configured and tested
- [ ] Database backups automated
- [ ] SSL certificates active
- [ ] Domain configured
- [ ] iOS app in App Store
- [ ] Android app in Play Store
- [ ] User documentation complete
- [ ] Soft launch successful (100+ users)
- [ ] Full launch ready

**Dependencies**: Sprint 7 complete

**Estimated Story Points**: 20 points

---

## Velocity Tracking

```
Sprint 1 (Baseline): 21 story points (85% complete)
  - Actual velocity: ~18 points delivered so far
  - Remaining: ~3 points (User Profile + Swagger)
  - Lessons learned: Initial estimates accurate, testing time critical

Sprint 2 (Planned): 21-25 story points
  - Adjust based on Sprint 1 completion velocity
  - Focus on consistent delivery pace

Sprint 3 (Planned): 23-27 story points
  - AI integration complexity factor
  - Buffer time for Gemini API challenges

Sprint 4 (Planned): 28-32 story points
  - Largest sprint (Web + Mobile)
  - Consider splitting if needed

Sprint 5 (Planned): 18-22 story points
  - Testing & optimization
  - Bug fix buffer included

Sprint 6 (Planned): 15-20 story points
  - Deployment & documentation
  - Launch preparation activities
```

## Sprint Metrics

### Definition of Done (DoD)

A task is considered "Done" when:

- ✅ Code written and compiles successfully
- ✅ Unit tests written with ≥70% coverage (services ≥80%)
- ✅ All tests passing (./gradlew test)
- ✅ Integration tests for critical paths
- ✅ Code reviewed (self-review checklist)
- ✅ JavaDoc added for public methods
- ✅ No security vulnerabilities
- ✅ No hardcoded secrets
- ✅ Error handling implemented
- ✅ Logging added appropriately
- ✅ Documentation updated (API docs, README)
- ✅ Committed with conventional commit message
- ✅ daily-log.md updated
- ✅ Session summary created

### Quality Gates

Each sprint must pass these quality gates before moving to next sprint:

1. **Code Quality**

   - All tests passing (100% pass rate)
   - Code coverage ≥70% overall
   - Service layer coverage ≥80%
   - No critical SonarQube issues

2. **Security**

   - No plain-text passwords/tokens
   - All sensitive data encrypted
   - JWT properly implemented
   - OWASP Top 10 checked

3. **Performance**

   - API response time <500ms (p99)
   - Database queries optimized
   - No N+1 query issues

4. **Documentation**
   - All endpoints documented in Swagger
   - README updated
   - Session summaries created
   - Code comments for complex logic

### Sprint Retrospective Template

After each sprint, document:

**What Went Well** ✅

- List 3-5 positive outcomes

**What Could Be Improved** 🔧

- List 3-5 areas for improvement

**Action Items** 🎯

- Concrete steps for next sprint

**Metrics** 📊

- Story points completed vs planned
- Test coverage achieved
- Bugs found vs fixed
- Velocity trend

---

## Sprint Status Legend

- ✅ **Done** - Task completed and meets DoD
- ⏳ **In Progress** - Currently being worked on
- 🔵 **Not Started** - Planned but not yet begun
- ⚠️ **Blocked** - Cannot proceed due to dependency
- 🔄 **In Review** - Awaiting review/testing
- ❌ **Cancelled** - Removed from scope

---

## Notes

- Sprint durations may be adjusted based on actual velocity
- Story point estimates will be refined after Sprint 1 completion
- AI assistant (GitHub Copilot) helps accelerate development
- Solo project allows flexible sprint adjustments
- Focus on delivering working software over documentation (but maintain good docs!)
- Test coverage is non-negotiable - maintain ≥70% always
