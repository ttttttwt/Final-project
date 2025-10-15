# LEXIA - API Specification

## Base URL
```
Development: http://localhost:8088/api/v1
Production: https://api.lexia.app/api/v1
```

## Authentication Header
```
Authorization: Bearer <JWT_ACCESS_TOKEN>
```

## Error Response Format
```json
{
  "status": 400,
  "message": "Invalid request",
  "errors": [
    {
      "field": "email",
      "message": "Email already exists"
    }
  ],
  "timestamp": "2025-01-15T10:30:00Z"
}
```

## Endpoint Categories

### 1. Authentication Endpoints
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /auth/register | No | Register new user |
| POST | /auth/login | No | Login user |
| POST | /auth/refresh | No | Refresh access token |
| POST | /auth/logout | Yes | Logout user |

### 2. User Endpoints
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /users/profile | Yes | Get current user profile |
| PUT | /users/profile | Yes | Update user profile |
| GET | /users/progress | Yes | Get overall progress |

### 3. Course Endpoints
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /courses | Yes | List all courses |
| GET | /courses/{id} | Yes | Get course details |
| POST | /courses/{id}/enroll | Yes | Enroll in course |

### 4. Lesson Endpoints
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /lessons/{id} | Yes | Get lesson content |
| POST | /lessons/{id}/complete | Yes | Mark lesson complete |
| GET | /lessons/{id}/progress | Yes | Get lesson progress |

### 5. AI Features Endpoints (Priority)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /features/role-play/generate | Yes | Generate role-play script |
| POST | /features/grammar/generate | Yes | Generate grammar exercise |
| POST | /features/flashcard/create | Yes | Create AI flashcard |

## Pagination
```json
GET /courses?page=1&size=10&sort=createdAt,desc

Response:
{
  "content": [...],
  "totalPages": 5,
  "totalElements": 50,
  "currentPage": 1
}
```
