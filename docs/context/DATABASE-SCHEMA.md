# LEXIA - Database Schema (v1.3)

## 🔐 Module 1: Users & Authentication

### 1.1. Key Tables

| Table            | Purpose                                                     |
| ---------------- | ----------------------------------------------------------- |
| `users`          | Manage user accounts and authentication providers           |
| `user_profiles`  | Store user learning profiles                                |
| `roles`          | Define system roles (`LEARNER`, `ADMIN`, `CONTENT_MANAGER`) |
| `user_roles`     | Assign roles to users                                       |
| `refresh_tokens` | Manage JWT refresh tokens (rotating tokens)                 |

### 1.2. Table Details

#### `users`

| Column        | Data Type    | Constraints & Notes                                       |
| ------------- | ------------ | --------------------------------------------------------- |
| id            | UUID         | PK, DEFAULT gen_random_uuid()                             |
| email         | VARCHAR(255) | UNIQUE, NOT NULL                                          |
| password_hash | VARCHAR(255) | NOT NULL                                                  |
| auth_provider | VARCHAR(50)  | NOT NULL, DEFAULT 'email' (`email`, `google`, `facebook`) |
| is_active     | BOOLEAN      | DEFAULT true                                              |
| created_at    | TIMESTAMPTZ  | DEFAULT NOW()                                             |
| updated_at    | TIMESTAMPTZ  | DEFAULT NOW()                                             |

#### `user_profiles`

| Column        | Data Type    | Constraints & Notes                                  |
| ------------- | ------------ | ---------------------------------------------------- |
| user_id       | UUID         | PK, FK → users(id)                                   |
| full_name     | VARCHAR(255) | NOT NULL (deprecated - use first_name + last_name)   |
| first_name    | VARCHAR(100) | User's first name                                    |
| last_name     | VARCHAR(100) | User's last name                                     |
| bio           | VARCHAR(500) | Short biography                                      |
| phone_number  | VARCHAR(20)  | Phone number (10-20 digits, optionally starting +)   |
| avatar_url    | VARCHAR(255) | URL to user's avatar image                           |
| timezone      | VARCHAR(50)  | DEFAULT 'UTC', user's timezone                       |
| language      | VARCHAR(10)  | DEFAULT 'en', ISO 639-1 code (e.g., en, vi)          |
| current_level | VARCHAR(10)  | (A1, A2, B1, B2, ...) — updated after placement test |
| learning_goal | TEXT         | Personal learning goal                               |
| created_at    | TIMESTAMPTZ  | DEFAULT NOW()                                        |
| updated_at    | TIMESTAMPTZ  | DEFAULT NOW()                                        |

#### `roles`

| Column | Data Type   | Constraints & Notes                                      |
| ------ | ----------- | -------------------------------------------------------- |
| id     | SERIAL      | PK                                                       |
| name   | VARCHAR(50) | UNIQUE, NOT NULL (`LEARNER`, `ADMIN`, `CONTENT_MANAGER`) |

#### `user_roles`

| Column  | Data Type | Constraints & Notes |
| ------- | --------- | ------------------- |
| user_id | UUID      | PK, FK → users(id)  |
| role_id | INTEGER   | PK, FK → roles(id)  |

#### `refresh_tokens`

| Column      | Data Type    | Constraints & Notes                                  |
| ----------- | ------------ | ---------------------------------------------------- |
| id          | BIGSERIAL    | PK                                                   |
| user_id     | UUID         | FK → users(id), NOT NULL, INDEXED                    |
| token_hash  | VARCHAR(255) | NOT NULL, UNIQUE (securely hashed)                   |
| family      | VARCHAR(255) | NOT NULL, INDEXED (used for rotating token families) |
| expires_at  | TIMESTAMPTZ  | NOT NULL                                             |
| created_at  | TIMESTAMPTZ  | DEFAULT NOW()                                        |
| revoked_at  | TIMESTAMPTZ  | NULL if not revoked                                  |
| device_info | TEXT         | NULLABLE (User Agent, IP Address, etc.)              |

---

## 📘 Module 2: Learning Content

### 2.1. Key Tables

| Table      | Purpose                                               |
| ---------- | ----------------------------------------------------- |
| `courses`  | Store course information                              |
| `sections` | Course sections                                       |
| `lessons`  | Lessons (Reading, Listening, Quiz, Speaking Practice) |

### 2.2. Table Details

#### `courses`

| Column        | Data Type    | Constraints & Notes      |
| ------------- | ------------ | ------------------------ |
| id            | UUID         | PK                       |
| title         | VARCHAR(255) | NOT NULL                 |
| description   | TEXT         |                          |
| thumbnail_url | VARCHAR(255) |                          |
| level         | VARCHAR(10)  | NOT NULL (A1, A2, B1...) |
| is_published  | BOOLEAN      | DEFAULT false            |
| created_at    | TIMESTAMPTZ  | DEFAULT NOW()            |
| updated_at    | TIMESTAMPTZ  | DEFAULT NOW()            |

#### `sections`

| Column      | Data Type    | Constraints & Notes        |
| ----------- | ------------ | -------------------------- |
| id          | UUID         | PK                         |
| course_id   | UUID         | FK → courses(id), NOT NULL |
| title       | VARCHAR(255) | NOT NULL                   |
| order_index | INTEGER      | NOT NULL                   |

#### `lessons`

| Column           | Data Type    | Constraints & Notes                                   |
| ---------------- | ------------ | ----------------------------------------------------- |
| id               | UUID         | PK                                                    |
| section_id       | UUID         | FK → sections(id), NOT NULL                           |
| title            | VARCHAR(255) | NOT NULL                                              |
| lesson_type      | VARCHAR(50)  | NOT NULL (`READING`, `LISTENING`, `QUIZ`, `SPEAKING`) |
| content          | JSONB        | Flexible depending on type (see JSONB schemas below)  |
| order_index      | INTEGER      | NOT NULL                                              |
| duration_minutes | INTEGER      | Estimated completion time                             |

### 2.3. JSONB Content Schemas by Lesson Type

> **Important**: All JSONB content must be validated against these schemas before saving to database.  
> Validation is enforced by `LessonContentValidator` service class.

#### 2.3.1. READING Lesson Schema

**Purpose**: Text-based reading comprehension exercises

**Required Fields**:

```json
{
  "passages": [
    {
      "text": "string (required) - Main reading passage",
      "title": "string (optional) - Passage title"
    }
  ],
  "questions": [
    {
      "question": "string (required) - Question text",
      "type": "multiple_choice | true_false | short_answer",
      "options": ["array of strings (required for multiple_choice)"],
      "correctAnswer": "string | number (required) - Answer or index",
      "explanation": "string (optional) - Why this is correct"
    }
  ],
  "vocabulary": [
    {
      "word": "string (required) - Target vocabulary",
      "definition": "string (required) - Definition",
      "example": "string (optional) - Example sentence",
      "partOfSpeech": "string (optional) - noun/verb/adj/adv"
    }
  ]
}
```

**Example**:

```json
{
  "passages": [
    {
      "title": "Working from Home",
      "text": "Remote work has become increasingly popular in recent years. Many companies now offer flexible working arrangements..."
    }
  ],
  "questions": [
    {
      "question": "What is the main topic of the passage?",
      "type": "multiple_choice",
      "options": [
        "Remote work trends",
        "Office management",
        "Company policies",
        "Employee benefits"
      ],
      "correctAnswer": 0,
      "explanation": "The passage focuses on the rise of remote work."
    }
  ],
  "vocabulary": [
    {
      "word": "flexible",
      "definition": "capable of bending easily without breaking",
      "example": "She has a flexible schedule.",
      "partOfSpeech": "adjective"
    }
  ]
}
```

**Validation Rules**:

- Must have at least 1 passage
- Must have at least 1 question
- Vocabulary array is optional
- For multiple_choice: options array must have 2-6 items
- correctAnswer index must be valid for options array

---

#### 2.3.2. LISTENING Lesson Schema

**Purpose**: Audio-based listening comprehension exercises

**Required Fields**:

```json
{
  "audioUrl": "string (required) - URL to audio file",
  "duration": "number (required) - Audio length in seconds",
  "transcript": "string (required) - Full audio transcript",
  "showTranscript": "boolean (optional, default: false) - Show transcript initially",
  "questions": [
    {
      "question": "string (required) - Question text",
      "type": "multiple_choice | true_false | fill_blank",
      "options": ["array of strings (required for multiple_choice)"],
      "correctAnswer": "string | number (required)",
      "explanation": "string (optional)",
      "timestamp": "number (optional) - Time in audio when answer appears"
    }
  ],
  "vocabulary": [
    {
      "word": "string (required)",
      "definition": "string (required)",
      "timestamp": "number (optional) - When word appears in audio"
    }
  ]
}
```

**Example**:

```json
{
  "audioUrl": "https://cdn.lexia.com/audio/lesson-15-conversation.mp3",
  "duration": 120,
  "transcript": "A: Good morning! How can I help you today?\nB: I'd like to make a reservation for two people...",
  "showTranscript": false,
  "questions": [
    {
      "question": "How many people is the reservation for?",
      "type": "multiple_choice",
      "options": ["One", "Two", "Three", "Four"],
      "correctAnswer": 1,
      "timestamp": 5.2
    }
  ],
  "vocabulary": [
    {
      "word": "reservation",
      "definition": "an arrangement to have a table, seat, etc. held for you",
      "timestamp": 3.5
    }
  ]
}
```

**Validation Rules**:

- audioUrl must be valid URL
- duration must be > 0
- transcript is mandatory (accessibility requirement)
- Must have at least 1 question
- If timestamp provided, must be <= duration

---

#### 2.3.3. QUIZ Lesson Schema

**Purpose**: Standalone assessment without passage/audio

**Required Fields**:

```json
{
  "title": "string (optional) - Quiz title",
  "instructions": "string (optional) - Special instructions",
  "timeLimit": "number (optional) - Time limit in seconds",
  "passingScore": "number (optional, default: 70) - Minimum score to pass",
  "questions": [
    {
      "question": "string (required) - Question text",
      "type": "multiple_choice | true_false | fill_blank | matching",
      "options": ["array of strings (for multiple_choice)"],
      "correctAnswer": "string | number | array (required)",
      "points": "number (optional, default: 1) - Points for this question",
      "explanation": "string (optional) - Shown after answering",
      "hint": "string (optional) - Hint text"
    }
  ]
}
```

**Example**:

```json
{
  "title": "Present Perfect Tense Quiz",
  "instructions": "Choose the correct form of the verb.",
  "timeLimit": 600,
  "passingScore": 80,
  "questions": [
    {
      "question": "I ___ to Paris three times.",
      "type": "multiple_choice",
      "options": ["have been", "was", "have gone", "went"],
      "correctAnswer": 0,
      "points": 2,
      "explanation": "Use 'have been' for completed actions with present relevance.",
      "hint": "Think about present perfect structure"
    },
    {
      "question": "She has lived in London since 2010.",
      "type": "true_false",
      "correctAnswer": "true",
      "points": 1,
      "explanation": "This is correct present perfect usage with 'since'."
    }
  ]
}
```

**Validation Rules**:

- Must have at least 1 question
- timeLimit if provided must be > 0
- passingScore must be 0-100
- points must be positive number
- Question types must match available answer formats

---

#### 2.3.4. SPEAKING Lesson Schema

**Purpose**: Speaking practice prompts (AI role-play integration in Sprint 3)

**Required Fields**:

```json
{
  "scenario": "string (required) - Scenario description",
  "difficulty": "string (required) - beginner | intermediate | advanced",
  "prompts": [
    {
      "prompt": "string (required) - What user should say",
      "context": "string (optional) - Situational context",
      "sampleAnswers": ["array of strings (optional) - Example responses"],
      "targetGrammar": [
        "array of strings (optional) - Grammar points to practice"
      ],
      "targetVocabulary": ["array of strings (optional) - Key words to use"]
    }
  ],
  "rolePlaySettings": {
    "aiPersona": "string (optional) - AI character role (e.g., 'hotel receptionist')",
    "turns": "number (optional, default: 5) - Number of conversation turns",
    "enableFeedback": "boolean (optional, default: true) - Give AI feedback"
  }
}
```

**Example**:

```json
{
  "scenario": "Ordering food at a restaurant",
  "difficulty": "beginner",
  "prompts": [
    {
      "prompt": "Greet the waiter and ask for a menu",
      "context": "You just sat down at a restaurant",
      "sampleAnswers": [
        "Hello! Could I see the menu, please?",
        "Good evening. May I have a menu?"
      ],
      "targetGrammar": ["modal verbs (could, may)", "polite requests"],
      "targetVocabulary": ["menu", "order", "waiter"]
    },
    {
      "prompt": "Order your main course",
      "sampleAnswers": [
        "I'd like the grilled chicken, please.",
        "Could I have the pasta carbonara?"
      ],
      "targetGrammar": ["I'd like...", "Could I have..."],
      "targetVocabulary": ["dish", "main course", "side"]
    }
  ],
  "rolePlaySettings": {
    "aiPersona": "friendly waiter",
    "turns": 6,
    "enableFeedback": true
  }
}
```

**Validation Rules**:

- Must have at least 1 prompt
- difficulty must be one of: beginner | intermediate | advanced
- scenario is mandatory
- turns if provided must be 1-20
- sampleAnswers minimum 1 if provided

---

### 2.4. JSONB Validation Service

**Implementation**: `com.lexia.backend.service.LessonContentValidator`

**Usage**:

```java
@Service
public class LessonContentValidator {

    public void validate(LessonType type, JsonNode content)
        throws InvalidLessonContentException {
        switch(type) {
            case READING -> validateReadingContent(content);
            case LISTENING -> validateListeningContent(content);
            case QUIZ -> validateQuizContent(content);
            case SPEAKING -> validateSpeakingContent(content);
            default -> throw new IllegalArgumentException("Unknown lesson type: " + type);
        }
    }

    private void validateReadingContent(JsonNode content) {
        // Check required fields: passages, questions
        if (!content.has("passages")) {
            throw new InvalidLessonContentException(
                "READING lesson must have 'passages' array"
            );
        }

        JsonNode passages = content.get("passages");
        if (!passages.isArray() || passages.size() == 0) {
            throw new InvalidLessonContentException(
                "READING lesson must have at least 1 passage"
            );
        }

        // Validate each passage has required 'text' field
        for (JsonNode passage : passages) {
            if (!passage.has("text") || passage.get("text").asText().isEmpty()) {
                throw new InvalidLessonContentException(
                    "Each passage must have non-empty 'text' field"
                );
            }
        }

        // Validate questions array
        if (!content.has("questions")) {
            throw new InvalidLessonContentException(
                "READING lesson must have 'questions' array"
            );
        }

        // Additional validations...
    }

    // Similar methods for other lesson types...
}
```

**Error Response Format** (RFC 7807):

```json
{
  "type": "https://lexia.com/errors/invalid-lesson-content",
  "title": "Invalid Lesson Content",
  "status": 400,
  "detail": "READING lesson must have 'passages' array",
  "instance": "/api/v1/lessons/123",
  "lessonType": "READING",
  "validationErrors": [
    {
      "field": "content.passages",
      "message": "Required field missing"
    }
  ]
}
```

---

## 📈 Module 3: Learning Journeys & Progress

### 3.1. Key Tables

| Table                   | Purpose                                                   |
| ----------------------- | --------------------------------------------------------- |
| `learning_paths`        | Define curated CEFR-aligned learning journeys             |
| `learning_path_courses` | Map ordered courses inside a learning path                |
| `user_learning_paths`   | Track which users have started/completed a path           |
| `enrollments`           | User-course enrollment with overall completion percentage |
| `lesson_progress`       | Per-lesson status, score, attempts, and analytics data    |

### 3.2. Learning Path Tables

#### `learning_paths`

| Column      | Data Type    | Constraints & Notes                          |
| ----------- | ------------ | -------------------------------------------- |
| id          | BIGSERIAL    | PK                                           |
| name        | VARCHAR(100) | NOT NULL                                     |
| description | TEXT         | Optional                                     |
| cefr_level  | VARCHAR(2)   | NOT NULL, CHECK IN (`A1`-`C2`)               |
| is_default  | BOOLEAN      | DEFAULT false (true for curated Lexia paths) |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                    |
| updated_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                    |

`idx_learning_paths_cefr_default` accelerates recommendation queries, while `idx_learning_paths_created_at` supports ordered listings.

#### `learning_path_courses`

| Column      | Data Type | Constraints & Notes                                    |
| ----------- | --------- | ------------------------------------------------------ |
| path_id     | BIGINT    | PK(∗), FK → learning_paths(id), ON DELETE CASCADE      |
| course_id   | BIGINT    | PK(∗), FK → courses(id), ON DELETE CASCADE             |
| order_index | INTEGER   | NOT NULL, CHECK ≥ 0 (0-based ordering inside the path) |

Compound index `idx_learning_path_courses_path_order` keeps curriculum retrieval sorted without in-application sorting.

#### `user_learning_paths`

| Column            | Data Type | Constraints & Notes                                  |
| ----------------- | --------- | ---------------------------------------------------- |
| id                | BIGSERIAL | PK                                                   |
| user_id           | UUID      | FK → users(id), NOT NULL, ON DELETE CASCADE          |
| path_id           | BIGINT    | FK → learning_paths(id), NOT NULL, ON DELETE CASCADE |
| current_course_id | BIGINT    | FK → courses(id), nullable, ON DELETE SET NULL       |
| started_at        | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP                            |
| completed_at      | TIMESTAMP | NULL until every course in the path is completed     |

Unique constraint `(user_id, path_id)` prevents duplicate enrollments per path. Indices on `user_id`, `path_id`, and the `(user_id, path_id)` composite power `/learning-paths/my-progress` and recommendation lookups.

### 3.3. Enrollment Tracking

| Column              | Data Type   | Constraints & Notes                           |
| ------------------- | ----------- | --------------------------------------------- |
| id                  | BIGSERIAL   | PK                                            |
| user_id             | UUID        | FK → users(id), NOT NULL, ON DELETE CASCADE   |
| course_id           | BIGINT      | FK → courses(id), NOT NULL, ON DELETE CASCADE |
| enrolled_at         | TIMESTAMPTZ | DEFAULT NOW()                                 |
| progress_percentage | INTEGER     | DEFAULT 0, CHECK BETWEEN 0 AND 100            |
| completed_at        | TIMESTAMPTZ | NULL until completed                          |

`unique_user_course_enrollment` blocks duplicate enrollments, while targeted indexes (`idx_enrollments_user`, `idx_enrollments_course`, `idx_enrollments_completed`) back `/enrollments`, `/progress/dashboard`, and admin analytics.

### 3.4. Lesson Progress Analytics

| Column         | Data Type   | Constraints & Notes                                                             |
| -------------- | ----------- | ------------------------------------------------------------------------------- |
| id             | BIGSERIAL   | PK                                                                              |
| user_id        | UUID        | FK → users(id), NOT NULL, ON DELETE CASCADE                                     |
| lesson_id      | BIGINT      | FK → lessons(id), NOT NULL, ON DELETE CASCADE                                   |
| status         | VARCHAR(20) | `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED` (default `NOT_STARTED`)               |
| score          | INTEGER     | 0-100, nullable                                                                 |
| attempts       | INTEGER     | DEFAULT 0, increments per completion attempt                                    |
| result_details | JSONB       | Stores per-lesson analytics (question breakdown, AI feedback, timestamps, etc.) |
| completed_at   | TIMESTAMPTZ | NULL until first completion                                                     |
| created_at     | TIMESTAMPTZ | DEFAULT NOW()                                                                   |
| updated_at     | TIMESTAMPTZ | DEFAULT NOW()                                                                   |

Rich indexing (`idx_lesson_progress_user_lesson`, `idx_lesson_progress_completed_at`, `idx_lesson_progress_date_range`, etc.) keeps streak calculations and `/progress/summary` rolling windows under 50 ms per request.

### 3.5. Dashboard Aggregations

- **ProgressController** derives `/progress/dashboard` and `/progress/summary` from `lesson_progress`, `enrollments`, and `user_learning_paths`. No extra tables are required—the service layer aggregates streaks, study minutes, recent activity, and recommendations on-the-fly using the indexes above.
- **LearningPathService** and **EnrollmentService** coordinate updates so that `progress_percentage` and `current_course_id` remain in sync after every `lesson_progress` mutation.

---

## 🤖 Module 4: AI Features

### 4.1. Key Tables

| Table                     | Purpose                                      |
| ------------------------- | -------------------------------------------- |
| `user_flashcards`         | AI-powered flashcards (SRS algorithm)        |
| `user_role_play_sessions` | Store user AI conversation practice sessions |
| `ai_usage_logs`           | Track API usage cost and frequency           |
| `audit_logs`              | Track user profile changes for compliance    |
| `admin_activity_logs`     | Track admin/content manager actions          |

### 4.2. Table Details

#### `user_flashcards`

| Column         | Data Type    | Constraints & Notes                                                                      |
| -------------- | ------------ | ---------------------------------------------------------------------------------------- |
| id             | UUID         | PK                                                                                       |
| user_id        | UUID         | FK → users(id), NOT NULL                                                                 |
| term           | VARCHAR(255) | NOT NULL                                                                                 |
| content        | JSONB        | AI-generated content: `{ "definition": "...", "example": "...", "image_prompt": "..." }` |
| next_review_at | TIMESTAMPTZ  | NOT NULL (used for SRS scheduling)                                                       |
| srs_level      | INTEGER      | DEFAULT 0                                                                                |
| created_at     | TIMESTAMPTZ  | DEFAULT NOW()                                                                            |

#### `user_role_play_sessions`

| Column     | Data Type   | Constraints & Notes                               |
| ---------- | ----------- | ------------------------------------------------- |
| id         | UUID        | PK                                                |
| user_id    | UUID        | FK → users(id), NOT NULL                          |
| prompt     | TEXT        | Initial conversation prompt                       |
| transcript | JSONB       | Full conversation record                          |
| feedback   | JSONB       | AI feedback on pronunciation, vocabulary, grammar |
| created_at | TIMESTAMPTZ | DEFAULT NOW()                                     |

#### `ai_usage_logs`

| Column        | Data Type     | Constraints & Notes                                               |
| ------------- | ------------- | ----------------------------------------------------------------- |
| id            | BIGSERIAL     | PK                                                                |
| user_id       | UUID          | FK → users(id)                                                    |
| feature_name  | VARCHAR(100)  | NOT NULL (`MAGIC_FLASHCARD`, `ROLEPLAY`, `GRAMMAR_SANDBOX`, etc.) |
| input_tokens  | INTEGER       |                                                                   |
| output_tokens | INTEGER       |                                                                   |
| cost          | DECIMAL(10,6) | API usage cost                                                    |
| created_at    | TIMESTAMPTZ   | DEFAULT NOW()                                                     |

#### `audit_logs`

| Column      | Data Type    | Constraints & Notes                                                           |
| ----------- | ------------ | ----------------------------------------------------------------------------- |
| id          | UUID         | PK, DEFAULT gen_random_uuid()                                                 |
| user_id     | UUID         | FK → users(id), NOT NULL                                                      |
| action      | VARCHAR(50)  | NOT NULL (e.g., `PROFILE_UPDATE`, `AVATAR_UPDATE`, `AVATAR_DELETE`)           |
| entity_type | VARCHAR(100) | NOT NULL (e.g., `UserProfile`, `User`)                                        |
| entity_id   | UUID         | NOT NULL, ID of the entity that was modified                                  |
| changes     | TEXT         | JSON format: `{"field": "firstName", "oldValue": "John", "newValue": "Jane"}` |
| ip_address  | VARCHAR(45)  | IP address of the user who made the change                                    |
| user_agent  | VARCHAR(500) | User agent string from the request                                            |
| created_at  | TIMESTAMPTZ  | DEFAULT NOW()                                                                 |

#### `admin_activity_logs`

| Column      | Data Type    | Constraints & Notes                                                          |
| ----------- | ------------ | ---------------------------------------------------------------------------- |
| id          | UUID         | PK, DEFAULT gen_random_uuid()                                                |
| user_id     | UUID         | NOT NULL, the admin/content manager who performed the action                 |
| user_name   | VARCHAR(255) | NOT NULL, display name of the user                                           |
| action      | VARCHAR(50)  | NOT NULL, enum: `COURSE_CREATED`, `COURSE_UPDATED`, `COURSE_PUBLISHED`, etc. |
| entity_type | VARCHAR(50)  | NOT NULL, enum: `COURSE`, `SECTION`, `LESSON`                                |
| entity_id   | VARCHAR(100) | NOT NULL, ID of the entity that was modified                                 |
| entity_name | VARCHAR(255) | NOT NULL, name/title of the entity for display                               |
| description | VARCHAR(500) | NOT NULL, human-readable description of the action                           |
| details     | TEXT         | Additional details about the change (JSON format, optional)                  |
| created_at  | TIMESTAMPTZ  | DEFAULT NOW()                                                                |

**Admin Activity Action Types**:

- `COURSE_CREATED`, `COURSE_UPDATED`, `COURSE_PUBLISHED`, `COURSE_UNPUBLISHED`, `COURSE_DELETED`
- `SECTION_CREATED`, `SECTION_UPDATED`, `SECTION_DELETED`
- `LESSON_CREATED`, `LESSON_UPDATED`, `LESSON_DELETED`

---

## 🧩 JSONB Columns (Dynamic Content)

### Summary Table

| Column                             | Lesson Type | Schema Reference                         |
| ---------------------------------- | ----------- | ---------------------------------------- |
| `lessons.content` (READING)        | READING     | See 2.3.1 - Reading Lesson Schema        |
| `lessons.content` (LISTENING)      | LISTENING   | See 2.3.2 - Listening Lesson Schema      |
| `lessons.content` (QUIZ)           | QUIZ        | See 2.3.3 - Quiz Lesson Schema           |
| `lessons.content` (SPEAKING)       | SPEAKING    | See 2.3.4 - Speaking Lesson Schema       |
| `lesson_progress.result_details`   | ALL         | Stores user answers and scores (see 3.3) |
| `user_flashcards.content`          | N/A         | AI-generated flashcard data (Sprint 3)   |
| `user_role_play_sessions.feedback` | N/A         | AI feedback for speaking (Sprint 3)      |

### 3.3. Lesson Progress Result Details Schema

**Purpose**: Store detailed results of lesson completion

**Schema**:

```json
{
  "submittedAt": "ISO 8601 timestamp",
  "completionTime": "number - seconds taken to complete",
  "answers": [
    {
      "questionId": "string | number - question identifier",
      "userAnswer": "string | number | array - user's answer",
      "correctAnswer": "string | number | array - correct answer",
      "isCorrect": "boolean",
      "points": "number - points earned",
      "timeSpent": "number (optional) - seconds on this question"
    }
  ],
  "totalPoints": "number - points earned",
  "maxPoints": "number - total possible points",
  "scorePercentage": "number - (totalPoints / maxPoints) * 100",
  "passed": "boolean - score >= passingScore",
  "feedback": {
    "strengths": ["array of strings - what user did well"],
    "improvements": ["array of strings - areas to improve"],
    "nextSteps": "string (optional) - recommended next actions"
  }
}
```

**Example** (Quiz Result):

```json
{
  "submittedAt": "2025-10-29T14:30:00Z",
  "completionTime": 320,
  "answers": [
    {
      "questionId": 1,
      "userAnswer": 0,
      "correctAnswer": 0,
      "isCorrect": true,
      "points": 2,
      "timeSpent": 15
    },
    {
      "questionId": 2,
      "userAnswer": "false",
      "correctAnswer": "true",
      "isCorrect": false,
      "points": 0,
      "timeSpent": 20
    }
  ],
  "totalPoints": 2,
  "maxPoints": 3,
  "scorePercentage": 66.67,
  "passed": false,
  "feedback": {
    "strengths": ["Good understanding of present perfect tense"],
    "improvements": ["Review present perfect with 'since' and 'for'"],
    "nextSteps": "Practice more present perfect exercises"
  }
}
```

---

## ⚙️ Indexing Strategy

### Sprint 1 Indexes (Implemented)

| Index                    | Purpose                      |
| ------------------------ | ---------------------------- |
| `users.email`            | UNIQUE for authentication    |
| `refresh_tokens.user_id` | Quickly find tokens by user  |
| `refresh_tokens.family`  | Token rotation family lookup |

### Sprint 2 Indexes (Planned)

| Index                                  | Purpose                            | Performance Impact     |
| -------------------------------------- | ---------------------------------- | ---------------------- |
| `courses (cefr_level, is_published)`   | Fast filtering by level and status | ~10x faster for search |
| `courses (created_at DESC)`            | Recent courses first               | ~5x faster for listing |
| `courses (title)`                      | Title search (B-tree)              | Exact/prefix matches   |
| `sections (course_id, order_index)`    | Ordered section retrieval          | Eliminates sorting     |
| `lessons (section_id, order_index)`    | Ordered lesson retrieval           | Eliminates sorting     |
| `lessons (lesson_type)`                | Filter by lesson type              | ~3x faster queries     |
| `enrollments (user_id, course_id)`     | UNIQUE constraint + lookup         | Prevents duplicates    |
| `lesson_progress (user_id, lesson_id)` | UNIQUE constraint + lookup         | Prevents duplicates    |

### Future Indexes (Sprint 3+)

| Index                                              | Purpose              | Note            |
| -------------------------------------------------- | -------------------- | --------------- |
| `courses USING GIN(to_tsvector('english', title))` | Full-text search     | Add when needed |
| `lessons USING GIN(content)`                       | JSONB content search | Add when needed |
| `user_role_play_sessions (user_id, created_at)`    | Conversation history | AI features     |

### Admin & Audit Indexes (Sprint 4+)

| Index                           | Purpose                     | Note        |
| ------------------------------- | --------------------------- | ----------- |
| `idx_admin_activity_created_at` | Recent activity logs (DESC) | Implemented |
| `idx_admin_activity_user`       | Filter by user_id           | Implemented |
| `idx_admin_activity_action`     | Filter by action type       | Implemented |
| `idx_audit_logs_user_created`   | User audit history          | Implemented |
| `idx_audit_logs_entity`         | Entity-based audit lookup   | Implemented |

---

## 📝 Migration Examples

### Sprint 2 - V5: Create Courses Table

```sql
-- V5__Create_courses_table.sql
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(255),
    cefr_level VARCHAR(2) NOT NULL CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    is_published BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_courses_cefr_published ON courses(cefr_level, is_published);
CREATE INDEX idx_courses_created_at ON courses(created_at DESC);
CREATE INDEX idx_courses_title ON courses(title);

-- Full-text search (deferred to Sprint 3)
-- CREATE INDEX idx_courses_title_gin ON courses USING GIN(to_tsvector('english', title));

COMMENT ON TABLE courses IS 'Course catalog with CEFR levels';
COMMENT ON COLUMN courses.cefr_level IS 'Common European Framework level: A1-C2';
COMMENT ON COLUMN courses.is_published IS 'Only published courses visible to learners';
```

### Sprint 2 - V6: Create Sections and Lessons Tables

```sql
-- V6__Create_sections_and_lessons_table.sql
CREATE TABLE sections (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (course_id, order_index)
);

CREATE INDEX idx_sections_course_order ON sections(course_id, order_index);

CREATE TYPE lesson_type_enum AS ENUM ('READING', 'LISTENING', 'QUIZ', 'SPEAKING');

CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    lesson_type lesson_type_enum NOT NULL,
    content JSONB NOT NULL,
    order_index INTEGER NOT NULL,
    duration_minutes INTEGER DEFAULT 15,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (section_id, order_index),
    CONSTRAINT valid_duration CHECK (duration_minutes > 0 AND duration_minutes <= 240)
);

CREATE INDEX idx_lessons_section_order ON lessons(section_id, order_index);
CREATE INDEX idx_lessons_type ON lessons(lesson_type);

-- JSONB content validation will be done at application layer
-- See LessonContentValidator for schema enforcement

COMMENT ON TABLE sections IS 'Course sections for organizing lessons';
COMMENT ON TABLE lessons IS 'Individual lessons with JSONB content';
COMMENT ON COLUMN lessons.content IS 'JSONB schema varies by lesson_type - see DATABASE-SCHEMA.md section 2.3';
COMMENT ON COLUMN lessons.lesson_type IS 'READING, LISTENING, QUIZ, or SPEAKING';
```

---

## 🔍 Query Examples

### Efficient Course Search

```sql
-- Search published courses by CEFR level (uses idx_courses_cefr_published)
SELECT id, title, description, thumbnail_url
FROM courses
WHERE cefr_level = 'B1'
  AND is_published = true
ORDER BY created_at DESC
LIMIT 10;

-- Title search (uses idx_courses_title)
SELECT id, title, cefr_level
FROM courses
WHERE title ILIKE '%business%'
  AND is_published = true;
```

### Retrieve Course with Lessons (Ordered)

```sql
-- Get course with sections and lessons in correct order
SELECT
    c.id as course_id,
    c.title as course_title,
    s.id as section_id,
    s.title as section_title,
    s.order_index as section_order,
    l.id as lesson_id,
    l.title as lesson_title,
    l.lesson_type,
    l.order_index as lesson_order,
    l.duration_minutes
FROM courses c
JOIN sections s ON s.course_id = c.id
JOIN lessons l ON l.section_id = s.id
WHERE c.id = 123
  AND c.is_published = true
ORDER BY s.order_index, l.order_index;
```

### Check Enrollment Status

```sql
-- Check if user already enrolled (uses idx_enrollments_user_course)
SELECT EXISTS(
    SELECT 1 FROM enrollments
    WHERE user_id = 'user-uuid'
      AND course_id = 123
) as is_enrolled;
```

### Calculate Course Progress

```sql
-- Calculate completion percentage
WITH lesson_counts AS (
    SELECT
        c.id as course_id,
        COUNT(l.id) as total_lessons,
        COUNT(CASE WHEN lp.status = 'COMPLETED' THEN 1 END) as completed_lessons
    FROM courses c
    JOIN sections s ON s.course_id = c.id
    JOIN lessons l ON l.section_id = s.id
    LEFT JOIN lesson_progress lp ON lp.lesson_id = l.id
        AND lp.user_id = 'user-uuid'
    WHERE c.id = 123
    GROUP BY c.id
)
SELECT
    course_id,
    total_lessons,
    completed_lessons,
    ROUND((completed_lessons::DECIMAL / total_lessons * 100), 2) as progress_percentage
FROM lesson_counts;
```

---

## 📋 Validation Checklist

### Before Creating Migration

- [ ] JSONB schema documented in section 2.3
- [ ] All required fields identified
- [ ] Validation rules defined
- [ ] Example JSON provided
- [ ] Indexes planned for performance

### Before Creating JPA Entity

- [ ] Read JSONB schema from this document
- [ ] Plan validation strategy (LessonContentValidator)
- [ ] Consider using `@Type(JsonBinaryType.class)` for JSONB
- [ ] Plan how to deserialize to Java objects

### Before Creating Service

- [ ] Implement LessonContentValidator
- [ ] Add tests for all lesson types
- [ ] Handle InvalidLessonContentException
- [ ] Return clear error messages (RFC 7807)

---

## 🔔 Module 5: Notifications

### 5.1. Key Tables

| Table                      | Purpose                    |
| -------------------------- | -------------------------- |
| `notifications`            | Store user notifications   |
| `notification_preferences` | User notification settings |

### 5.2. Table Details

#### `notifications`

| Column     | Data Type    | Constraints & Notes                                  |
| ---------- | ------------ | ---------------------------------------------------- |
| id         | UUID         | PK, DEFAULT gen_random_uuid()                        |
| user_id    | UUID         | FK → users(id), NOT NULL, ON DELETE CASCADE          |
| type       | VARCHAR(50)  | NOT NULL (see types below)                           |
| title      | VARCHAR(255) | NOT NULL                                             |
| message    | TEXT         | NOT NULL                                             |
| data       | JSONB        | DEFAULT '{}' - Additional payload                    |
| priority   | VARCHAR(20)  | NOT NULL, DEFAULT 'NORMAL' (`HIGH`, `NORMAL`, `LOW`) |
| is_read    | BOOLEAN      | DEFAULT false                                        |
| read_at    | TIMESTAMPTZ  | NULL until read                                      |
| created_at | TIMESTAMPTZ  | DEFAULT NOW()                                        |
| expires_at | TIMESTAMPTZ  | NULL - Auto-cleanup date                             |

**Notification Types**:

- `COURSE_PUBLISHED`, `LESSON_ADDED`, `ENROLLMENT_CONFIRMED`
- `LESSON_COMPLETED`, `COURSE_COMPLETED`, `ACHIEVEMENT_UNLOCKED`
- `STREAK_REMINDER`, `STREAK_LOST`, `STREAK_MILESTONE`
- `LEVEL_UP`, `SYSTEM_ANNOUNCEMENT`, `MAINTENANCE_NOTICE`

#### `notification_preferences`

| Column               | Data Type   | Constraints & Notes                   |
| -------------------- | ----------- | ------------------------------------- |
| user_id              | UUID        | PK, FK → users(id), ON DELETE CASCADE |
| in_app_enabled       | BOOLEAN     | DEFAULT true                          |
| email_enabled        | BOOLEAN     | DEFAULT true                          |
| push_enabled         | BOOLEAN     | DEFAULT true                          |
| learning_enabled     | BOOLEAN     | DEFAULT true                          |
| achievements_enabled | BOOLEAN     | DEFAULT true                          |
| reminders_enabled    | BOOLEAN     | DEFAULT true                          |
| system_enabled       | BOOLEAN     | DEFAULT true                          |
| quiet_hours_start    | TIME        | NULL                                  |
| quiet_hours_end      | TIME        | NULL                                  |
| quiet_hours_timezone | VARCHAR(50) | DEFAULT 'UTC'                         |
| created_at           | TIMESTAMPTZ | DEFAULT NOW()                         |
| updated_at           | TIMESTAMPTZ | DEFAULT NOW()                         |

> **Full Specification**: See `docs/context/NOTIFICATION-SPECIFICATION.md`

---

## 📁 Module 6: File Storage

### 6.1. Key Tables

| Table   | Purpose             |
| ------- | ------------------- |
| `files` | Store file metadata |

### 6.2. Table Details

#### `files`

| Column            | Data Type    | Constraints & Notes                                           |
| ----------------- | ------------ | ------------------------------------------------------------- |
| id                | UUID         | PK, DEFAULT gen_random_uuid()                                 |
| original_filename | VARCHAR(255) | NOT NULL                                                      |
| storage_path      | VARCHAR(500) | NOT NULL, UNIQUE - Relative path from upload root             |
| mime_type         | VARCHAR(100) | NOT NULL                                                      |
| file_size         | BIGINT       | NOT NULL, CHECK > 0 AND <= 52428800 (50MB)                    |
| category          | VARCHAR(50)  | NOT NULL (`AVATAR`, `COURSE_THUMBNAIL`, `LESSON_AUDIO`, etc.) |
| uploaded_by       | UUID         | FK → users(id), ON DELETE SET NULL                            |
| uploaded_at       | TIMESTAMPTZ  | DEFAULT NOW()                                                 |
| width             | INTEGER      | NULL - For images                                             |
| height            | INTEGER      | NULL - For images                                             |
| duration_seconds  | INTEGER      | NULL - For audio                                              |
| is_public         | BOOLEAN      | DEFAULT false                                                 |
| access_count      | INTEGER      | DEFAULT 0                                                     |
| last_accessed_at  | TIMESTAMPTZ  | NULL                                                          |

**File Categories**:

- `AVATAR` - User profile pictures (max 5MB)
- `COURSE_THUMBNAIL` - Course cover images (max 5MB)
- `LESSON_AUDIO` - Listening lesson audio (max 50MB)
- `LESSON_IMAGE` - Lesson content images (max 10MB)
- `DOCUMENT` - PDFs, certificates (max 10MB)
- `CERTIFICATE` - Course completion certificates (max 10MB)

**Allowed MIME Types**:

- Images: `image/jpeg`, `image/png`, `image/gif`, `image/webp`
- Audio: `audio/mpeg`, `audio/wav`, `audio/ogg`, `audio/mp4`
- Documents: `application/pdf`

> **Full Specification**: See `docs/context/FILE-UPLOAD-SPECIFICATION.md`

---

**Last Updated**: December 1, 2025 (Sprint 4 - Implemented Notifications & File Storage)  
**Version**: 1.5  
**Next Update**: Sprint 5 (AI Features implementation)
