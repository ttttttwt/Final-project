# 🎯 SPRINT 2 - LEXIA Backend

**Duration**: October 29 – November 11, 2025 (14 days)  
**Target Story Points**: 18-20 points  
**Focus**: Course/Lesson Management + Learning Path + Progress Tracking Foundation

---

## 📊 Sprint Overview

### Objectives

1. ✅ Complete Course & Lesson Management APIs (CRUD, search, filter)
2. ✅ Implement CEFR-based Learning Path structure
3. ✅ Build Progress Tracking foundation (enrollment, completion)
4. ✅ Maintain 70%+ overall coverage, 80%+ service coverage
5. ✅ Keep Swagger documentation up-to-date

### Velocity Constraints

- Sprint 1 velocity: ~18 points (with 81% coverage achieved)
- Sprint 2 capacity: 18-20 points (sustainable pace)
- Team size: 1 developer
- Working days: 10 days (excluding weekends)

---

## 🎯 EPIC A: Course & Lesson Management (13 points)

### Task A1: Database Migrations (3 points)

**Priority**: P0 (Must Have) | **Dependencies**: None

**Description**: Create Flyway migrations for course, lesson, section tables with proper indexing.

**Technical Details**:

- `V5__Create_courses_table.sql`
  - Fields: id, title, description, thumbnail_url, cefr_level (A1-C2), is_published, created_at, updated_at
  - Indexes: cefr_level, is_published, created_at, B-tree on title (simple first)
  - Note: Defer GIN full-text search to Sprint 3 (YAGNI principle)
- `V6__Create_sections_and_lessons_table.sql`
  - sections: id, course_id (FK), title, order_index
  - lessons: id, section_id (FK), title, lesson_type (ENUM), content (JSONB), order_index, duration_minutes
  - Indexes: composite (section_id, order_index), lesson_type
  - Note: Document JSONB schemas in `docs/context/DATABASE-SCHEMA.md` BEFORE migration

**Acceptance Criteria**:

- [ ] Migrations run successfully on clean database
- [ ] Rollback works without data loss
- [ ] Indexes improve query performance (test with EXPLAIN ANALYZE)
- [ ] Foreign keys enforce referential integrity
- [ ] JSONB content field supports complex lesson data
- [ ] JSONB schemas documented in `docs/context/DATABASE-SCHEMA.md`

**Definition of Done**:

- [ ] `./gradlew flywayMigrate` executes without errors
- [ ] Migration scripts reviewed for SQL injection risks
- [ ] Naming conventions follow project standards
- [ ] JSONB schemas for all lesson types documented with examples
- [ ] Documentation added to `docs/context/DATABASE-SCHEMA.md`

---

### Task A2: JPA Entities & Repositories (3 points)

**Priority**: P0 (Must Have) | **Dependencies**: A1

**Description**: Create Course, Section, Lesson entities with Spring Data JPA repositories and Specifications for filtering.

**Technical Details**:

```java
@Entity
public class Course {
    @Id @GeneratedValue
    private Long id;

    @NotBlank
    private String title;

    @Pattern(regexp = "A1|A2|B1|B2|C1|C2")
    private String cefrLevel;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Section> sections;

    private boolean isPublished;
}

@Entity
public class Lesson {
    @Enumerated(EnumType.STRING)
    private LessonType lessonType; // READING, LISTENING, QUIZ, SPEAKING

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode content;
}
```

**Filtering Requirements**:

- Search by: title (ILIKE), cefrLevel, tags, isPublished
- Sort by: createdAt, title
- Date range filter: createdAt between start and end

**Acceptance Criteria**:

- [ ] Entities map correctly to database tables
- [ ] CRUD operations work via repositories
- [ ] Specifications support all required filters
- [ ] JSONB content serialization/deserialization works
- [ ] Bidirectional relationships handled properly

**Definition of Done**:

- [ ] `@DataJpaTest` covers all repository methods
- [ ] 80%+ coverage on repository layer
- [ ] Bean Validation annotations tested
- [ ] No N+1 query problems (verified with logging)

---

### Task A3: Service Layer + DTOs (3 points)

**Priority**: P0 (Must Have) | **Dependencies**: A2

**Description**: Implement CourseService and LessonService with DTOs, validation, and business logic.

**DTOs Required**:

- `CourseDTO` (response)
- `CreateCourseDTO` (input with validation)
- `UpdateCourseDTO` (input with validation)
- `CourseSearchDTO` (filter parameters)
- `LessonDTO`, `CreateLessonDTO`, `UpdateLessonDTO`

**Business Rules**:

1. Only users with CONTENT_MANAGER role can create/edit courses
2. Published courses cannot be deleted (must unpublish first)
3. Lesson order_index must be unique within a section
4. JSONB content must match schema for lesson_type (validated by LessonContentValidator)

**LessonContentValidator Design**:

```java
// Validates JSONB content against lesson type schemas
// - READING: requires passages[], vocabulary[]
// - QUIZ: requires questions[] with type, options, correct answer
// - LISTENING: requires audio_url, transcript, questions[]
// - SPEAKING: requires prompts[], sample_answers[]
```

**Acceptance Criteria**:

- [ ] All CRUD operations implemented
- [ ] Validation errors return proper format (RFC 7807)
- [ ] Role-based access control enforced
- [ ] Duplicate course titles prevented
- [ ] JSONB validation per lesson type (LessonContentValidator)
- [ ] Invalid JSONB throws InvalidLessonContentException with clear message

**Definition of Done**:

- [ ] Unit tests cover 80%+ of service methods
- [ ] Custom exceptions: `CourseNotFoundException`, `DuplicateCourseException`, `InvalidLessonContentException`
- [ ] MapStruct mappers (or manual mappers) implemented
- [ ] JavaDoc comments on public methods
- [ ] LessonContentValidator tested for all lesson types

---

### Task A4: REST API Controllers (2 points)

**Priority**: P0 (Must Have) | **Dependencies**: A3

**Description**: Create CourseController and LessonController with versioned endpoints.

**Endpoints**:

```
GET    /api/v1/courses               # List with pagination/filter
GET    /api/v1/courses/{id}          # Get by ID
GET    /api/v1/courses/search        # Advanced search
POST   /api/v1/courses               # Create (CONTENT_MANAGER)
PUT    /api/v1/courses/{id}          # Update (CONTENT_MANAGER)
DELETE /api/v1/courses/{id}          # Delete (CONTENT_MANAGER)
POST   /api/v1/courses/{id}/publish  # Publish course

GET    /api/v1/lessons/{id}          # Get lesson details
POST   /api/v1/courses/{courseId}/sections/{sectionId}/lessons  # Create lesson
```

**Pagination Spec**:

- Query params: `page`, `size`, `sort`
- Response: includes `totalElements`, `totalPages`, `number`, `size`

**Acceptance Criteria**:

- [ ] All endpoints return correct HTTP status codes
- [ ] Pagination works with Spring Data Pageable
- [ ] @PreAuthorize restricts access properly
- [ ] Input validation triggers 400 Bad Request
- [ ] Entity not found triggers 404 Not Found

**Definition of Done**:

- [ ] MockMvc integration tests cover all endpoints
- [ ] Swagger annotations with examples
- [ ] 70%+ controller coverage
- [ ] Postman collection updated

---

### Task A5: Swagger Documentation Update (1 point)

**Priority**: P1 (Should Have) | **Dependencies**: A4

**Description**: Add comprehensive Swagger documentation for all new endpoints.

**Requirements**:

- @Tag on controllers
- @Operation with summary, description, examples
- @Schema on DTOs with field descriptions
- @ApiResponse for all status codes

**Acceptance Criteria**:

- [ ] Swagger UI renders without errors
- [ ] All request/response examples provided
- [ ] Authentication requirements documented
- [ ] Error responses documented

**Definition of Done**:

- [ ] Swagger JSON validated
- [ ] Screenshots added to docs
- [ ] Team reviewed documentation

---

### Task A6: Seed Data Script (1 point)

**Priority**: P1 (Should Have) | **Dependencies**: A1, A2

**Description**: Create dev-only seed script to populate sample courses for testing.

**Technical Details**:

```java
@Component
@Profile("dev")
public class CourseSeeder implements ApplicationRunner {
    // Seeds 3 courses: A1, B1, C1
    // Each with 2 sections, 3 lessons per section
    // Different lesson types (READING, QUIZ, LISTENING, SPEAKING)
    // Idempotent (checks if data exists before seeding)
}
```

**Sample Data**:

- Course 1: "English Basics (A1)" - 2 sections, 6 lessons
- Course 2: "Intermediate English (B1)" - 2 sections, 6 lessons
- Course 3: "Advanced English (C1)" - 2 sections, 6 lessons
- Mix of lesson types with realistic JSONB content

**Acceptance Criteria**:

- [ ] Seed script runs on app startup (dev profile only)
- [ ] Script is idempotent (can run multiple times)
- [ ] Creates 3 courses with proper relationships
- [ ] JSONB content matches documented schemas
- [ ] Does NOT run in production profile

**Definition of Done**:

- [ ] Seeder component tested
- [ ] Seed data verified in database
- [ ] Documentation added to README
- [ ] Script logs seed status clearly

---

## 🎯 EPIC B: Learning Path (CEFR-based) (4 points)

### Task B1: Learning Path Migrations + Seed Data (2 points)

**Priority**: P1 (Should Have) | **Dependencies**: A1

**Description**: Create learning_path table and seed default CEFR paths.

**Schema**:

```sql
CREATE TABLE learning_paths (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cefr_level VARCHAR(2) NOT NULL,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE learning_path_courses (
    path_id BIGINT REFERENCES learning_paths(id),
    course_id BIGINT REFERENCES courses(id),
    order_index INT NOT NULL,
    PRIMARY KEY (path_id, course_id)
);

CREATE TABLE user_learning_paths (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    path_id BIGINT REFERENCES learning_paths(id),
    current_course_id BIGINT REFERENCES courses(id),
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, path_id)
);
```

**Seed Data**:

- 6 default paths: A1, A2, B1, B2, C1, C2
- Each path contains 3-5 courses (placeholder data or reference seeded courses)

**Acceptance Criteria**:

- [ ] Seed script runs idempotently
- [ ] Default paths created for all CEFR levels
- [ ] Many-to-many relationship works
- [ ] User path tracking table supports current position

**Definition of Done**:

- [ ] Migration tested with rollback
- [ ] Seed data documented
- [ ] Query performance tested

---

### Task B2: Learning Path API (2 points)

**Priority**: P1 (Should Have) | **Dependencies**: B1

**Description**: Implement LearningPathService and REST API.

**Endpoints**:

```
GET  /api/v1/learning-paths            # List all paths
GET  /api/v1/learning-paths/{id}       # Get path details with courses
GET  /api/v1/learning-paths/recommend  # Get recommended path for user
POST /api/v1/learning-paths/{id}/start # Start learning path (track position)
GET  /api/v1/learning-paths/my-progress # Get user's current path progress
```

**Recommendation Logic** (v1 - Simple):

- If user has `cefrLevel` in profile → return matching path
- Else → return A1 (beginner) path
- TODO Sprint 3: Progressive recommendation (track completed courses, suggest next level)

**Path Tracking**:

- When user starts path → create user_learning_paths record
- Track current_course_id as user progresses
- Update current_course_id when course completed

**Acceptance Criteria**:

- [ ] Endpoints return correct data
- [ ] Recommendation works based on user profile
- [ ] Courses are ordered correctly in path
- [ ] Starting path creates tracking record
- [ ] Current position persisted and retrievable
- [ ] Cannot start same path twice (return existing progress)

**Definition of Done**:

- [ ] Service unit tests 80%+
- [ ] Controller integration tests 70%+
- [ ] Swagger documented
- [ ] Path tracking tested (start, retrieve, duplicate prevention)

---

## 🎯 EPIC C: Progress Tracking Foundation (3 points)

### Task C1: Progress Tracking Migrations (1 point)

**Priority**: P1 (Should Have) | **Dependencies**: A1, A2

**Description**: Create tables for enrollment and lesson progress.

**Schema**:

```sql
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    course_id BIGINT REFERENCES courses(id),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress_percentage INT DEFAULT 0,
    completed_at TIMESTAMP,
    UNIQUE (user_id, course_id)
);

CREATE TABLE lesson_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    lesson_id BIGINT REFERENCES lessons(id),
    status VARCHAR(20) NOT NULL, -- NOT_STARTED, IN_PROGRESS, COMPLETED
    score INT,
    attempts INT DEFAULT 0,
    result_details JSONB,
    completed_at TIMESTAMP,
    UNIQUE (user_id, lesson_id)
);
```

**Acceptance Criteria**:

- [ ] Unique constraints prevent duplicate enrollments
- [ ] Indexes on user_id for performance
- [ ] JSONB field supports detailed quiz results

**Definition of Done**:

- [ ] Migration tested
- [ ] Constraints validated

---

### Task C2: Progress Tracking API (2 points)

**Priority**: P1 (Should Have) | **Dependencies**: C1

**Description**: Implement enrollment and progress tracking endpoints.

**Endpoints**:

```
POST /api/v1/enrollments                      # Enroll in course
GET  /api/v1/enrollments                      # User's enrollments
GET  /api/v1/enrollments/{courseId}/progress  # Course progress detail

POST /api/v1/lessons/{id}/complete            # Mark lesson complete
GET  /api/v1/progress/streak                  # Get current streak
```

**Business Logic**:

- Auto-calculate course progress: (completed lessons / total lessons) \* 100
- Streak: consecutive days with at least 1 lesson completed (use user's timezone if available)
- Cannot enroll twice in same course (return 409 Conflict)
- Note: Unenrollment NOT implemented in Sprint 2 (deferred - TBD)

**Timezone Handling**:

```java
// Use user's timezone from profile (if set), else UTC
ZoneId userZone = user.getTimezone() != null
    ? ZoneId.of(user.getTimezone())
    : ZoneId.of("UTC");
LocalDate today = LocalDate.now(userZone);
```

**Concurrent Enrollment Prevention**:

- UNIQUE constraint (user_id, course_id) in database
- @Transactional on enrollment service method
- Test with concurrent requests (2 threads)

**Acceptance Criteria**:

- [ ] Enrollment prevents duplicates (UNIQUE constraint + 409)
- [ ] Progress calculation accurate
- [ ] Streak calculation works with user timezone (test edge cases)
- [ ] Concurrent enrollment handled gracefully (race condition test)

**Definition of Done**:

- [ ] Service tests 80%+
- [ ] Controller tests 70%+
- [ ] Edge cases tested (same-day completions, timezone boundaries)
- [ ] Concurrent enrollment test passes
- [ ] Streak calculation tested across timezones (GMT, GMT+7, GMT-5)

---

## 🎯 EPIC D: Technical Improvements (1 point)

### Task D1: Actuator Configuration (1 point)

**Priority**: P0 (Must Have) | **Dependencies**: None

**Description**: Configure Spring Boot Actuator for health monitoring.

**API Versioning**:

- All existing endpoints already use `/api/v1` prefix ✅
- New endpoints must follow same convention
- No migration needed

**Actuator Setup**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
```

**Security**:

- Restrict `/actuator/**` to ADMIN role only
- Do not expose sensitive info in production

**Acceptance Criteria**:

- [ ] All NEW endpoints use /api/v1 prefix (existing ones already compliant)
- [ ] Actuator health check returns 200
- [ ] Metrics endpoint requires authentication
- [ ] No sensitive data exposed in actuator endpoints

**Definition of Done**:

- [ ] Smoke tests pass
- [ ] Documentation updated in README
- [ ] Security review passed (ADMIN-only for /actuator/\*\*)
- [ ] No sensitive data logged or exposed

---

## 📈 Execution Plan (14 days)

### Week 1 (Days 1-7): Core Foundations

```
Day 1-2:   Task A1 (Migrations) + Document JSONB schemas
Day 3-4:   Task A2 (Entities/Repos) + Task A6 (Seed Data)
Day 5-6:   Task A3 (Services + LessonContentValidator)
Day 7:     Task D1 (Actuator) + Buffer
```

### Week 2 (Days 8-14): APIs & Features (Rebalanced)

```
Day 8-9:   Task A4 (Controllers)
Day 10:    Task B1 (Learning Path Migrations + Seed)
Day 11:    Task A5 (Swagger Updates)
Day 12:    Task B2 (Learning Path API + Tracking)
Day 13:    Task C1 (Progress Migrations)
Day 14:    Task C2 (Progress API + Testing)
```

**Rationale for Rebalancing**:

- Week 1: Front-loads seed data setup for easier testing
- Week 2: Spreads 10 points more evenly (was 7 points in 7 days)
- Day 10-12: Completes Learning Path before Progress Tracking (logical dependency)
- Day 14: Reserved for final integration testing and fixes

---

## ⚠️ Risks & Mitigation

| Risk                   | Impact | Probability | Mitigation                                               |
| ---------------------- | ------ | ----------- | -------------------------------------------------------- |
| JSONB complexity       | High   | Medium      | Document schemas FIRST, start simple, iterate            |
| JSONB validation       | Medium | Medium      | LessonContentValidator with clear error messages         |
| Concurrent enrollments | Medium | Low         | UNIQUE constraint + @Transactional + race condition test |
| Timezone handling      | Medium | Medium      | Use user timezone, fallback UTC, test edge cases         |
| Actuator security      | High   | Low         | Restrict endpoints, review Spring Security config        |
| Week 2 overload        | Medium | Medium      | Rebalanced execution plan, buffer on Day 7 & 14          |

---

## ✅ Definition of Done (Sprint Level)

**Completion Criteria**:

- [ ] All planned stories completed (19-21 points)
- [ ] Overall test coverage ≥70%
- [ ] Service layer coverage ≥80%
- [ ] All endpoints documented in Swagger
- [ ] No critical/high severity bugs
- [ ] Code reviewed and merged to main
- [ ] `docs/implement/sprint-2/daily-log.md` updated daily
- [ ] Postman collection updated
- [ ] API-SPECIFICATION.md reflects new endpoints

**Functional Exit Criteria** (Must Pass):

- [ ] Can create course with 3+ sections and 10+ lessons via API
- [ ] Can search/filter courses by CEFR level and published status
- [ ] Can retrieve learning path with courses in correct order
- [ ] Can enroll in course (duplicate prevented with 409)
- [ ] Can complete lesson and see progress update to 100%
- [ ] Can track current position in learning path
- [ ] Streak calculation accurate across timezones (tested)
- [ ] Actuator health endpoint returns UP status
- [ ] Swagger UI shows 18+ new endpoints with examples
- [ ] Seed data creates 3 courses successfully

**Quality Gates**:

- [ ] All tests passing (100% pass rate)
- [ ] No TODO comments in production code
- [ ] JSONB schemas documented with examples
- [ ] LessonContentValidator tested for all 4 lesson types
- [ ] Concurrent enrollment test passes
- [ ] Performance: p95 <500ms for list endpoints (local)

---

## 📊 Success Metrics

- **Velocity**: Achieve 19-21 story points (includes seed data task)
- **Quality**: 70%+ overall coverage, 80%+ service coverage
- **API Completeness**: 18+ new endpoints functional
- **Documentation**: 100% Swagger coverage + JSONB schemas documented
- **Performance**: p95 < 500ms for list endpoints (local)
- **Data Quality**: 3 seeded courses with 18 total lessons
- **Functional Tests**: 10+ exit criteria passed

---

## 🔄 Learnings from Sprint 1 Applied

1. ✅ Start with database migrations (avoid schema changes mid-sprint)
2. ✅ Write tests alongside code (not at the end)
3. ✅ Keep PRs small (<500 lines)
4. ✅ Update Swagger documentation incrementally
5. ✅ Use JaCoCo reports daily to track coverage
6. ✅ Reserve last 2 days for integration testing & bug fixes

## 📝 New Best Practices for Sprint 2

1. ✅ Document JSONB schemas BEFORE writing migrations
2. ✅ Create seed data early (Day 3-4) for easier manual testing
3. ✅ Test concurrent operations (race conditions) for critical paths
4. ✅ Add timezone tests for date-based calculations
5. ✅ Validate complex inputs (JSONB) with dedicated validator classes
6. ✅ Keep execution plan balanced (avoid overloading final days)

## ❓ Open Questions / TBD

1. **Unenrollment**: User can unenroll from course? → **TBD** (not Sprint 2 scope)
2. **Course deletion**: What happens to enrollments when course deleted? → Document cascade policy
3. **Performance baseline**: Track Sprint 1 endpoint performance? → **NO** (skip for Sprint 2)
4. **Advanced recommendation**: Track cross-path progress? → **Sprint 3** (document TODO in code)
