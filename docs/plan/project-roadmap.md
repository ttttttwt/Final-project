# LEXIA - Project Roadmap

## Timeline Overview

```
Phase 1              Phase 2              Phase 3
Backend             Frontend+AI          Testing+Deploy
Sprints 1-2         Sprints 3-4          Sprints 5-8
```

## Phases

### Phase 1: Backend Foundation (Week 1-2, Sprints 1-2)

**Goal**: Solid, secure backend with core APIs _(Phase complete)_

**Sprint 1** ✅ COMPLETE (October 16-28, 2025)

- [x] Project setup + Git infrastructure
- [x] JWT authentication implementation
- [x] User registration/login API
- [x] User profile management (Full CRUD operations)
- [x] Database validation
- [x] Comprehensive testing (81% coverage)
- [x] API documentation (Swagger/OpenAPI)

**Sprint 2** ✅ COMPLETE (October 29 – November 7, 2025)

- [x] Course/Lesson management API (entities, Flyway, CRUD, search/filter)
- [x] Learning path structure (CEFR-based defaults, retrieval API)
- [x] Progress tracking (completion, score, streak endpoints)
- [x] API documentation (Swagger) updated
- [x] Unit tests overall ≥70% (services ≥80%) — Achieved 87% overall / 93% services

**Deliverables**: Working REST API covering courses, learning paths, progress, and auth flows with updated Postman suite

### Phase 2: Frontend + AI Features (Week 3-6, Sprints 3-5)

**Goal**: Frontend connected to existing APIs, then add AI features

**Sprint 3** 🔵 In Progress (November 8-21, 2025)

- [ ] Next.js 14+ web app setup with TypeScript
- [ ] Authentication pages (Login/Register) with JWT
- [ ] Dashboard layout with navigation
- [ ] Course listing and detail pages
- [ ] Learning path display
- [ ] Progress tracking UI
- [ ] Profile management UI
- [ ] Responsive design (mobile, tablet, desktop)
- [ ] Form validation and error handling
- [ ] Integration with Sprint 1-2 APIs

**Sprint 4** 🔵 Not Started

- [ ] React Native mobile app setup (Expo)
- [ ] Mobile authentication flow
- [ ] Tab navigation structure
- [ ] Course browsing screens
- [ ] Lesson viewer screen
- [ ] Profile screen
- [ ] Progress tracking screen
- [ ] Basic offline support
- [ ] Integration with backend APIs

**Sprint 5** 🔵 Not Started

- [ ] Gemini API integration (Backend)
- [ ] Role-Play generator service + API
- [ ] Grammar Sandbox service + API
- [ ] Flashcard generator
- [ ] Role-play practice UI (Web)
- [ ] Grammar sandbox UI (Web)
- [ ] AI features in mobile app
- [ ] AI usage tracking and monitoring

**Deliverables**: Fully functional Web + Mobile apps with AI-powered learning features

### Phase 3: Testing & Deployment (Week 7-10, Sprints 6-8)

**Goal**: Production-ready, tested, deployed application

**Sprint 6** 🔵 Not Started

- [ ] End-to-end testing (Web + Mobile)
- [ ] Performance optimization
- [ ] Bug fixes from testing
- [ ] Cross-browser testing
- [ ] Mobile testing (iOS + Android)
- [ ] Accessibility improvements

**Sprint 7** 🔵 Not Started

- [ ] Security audit & penetration testing
- [ ] Load testing (100+ concurrent users)
- [ ] Database query optimization
- [ ] API response caching
- [ ] Frontend bundle optimization
- [ ] Mobile app performance tuning

**Sprint 8** 🔵 Not Started

- [ ] Production deployment setup (Cloud)
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Monitoring & logging setup
- [ ] Database migration to production
- [ ] SSL/TLS configuration
- [ ] User documentation finalization
- [ ] Release preparation

**Deliverables**: Deployed application on production, comprehensive documentation

## Key Milestones

| Milestone             | Status         | Target Date       |
| --------------------- | -------------- | ----------------- |
| Backend MVP complete  | ✅ Complete    | October 28, 2025  |
| All APIs functional   | ✅ Complete    | November 6, 2025  |
| Web frontend complete | ⏳ In Progress | November 21, 2025 |
| Mobile app complete   | 🔵 Planned     | December 5, 2025  |
| AI features working   | 🔵 Planned     | December 19, 2025 |
| Testing complete      | 🔵 Planned     | January 9, 2026   |
| Deployment ready      | 🔵 Planned     | January 23, 2026  |

## Risk Mitigation

| Risk                  | Impact | Mitigation                          | Status |
| --------------------- | ------ | ----------------------------------- | ------ |
| Frontend complexity   | High   | Use Next.js + Tailwind, start early | ⏳     |
| Gemini API delays     | Medium | Build without AI first, add later   | 🟢     |
| Mobile compatibility  | Medium | Test early, use Expo                | 🔵     |
| Database scaling      | Medium | Use JSONB, proper indexing          | ✅     |
| Deployment complexity | Medium | Use Docker, CI/CD automation        | 🔵     |

## Success Criteria

- [ ] Placement test works with 95%+ accuracy
- [ ] AI generates 50+ unique role-play scenarios
- [ ] User retention: 50%+ after week 1
- [ ] API response time: <500ms (p99)
- [ ] Mobile app: Android 8+, iOS 14+
