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

**Status**: 🔵 Not Started  
**Duration**: TBD (2 weeks planned)  
**Focus**: Course/lesson management, progress tracking

**Key Features**:

- Course CRUD operations
- Lesson content management
- Course enrollment system
- Learning progress tracking
- Placement test API
- API documentation updates

**Planned Tasks**:

1. Course entity & repository
2. Lesson entity & repository
3. Enrollment entity & repository
4. Progress tracking entity & repository
5. Course management service
6. Lesson content service
7. Enrollment service
8. Progress tracking service
9. Placement test algorithm
10. REST controllers for all features
11. Integration tests
12. Swagger documentation updates

**Acceptance Criteria**:

- [ ] User can browse courses
- [ ] User can enroll in course
- [ ] User can access lessons
- [ ] Placement test calculates level correctly (A1-C2)
- [ ] Progress saved and retrieved accurately
- [ ] All endpoints documented in Swagger
- [ ] Unit tests: 70%+ coverage maintained
- [ ] Integration tests cover main flows

**Dependencies**: Sprint 1 must be 100% complete

**Estimated Story Points**: 21-25 points

---

### Sprint 3: AI Integration

**Status**: 🔵 Not Started  
**Duration**: TBD (2 weeks planned)  
**Focus**: Gemini API integration, AI-powered features

**Key Features**:

- Gemini API client setup
- Role-play scenario generation (EPIC 3)
- Grammar exercise generation (EPIC 4)
- Flashcard generation
- AI usage tracking & cost monitoring
- Error handling & fallback scenarios
- Rate limiting & retry logic

**Planned Tasks**:

1. Gemini API configuration & authentication
2. AI client service with retry logic
3. Role-play generator service
4. Grammar sandbox service
5. Flashcard generator service
6. AI usage log entity & repository
7. Cost tracking & monitoring
8. Fallback content service
9. REST controllers for AI features
10. Integration tests with mock AI responses
11. Swagger documentation for AI endpoints

**Acceptance Criteria**:

- [ ] Role-play API generates 10+ unique scenarios per context
- [ ] Grammar exercises generated with correct explanations
- [ ] Flashcards generated from lesson content
- [ ] Graceful handling of API failures (fallback content)
- [ ] AI usage logged with cost tracking
- [ ] Results saved to database
- [ ] Rate limiting prevents API abuse
- [ ] Retry logic handles transient failures
- [ ] Unit tests: 70%+ coverage maintained
- [ ] Mock tests for AI responses

**Dependencies**: Sprint 2 must be 100% complete

**Estimated Story Points**: 23-27 points

**Technical Considerations**:

- Gemini API key management (environment variables)
- Token usage monitoring
- Response time optimization (<3 seconds)
- Caching for repeated requests

---

### Sprint 4: Frontend Development

**Status**: 🔵 Not Started  
**Duration**: TBD (2 weeks planned)  
**Focus**: User interface (Web + Mobile)

**Key Features**:

- Next.js web application setup
- Authentication pages (login, register)
- User dashboard
- Course browsing & enrollment UI
- Lesson player interface
- Role-play practice interface
- Grammar sandbox UI
- Progress tracking display
- React Native mobile app setup
- Mobile authentication flow
- Mobile basic navigation

**Planned Tasks**:

**Web (Next.js)**:

1. Project setup with TypeScript
2. Authentication context & protected routes
3. Login/Register pages
4. Dashboard layout
5. Course listing page
6. Course detail & enrollment page
7. Lesson player component
8. Role-play interface
9. Grammar sandbox interface
10. Progress charts & statistics
11. Profile management page

**Mobile (React Native)**:

1. Project setup with Expo
2. Navigation setup (React Navigation)
3. Authentication screens
4. Tab navigation layout
5. Course list screen
6. Lesson screen
7. Practice screens (basic)
8. Profile screen

**Acceptance Criteria**:

- [ ] All backend features accessible via web UI
- [ ] Responsive design (mobile, tablet, desktop)
- [ ] Mobile app has basic navigation (4+ screens)
- [ ] JWT authentication works on both platforms
- [ ] Forms have proper validation
- [ ] Loading states & error handling
- [ ] Tests: 60%+ coverage (Jest + React Testing Library)
- [ ] Accessibility: WCAG 2.1 AA compliance

**Dependencies**: Sprint 3 must be 100% complete

**Estimated Story Points**: 28-32 points

**Technical Stack**:

- Web: Next.js 14+, TypeScript, Tailwind CSS, Zustand/Redux
- Mobile: React Native, Expo, TypeScript, React Navigation
- Testing: Jest, React Testing Library, Cypress (E2E)

---

### Sprint 5: Testing & QA

**Status**: 🔵 Not Started  
**Duration**: TBD (2 weeks planned)  
**Focus**: End-to-end testing, performance optimization, security audit

**Key Features**:

- End-to-end testing (Selenium/Cypress)
- Performance testing & optimization
- Security audit & penetration testing
- Load testing
- Bug fixes from testing phase
- Database query optimization
- API response time optimization
- Mobile app performance tuning

**Planned Tasks**:

**Testing**:

1. E2E test suite setup
2. Critical user flow tests (10+ scenarios)
3. Performance benchmarking
4. Load testing with JMeter/K6
5. Security audit checklist
6. Penetration testing
7. Cross-browser testing
8. Mobile app testing (iOS + Android)

**Optimization**:

1. Database indexing review
2. N+1 query fixes
3. API response caching
4. Frontend bundle optimization
5. Image optimization
6. Database connection pooling tuning

**Bug Fixes**:

1. Critical bugs (P0)
2. Major bugs (P1)
3. Minor bugs (P2)
4. UI/UX improvements

**Acceptance Criteria**:

- [ ] All critical bugs fixed (0 P0 bugs)
- [ ] Major bugs fixed (≤2 P1 bugs)
- [ ] Performance: p99 <500ms for all API endpoints
- [ ] Security audit passed (OWASP Top 10 checked)
- [ ] Load test: 100 concurrent users sustained
- [ ] E2E test coverage: 80%+ of critical flows
- [ ] Mobile app: 60fps maintained
- [ ] Database queries optimized (no N+1)
- [ ] API documentation verified accurate

**Dependencies**: Sprint 4 must be 100% complete

**Estimated Story Points**: 18-22 points

**Testing Tools**:

- E2E: Cypress, Selenium
- Load: JMeter, K6
- Security: OWASP ZAP, SonarQube
- Performance: Lighthouse, React DevTools Profiler

---

### Sprint 6: Deployment & Documentation

**Status**: 🔵 Not Started  
**Duration**: TBD (2 weeks planned)  
**Focus**: Production deployment, documentation finalization, launch preparation

**Key Features**:

- Production environment setup
- Database migration to production
- CI/CD pipeline setup
- Monitoring & logging setup
- Documentation finalization
- User guide creation
- Admin documentation
- Release preparation

**Planned Tasks**:

**Infrastructure**:

1. Cloud provider setup (AWS/Azure/GCP)
2. PostgreSQL production database
3. Environment variables configuration
4. SSL/TLS certificates
5. Domain setup & DNS configuration
6. CDN setup for static assets
7. Backup strategy implementation

**Deployment**:

1. Docker containerization
2. Kubernetes/Docker Compose setup
3. CI/CD pipeline (GitHub Actions)
4. Automated testing in pipeline
5. Blue-green deployment strategy
6. Rollback procedures

**Monitoring & Logging**:

1. Application monitoring (Prometheus/Grafana)
2. Log aggregation (ELK Stack)
3. Error tracking (Sentry)
4. Uptime monitoring
5. Performance monitoring (APM)
6. Alert configuration

**Documentation**:

1. API documentation review
2. User guide (web + mobile)
3. Admin documentation
4. Deployment guide
5. Troubleshooting guide
6. Release notes
7. Changelog

**Acceptance Criteria**:

- [ ] App deployed to production (web + mobile)
- [ ] Database migrations run successfully
- [ ] CI/CD pipeline functional (auto-deploy on merge)
- [ ] Monitoring dashboards configured
- [ ] Logging aggregation working
- [ ] SSL certificates active
- [ ] Custom domain configured
- [ ] Documentation complete (user + admin)
- [ ] Release notes published
- [ ] Backup & restore tested
- [ ] Rollback procedure tested
- [ ] Performance metrics meet SLA (<500ms p99)

**Dependencies**: Sprint 5 must be 100% complete

**Estimated Story Points**: 15-20 points

**Production Stack**:

- Hosting: AWS/Azure/GCP
- Database: PostgreSQL (managed service)
- Container: Docker + Kubernetes
- CI/CD: GitHub Actions
- Monitoring: Prometheus + Grafana
- Logging: ELK Stack or CloudWatch
- CDN: CloudFront or Azure CDN

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
