# LEXIA Sprint 5 - AI Features API Usage Guide

**Version**: 1.0.0  
**Date**: December 14, 2025  
**Scope**: AI-Powered Role-Play, Grammar, Flashcards, and Usage Monitoring

---

## 1. Introduction

This guide provides detailed documentation for the AI-powered features introduced in Sprint 5. These APIs leverage Google Gemini to deliver personalized learning experiences.

### Base URL
`http://localhost:8080/api/v1`

### Common Headers
| Header | Value | Description |
|--------|-------|-------------|
| `Authorization` | `Bearer <token>` | **Required**. JWT Token obtained from login. |
| `Content-Type` | `application/json` | Request body format. |

---

## 2. AI-Powered Role-Play API

Base Path: `/ai/roleplay`

### 2.1 Generate Scenario
Generate a unique role-play scenario based on CEFR level and domain.

**Endpoint**: `POST /scenarios`

**Request Body**:
```json
{
  "cefrLevel": "B1",
  "domain": "BUSINESS",
  "industry": "Technology",
  "topic": "Project Management"
}
```

**Response (201 Created)**:
```json
{
  "id": "uuid",
  "title": "Project Deadline Negotiation",
  "context": "You are a project manager negotiating a deadline extension...",
  "yourRole": "Project Manager",
  "aiRole": "Client",
  "cefrLevel": "B1",
  "objectives": ["Explain delay reasons", "Propose new timeline"],
  "keyVocabulary": ["deadline", "extension", "milestone"]
}
```

### 2.2 Start Conversation
Initialize a conversation session from a scenario.

**Endpoint**: `POST /conversations`

**Request Body**:
```json
{
  "scenarioId": "uuid",
  "mode": "LEARNING" // or "IMMERSIVE"
}
```

**Response (201 Created)**:
```json
{
  "id": "uuid",
  "scenarioId": "uuid",
  "status": "IN_PROGRESS",
  "messages": []
}
```

### 2.3 Send Message (Learning Mode)
Send a message and receive AI response with detailed feedback.

**Endpoint**: `POST /conversations/{id}/messages/learning`

**Request Body**:
```json
{
  "content": "I need more time for the project."
}
```

**Response (200 OK)**:
```json
{
  "messageId": "uuid",
  "content": "I understand, but we have a strict launch date. How much time?",
  "role": "ASSISTANT",
  "feedback": {
    "grammarCorrection": "I need more time for the project.",
    "betterAlternative": "I would like to request an extension for the project.",
    "explanation": "Using 'would like' is more polite in business contexts."
  }
}
```

### 2.4 Send Message (Immersive Mode)
Fast, chat-only interaction without feedback.

**Endpoint**: `POST /conversations/{id}/messages/immersive`

**Request Body**:
```json
{
  "content": "Just two more days."
}
```

**Response (200 OK)**:
```json
{
  "messageId": "uuid",
  "content": "Two days is acceptable if we can guarantee quality.",
  "role": "ASSISTANT"
}
```

### 2.5 Stream Message (SSE)
Real-time streaming of AI response.

**Endpoint**: `POST /conversations/{id}/messages/stream`
**Content-Type**: `text/event-stream`

**Events**:
- `token`: Partial content chunk
- `complete`: Full message metadata
- `error`: Error details

---

## 3. Grammar Exercise API

Base Path: `/ai/grammar`

### 3.1 Generate Exercises
Create a set of grammar exercises tailored to a specific topic and level.

**Endpoint**: `POST /generate`

**Request Body**:
```json
{
  "grammarTopic": "Present Perfect",
  "cefrLevel": "A2",
  "count": 5
}
```

**Response (201 Created)**:
```json
{
  "id": "uuid",
  "grammarPoint": "Present Perfect",
  "cefrLevel": "A2",
  "explanation": {
    "rule": "Used for actions in the past with present relevance...",
    "examples": ["I have lived here for 5 years."],
    "commonMistakes": ["Using past simple for unfinished time."],
    "tips": ["Look for 'for' and 'since'."]
  },
  "exercises": [
    {
      "type": "multiple_choice",
      "instruction": "Choose the correct form.",
      "question": "I ___ (live) here for 5 years.",
      "options": ["have lived", "lived", "living"],
      "correctAnswer": "have lived",
      "explanation": "Correct! 'For 5 years' indicates duration up to now."
    }
  ],
  "exerciseCount": 5,
  "timeLimitSeconds": 600,
  "isFallback": false,
  "createdAt": "2025-12-16T10:00:00Z"
}
```

### 3.2 Submit Answers
Submit user answers for grading.

**Endpoint**: `POST /exercises/{setId}/submit`

**Request Body**:
```json
{
  "exerciseSetId": "uuid",
  "answers": [
    {
      "questionIndex": 0,
      "userAnswer": "have lived"
    }
  ],
  "totalTimeSeconds": 120
}
```

**Response (200 OK)**:
```json
{
  "exerciseSetId": "uuid",
  "grammarPoint": "Present Perfect",
  "score": 5,
  "maxScore": 5,
  "percentage": 100.0,
  "passed": true,
  "feedback": [
    {
      "questionIndex": 0,
      "correct": true,
      "explanation": "Correct! 'For 5 years' indicates duration up to now."
    }
  ],
  "encouragement": "Great job!"
}
```

---

## 4. Flashcard API

Base Path: `/ai/flashcards`

### 4.1 Generate from Lesson
Create a flashcard deck from a completed lesson.

**Endpoint**: `POST /generate`

**Request Body**:
```json
{
  "lessonId": "uuid",
  "count": 10
}
```

**Response (201 Created)**:
```json
{
  "id": "uuid",
  "title": "Lesson 5 Vocabulary",
  "cards": [
    {
      "front": "Negotiation",
      "back": "Discussion aimed at reaching an agreement.",
      "example": "The negotiation took three hours.",
      "ipa": "/nɪˌɡəʊʃiˈeɪʃn/"
    }
  ]
}
```

### 4.2 Get Study Session
Get cards due for review based on Spaced Repetition (SM-2).

**Endpoint**: `GET /decks/{id}/study`

**Response (200 OK)**:
```json
{
  "sessionId": "uuid",
  "cards": [
    {
      "id": "uuid",
      "front": "Negotiation",
      "back": "..."
    }
  ]
}
```

### 4.3 Submit Review
Record study results to update card scheduling.

**Endpoint**: `POST /decks/{id}/review`

**Request Body**:
```json
{
  "reviews": [
    {
      "cardId": "uuid",
      "quality": 5 // 0-5 scale (5 = perfect recall)
    }
  ]
}
```

---

## 5. Admin AI Usage API

Base Path: `/admin/ai-usage`
**Role Required**: `ADMIN`

### 5.1 Get Usage Logs
Retrieve detailed logs of AI interactions.

**Endpoint**: `GET /`

**Query Parameters**:
- `userId`: Filter by user UUID
- `featureName`: Filter by feature (ROLE_PLAY, GRAMMAR, FLASHCARD)
- `startDate`: ISO 8601 date
- `endDate`: ISO 8601 date
- `page`: Page number (default 0)
- `size`: Page size (default 10)

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": 1,
      "userId": "uuid",
      "featureName": "ROLE_PLAY",
      "tokensInput": 150,
      "tokensOutput": 50,
      "cost": 0.0002,
      "createdAt": "2025-12-14T10:00:00Z"
    }
  ],
  "totalPages": 5,
  "totalElements": 50
}
```

### 5.2 Get Usage Statistics
Get aggregated usage stats.

**Endpoint**: `GET /stats`

**Query Parameters**:
- `period`: `today`, `week`, `month`, `all` (default: `today`)

**Response (200 OK)**:
```json
{
  "totalRequests": 1500,
  "totalTokens": 500000,
  "estimatedCost": 1.50,
  "requestsByFeature": {
    "ROLE_PLAY": 800,
    "GRAMMAR": 500,
    "FLASHCARD": 200
  }
}
```

---

## 6. Error Handling

| Status Code | Error Code | Description |
|-------------|------------|-------------|
| 400 | `BAD_REQUEST` | Invalid input parameters. |
| 401 | `UNAUTHORIZED` | Missing or invalid JWT token. |
| 403 | `FORBIDDEN` | Access denied (e.g., accessing another user's data). |
| 404 | `NOT_FOUND` | Resource (scenario, conversation, deck) not found. |
| 429 | `RATE_LIMIT_EXCEEDED` | User has exceeded daily AI quota. |
| 500 | `INTERNAL_SERVER_ERROR` | Unexpected server error. |
| 503 | `SERVICE_UNAVAILABLE` | AI service is down (fallback may be active). |

---

## 7. Rate Limiting & Quotas

To ensure fair usage and cost control, the following limits apply per user:

| Feature | Daily Limit | Reset Time |
|---------|-------------|------------|
| Role-Play Scenarios | 20 | 00:00 UTC |
| Grammar Generations | 50 | 00:00 UTC |
| Flashcard Generations | 20 | 00:00 UTC |

*Note: Admin users are exempt from rate limits.*
