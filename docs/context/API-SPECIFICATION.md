# LEXIA - API Specification

**Version**: 2.0.0  
**Last Updated**: October 31, 2025  
**Sprint**: 2 / 6

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
| **ADMIN**           | Full system access (Coming in Sprint 4)                       |

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

| Code | Meaning      | Usage                                           |
| ---- | ------------ | ----------------------------------------------- |
| 200  | OK           | Successful GET, PUT, PATCH                      |
| 201  | Created      | Successful POST (resource created)              |
| 204  | No Content   | Successful DELETE                               |
| 400  | Bad Request  | Validation errors, invalid input                |
| 401  | Unauthorized | Missing or invalid token                        |
| 403  | Forbidden    | Insufficient permissions (wrong role)           |
| 404  | Not Found    | Resource does not exist                         |
| 409  | Conflict     | Duplicate resource (e.g., title already exists) |

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

| Method | Endpoint         | Auth Required | Role | Description              |
| ------ | ---------------- | ------------- | ---- | ------------------------ |
| GET    | `/users/profile` | Yes           | Any  | Get current user profile |
| PUT    | `/users/profile` | Yes           | Any  | Update user profile      |

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
  "cefrLevel": "B1",
  "preferredLanguage": "en",
  "learningGoal": "Business communication",
  "createdAt": "2025-10-30T10:15:30"
}
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

### 4. Lesson Management Endpoints

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

### 5. AI Features Endpoints (Coming in Sprint 3)

| Method | Endpoint                 | Auth Required | Role | Description                    |
| ------ | ------------------------ | ------------- | ---- | ------------------------------ |
| POST   | `/ai/role-play/generate` | Yes           | Any  | Generate AI role-play scenario |
| POST   | `/ai/grammar/check`      | Yes           | Any  | Check grammar with AI          |
| POST   | `/ai/flashcard/generate` | Yes           | Any  | Generate AI flashcards         |

**Status**: Not yet implemented (Planned for Sprint 3)

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

| Version | Date         | Changes                                                          |
| ------- | ------------ | ---------------------------------------------------------------- |
| 1.0.0   | Oct 28, 2025 | Initial release - Auth & User Profile                            |
| 2.0.0   | Oct 31, 2025 | Added Course & Lesson Management with comprehensive Swagger docs |

---

**For detailed JSONB schemas and validation rules**, see `DATABASE-SCHEMA.md` section 2.3
