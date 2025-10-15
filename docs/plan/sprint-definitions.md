# LEXIA - Sprint Definitions

## Sprint Length

* Duration: 2 weeks (Tuesday to Monday)
* Sprint Planning: Tuesday 10:00 AM
* Daily Standup: Async via Slack
* Sprint Review: Monday 5:00 PM
* Sprint Retrospective: Monday 5:30 PM

## Roles (Solo Project)

* **Developer**: You (feature development, testing)
* **AI Assistant**: Claude (code generation, problem-solving)
* **Reviewer**: Self-review before "shipping"

## Sprint Structure

### Sprint 1: Backend & Authentication

**Focus**: Foundation, security, database validation
**Key Features**:

* User registration/login with JWT
* User profile CRUD
* Database connections validated

**Acceptance Criteria**:

* [ ] User can register with email/password
* [ ] User can login and get JWT token
* [ ] JWT token refreshes properly
* [ ] Unit tests: 70%+ coverage
* [ ] Swagger docs complete

---

### Sprint 2: Core APIs

**Focus**: Course/lesson management, progress tracking
**Key Features**:

* Course listing and enrollment
* Lesson content delivery
* Progress tracking
* Placement test API

**Acceptance Criteria**:

* [ ] User can browse courses
* [ ] User can enroll in course
* [ ] User can access lessons
* [ ] Placement test calculates level correctly
* [ ] All endpoints documented

---

### Sprint 3: AI Integration

**Focus**: Gemini integration, AI features
**Key Features**:

* Role-play scenario generation
* Grammar exercise generation
* AI API error handling

**Acceptance Criteria**:

* [ ] Role-play API generates 10+ unique scenarios
* [ ] Grammar exercises generated correctly
* [ ] Graceful handling of API failures
* [ ] Results saved to database

---

### Sprint 4: Frontend Development

**Focus**: User interface, mobile app
**Key Features**:

* Next.js dashboard
* Learning interface
* Mobile app basics

**Acceptance Criteria**:

* [ ] All features from backend accessible via UI
* [ ] Mobile app has basic navigation
* [ ] Tests: 60%+ coverage

---

### Sprint 5: Testing & QA

**Focus**: End-to-end testing, performance, security

**Acceptance Criteria**:

* [ ] All critical bugs fixed
* [ ] Performance: p99 <500ms
* [ ] Security audit passed
* [ ] Load test: 100 concurrent users

---

### Sprint 6: Deployment

**Focus**: Production deployment, documentation

**Acceptance Criteria**:

* [ ] App deployed to production
* [ ] Database migrations run successfully
* [ ] Documentation complete
* [ ] Release notes published

---

## Velocity Tracking

```
Sprint 1 (Baseline): 21 story points
Sprint 2 (Adjustment): 18-25 story points
Sprint 3+: Stabilize around 20 story points
```
