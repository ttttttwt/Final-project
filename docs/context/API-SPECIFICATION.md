# LEXIA - API Specification

**Version**: 2.5.0  
**Last Updated**: December 1, 2025  
**Sprint**: 4 / 8

---

## Base URL

```
Development: http://localhost:8088/api/v1
Production: https://api.lexia.app/api/v1
```

## Interactive API Documentation

**Swagger UI**: Available at `http://localhost:8088/swagger-ui.html`

The Swagger UI provides:

- Interactive API testing with "Try it out" functionality
- Comprehensive request/response examples for all endpoints
- Authentication testing with JWT tokens
- Detailed schema documentation for all DTOs
- JSONB content examples for all 4 lesson types (READING, LISTENING, QUIZ, SPEAKING)
- Quick links to runtime health checks via `/actuator`

---

## Authentication

### JWT Bearer Token

All endpoints (except registration and login) require JWT Bearer token authentication.

**Header Format**:

```
Authorization: Bearer <JWT_ACCESS_TOKEN>
```

**Token Lifecycle**:

- **Access Token**: Valid for 15 minutes
- **Refresh Token**: Valid for 7 days (family-based rotation)

**How to Obtain Token**:

1. Register via `POST /auth/register`
2. Login via `POST /auth/login` → Receive access token
3. Use access token in `Authorization` header for all subsequent requests
4. Refresh token via `POST /auth/refresh` when access token expires

---

## Roles & Permissions

| Role                | Permissions                                                   |
| ------------------- | ------------------------------------------------------------- |
| **USER** (Default)  | View published courses, manage own profile, enroll in courses |
| **CONTENT_MANAGER** | Create, update, delete, publish/unpublish courses and lessons |
| **ADMIN**           | Full system access (admin panel, monitoring, notifications)   |

---

## Error Response Format (RFC 7807)

All error responses follow RFC 7807 Problem Details format:

```json
{
  "timestamp": "2025-10-31T14:22:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for object='createCourseDTO'",
  "path": "/api/v1/courses",
  "errors": [
    {
      "field": "cefrLevel",
      "rejectedValue": "D1",
      "message": "CEFR level must be one of: A1, A2, B1, B2, C1, C2"
    }
  ]
}
```

### Common HTTP Status Codes

| Code | Meaning      | Usage                                                      |
| ---- | ------------ | ---------------------------------------------------------- |
| 200  | OK           | Successful GET, PUT, PATCH                                 |
| 201  | Created      | Successful POST (resource created)                         |
| 204  | No Content   | Successful DELETE                                          |
| 400  | Bad Request  | Validation errors, invalid input                           |
| 401  | Unauthorized | Missing or invalid token (e.g., anonymous actuator access) |
| 403  | Forbidden    | Insufficient permissions (e.g., non-admin actuator access) |
| 404  | Not Found    | Resource does not exist                                    |
| 409  | Conflict     | Duplicate resource (e.g., title already exists)            |

---

## Endpoint Categories

### 1. Authentication Endpoints

| Method | Endpoint         | Auth Required | Role | Description                              |
| ------ | ---------------- | ------------- | ---- | ---------------------------------------- |
| POST   | `/auth/register` | No            | -    | Register new user account                |
| POST   | `/auth/login`    | No            | -    | Login and obtain access/refresh tokens   |
| POST   | `/auth/refresh`  | No            | -    | Refresh access token using refresh token |
| POST   | `/auth/logout`   | Yes           | Any  | Logout and invalidate refresh token      |

**Example: Register**

```json
POST /api/v1/auth/register
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}

Response (201):
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER"
}
```

---

### 2. User Profile Endpoints

| Method | Endpoint                | Auth Required | Role | Description              |
| ------ | ----------------------- | ------------- | ---- | ------------------------ |
| GET    | `/users/profile`        | Yes           | Any  | Get current user profile |
| PUT    | `/users/profile`        | Yes           | Any  | Update user profile      |
| POST   | `/users/profile/avatar` | Yes           | Any  | Upload or update avatar  |
| DELETE | `/users/profile/avatar` | Yes           | Any  | Delete user avatar       |

**Example: Get Profile**

```json
GET /api/v1/users/profile
Authorization: Bearer <token>

Response (200):
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "bio": "English learner passionate about business communication",
  "phoneNumber": "+84901234567",
  "avatarUrl": "https://cdn.lexia.com/avatars/john-doe.jpg",
  "timezone": "Asia/Ho_Chi_Minh",
  "language": "en",
  "currentLevel": "B1",
  "learningGoal": "Business communication",
  "createdAt": "2025-10-30T10:15:30",
  "updatedAt": "2025-10-31T14:22:45"
}
```

**Example: Update Avatar**

```json
POST /api/v1/users/profile/avatar
Authorization: Bearer <token>
{
  "avatarUrl": "https://cdn.lexia.com/avatars/new-avatar.jpg"
}

Response (200): Empty body
```

**Example: Delete Avatar**

```json
DELETE /api/v1/users/profile/avatar
Authorization: Bearer <token>

Response (200): Empty body
```

---

### 3. Course Management Endpoints

| Method | Endpoint                  | Auth Required | Role            | Description                               |
| ------ | ------------------------- | ------------- | --------------- | ----------------------------------------- |
| GET    | `/courses`                | Yes           | Any             | List all published courses (paginated)    |
| GET    | `/courses/{id}`           | Yes           | Any             | Get course by ID                          |
| GET    | `/courses/search`         | Yes           | Any             | Advanced search with filters              |
| POST   | `/courses`                | Yes           | CONTENT_MANAGER | Create new course                         |
| PUT    | `/courses/{id}`           | Yes           | CONTENT_MANAGER | Update existing course                    |
| DELETE | `/courses/{id}`           | Yes           | CONTENT_MANAGER | Delete course (must unpublish first)      |
| POST   | `/courses/{id}/publish`   | Yes           | CONTENT_MANAGER | Publish course (make visible to learners) |
| POST   | `/courses/{id}/unpublish` | Yes           | CONTENT_MANAGER | Unpublish course                          |

**Example: List Published Courses**

```json
GET /api/v1/courses?page=0&size=10&sort=createdAt,desc
Authorization: Bearer <token>

Response (200):
{
  "content": [
    {
      "id": 1,
      "title": "English Basics (A1)",
      "description": "Foundation course for beginners",
      "thumbnailUrl": "https://cdn.lexia.com/courses/a1-basics.jpg",
      "cefrLevel": "A1",
      "isPublished": true,
      "sectionCount": 3,
      "createdAt": "2025-10-30T10:15:30",
      "updatedAt": "2025-10-31T14:22:45"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "first": true
}
```

**Example: Create Course**

```json
POST /api/v1/courses
Authorization: Bearer <token>
{
  "title": "Business English for Professionals",
  "description": "Master business English for office communication",
  "thumbnailUrl": "https://cdn.lexia.com/courses/business-english.jpg",
  "cefrLevel": "B1"
}

Response (201):
{
  "id": 2,
  "title": "Business English for Professionals",
  "description": "Master business English for office communication",
  "thumbnailUrl": "https://cdn.lexia.com/courses/business-english.jpg",
  "cefrLevel": "B1",
  "isPublished": false,
  "sectionCount": 0,
  "createdAt": "2025-10-31T15:00:00",
  "updatedAt": "2025-10-31T15:00:00"
}
```

**Example: Search Courses**

```json
GET /api/v1/courses/search?title=business&cefrLevel=B1&isPublished=true&page=0&size=10
Authorization: Bearer <token>

Response (200): Same format as list endpoint
```

---

### 4. Section Management Endpoints

| Method | Endpoint                               | Auth Required | Role            | Description                      |
| ------ | -------------------------------------- | ------------- | --------------- | -------------------------------- |
| GET    | `/courses/{courseId}/sections`         | Yes           | Any             | Get all sections for a course    |
| GET    | `/courses/{courseId}/sections/{id}`    | Yes           | Any             | Get section by ID                |
| POST   | `/courses/{courseId}/sections`         | Yes           | CONTENT_MANAGER | Create new section               |
| PUT    | `/courses/{courseId}/sections/{id}`    | Yes           | CONTENT_MANAGER | Update existing section          |
| DELETE | `/courses/{courseId}/sections/{id}`    | Yes           | CONTENT_MANAGER | Delete section                   |
| POST   | `/courses/{courseId}/sections/reorder` | Yes           | CONTENT_MANAGER | Reorder sections within a course |

**Example: Get Sections**

```json
GET /api/v1/courses/1/sections
Authorization: Bearer <token>

Response (200):
[
  {
    "id": 1,
    "courseId": 1,
    "title": "Getting Started",
    "orderIndex": 0,
    "lessonCount": 3,
    "createdAt": "2025-10-30T10:15:30"
  },
  {
    "id": 2,
    "courseId": 1,
    "title": "Basic Grammar",
    "orderIndex": 1,
    "lessonCount": 5,
    "createdAt": "2025-10-30T10:20:00"
  }
]
```

**Example: Create Section**

```json
POST /api/v1/courses/1/sections
Authorization: Bearer <token>
{
  "title": "Advanced Grammar",
  "orderIndex": 2
}

Response (201):
{
  "id": 3,
  "courseId": 1,
  "title": "Advanced Grammar",
  "orderIndex": 2,
  "lessonCount": 0,
  "createdAt": "2025-10-31T15:00:00"
}
```

**Example: Reorder Sections**

```json
POST /api/v1/courses/1/sections/reorder
Authorization: Bearer <token>
{
  "sectionIds": [3, 1, 2]
}

Response (200):
[
  {
    "id": 3,
    "courseId": 1,
    "title": "Advanced Grammar",
    "orderIndex": 0,
    "lessonCount": 0,
    "createdAt": "2025-10-31T15:00:00"
  },
  ...
]
```

---

### 5. Lesson Management Endpoints

| Method | Endpoint                                | Auth Required | Role            | Description                         |
| ------ | --------------------------------------- | ------------- | --------------- | ----------------------------------- |
| GET    | `/lessons/{id}`                         | Yes           | Any             | Get lesson by ID with JSONB content |
| GET    | `/lessons/sections/{sectionId}`         | Yes           | Any             | Get all lessons for a section       |
| GET    | `/lessons/courses/{courseId}`           | Yes           | Any             | Get all lessons for a course        |
| POST   | `/lessons/sections/{sectionId}/lessons` | Yes           | CONTENT_MANAGER | Create new lesson                   |
| PUT    | `/lessons/{id}`                         | Yes           | CONTENT_MANAGER | Update existing lesson              |
| DELETE | `/lessons/{id}`                         | Yes           | CONTENT_MANAGER | Delete lesson                       |
| PATCH  | `/lessons/{id}/reorder`                 | Yes           | CONTENT_MANAGER | Reorder lesson within section       |

**Example: Get Lesson**

```json
GET /api/v1/lessons/1
Authorization: Bearer <token>

Response (200):
{
  "id": 1,
  "sectionId": 1,
  "title": "Basic Greetings and Introductions",
  "lessonType": "READING",
  "content": "{\"passages\":[{\"title\":\"Meeting People\",\"text\":\"When you meet someone new...\"}],\"questions\":[{\"question\":\"What should you say first?\",\"type\":\"multiple_choice\",\"options\":[\"Hello\",\"Goodbye\",\"Thank you\"],\"correctAnswer\":0}]}",
  "orderIndex": 0,
  "durationMinutes": 15,
  "createdAt": "2025-10-30T10:15:30",
  "updatedAt": "2025-10-31T14:22:45"
}
```

**Example: Create READING Lesson**

```json
POST /api/v1/lessons/sections/1/lessons
Authorization: Bearer <token>
{
  "title": "Basic Greetings and Introductions",
  "lessonType": "READING",
  "content": "{\"passages\":[{\"title\":\"Meeting People\",\"text\":\"When you meet someone new, it's important to make a good first impression. Start with a friendly greeting like 'Hello' or 'Hi'. Then introduce yourself by saying 'My name is...' or 'I'm...'. Don't forget to smile!\"}],\"questions\":[{\"question\":\"What should you say first when meeting someone?\",\"type\":\"multiple_choice\",\"options\":[\"Hello\",\"Goodbye\",\"Thank you\",\"Sorry\"],\"correctAnswer\":0,\"explanation\":\"A friendly greeting is the best way to start a conversation.\"}],\"vocabulary\":[{\"word\":\"greeting\",\"definition\":\"A polite word or sign of welcome\",\"example\":\"She waved in greeting.\"}]}",
  "orderIndex": 0,
  "durationMinutes": 15
}

Response (201): Same format as GET lesson response
```

**Example: Create LISTENING Lesson**

```json
POST /api/v1/lessons/sections/1/lessons
{
  "title": "Understanding Daily Conversations",
  "lessonType": "LISTENING",
  "content": "{\"audioUrl\":\"https://cdn.lexia.com/audio/conversation-01.mp3\",\"duration\":120,\"transcript\":\"A: Good morning! How are you today? B: I'm fine, thank you. And you?\",\"questions\":[{\"question\":\"How is person B feeling?\",\"type\":\"multiple_choice\",\"options\":[\"Fine\",\"Sad\",\"Angry\",\"Tired\"],\"correctAnswer\":0,\"timestamp\":15}]}",
  "orderIndex": 1,
  "durationMinutes": 20
}
```

**Example: Create QUIZ Lesson**

```json
POST /api/v1/lessons/sections/1/lessons
{
  "title": "Grammar Quiz: Present Tense",
  "lessonType": "QUIZ",
  "content": "{\"title\":\"Present Simple Tense\",\"description\":\"Test your knowledge of present simple tense\",\"questions\":[{\"question\":\"She ___ to school every day.\",\"type\":\"multiple_choice\",\"options\":[\"go\",\"goes\",\"going\",\"gone\"],\"correctAnswer\":1,\"points\":10}],\"passingScore\":70}",
  "orderIndex": 2,
  "durationMinutes": 10
}
```

**Example: Create SPEAKING Lesson**

```json
POST /api/v1/lessons/sections/1/lessons
{
  "title": "Restaurant Conversation Practice",
  "lessonType": "SPEAKING",
  "content": "{\"scenario\":\"You are ordering food at a restaurant\",\"difficulty\":\"intermediate\",\"turns\":5,\"prompts\":[\"Greet the waiter\",\"Order a drink\",\"Ask about the menu\",\"Place your order\",\"Ask for the bill\"],\"sampleAnswers\":[\"Good evening!\",\"I'd like a glass of water, please.\",\"What do you recommend?\",\"I'll have the pasta, please.\",\"Can I get the check, please?\"]}",
  "orderIndex": 3,
  "durationMinutes": 25
}
```

---

### 6. Learning Path Endpoints

| Method | Endpoint                      | Auth Required | Role | Description                                                |
| ------ | ----------------------------- | ------------- | ---- | ---------------------------------------------------------- |
| GET    | `/learning-paths`             | Yes           | Any  | List all curated learning paths with ordered course data   |
| GET    | `/learning-paths/{id}`        | Yes           | Any  | Retrieve details for a specific path                       |
| GET    | `/learning-paths/recommend`   | Yes           | Any  | Recommend the best path for the authenticated user's CEFR  |
| POST   | `/learning-paths/{id}/start`  | Yes           | Any  | Enroll user in a path and create `user_learning_paths` row |
| GET    | `/learning-paths/my-progress` | Yes           | Any  | List every path the user has started with progress stats   |

Examples and payloads are described in the Sprint 4 mobile spec; see `/learning-paths/recommend` and `/learning-paths/{id}/start` samples earlier in this document.

---

### 7. Enrollment Endpoints

| Method | Endpoint                           | Auth Required | Role | Description                               |
| ------ | ---------------------------------- | ------------- | ---- | ----------------------------------------- |
| POST   | `/enrollments?courseId={id}`       | Yes           | Any  | Enroll user in a published course         |
| GET    | `/enrollments`                     | Yes           | Any  | List current and completed enrollments    |
| GET    | `/enrollments/{courseId}/progress` | Yes           | Any  | Detailed lesson-by-lesson course progress |

See the examples above for enrollment creation and course progress details.

---

### 8. Progress & Dashboard Endpoints

| Method | Endpoint                                | Auth Required | Role | Description                                                         |
| ------ | --------------------------------------- | ------------- | ---- | ------------------------------------------------------------------- |
| POST   | `/progress/lessons/{lessonId}/complete` | Yes           | Any  | Mark lesson completion with rich analytics payload                  |
| GET    | `/progress/courses/{courseId}/lessons`  | Yes           | Any  | Fetch lesson statuses for a course                                  |
| GET    | `/progress/streak`                      | Yes           | Any  | Retrieve current + longest streak                                   |
| GET    | `/progress/dashboard`                   | Yes           | Any  | Aggregated dashboard data (stats, goals, activity, recommendations) |
| GET    | `/progress/summary?days={n}`            | Yes           | Any  | Rolling window of daily activity stats (defaults to 30 days)        |

Payload samples for `/progress/lessons/{lessonId}/complete`, `/progress/dashboard`, and `/progress/summary` are documented above.

---

### 9. Admin Operations (Role-Restricted)

| Method | Endpoint                         | Roles Allowed          | Description                          |
| ------ | -------------------------------- | ---------------------- | ------------------------------------ |
| GET    | `/admin/dashboard`               | ADMIN, CONTENT_MANAGER | Aggregated KPIs for the admin panel  |
| GET    | `/admin/users`                   | ADMIN                  | Paginated list with filters          |
| GET    | `/admin/users/{id}`              | ADMIN                  | Single user detail                   |
| POST   | `/admin/users`                   | ADMIN                  | Create new user                      |
| PUT    | `/admin/users/{id}`              | ADMIN                  | Update user                          |
| DELETE | `/admin/users/{id}`              | ADMIN                  | Delete/deactivate user               |
| GET    | `/admin/ai-usage`                | ADMIN                  | Paginated AI usage logs              |
| GET    | `/admin/ai-usage/stats`          | ADMIN                  | Aggregated AI usage metrics          |
| GET    | `/admin/activity-logs`           | ADMIN                  | Filterable admin activity log stream |
| GET    | `/admin/activity-logs/stats`     | ADMIN                  | Counts per action/entity             |
| GET    | `/admin/activity-logs/export`    | ADMIN                  | Export filtered logs as CSV          |
| GET    | `/admin/activity-logs/actions`   | ADMIN                  | Distinct action codes                |
| GET    | `/admin/activity-logs/users`     | ADMIN                  | Distinct actor names                 |
| GET    | `/admin/audit-logs`              | ADMIN                  | Paginated user audit logs            |
| GET    | `/admin/audit-logs/export`       | ADMIN                  | Export audit logs as CSV             |
| GET    | `/admin/audit-logs/actions`      | ADMIN                  | Distinct audit action types          |
| GET    | `/admin/audit-logs/entity-types` | ADMIN                  | Distinct entity types                |

Each admin endpoint requires the caller to hold the listed role(s) in addition to a valid JWT.

**Example: Get Admin Dashboard**

```json
GET /api/v1/admin/dashboard
Authorization: Bearer <token>

Response (200):
{
  "totalUsers": 1250,
  "activeUsers": 892,
  "totalCourses": 45,
  "publishedCourses": 32,
  "totalEnrollments": 3456,
  "completionRate": 68.5,
  "recentActivities": [
    {
      "id": "...",
      "userName": "John Doe",
      "action": "COURSE_PUBLISHED",
      "entityType": "COURSE",
      "entityName": "Business English",
      "description": "Published course Business English",
      "createdAt": "2025-11-29T10:30:00"
    }
  ]
}
```

**Example: Get Activity Logs with Filters**

```json
GET /api/v1/admin/activity-logs?action=COURSE_CREATED&page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <token>

Response (200):
{
  "content": [
    {
      "id": "uuid-here",
      "userId": "user-uuid",
      "userName": "Content Manager",
      "action": "COURSE_CREATED",
      "entityType": "COURSE",
      "entityId": "1",
      "entityName": "New Course",
      "description": "Created course New Course",
      "createdAt": "2025-11-29T10:00:00"
    }
  ],
  "pageable": {...},
  "totalElements": 50,
  "totalPages": 5
}
```

**Example: Get Audit Logs**

```json
GET /api/v1/admin/audit-logs?action=PROFILE_UPDATE&page=0&size=10
Authorization: Bearer <token>

Response (200):
{
  "content": [
    {
      "id": "uuid-here",
      "userId": "user-uuid",
      "userEmail": "user@example.com",
      "action": "PROFILE_UPDATE",
      "entityType": "UserProfile",
      "entityId": "user-uuid",
      "changes": "{\"field\":\"firstName\",\"oldValue\":\"John\",\"newValue\":\"Johnny\"}",
      "ipAddress": "192.168.1.1",
      "userAgent": "Mozilla/5.0...",
      "createdAt": "2025-11-29T09:30:00"
    }
  ],
  "pageable": {...},
  "totalElements": 120,
  "totalPages": 12
}
```

---

### 10. AI Features Endpoints (Coming in Sprint 5)

| Method | Endpoint                 | Auth Required | Role | Description                    |
| ------ | ------------------------ | ------------- | ---- | ------------------------------ |
| POST   | `/ai/role-play/generate` | Yes           | Any  | Generate AI role-play scenario |
| POST   | `/ai/grammar/check`      | Yes           | Any  | Check grammar with AI          |
| POST   | `/ai/flashcard/generate` | Yes           | Any  | Generate AI flashcards         |

**Status**: Not yet implemented (Planned for Sprint 5)

---

### 11. Notification Endpoints

| Method | Endpoint                         | Auth Required | Role  | Description                         |
| ------ | -------------------------------- | ------------- | ----- | ----------------------------------- |
| GET    | `/notifications`                 | Yes           | Any   | List user notifications (paginated) |
| GET    | `/notifications/unread-count`    | Yes           | Any   | Get unread notification count       |
| GET    | `/notifications/{id}`            | Yes           | Any   | Get notification details            |
| PUT    | `/notifications/{id}/read`       | Yes           | Any   | Mark single notification as read    |
| PUT    | `/notifications/read-all`        | Yes           | Any   | Mark all notifications as read      |
| DELETE | `/notifications/{id}`            | Yes           | Any   | Delete single notification          |
| DELETE | `/notifications`                 | Yes           | Any   | Delete all read notifications       |
| GET    | `/notifications/preferences`     | Yes           | Any   | Get user notification preferences   |
| PUT    | `/notifications/preferences`     | Yes           | Any   | Update notification preferences     |
| POST   | `/admin/notifications/broadcast` | Yes           | ADMIN | Broadcast to all users              |
| POST   | `/admin/notifications/send`      | Yes           | ADMIN | Send to specific users              |

**WebSocket Endpoint**: `/ws` (STOMP over WebSocket)

**STOMP Destinations**:

- `/user/queue/notifications` - Personal notifications
- `/topic/announcements` - Broadcast announcements

**Status**: Implemented (Sprint 4)

> **Full Specification**: See `docs/context/NOTIFICATION-SPECIFICATION.md`

---

### 12. File Upload Endpoints

| Method | Endpoint                | Auth Required | Role        | Max Size | Description          |
| ------ | ----------------------- | ------------- | ----------- | -------- | -------------------- |
| POST   | `/files/upload`         | Yes           | Any         | 10 MB    | Upload single file   |
| GET    | `/files/{id}/metadata`  | Yes           | Any         | -        | Get file metadata    |
| GET    | `/files/{id}/download`  | Conditional   | -           | -        | Download file        |
| GET    | `/files/{id}/exists`    | Yes           | Any         | -        | Check if file exists |
| DELETE | `/files/{id}`           | Yes           | Owner/Admin | -        | Delete file          |
| POST   | `/users/profile/avatar` | Yes           | Any         | 5 MB     | Upload user avatar   |
| DELETE | `/users/profile/avatar` | Yes           | Any         | -        | Delete user avatar   |

**Supported File Types**:

- Images: JPG, PNG, GIF, WebP (max 5-10 MB)
- Audio: MP3, WAV, OGG, M4A (max 50 MB)
- Documents: PDF (max 10 MB)

**Status**: Implemented (Sprint 4)

> **Full Specification**: See `docs/context/FILE-UPLOAD-SPECIFICATION.md`

---

### 13. Monitoring & Actuator Endpoints (Sprint 2 Technical Improvements)

Actuator endpoints are exposed outside the `/api/v1` scope at `http://localhost:8088/actuator`. They provide operational insight while adhering to least-privilege access rules.

| Method | Endpoint              | Auth Required | Role  | Description                                        |
| ------ | --------------------- | ------------- | ----- | -------------------------------------------------- |
| GET    | `/actuator/health`    | No            | -     | Basic liveness check (suitable for load balancers) |
| GET    | `/actuator/health/**` | Yes           | ADMIN | Detailed health info (components, readiness)       |
| GET    | `/actuator/info`      | Yes           | ADMIN | Application metadata (version, description)        |
| GET    | `/actuator/metrics`   | Yes           | ADMIN | Aggregated runtime metrics (JVM, HTTP, database)   |

**Security Behaviour**

- Anonymous requests to protected actuator endpoints receive **401 Unauthorized**.
- Authenticated users without `ROLE_ADMIN` receive **403 Forbidden**.
- Health details (`/actuator/health/**`) remain hidden unless the caller has `ROLE_ADMIN`.
- Health probes (`/actuator/health`) return a compact status payload (`{"status":"UP"}`) for external monitoring.

**Sample Responses**

```json
GET /actuator/health
{
  "status": "UP"
}
```

```json
GET /actuator/info (ADMIN)
{
  "app": {
    "name": "LEXIA Backend API",
    "description": "Spring Boot 3 service for the LEXIA learning platform"
  }
}
```

```json
GET /actuator/metrics/http.server.requests (ADMIN)
{
  "name": "http.server.requests",
  "measurements": [
    { "statistic": "COUNT", "value": 128.0 },
    { "statistic": "TOTAL_TIME", "value": 32.7 }
  ],
  "availableTags": [
    { "tag": "uri", "values": ["/api/v1/courses", "/actuator/health"] }
  ]
}
```

---

## CEFR Levels

All courses are categorized by Common European Framework of Reference levels:

| Level  | Name               | Description                                          |
| ------ | ------------------ | ---------------------------------------------------- |
| **A1** | Beginner           | Can understand and use familiar everyday expressions |
| **A2** | Elementary         | Can communicate in simple and routine tasks          |
| **B1** | Intermediate       | Can deal with most situations while traveling        |
| **B2** | Upper Intermediate | Can interact with fluency and spontaneity            |
| **C1** | Advanced           | Can express ideas fluently and spontaneously         |
| **C2** | Proficiency        | Can understand virtually everything heard or read    |

---

## Lesson Types & JSONB Content Schemas

Lessons store structured content in JSONB format. Each lesson type has a specific schema:

### 1. READING Lesson Schema

```json
{
  "passages": [
    {
      "title": "string (optional)",
      "text": "string (required)"
    }
  ],
  "questions": [
    {
      "question": "string (required)",
      "type": "multiple_choice | fill_in_blank (required)",
      "options": ["string"] (required for multiple_choice),
      "correctAnswer": "number | string (required)",
      "explanation": "string (optional)"
    }
  ],
  "vocabulary": [
    {
      "word": "string (required)",
      "definition": "string (required)",
      "example": "string (optional)"
    }
  ] (optional)
}
```

### 2. LISTENING Lesson Schema

```json
{
  "audioUrl": "string (required, valid URL)",
  "duration": "number (required, seconds)",
  "transcript": "string (required)",
  "questions": [
    {
      "question": "string (required)",
      "type": "multiple_choice | fill_in_blank (required)",
      "options": ["string"] (required for multiple_choice),
      "correctAnswer": "number | string (required)",
      "timestamp": "number (optional, seconds)"
    }
  ]
}
```

### 3. QUIZ Lesson Schema

```json
{
  "title": "string (optional)",
  "description": "string (optional)",
  "questions": [
    {
      "question": "string (required)",
      "type": "multiple_choice | fill_in_blank (required)",
      "options": ["string"] (required for multiple_choice),
      "correctAnswer": "number | string (required)",
      "points": "number (required, positive)"
    }
  ],
  "passingScore": "number (required, 0-100)"
}
```

### 4. SPEAKING Lesson Schema

```json
{
  "scenario": "string (required)",
  "difficulty": "beginner | intermediate | advanced (required)",
  "turns": "number (required, 1-20)",
  "prompts": ["string"] (required),
  "sampleAnswers": ["string"] (required, same length as prompts)
}
```

**Note**: All JSONB content is validated by `LessonContentValidator` service before creation/update.

---

## Pagination

All list endpoints support pagination and sorting:

**Query Parameters**:

- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 10, max: 100)
- `sort`: Sort field and direction (e.g., `createdAt,desc` or `title,asc`)

**Response Format**:

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 5,
  "totalElements": 50,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

---

## Swagger UI Examples

The Swagger UI (http://localhost:8088/swagger-ui.html) provides:

1. **Course Management API** tag with 8 endpoints
2. **Lesson Management API** tag with 6 endpoints
3. **Authentication API** tag with 4 endpoints
4. **User Profile API** tag with 2 endpoints

Each endpoint includes:

- Comprehensive description and usage notes
- Request/response examples (including all 4 lesson types)
- Parameter descriptions with examples
- All possible HTTP status codes with error examples
- Security requirements (JWT Bearer token)
- "Try it out" interactive testing

---

## Version History

| Version | Date         | Changes                                                                   |
| ------- | ------------ | ------------------------------------------------------------------------- |
| 1.0.0   | Oct 28, 2025 | Initial release - Auth & User Profile                                     |
| 2.0.0   | Oct 31, 2025 | Added Course & Lesson Management with comprehensive Swagger docs          |
| 2.1.0   | Nov 6, 2025  | Added Actuator endpoints documentation                                    |
| 2.2.0   | Nov 28, 2025 | Added Notification & File Upload specifications (planned)                 |
| 2.3.0   | Nov 29, 2025 | Documented learning path, enrollment, progress, admin endpoints           |
| 2.4.0   | Nov 29, 2025 | Added Section Management, Avatar endpoints, Audit logs, Activity logs API |
| 2.5.0   | Dec 01, 2025 | Implemented Notification & File Upload endpoints (Sprint 4)               |

---

**For detailed JSONB schemas and validation rules**, see `DATABASE-SCHEMA.md` section 2.3  
**For notification system details**, see `NOTIFICATION-SPECIFICATION.md`  
**For file upload system details**, see `FILE-UPLOAD-SPECIFICATION.md`
