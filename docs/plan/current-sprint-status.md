# LEXIA - Current Sprint Status

## Sprint 2 — In Progress

**Sprint**: 2 / 6  
**Duration**: October 29 – November 11, 2025 (14 days)  
**Status**: 🔵 In Progress  
**Planned Story Points**: 18–20

### Sprint 2 Focus (Plan A)

- Course/Lesson management API (entities, Flyway, CRUD, search/filter)
- Learning Path structure (CEFR-based defaults, retrieval API)
- Progress Tracking (completion, score, streak endpoints)
- API documentation (Swagger) kept up to date
- Testing targets: overall ≥70% coverage, services ≥80%

Out of scope for Sprint 2: AI/Gemini integration (deferred to Sprints 3–4; optional POC/spike only if capacity allows, but not a deliverable).

---

## Sprint 1 — Completed

**Sprint**: 1 / 6  
**Duration**: October 16-28, 2025 (COMPLETED ✅)  
**Status**: ✅ Complete (100%)  
**Completion Date**: October 28, 2025

## Sprint Goals

- [x] Environment setup complete
- [x] Database schema migration complete
- [x] JPA entities complete
- [x] Repository interfaces complete
- [x] JWT authentication complete
- [x] User registration API
- [x] User login API
- [x] Token refresh API
- [x] Unit tests (81% coverage - Target 70% ✅)
- [x] User profile management API (GET, PUT, POST, DELETE endpoints)
- [x] API documentation (Swagger) - All 6 subtasks complete

## Story Breakdown

| Story Category              | Status  | %Complete | Notes                                                              |
| --------------------------- | ------- | --------- | ------------------------------------------------------------------ |
| Project Setup & Database    | ✅ Done | 100%      | Spring Boot project, Flyway migrations, JPA entities, repositories |
| JWT Authentication System   | ✅ Done | 100%      | JwtTokenProvider, AuthService, BCrypt hashing, token rotation      |
| Auth API Endpoints          | ✅ Done | 100%      | Register, login, refresh endpoints with DTOs                       |
| Testing & Quality Assurance | ✅ Done | 100%      | 81% coverage, unit tests, comprehensive testing                    |
| User Profile Management     | ✅ Done | 100%      | CRUD operations, validation, audit logging                         |
| API Documentation           | ✅ Done | 100%      | Swagger/OpenAPI 3.0 documentation                                  |

## Next Sprint Preparation 🚀

**Sprint 2 Focus (Plan A)**: Course/Lesson management, Learning Path, Progress Tracking  
**Key Priorities**: Course & Lesson entities + CRUD + search; Learning Path defaults & retrieval; Progress model & endpoints; Swagger updates; tests (services ≥80%)  
**Estimated Duration**: 14 days  
**Story Points**: 18–20 points

---

**Sprint 1 Status**: ✅ **COMPLETE**  
**Ready for Sprint 2**: ✅ **YES**  
**Next Sprint Start**: TBD
