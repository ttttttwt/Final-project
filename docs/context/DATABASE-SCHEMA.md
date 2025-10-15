# LEXIA - Database Schema (v1.0)

## 🔐 Module 1: Users & Authentication
### 1.1. Key Tables
| Table | Purpose |
|-------|----------|
| `users` | Manage user accounts and authentication providers |
| `user_profiles` | Store user learning profiles |
| `roles` | Define system roles (`LEARNER`, `ADMIN`, `CONTENT_MANAGER`) |
| `user_roles` | Assign roles to users |
| `refresh_tokens` | Manage JWT refresh tokens (rotating tokens) |

### 1.2. Table Details
#### `users`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | UUID | PK, DEFAULT gen_random_uuid() |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| auth_provider | VARCHAR(50) | NOT NULL, DEFAULT 'email' (`email`, `google`, `facebook`) |
| is_active | BOOLEAN | DEFAULT true |
| created_at | TIMESTAMPTZ | DEFAULT NOW() |
| updated_at | TIMESTAMPTZ | DEFAULT NOW() |

#### `user_profiles`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| user_id | UUID | PK, FK → users(id) |
| full_name | VARCHAR(255) | NOT NULL |
| avatar_url | VARCHAR(255) |  |
| current_level | VARCHAR(10) | (A1, A2, B1, B2, ...) — updated after placement test |
| learning_goal | TEXT | Personal learning goal |

#### `roles`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | SERIAL | PK |
| name | VARCHAR(50) | UNIQUE, NOT NULL (`LEARNER`, `ADMIN`, `CONTENT_MANAGER`) |

#### `user_roles`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| user_id | UUID | PK, FK → users(id) |
| role_id | INTEGER | PK, FK → roles(id) |

#### `refresh_tokens`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | BIGSERIAL | PK |
| user_id | UUID | FK → users(id), NOT NULL, INDEXED |
| token_hash | VARCHAR(255) | NOT NULL, UNIQUE (securely hashed) |
| family | VARCHAR(255) | NOT NULL, INDEXED (used for rotating token families) |
| expires_at | TIMESTAMPTZ | NOT NULL |
| created_at | TIMESTAMPTZ | DEFAULT NOW() |
| revoked_at | TIMESTAMPTZ | NULL if not revoked |
| device_info | TEXT | NULLABLE (User Agent, IP Address, etc.) |

---

## 📘 Module 2: Learning Content
### 2.1. Key Tables
| Table | Purpose |
|-------|----------|
| `courses` | Store course information |
| `sections` | Course sections |
| `lessons` | Lessons (Reading, Listening, Quiz, Speaking Practice) |

### 2.2. Table Details
#### `courses`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | UUID | PK |
| title | VARCHAR(255) | NOT NULL |
| description | TEXT |  |
| thumbnail_url | VARCHAR(255) |  |
| level | VARCHAR(10) | NOT NULL (A1, A2, B1...) |
| is_published | BOOLEAN | DEFAULT false |
| created_at | TIMESTAMPTZ | DEFAULT NOW() |
| updated_at | TIMESTAMPTZ | DEFAULT NOW() |

#### `sections`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | UUID | PK |
| course_id | UUID | FK → courses(id), NOT NULL |
| title | VARCHAR(255) | NOT NULL |
| order_index | INTEGER | NOT NULL |

#### `lessons`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | UUID | PK |
| section_id | UUID | FK → sections(id), NOT NULL |
| title | VARCHAR(255) | NOT NULL |
| lesson_type | VARCHAR(50) | NOT NULL (`READING`, `LISTENING`, `QUIZ`, `SPEAKING_PRACTICE`) |
| content | JSONB | Flexible depending on type:<br>• Listening: `{ "text": "...", "audio_url": "..." }`<br>• Quiz: `[{"question": "...", "options": [...]}]` |
| order_index | INTEGER | NOT NULL |

---

## 📈 Module 3: User Progress
### 3.1. Key Tables
| Table | Purpose |
|-------|----------|
| `enrollments` | User enrollments in courses |
| `lesson_progress` | Track individual lesson progress |

### 3.2. Table Details
#### `enrollments`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | UUID | PK |
| user_id | UUID | FK → users(id), NOT NULL |
| course_id | UUID | FK → courses(id), NOT NULL |
| enrolled_at | TIMESTAMPTZ | DEFAULT NOW() |
| progress_percentage | INTEGER | DEFAULT 0 |
| completed_at | TIMESTAMPTZ | NULL until completed |

#### `lesson_progress`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | UUID | PK |
| user_id | UUID | FK → users(id), NOT NULL |
| lesson_id | UUID | FK → lessons(id), NOT NULL |
| status | VARCHAR(50) | `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED` |
| score | DECIMAL(5,2) | Quiz score (if applicable) |
| result_details | JSONB | Detailed results (answers, AI analysis, etc.) |
| completed_at | TIMESTAMPTZ |  |
| updated_at | TIMESTAMPTZ | DEFAULT NOW() |

---

## 🤖 Module 4: AI Features
### 4.1. Key Tables
| Table | Purpose |
|-------|----------|
| `user_flashcards` | AI-powered flashcards (SRS algorithm) |
| `user_role_play_sessions` | Store user AI conversation practice sessions |
| `ai_usage_logs` | Track API usage cost and frequency |

### 4.2. Table Details
#### `user_flashcards`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | UUID | PK |
| user_id | UUID | FK → users(id), NOT NULL |
| term | VARCHAR(255) | NOT NULL |
| content | JSONB | AI-generated content: `{ "definition": "...", "example": "...", "image_prompt": "..." }` |
| next_review_at | TIMESTAMPTZ | NOT NULL (used for SRS scheduling) |
| srs_level | INTEGER | DEFAULT 0 |
| created_at | TIMESTAMPTZ | DEFAULT NOW() |

#### `user_role_play_sessions`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | UUID | PK |
| user_id | UUID | FK → users(id), NOT NULL |
| prompt | TEXT | Initial conversation prompt |
| transcript | JSONB | Full conversation record |
| feedback | JSONB | AI feedback on pronunciation, vocabulary, grammar |
| created_at | TIMESTAMPTZ | DEFAULT NOW() |

#### `ai_usage_logs`
| Column | Data Type | Constraints & Notes |
|--------|------------|---------------------|
| id | BIGSERIAL | PK |
| user_id | UUID | FK → users(id) |
| feature_name | VARCHAR(100) | NOT NULL (`MAGIC_FLASHCARD`, `ROLEPLAY`, `GRAMMAR_SANDBOX`, etc.) |
| input_tokens | INTEGER |  |
| output_tokens | INTEGER |  |
| cost | DECIMAL(10,6) | API usage cost |
| created_at | TIMESTAMPTZ | DEFAULT NOW() |

---

## 🧩 JSONB Columns (Dynamic Content)
| Column | Description |
|---------|-------------|
| `lessons.content` | Stores dynamic content depending on `lesson_type` |
| `lesson_progress.result_details` | Stores quiz results or speech analysis |
| `user_flashcards.content` | Stores AI-generated data (definitions, examples, images) |
| `user_role_play_sessions.feedback` | Stores AI feedback for speaking sessions |

---

## ⚙️ Indexing Strategy
| Index | Purpose |
|--------|----------|
| `users.email` | UNIQUE for authentication |
| `refresh_tokens.user_id` | Quickly find tokens by user |
| `lesson_progress (user_id, lesson_id)` | UNIQUE to track progress per lesson |
| `user_role_play_sessions (user_id, created_at)` | Efficient filtering of conversation history |
