# Sprint 2 - Detailed Task Breakdown

**Sprint**: 2 / 6  
**Duration**: October 29 – November 11, 2025 (14 days)  
**Total Story Points**: 21 points  
**Status**: 🔵 In Progress (Day 2)  
**Completed**: 7.75/21 points (36.9%)  
**Last Updated**: October 31, 2025 20:30

---

## 📋 Task Breakdown Overview

| Epic                          | Tasks  | Subtasks | Completed | Total Points | Progress  |
| ----------------------------- | ------ | -------- | --------- | ------------ | --------- |
| A: Course & Lesson Management | 6      | 28       | 11/28     | 13           | 39.3%     |
| B: Learning Path              | 2      | 10       | 0/10      | 4            | 0%        |
| C: Progress Tracking          | 2      | 9        | 0/9       | 3            | 0%        |
| D: Technical Improvements     | 1      | 3        | 0/3       | 1            | 0%        |
| **TOTAL**                     | **11** | **50**   | **11/50** | **21**       | **36.9%** |

---

## 🎯 EPIC A: Course & Lesson Management (13 points)

**Progress**: 7.75/13 points completed (59.6%)

---

### Task A1: Database Migrations (3 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: None | **Estimated**: 2 days  
**Status**: ✅ Complete | **Progress**: 3/3 points (100%)  
**Started**: 2025-10-30 | **Completed**: 2025-10-30

#### Subtasks:

#### A1.1: Create V5 Migration - Courses Table (1 point) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-30

- [x] Create `V5__Create_courses_table.sql` file
- [x] Define courses table schema:
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] title (VARCHAR(255) NOT NULL)
  - [x] description (TEXT)
  - [x] thumbnail_url (VARCHAR(255))
  - [x] cefr_level (VARCHAR(2) CHECK constraint for A1-C2)
  - [x] is_published (BOOLEAN DEFAULT false)
  - [x] created_at (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
  - [x] updated_at (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
- [x] Add indexes:
  - [x] Composite index on (cefr_level, is_published)
  - [x] Index on created_at DESC
  - [x] B-tree index on title
- [x] Add table and column comments
- [x] Test migration: Successfully applied via Flyway (48ms)
- [x] Verify indexes: All 3 indexes created successfully

**Deliverables**:

- ✅ `src/main/resources/db/migration/V5__Create_courses_table.sql`
- ✅ `docs/implement/sprint-2/migration-v5-verification.md`
- ✅ Migration applied to PostgreSQL v17.6
- ✅ All tests passing

#### A1.2: Create V6 Migration - Sections and Lessons Tables (1.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-30

- [x] Create `V6__Create_sections_and_lessons_table.sql` file
- [x] Create lesson_type_enum:
  - [x] READING, LISTENING, QUIZ, SPEAKING
- [x] Define sections table:
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] course_id (FK to courses, CASCADE DELETE)
  - [x] title (VARCHAR(255) NOT NULL)
  - [x] order_index (INTEGER NOT NULL)
  - [x] UNIQUE constraint on (course_id, order_index)
  - [x] Index on (course_id, order_index)
- [x] Define lessons table:
  - [x] id (BIGSERIAL PRIMARY KEY)
  - [x] section_id (FK to sections, CASCADE DELETE)
  - [x] title (VARCHAR(255) NOT NULL)
  - [x] lesson_type (lesson_type_enum NOT NULL)
  - [x] content (JSONB NOT NULL)
  - [x] order_index (INTEGER NOT NULL)
  - [x] duration_minutes (INTEGER DEFAULT 15)
  - [x] CHECK constraint (duration 1-240 minutes)
  - [x] UNIQUE constraint on (section_id, order_index)
  - [x] Indexes on (section_id, order_index) and lesson_type
- [x] Add comments referencing JSONB schemas
- [x] Test migration: Successfully applied via Flyway (28ms)
- [x] Verify JSONB column accepts valid data

**Deliverables**:

- ✅ `src/main/resources/db/migration/V6__Create_sections_and_lessons_table.sql`
- ✅ Created lesson_type_enum with 4 types
- ✅ Sections table with CASCADE DELETE and unique ordering
- ✅ Lessons table with JSONB content and duration constraint
- ✅ 2 indexes for performance optimization
- ✅ Migration applied to PostgreSQL v17.6
- ✅ All tests passing

#### A1.3: Test and Verify Migrations (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-30

- [x] Run migrations on clean database
- [x] Test rollback: Flyway clean/migrate (manual testing)
- [x] Verify all constraints work:
  - [x] Try insert invalid cefr_level (should fail) → PASS: Rejected D1
  - [x] Try duplicate order_index (should fail) → PASS: Rejected duplicates
  - [x] Try invalid duration (should fail) → PASS: Rejected 0 and 300
- [x] Run EXPLAIN ANALYZE on key queries:
  - [x] Search by cefr_level and is_published → Index used (0.029ms)
  - [x] Get course with sections (ordered) → Index used (0.023ms)
  - [x] Filter lessons by type → Index used (0.024ms)
- [x] Document performance findings → See migration-testing-report-a1.3.md
- [x] Update DATABASE-SCHEMA.md if needed → No changes required

**Deliverables**:

- ✅ `test-migrations-clean.sql` - Comprehensive test script with DO blocks
- ✅ `docs/implement/sprint-2/migration-testing-report-a1.3.md` - 20+ page report
- ✅ 8/8 test categories passed (100% success rate)
- ✅ 20/20 constraints verified
- ✅ 5/5 indexes verified and used by query planner
- ✅ CASCADE DELETE behavior confirmed
- ✅ All tests passing (81% coverage maintained)

---

### Task A2: JPA Entities & Repositories (3 points) ✅ COMPLETE

**Priority**: P0 | **Dependencies**: A1 | **Estimated**: 2 days  
**Status**: ✅ Complete | **Progress**: 6/6 subtasks (100%)  
**Started**: 2025-10-31 | **Completed**: 2025-10-31

#### Subtasks:

#### A2.1: Create Course Entity (0.75 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-31

- [x] Create `Course.java` entity class
- [x] Add annotations:
  - [x] @Entity, @Table(name = "courses")
  - [x] @Id @GeneratedValue(strategy = IDENTITY)
  - [x] @NotBlank on title
  - [x] @Pattern for cefrLevel validation
- [x] Define fields matching database schema
- [x] Add relationships:
  - [x] @OneToMany to sections (cascade ALL, orphanRemoval)
- [x] Add @CreationTimestamp, @UpdateTimestamp (auditing)
- [x] Override equals/hashCode (based on id)
- [x] Add JavaDoc comments

**Deliverables**:

- ✅ `src/main/java/com/lexia/backend/entity/Course.java` (137 lines)
- ✅ Helper methods: addSection(), removeSection()
- ✅ Comprehensive JavaDoc documentation

#### A2.2: Create Section Entity (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-31

- [x] Create `Section.java` entity class
- [x] Add @Entity, @Table(name = "sections")
- [x] Define relationships:
  - [x] @ManyToOne to Course
  - [x] @OneToMany to Lesson
- [x] Add @OrderBy("orderIndex") on lessons
- [x] Implement Comparable<Section> (order by orderIndex)
- [x] Override equals/hashCode

**Deliverables**:

- ✅ `src/main/java/com/lexia/backend/entity/Section.java` (140 lines)
- ✅ Helper methods: addLesson(), removeLesson()
- ✅ Implements Comparable interface with null-safe compareTo()

#### A2.3: Create Lesson Entity (0.75 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-31

- [x] Create `Lesson.java` entity class
- [x] Add @Entity, @Table(name = "lessons")
- [x] Define LessonType enum (READING, LISTENING, QUIZ, SPEAKING)
- [x] Add JSONB support:
  - [x] @JdbcTypeCode(SqlTypes.JSON)
  - [x] @Column(columnDefinition = "jsonb") for content
  - [x] Use String for content field (flexible serialization)
- [x] Add @ManyToOne to Section
- [x] Add validation annotations
- [x] Override equals/hashCode

**Deliverables**:

- ✅ `src/main/java/com/lexia/backend/entity/Lesson.java` (166 lines)
- ✅ LessonType enum with 4 values and JavaDoc
- ✅ JSONB support ready for LessonContentValidator

#### A2.4: Create Repository Interfaces (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-31

- [x] Create `CourseRepository` extends JpaRepository
  - [x] Custom query: findByCefrLevelAndIsPublished
  - [x] Custom query: findByTitleContainingIgnoreCase
  - [x] Query with @EntityGraph to avoid N+1
  - [x] Extends JpaSpecificationExecutor for dynamic queries
- [x] Create `SectionRepository`
  - [x] findByCourseIdOrderByOrderIndexAsc
  - [x] Order management methods
- [x] Create `LessonRepository`
  - [x] findBySectionIdOrderByOrderIndexAsc
  - [x] findByLessonType
  - [x] countBySectionId
  - [x] Cross-section analytics queries

**Deliverables**:

- ✅ `src/main/java/com/lexia/backend/repository/CourseRepository.java` (98 lines, 10 methods)
- ✅ `src/main/java/com/lexia/backend/repository/SectionRepository.java` (73 lines, 7 methods)
- ✅ `src/main/java/com/lexia/backend/repository/LessonRepository.java` (140 lines, 12 methods)
- ✅ All queries documented with index usage
- ✅ @EntityGraph prevents N+1 queries

#### A2.5: Create Specifications for Filtering (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-31

- [x] Create `CourseSpecifications` class
- [x] Add specification methods:
  - [x] hasTitle(String title) - ILIKE search
  - [x] hasCefrLevel(String level)
  - [x] isPublished(Boolean published)
  - [x] createdBetween(LocalDateTime start, end)
  - [x] Additional methods: createdAfter, createdBefore, descriptionContains
  - [x] Composite methods: searchCourses, advancedSearch
- [x] Test specification combinations (AND/OR) - composable design

**Deliverables**:

- ✅ `src/main/java/com/lexia/backend/specification/CourseSpecifications.java` (220 lines, 9 methods)
- ✅ All specifications null-safe with conjunction fallback
- ✅ Comprehensive JavaDoc with usage examples
- ✅ Composable design for complex queries

#### A2.6: Write Repository Tests (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-31

- [x] Create @DataJpaTest for CourseRepository
- [x] Test CRUD operations
- [x] Test custom queries
- [x] Test specifications
- [x] Test N+1 query prevention (@EntityGraph)
- [x] Verify 80%+ coverage on repository layer
- [x] Test JSONB serialization/deserialization

**Deliverables**:

- ✅ `CourseRepositoryTest.java` (600+ lines, 30 tests)
- ✅ `SectionRepositoryTest.java` (500+ lines, 20 tests)
- ✅ `LessonRepositoryTest.java` (700+ lines, 32 tests)
- ✅ Total: 82 tests, all passing (100%)
- ✅ Comprehensive coverage of all repositories

---

### Task A3: Service Layer + DTOs (3 points)

**Priority**: P0 | **Dependencies**: A2 | **Estimated**: 2 days  
**Status**: 🔵 In Progress | **Progress**: 1.25/3 points (41.7%)

#### Subtasks:

#### A3.1: Create DTOs (0.75 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-31

- [x] Create `CourseDTO` (response):
  - [x] id, title, description, thumbnailUrl, cefrLevel
  - [x] isPublished, createdAt, updatedAt
  - [x] sectionCount (derived)
- [x] Create `CreateCourseDTO` (input):
  - [x] @NotBlank title
  - [x] @Size(max=1000) description
  - [x] @Pattern cefrLevel
  - [x] @URL thumbnailUrl
- [x] Create `UpdateCourseDTO` (input) - all optional
- [x] Create `CourseSearchDTO` (filter params):
  - [x] title, cefrLevel, isPublished
  - [x] createdAfter, createdBefore
  - [x] Pageable (page, size, sort)
- [x] Create `SectionDTO`, `LessonDTO`
- [x] Create `CreateLessonDTO` with content validation

**Deliverables**:

- ✅ 7 DTOs created with comprehensive validation and Swagger annotations
- ✅ All DTOs follow Spring Boot best practices

#### A3.2: Create Mappers (0.5 points) ✅ COMPLETE

**Status**: ✅ Complete | **Completed**: 2025-10-31

- [x] Create `CourseMapper` (manual mapping)
  - [x] toDTO(Course entity)
  - [x] toEntity(CreateCourseDTO dto)
  - [x] updateEntity(UpdateCourseDTO dto, Course entity)
- [x] Create `SectionMapper`
- [x] Create `LessonMapper`
- [x] Write mapper tests (verify all fields mapped)

**Deliverables**:

- ✅ 3 mapper utility classes with 31 comprehensive tests
- ✅ All tests passing (100%)

#### A3.3: Create LessonContentValidator (0.75 points)

- [ ] Create `LessonContentValidator` service
- [ ] Implement validate(LessonType, JsonNode) method
- [ ] Implement validateReadingContent():
  - [ ] Check passages[] exists and not empty
  - [ ] Check questions[] exists and not empty
  - [ ] Validate correctAnswer indexes
  - [ ] Validate vocabulary[] if present
- [ ] Implement validateListeningContent():
  - [ ] Check audioUrl is valid URL
  - [ ] Check duration > 0
  - [ ] Check transcript exists
  - [ ] Validate timestamps <= duration
- [ ] Implement validateQuizContent():
  - [ ] Check questions[] exists
  - [ ] Validate passingScore (0-100)
  - [ ] Validate points are positive
- [ ] Implement validateSpeakingContent():
  - [ ] Check scenario exists
  - [ ] Check difficulty is valid enum
  - [ ] Check prompts[] not empty
  - [ ] Validate turns (1-20)
- [ ] Create `InvalidLessonContentException`
- [ ] Write comprehensive tests for all types

#### A3.4: Create CourseService (0.5 points)

- [ ] Create `CourseService` interface
- [ ] Create `CourseServiceImpl`
- [ ] Implement create(CreateCourseDTO):
  - [ ] Check for duplicate title
  - [ ] Check user has CONTENT_MANAGER role
  - [ ] Save and return DTO
- [ ] Implement update(Long id, UpdateCourseDTO):
  - [ ] Check course exists
  - [ ] Check user authorization
  - [ ] Update only provided fields
- [ ] Implement delete(Long id):
  - [ ] Check if published (must unpublish first)
  - [ ] Delete course
- [ ] Implement publish(Long id):
  - [ ] Validate course has content
  - [ ] Set isPublished = true
- [ ] Implement search(CourseSearchDTO):
  - [ ] Use Specifications
  - [ ] Return Page<CourseDTO>

#### A3.5: Create LessonService (0.5 points)

- [ ] Create `LessonService` interface
- [ ] Create `LessonServiceImpl`
- [ ] Implement create(CreateLessonDTO):
  - [ ] Validate content with LessonContentValidator
  - [ ] Check section exists
  - [ ] Set order_index
- [ ] Implement update(Long id, UpdateLessonDTO):
  - [ ] Validate content if changed
  - [ ] Check authorization
- [ ] Implement getById(Long id)
- [ ] Implement delete(Long id)

#### A3.6: Write Service Tests (0.5 points)

- [ ] Create CourseServiceTest
- [ ] Test all CRUD operations
- [ ] Test business rules:
  - [ ] Duplicate title prevented
  - [ ] Published course can't be deleted
  - [ ] Unpublished course can be deleted
  - [ ] Only CONTENT_MANAGER can create/edit
- [ ] Test exception cases
- [ ] Create LessonServiceTest
- [ ] Test JSONB validation for all types
- [ ] Achieve 80%+ service coverage
- [ ] Mock repository dependencies

---

### Task A4: REST API Controllers (2 points)

**Priority**: P0 | **Dependencies**: A3 | **Estimated**: 1.5 days

#### Subtasks:

#### A4.1: Create CourseController (0.75 points)

- [ ] Create `CourseController` class
- [ ] Add @RestController, @RequestMapping("/api/v1/courses")
- [ ] Implement endpoints:
  - [ ] GET / - list with pagination/filter
  - [ ] GET /{id} - get by ID
  - [ ] GET /search - advanced search
  - [ ] POST / - create (CONTENT_MANAGER only)
  - [ ] PUT /{id} - update (CONTENT_MANAGER only)
  - [ ] DELETE /{id} - delete (CONTENT_MANAGER only)
  - [ ] POST /{id}/publish - publish course
- [ ] Add @PreAuthorize on protected endpoints
- [ ] Add @Valid on request bodies
- [ ] Add proper HTTP status codes
- [ ] Add ResponseEntity with proper headers

#### A4.2: Create LessonController (0.5 points)

- [ ] Create `LessonController`
- [ ] Add @RequestMapping("/api/v1/lessons")
- [ ] Implement endpoints:
  - [ ] GET /{id} - get lesson details
  - [ ] POST /courses/{courseId}/sections/{sectionId}/lessons - create
  - [ ] PUT /{id} - update
  - [ ] DELETE /{id} - delete
- [ ] Add authorization checks
- [ ] Handle JSONB content in responses

#### A4.3: Create Exception Handlers (0.25 points)

- [ ] Add to GlobalExceptionHandler:
  - [ ] CourseNotFoundException → 404
  - [ ] DuplicateCourseException → 409
  - [ ] InvalidLessonContentException → 400
- [ ] Return RFC 7807 format
- [ ] Include validation errors in response

#### A4.4: Write Controller Tests (0.5 points)

- [ ] Create CourseControllerTest (@WebMvcTest)
- [ ] Test all endpoints with MockMvc
- [ ] Test authorization (roles)
- [ ] Test validation errors (400)
- [ ] Test not found errors (404)
- [ ] Test pagination
- [ ] Create LessonControllerTest
- [ ] Test JSONB content handling
- [ ] Achieve 70%+ controller coverage

---

### Task A5: Swagger Documentation Update (1 point)

**Priority**: P1 | **Dependencies**: A4 | **Estimated**: 0.5 days

#### Subtasks:

#### A5.1: Add Swagger Annotations to DTOs (0.3 points)

- [ ] Add @Schema to all DTOs
- [ ] Add description to each field
- [ ] Add examples for each field
- [ ] Document validation constraints
- [ ] Add @Schema(example = "...") for complex types

#### A5.2: Add Swagger Annotations to Controllers (0.4 points)

- [ ] Add @Tag to CourseController
- [ ] Add @Operation to each endpoint:
  - [ ] summary
  - [ ] description
  - [ ] request/response examples
- [ ] Add @ApiResponse for all status codes:
  - [ ] 200 OK
  - [ ] 201 Created
  - [ ] 400 Bad Request
  - [ ] 401 Unauthorized
  - [ ] 403 Forbidden
  - [ ] 404 Not Found
  - [ ] 409 Conflict
- [ ] Document authentication requirements
- [ ] Add @Parameter descriptions

#### A5.3: Test and Validate Swagger UI (0.3 points)

- [ ] Start application
- [ ] Access http://localhost:8080/swagger-ui.html
- [ ] Verify all endpoints render correctly
- [ ] Test "Try it out" functionality
- [ ] Verify request/response examples
- [ ] Take screenshots
- [ ] Update API-SPECIFICATION.md
- [ ] Update Postman collection

---

### Task A6: Seed Data Script (1 point)

**Priority**: P1 | **Dependencies**: A1, A2 | **Estimated**: 0.5 days

#### Subtasks:

#### A6.1: Create CourseSeeder Component (0.5 points)

- [ ] Create `CourseSeeder` class
- [ ] Add @Component, @Profile("dev")
- [ ] Implement ApplicationRunner interface
- [ ] Create method to check if data exists
- [ ] Create seed data:
  - [ ] Course 1: "English Basics (A1)"
    - [ ] Section 1: "Getting Started" (3 lessons)
    - [ ] Section 2: "Daily Conversations" (3 lessons)
  - [ ] Course 2: "Intermediate English (B1)"
    - [ ] Section 1: "Work and Career" (3 lessons)
    - [ ] Section 2: "Travel and Culture" (3 lessons)
  - [ ] Course 3: "Advanced English (C1)"
    - [ ] Section 1: "Business English" (3 lessons)
    - [ ] Section 2: "Academic Writing" (3 lessons)

#### A6.2: Create Sample Lesson Content (0.3 points)

- [ ] Create READING lesson samples (valid JSONB):
  - [ ] Use schemas from DATABASE-SCHEMA.md
  - [ ] Realistic passages and questions
- [ ] Create LISTENING lesson samples:
  - [ ] Use sample audio URLs
  - [ ] Include transcripts
- [ ] Create QUIZ lesson samples:
  - [ ] Grammar questions
  - [ ] Vocabulary questions
- [ ] Create SPEAKING lesson samples:
  - [ ] Conversation prompts
  - [ ] Sample answers

#### A6.3: Test and Verify Seeder (0.2 points)

- [ ] Run application with dev profile
- [ ] Verify 3 courses created
- [ ] Verify 6 sections created
- [ ] Verify 18 lessons created
- [ ] Verify all JSONB content is valid
- [ ] Test idempotency (run twice, same result)
- [ ] Log seed status clearly
- [ ] Update README with seed data info

---

## 🎯 EPIC B: Learning Path (4 points)

---

### Task B1: Learning Path Migrations + Seed Data (2 points)

**Priority**: P1 | **Dependencies**: A1 | **Estimated**: 1 day

#### Subtasks:

#### B1.1: Create V7 Migration - Learning Path Tables (1 point)

- [ ] Create `V7__Create_learning_paths_table.sql`
- [ ] Define learning_paths table:
  - [ ] id (BIGSERIAL PRIMARY KEY)
  - [ ] name (VARCHAR(100) NOT NULL)
  - [ ] description (TEXT)
  - [ ] cefr_level (VARCHAR(2) NOT NULL)
  - [ ] is_default (BOOLEAN DEFAULT false)
  - [ ] created_at (TIMESTAMP)
- [ ] Define learning_path_courses table:
  - [ ] path_id (FK to learning_paths)
  - [ ] course_id (FK to courses)
  - [ ] order_index (INTEGER NOT NULL)
  - [ ] PRIMARY KEY (path_id, course_id)
- [ ] Define user_learning_paths table:
  - [ ] id (BIGSERIAL PRIMARY KEY)
  - [ ] user_id (FK to users)
  - [ ] path_id (FK to learning_paths)
  - [ ] current_course_id (FK to courses)
  - [ ] started_at (TIMESTAMP)
  - [ ] UNIQUE (user_id, path_id)
- [ ] Add indexes for performance
- [ ] Test migration

#### B1.2: Create Seed Data for Default Paths (0.75 points)

- [ ] Create SQL seed script or Java seeder
- [ ] Insert 6 default paths:
  - [ ] "Beginner Path (A1)"
  - [ ] "Elementary Path (A2)"
  - [ ] "Intermediate Path (B1)"
  - [ ] "Upper Intermediate Path (B2)"
  - [ ] "Advanced Path (C1)"
  - [ ] "Proficiency Path (C2)"
- [ ] Link courses to paths (use seeded courses)
- [ ] Set order_index for each course in path
- [ ] Mark all as is_default = true
- [ ] Test seed script idempotency

#### B1.3: Test and Verify (0.25 points)

- [ ] Verify all paths created
- [ ] Verify course associations correct
- [ ] Test queries for path retrieval
- [ ] Document seed data

---

### Task B2: Learning Path API (2 points)

**Priority**: P1 | **Dependencies**: B1 | **Estimated**: 1 day

#### Subtasks:

#### B2.1: Create Entities and Repositories (0.5 points)

- [ ] Create `LearningPath` entity
- [ ] Create `LearningPathCourse` entity (join table)
- [ ] Create `UserLearningPath` entity
- [ ] Create `LearningPathRepository`:
  - [ ] findByCefrLevel
  - [ ] findByIsDefaultTrue
- [ ] Create `UserLearningPathRepository`:
  - [ ] findByUserIdAndPathId
  - [ ] findByUserId

#### B2.2: Create DTOs and Mappers (0.3 points)

- [ ] Create `LearningPathDTO`:
  - [ ] id, name, description, cefrLevel
  - [ ] courses[] (with order)
  - [ ] totalCourses, estimatedHours
- [ ] Create `UserPathProgressDTO`:
  - [ ] pathId, pathName
  - [ ] currentCourseId, currentCourseTitle
  - [ ] coursesCompleted, totalCourses
  - [ ] progressPercentage
- [ ] Create mappers

#### B2.3: Create LearningPathService (0.5 points)

- [ ] Create `LearningPathService` interface
- [ ] Implement getAllPaths()
- [ ] Implement getPathById(Long id)
- [ ] Implement getRecommendedPath(User user):
  - [ ] Check user's cefrLevel
  - [ ] Return matching default path
  - [ ] Return A1 if no level set
  - [ ] TODO: Add progressive recommendation (Sprint 3)
- [ ] Implement startPath(User user, Long pathId):
  - [ ] Check if already started
  - [ ] Create UserLearningPath record
  - [ ] Set current_course_id to first course
- [ ] Implement getMyProgress(User user)

#### B2.4: Create LearningPathController (0.4 points)

- [ ] Create `LearningPathController`
- [ ] Add @RequestMapping("/api/v1/learning-paths")
- [ ] Implement endpoints:
  - [ ] GET / - list all paths
  - [ ] GET /{id} - get path details
  - [ ] GET /recommend - get recommended path
  - [ ] POST /{id}/start - start learning path
  - [ ] GET /my-progress - get user's progress
- [ ] Add @PreAuthorize where needed
- [ ] Handle duplicate start (409 Conflict)

#### B2.5: Write Tests (0.3 points)

- [ ] Write service tests (80%+ coverage):
  - [ ] Test recommendation logic
  - [ ] Test start path (new and duplicate)
  - [ ] Test progress calculation
- [ ] Write controller tests (70%+ coverage)
- [ ] Update Swagger documentation

---

## 🎯 EPIC C: Progress Tracking (3 points)

---

### Task C1: Progress Tracking Migrations (1 point)

**Priority**: P1 | **Dependencies**: A1, A2 | **Estimated**: 0.5 days

#### Subtasks:

#### C1.1: Create V8 Migration - Enrollment Table (0.5 points)

- [ ] Create `V8__Create_enrollments_table.sql`
- [ ] Define enrollments table:
  - [ ] id (BIGSERIAL PRIMARY KEY)
  - [ ] user_id (FK to users)
  - [ ] course_id (FK to courses)
  - [ ] enrolled_at (TIMESTAMP DEFAULT NOW)
  - [ ] progress_percentage (INTEGER DEFAULT 0)
  - [ ] completed_at (TIMESTAMP NULL)
  - [ ] UNIQUE constraint (user_id, course_id)
- [ ] Add index on user_id
- [ ] Add index on course_id
- [ ] Test migration

#### C1.2: Create V9 Migration - Lesson Progress Table (0.5 points)

- [ ] Create `V9__Create_lesson_progress_table.sql`
- [ ] Define lesson_progress table:
  - [ ] id (BIGSERIAL PRIMARY KEY)
  - [ ] user_id (FK to users)
  - [ ] lesson_id (FK to lessons)
  - [ ] status (VARCHAR(20) CHECK: NOT_STARTED, IN_PROGRESS, COMPLETED)
  - [ ] score (INTEGER NULL)
  - [ ] attempts (INTEGER DEFAULT 0)
  - [ ] result_details (JSONB NULL)
  - [ ] completed_at (TIMESTAMP NULL)
  - [ ] UNIQUE constraint (user_id, lesson_id)
- [ ] Add index on (user_id, lesson_id)
- [ ] Test migration and constraints

---

### Task C2: Progress Tracking API (2 points)

**Priority**: P1 | **Dependencies**: C1 | **Estimated**: 1 day

#### Subtasks:

#### C2.1: Create Entities and Repositories (0.5 points)

- [ ] Create `Enrollment` entity:
  - [ ] Add @Transactional for concurrent safety
  - [ ] Add method to calculate progress
- [ ] Create `LessonProgress` entity:
  - [ ] Add JSONB field for result_details
  - [ ] Add status enum
- [ ] Create `EnrollmentRepository`:
  - [ ] findByUserId
  - [ ] findByUserIdAndCourseId
  - [ ] existsByUserIdAndCourseId
- [ ] Create `LessonProgressRepository`:
  - [ ] findByUserIdAndLessonId
  - [ ] findByUserIdAndStatus
  - [ ] countByUserIdAndCompletedAtBetween (for streak)

#### C2.2: Create DTOs (0.3 points)

- [ ] Create `EnrollmentDTO`:
  - [ ] courseId, courseTitle, thumbnailUrl
  - [ ] enrolledAt, progressPercentage
  - [ ] completedAt
- [ ] Create `CourseProgressDTO`:
  - [ ] courseId, courseTitle
  - [ ] totalLessons, completedLessons
  - [ ] progressPercentage
  - [ ] lessonProgress[] (per lesson status)
- [ ] Create `StreakDTO`:
  - [ ] currentStreak (days)
  - [ ] longestStreak
  - [ ] lastActivityDate

#### C2.3: Create EnrollmentService (0.5 points)

- [ ] Create `EnrollmentService` interface
- [ ] Implement enroll(User user, Long courseId):
  - [ ] Check if already enrolled (409 if exists)
  - [ ] Use @Transactional
  - [ ] Handle race condition (UNIQUE constraint)
  - [ ] Create enrollment record
- [ ] Implement getMyEnrollments(User user)
- [ ] Implement getCourseProgress(User user, Long courseId):
  - [ ] Calculate: (completed_lessons / total_lessons) \* 100
  - [ ] Return detailed progress per lesson

#### C2.4: Create ProgressService (0.4 points)

- [ ] Create `ProgressService` interface
- [ ] Implement completeLesson(User user, Long lessonId, resultDetails):
  - [ ] Create or update LessonProgress
  - [ ] Set status = COMPLETED
  - [ ] Increment attempts
  - [ ] Save result_details (JSONB)
  - [ ] Update enrollment progress_percentage
  - [ ] Check if course completed
- [ ] Implement getStreak(User user):
  - [ ] Get all lesson completions
  - [ ] Calculate consecutive days
  - [ ] Handle timezone (use user profile or UTC)
  - [ ] Return current and longest streak

#### C2.5: Create Controllers (0.3 points)

- [ ] Add endpoints to `EnrollmentController`:
  - [ ] POST /api/v1/enrollments - enroll in course
  - [ ] GET /api/v1/enrollments - my enrollments
  - [ ] GET /api/v1/enrollments/{courseId}/progress - course detail
- [ ] Add endpoints to `ProgressController`:
  - [ ] POST /api/v1/lessons/{id}/complete - mark complete
  - [ ] GET /api/v1/progress/streak - get streak

#### C2.6: Write Tests (0.5 points)

- [ ] Write EnrollmentServiceTest:
  - [ ] Test enrollment (new and duplicate)
  - [ ] Test concurrent enrollment (race condition)
  - [ ] Test progress calculation
- [ ] Write ProgressServiceTest:
  - [ ] Test lesson completion
  - [ ] Test streak calculation with different timezones
  - [ ] Test edge cases (same-day completions)
- [ ] Write controller tests
- [ ] Achieve 80%+ service, 70%+ controller coverage
- [ ] Update Swagger documentation

---

## 🎯 EPIC D: Technical Improvements (1 point)

---

### Task D1: Actuator Configuration (1 point)

**Priority**: P0 | **Dependencies**: None | **Estimated**: 0.5 days

#### Subtasks:

#### D1.1: Configure Spring Boot Actuator (0.4 points)

- [ ] Add actuator dependency to build.gradle (if not present)
- [ ] Configure in application.yml:
  - [ ] Expose endpoints: health, info, metrics
  - [ ] Set base-path: /actuator
  - [ ] Configure health details: when-authorized
- [ ] Test actuator endpoints:
  - [ ] http://localhost:8080/actuator/health
  - [ ] http://localhost:8080/actuator/info
  - [ ] http://localhost:8080/actuator/metrics

#### D1.2: Configure Security for Actuator (0.3 points)

- [ ] Update SecurityConfig:
  - [ ] Restrict /actuator/\*\* to ADMIN role only
  - [ ] Allow /actuator/health publicly
  - [ ] Require authentication for metrics
- [ ] Test security:
  - [ ] Try access without auth (should 401)
  - [ ] Try with LEARNER role (should 403)
  - [ ] Try with ADMIN role (should 200)
- [ ] Ensure no sensitive data exposed

#### D1.3: Document and Test (0.3 points)

- [ ] Update README with actuator endpoints
- [ ] Document which role can access what
- [ ] Create smoke tests for actuator
- [ ] Verify no sensitive data in logs
- [ ] Update Postman collection with actuator requests
- [ ] Add to API-SPECIFICATION.md

---

## 📊 Task Tracking

### Completion Tracking Template

```markdown
## [Task ID]: [Task Name]

**Status**: 🔵 Not Started | ⏳ In Progress | ✅ Complete | ⚠️ Blocked
**Assignee**: [Name]
**Started**: YYYY-MM-DD
**Completed**: YYYY-MM-DD
**Actual Points**: X.X

### Subtasks Progress: X/Y completed

- [ ] Subtask 1
- [ ] Subtask 2
- [ ] ...

### Notes:

- Any blockers or important decisions
```

---

## 🎯 Daily Task Targets

### Week 1 (Days 1-7): Foundation

**Day 1 (Oct 30)**:

- [ ] A1.1: V5 Migration (courses)
- [ ] A1.2: V6 Migration (sections/lessons)
- [ ] A1.3: Test migrations

**Day 2 (Oct 31)**:

- [ ] A2.1: Course entity
- [ ] A2.2: Section entity
- [ ] A2.3: Lesson entity
- [ ] A2.4: Repositories

**Day 3 (Nov 1)**:

- [ ] A2.5: Specifications
- [ ] A2.6: Repository tests
- [ ] A6.1: CourseSeeder start

**Day 4 (Nov 3)**:

- [ ] A6.2: Sample lesson content
- [ ] A6.3: Test seeder
- [ ] A3.1: Create DTOs

**Day 5 (Nov 4)**:

- [ ] A3.2: Create mappers
- [ ] A3.3: LessonContentValidator

**Day 6 (Nov 5)**:

- [ ] A3.4: CourseService
- [ ] A3.5: LessonService

**Day 7 (Nov 6)**:

- [ ] A3.6: Service tests
- [ ] D1.1-D1.3: Actuator configuration

### Week 2 (Days 8-14): APIs & Features

**Day 8 (Nov 7)**:

- [ ] A4.1: CourseController
- [ ] A4.2: LessonController

**Day 9 (Nov 8)**:

- [ ] A4.3: Exception handlers
- [ ] A4.4: Controller tests

**Day 10 (Nov 10)**:

- [ ] B1.1: Learning path migrations
- [ ] B1.2: Seed default paths
- [ ] B1.3: Test paths

**Day 11 (Nov 11)**:

- [ ] A5.1-A5.3: Swagger documentation
- [ ] B2.1: Path entities/repos

**Day 12 (Nov 12)**:

- [ ] B2.2: Path DTOs
- [ ] B2.3: LearningPathService
- [ ] B2.4: LearningPathController

**Day 13 (Nov 13)**:

- [ ] B2.5: Path tests
- [ ] C1.1: Enrollment migration
- [ ] C1.2: Lesson progress migration

**Day 14 (Nov 14)**:

- [ ] C2.1-C2.6: Progress tracking complete
- [ ] Final integration testing
- [ ] Sprint review prep

---

## ✅ Definition of Done Checklist

For each task to be considered "Done":

### Code Quality

- [ ] Code compiles without errors
- [ ] All tests pass (`./gradlew test`)
- [ ] Coverage targets met (70% overall, 80% services)
- [ ] No SonarQube critical issues
- [ ] Code reviewed (self-review checklist)

### Testing

- [ ] Unit tests written
- [ ] Integration tests for critical paths
- [ ] Edge cases tested
- [ ] Performance tested (if applicable)

### Documentation

- [ ] JavaDoc on public methods
- [ ] README updated (if needed)
- [ ] API docs updated (Swagger)
- [ ] daily-log.md updated

### Security

- [ ] No hardcoded secrets
- [ ] No plain-text passwords
- [ ] Input validation implemented
- [ ] Authorization checked

### Commit

- [ ] Conventional commit message
- [ ] Changes committed to feature branch
- [ ] No sensitive data in commits

---

## 📊 Sprint 2 Progress Tracker

### Overall Progress

- **Completed**: 3/50 subtasks (6%)
- **Story Points**: 3/21 points (14.3%)
- **Days Elapsed**: 2/14 days (14%)
- **Status**: ✅ On Schedule

### Completed Tasks

1. ✅ **A1.1** - V5 Migration: Courses Table (1 point) - Oct 30
2. ✅ **A1.2** - V6 Migration: Sections & Lessons Tables (1.5 points) - Oct 30
3. ✅ **A1.3** - Test and Verify Migrations (0.5 points) - Oct 30

### Current Sprint

- 📋 **A2** - JPA Entities & Repositories (3 points) - Ready to start

### Upcoming Next

- 📋 **A2.1-A2.6** - JPA Entities & Repositories (3 points)
- 📋 **A3.1-A3.6** - Service Layer + DTOs (3 points)

### Sprint Health Indicators

- ✅ No blockers
- ✅ All tests passing (81% coverage)
- ✅ Documentation up to date
- ✅ Zero technical debt introduced
- ✅ On schedule for Week 1 targets

---

**Total Subtasks**: 50  
**Completed Subtasks**: 2  
**Estimated Total**: 21 points  
**Points Completed**: 2.5  
**Ready to Execute**: ✅

**Last Updated**: October 30, 2025 19:52
