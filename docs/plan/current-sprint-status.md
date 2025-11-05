# LEXIA - Current Sprint Status

## Sprint 2 — In Progress

**Sprint**: 2 / 6 | **Duration**: Oct 29 – Nov 11, 2025 (14 days)  
**Status**: ✅ On Track (Day 7) | **Progress**: 17.0/21 points (81%)

### Focus

- Course/Lesson management (entities, CRUD, search)
- Learning Path (CEFR-based defaults, retrieval API)
- Progress Tracking (enrollment, completion, streak)
- Testing: ≥70% coverage, services ≥80%

### Story Breakdown

| Epic                          | Status         | Progress    |
| ----------------------------- | -------------- | ----------- |
| A: Course & Lesson Management | ✅ Complete    | 13/13 pts   |
| B: Learning Path              | ✅ Complete    | 4.0/4.0 pts |
| C: Progress Tracking          | 🔵 Not Started | 0/3 pts     |
| D: Technical Improvements     | 🔵 Not Started | 0/1 pts     |

### Current Tasks

**Completed** ✅

- A1.1: V5 Migration - Courses Table (1 pt)
- A1.2: V6 Migration - Sections & Lessons (1.5 pts)
- A1.3: Test & Verify Migrations (0.5 pts)
- A2.1: Course Entity (0.75 pts)
- A2.2: Section Entity (0.5 pts)
- A2.3: Lesson Entity (0.75 pts)
- A2.4: Create Repository Interfaces (0.5 pts)
- A2.5: Create Specifications (0.5 pts)
- A2.6: Write Repository Tests (0.5 pts)
- A3.1: Create DTOs (0.75 pts)
- A3.2: Create Mappers (0.5 pts)
- A3.3: LessonContentValidator (0.75 pts)
- A3.4: CourseService (0.5 pts)
- A3.5: LessonService (0.5 pts)
- A3.6: Service Tests (0.5 pts)
- A4.1: CourseController (0.75 pts)
- A4.2: LessonController (0.5 pts)
- A4.3: Exception Handlers (0.25 pts)
- A4.4: Controller Tests (0.5 pts)
- A5: Swagger Documentation (1 pt) ✅
- B1.1: V7 Migration - Learning Path Tables (1 pt) ✅
- B1.2: Seed Default Learning Paths (0.75 pts) ✅
- B1.3: Test and Verify Learning Paths (0.25 pts) ✅
- B2.1: Create Entities and Repositories (0.5 pts) ✅
- B2.2: Create DTOs and Mappers (0.3 pts) ✅
- B2.3: Create LearningPathService (0.5 pts) ✅
- B2.4: Create LearningPathController (0.4 pts) ✅
- B2.5: Write Tests for Learning Path API (0.3 pts) ✅
  - 48 service tests (comprehensive business logic coverage)
  - 6 controller tests (endpoints without @AuthenticationPrincipal)
  - 87% overall coverage, 93% service layer coverage
  - All 414 tests passing

**Next Up** 📋

- C1: Progress Tracking Migrations (1 pt)
- C2: Progress Tracking API (2 pts)
- D1: Implement Caching (1 pt)

---

## Sprint 1 — Completed ✅

**Sprint**: 1 / 6 | **Duration**: Oct 16-28, 2025  
**Status**: ✅ Complete (100%) | **Coverage**: 81%

**Delivered**: JWT Auth, User Management, Profile API, Swagger Docs, Token Rotation
