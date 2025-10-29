# LEXIA - Project Roadmap

## Timeline Overview

```
Phase 1              Phase 2              Phase 3
Backend             Frontend+AI          Testing+Deploy
Sprints 1-2         Sprints 3-4          Sprints 5-8
```

## Phases

### Phase 1: Backend Foundation (Week 1-2, Sprints 1-2)

**Goal**: Solid, secure backend with core APIs

**Sprint 1** ✅ COMPLETE (October 16-28, 2025)

- [x] Project setup + Git infrastructure
- [x] JWT authentication implementation
- [x] User registration/login API
- [x] User profile management (Full CRUD operations)
- [x] Database validation
- [x] Comprehensive testing (81% coverage)
- [x] API documentation (Swagger/OpenAPI)

**Sprint 2** 🔵 In Progress (October 29 – November 11, 2025)

- [ ] Course/Lesson management API (entities, Flyway, CRUD, search/filter)
- [ ] Learning path structure (CEFR-based defaults, retrieval API)
- [ ] Progress tracking (completion, score, streak endpoints)
- [ ] API documentation (Swagger) updated
- [ ] Unit tests overall ≥70% (services ≥80%)

**Deliverables**: Working REST API with 10+ endpoints, Postman collection

### Phase 2: AI Features + Frontend (Week 3-6, Sprints 3-4)

**Goal**: AI features operational, frontend connected

**Sprint 3**

- [ ] Gemini API integration
- [ ] Role-Play generator (EPIC 3)
- [ ] Grammar Sandbox (EPIC 4)
- [ ] Next.js web app setup
- [ ] Authentication page

**Sprint 4**

- [ ] Frontend dashboard
- [ ] Lesson player UI
- [ ] Role-play practice interface
- [ ] React Native mobile setup
- [ ] Mobile basic flows

**Deliverables**: Web + Mobile apps with working AI features

### Phase 3: Testing & Polish (Week 7-8, Sprints 5-6)

**Goal**: Production-ready, tested application

**Sprint 5**

- [ ] End-to-end testing
- [ ] Bug fixes
- [ ] Performance optimization
- [ ] Security audit
- [ ] Load testing

**Sprint 6**

- [ ] Deployment setup
- [ ] Database migration
- [ ] Documentation finalization
- [ ] Release preparation

**Deliverables**: Deployed application, user documentation

## Key Milestones

| Milestone            | Status      | Completion Date  |
| -------------------- | ----------- | ---------------- |
| Backend MVP complete | ✅ Complete | October 28, 2025 |
| All APIs functional  | 🔵 Planned  | TBD              |
| AI features working  | 🔵 Planned  | TBD              |
| Frontend complete    | 🔵 Planned  | TBD              |
| Deployment ready     | 🔵 Planned  | TBD              |

## Risk Mitigation

| Risk                | Impact | Mitigation                 |
| ------------------- | ------ | -------------------------- |
| Gemini API delays   | High   | Have fallback, test early  |
| Database scaling    | Medium | Use JSONB, proper indexing |
| Frontend complexity | Medium | Use component library      |

## Success Criteria

- [ ] Placement test works with 95%+ accuracy
- [ ] AI generates 50+ unique role-play scenarios
- [ ] User retention: 50%+ after week 1
- [ ] API response time: <500ms (p99)
- [ ] Mobile app: Android 8+, iOS 14+
