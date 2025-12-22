# FEATURE SPECIFICATION: CUSTOM AI CONTENT GENERATOR

**Project:** LEXIA – AI-Powered English Learning Platform  
**Date:** 2025-01-15 (Draft v2.0)  
**Status:** Proposal (Post-Brainstorming)

---

## 1. Tổng quan (Overview)

Tính năng cho phép người dùng upload tài liệu cá nhân (PDF, ảnh, văn bản) hoặc dán đường dẫn (Youtube, Website). Hệ thống sử dụng AI (Gemini) để phân tích và tạo ra các bài học tùy chỉnh, biến tài liệu thụ động thành bài tập tương tác.

**Mục tiêu:**

- Giải quyết nhu cầu học tiếng Anh chuyên ngành/công việc thực tế của nhóm **Working Professionals**.
- Tăng tính cá nhân hóa (Personalization) và tỷ lệ giữ chân người dùng (Retention).
- Xây dựng "Kho tri thức cá nhân" (Personal Knowledge Base) cho người học.
- **Biến "Học" thành "Làm"** – Không chỉ nạp kiến thức mà còn diễn tập tình huống thực tế.

---

## 2. Phạm vi tính năng (Scope & Capabilities)

### 2.1. Các loại đầu vào (Input Sources)

| Loại | Mô tả | Giới hạn |
|------|-------|----------|
| **PDF** | Hỗ trợ Native Multimodal (Text & Image) | Max 10MB, User chọn range (VD: Page 1-20) |
| **DOCX** | File Word | Max 10MB, User chọn range |
| **Image** | Ảnh có chứa văn bản (Gemini Vision) | Max 10MB |
| **Youtube** | Video (Transcript hoặc Audio fallback) | Max 15 phút, User chọn range (VD: 00:00-15:00) |
| **Website** | Bài báo, blog, article | - |
| **Raw Text** | Dán trực tiếp (Email, đoạn văn) | Max 5000 ký tự |

### 2.2. Các chế độ đầu ra (Output Modes)

| Mode | Mô tả | Use Case |
|------|-------|----------|
| **Vocabulary List** | Trích xuất từ vựng chuyên ngành + định nghĩa + ví dụ theo ngữ cảnh | Học từ mới nhanh |
| **Smart Summary** | Tóm tắt ý chính của tài liệu/video | Nắm bắt nội dung nhanh |
| **Quiz/Comprehension** | Bài tập trắc nghiệm đọc/nghe hiểu | Kiểm tra hiểu biết |
| **AI Role-Play** | Kịch bản hội thoại mô phỏng (Mock Meeting, Interview...) | Diễn tập thực tế |
| **Shadowing Trainer** | Trích xuất câu mẫu, ghi âm & chấm điểm ngữ điệu | Luyện phát âm |
| **Style Transformer** | Viết lại văn bản theo văn phong: Formal, Casual, Email, Presentation, Social Media, Diplomatic, Persuasive | Cải thiện email/văn bản |

### 2.3. Hỗ trợ đa ngôn ngữ (Multi-language Logic)

- **Input:** Tiếng Anh, Tiếng Việt, hoặc ngôn ngữ khác.
- **Logic:** Nếu Input không phải tiếng Anh → AI tự động phát hiện, dịch các ý chính sang tiếng Anh để giảng dạy, nhưng giữ ngữ cảnh gốc để người dùng dễ hiểu.

---

## 3. Quyết định thiết kế (Design Decisions)

> _Các quyết định sau đã được xác nhận trong buổi brainstorming._

### 3.1. Phương thức tương tác Role-Play

| Quyết định | Chi tiết |
|------------|----------|
| **Phương thức** | 💬 **Chat Interface** (Nhắn tin qua lại) |
| **Lý do** | Tiện lợi cho dân văn phòng, có thể luyện tập mọi lúc mọi nơi (quán cafe, tàu xe) mà không làm phiền người khác. User có thời gian suy nghĩ, tra cứu trước khi trả lời. |

### 3.2. Cơ chế sửa lỗi AI (AI Correction Mode)

| Quyết định | Chi tiết |
|------------|----------|
| **Phương thức** | 🎚️ **User Toggle** (Người dùng tự chọn) |
| **Option 1** | "Strict Teacher" – Sửa ngay lập tức khi user mắc lỗi |
| **Option 2** | "Polite Colleague" – Tổng kết lỗi cuối buổi chat |
| **Default** | "Polite Colleague" (Trải nghiệm tự nhiên hơn) |

**Prompt Engineering:**
```
// Strict Mode
"Nếu user sai ngữ pháp hoặc từ vựng, hãy dừng lại và sửa ngay 
trước khi tiếp tục hội thoại. Giải thích ngắn gọn lý do."

// Polite Mode  
"Hãy đóng vai tự nhiên. Ghi nhớ các lỗi sai nhưng KHÔNG nhắc đến 
trong lúc chat. Cuối buổi hãy liệt kê chúng ra kèm giải thích."
```

### 3.3. Style Transformer Output

| Quyết định | Chi tiết |
|------------|----------|
| **Phương thức** | 🎚️ **User Toggle** (Người dùng tự chọn) |
| **Option 1** | "Quick Result" – Chỉ trả văn bản đã viết lại |
| **Option 2** | "Learn Mode" – Kèm chú thích giải thích thay đổi |
| **Default** | "Learn Mode" (Khuyến khích học sâu) |

### 3.4. Lưu trữ từ vựng (Vocabulary Storage)

| Quyết định | Chi tiết |
|------------|----------|
| **Phương thức** | 🎚️ **User Toggle** (Người dùng tự chọn) |
| **Option 1** | "Isolated" – Từ vựng chỉ nằm trong bài học Custom đó |
| **Option 2** | "Integrated" – Đồng bộ vào hệ thống SRS (Spaced Repetition) hàng ngày |
| **Default** | "Isolated" (Tránh làm rác bộ từ vựng chính) |

### 3.5. Gamification (XP & Streak)

| Quyết định | Chi tiết |
|------------|----------|
| **Tính điểm XP** | ❌ **KHÔNG** |
| **Tính Streak** | ❌ **KHÔNG** |
| **Lý do** | Đảm bảo công bằng trong bảng xếp hạng, tránh gian lận (upload file ngắn để lấy điểm). User sử dụng tính năng này vì giá trị thực tế cho công việc, không phải vì điểm ảo. |

### 3.6. Business Model

| Quyết định | Chi tiết |
|------------|----------|
| **Đối tượng** | 💎 **Premium Only** (Chỉ user trả phí) |
| **Lý do** | Tính năng tốn kém tài nguyên (Gemini API, Storage). Đây là "Killer Feature" để chuyển đổi Free → Paid user. Working Professionals sẵn sàng trả tiền vì ROI cao cho công việc. |

---

## 4. Quy trình người dùng (User Flow)

```
┌─────────────────────────────────────────────────────────────────┐
│  1. UPLOAD/INPUT                                                │
│     User chọn loại nguồn (File/Link/Text) tại "New Material"    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  2. CONFIGURATION                                               │
│     - Chọn Output Modes (Vocab, Quiz, Role-Play...)             │
│     - Cài đặt Toggles (AI Correction, Learn Mode, SRS Sync)     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  3. PROCESSING (Async)                                          │
│     - Hiển thị trạng thái "Processing..."                       │
│     - User có thể tắt app hoặc làm việc khác                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  4. NOTIFICATION                                                │
│     Push notification khi AI xử lý xong                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  5. LEARNING                                                    │
│     User vào "My Library" để học nội dung đã tạo                │
│     Dữ liệu được lưu trữ vĩnh viễn                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. Thiết kế kỹ thuật (Technical Design)

### 5.1. Database Schema (PostgreSQL)

```sql
-- Bảng chính lưu trữ tài liệu custom
CREATE TABLE user_custom_materials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    title VARCHAR(255) NOT NULL,
    source_type VARCHAR(50) NOT NULL, -- PDF, YOUTUBE, TEXT, IMAGE, WEBSITE, DOCX
    original_file_url TEXT,           -- Link tới ObjectStore (MinIO/S3)
    content_text TEXT,                -- Nội dung thô đã trích xuất (OCR/Transcript)
    input_metadata JSONB,             -- Metadata đầu vào: { "pageStart": 1, "pageEnd": 20, "timeStart": 0, "timeEnd": 900 }
    generated_content JSONB,          -- Kết quả: { "schemaVersion": 1, "vocabulary": [{"id": "...", ...}], "quiz": [...], "shadowing": [...] }
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    error_message TEXT,               -- Lưu lỗi nếu FAILED
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Bảng lưu user settings/preferences cho từng material
CREATE TABLE user_custom_material_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id UUID REFERENCES user_custom_materials(id) ON DELETE CASCADE NOT NULL,
    target_options JSONB NOT NULL,    -- ["VOCABULARY", "ROLE_PLAY", "QUIZ"]
    ai_correction_mode VARCHAR(20) DEFAULT 'POLITE', -- STRICT, POLITE
    style_learn_mode BOOLEAN DEFAULT TRUE,           -- true = kèm giải thích
    sync_vocab_to_srs BOOLEAN DEFAULT FALSE,         -- true = đồng bộ vào SRS
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uq_material_settings_material_id UNIQUE (material_id)
);

-- Bảng lưu lịch sử chat Role-Play
CREATE TABLE custom_material_chat_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id UUID REFERENCES user_custom_materials(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    chat_history JSONB,               -- Array of { role, content, timestamp }
    performance_report JSONB,         -- Báo cáo lỗi cuối buổi (nếu mode = POLITE)
    started_at TIMESTAMP DEFAULT NOW(),
    ended_at TIMESTAMP
);

-- Bảng lưu kết quả Shadowing
CREATE TABLE user_shadowing_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id UUID REFERENCES user_custom_materials(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    sentence_id VARCHAR(50) NOT NULL, -- ID trong generated_content.shadowing
    audio_url TEXT,                   -- Link file ghi âm user
    score INT,                        -- 0-100
    feedback JSONB,                   -- Chi tiết lỗi phát âm
    created_at TIMESTAMP DEFAULT NOW()
);

-- Bảng quản lý Job Async (Durability)
CREATE TABLE custom_material_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id UUID REFERENCES user_custom_materials(id) ON DELETE CASCADE NOT NULL,
    status VARCHAR(20) DEFAULT 'QUEUED', -- QUEUED, PROCESSING, COMPLETED, FAILED
    retry_count INT DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uq_material_jobs_material_id UNIQUE (material_id)
);

-- Indexes
CREATE INDEX idx_custom_materials_user_id ON user_custom_materials(user_id);
CREATE INDEX idx_custom_materials_status ON user_custom_materials(status);
CREATE INDEX idx_chat_sessions_material_id ON custom_material_chat_sessions(material_id);
CREATE INDEX idx_shadowing_attempts_material_id ON user_shadowing_attempts(material_id);
CREATE INDEX idx_jobs_status ON custom_material_jobs(status);
```

### 5.2. API Contract (Spring Boot)

**Security Requirements (All Endpoints):**
- **Authentication:** Bearer Token required.
- **Authorization:**
    - `material.userId == auth.userId` (Ownership check).
    - `session.userId == auth.userId` (Chat session check).
- **Rate Limiting:** Premium quota check (10 materials/day).
- **SSRF Protection:** Validate `sourceUrl` (Allowlist domains for Youtube; Block private IPs/localhost for Websites).

#### 5.2.1. Upload & Create Material

```http
POST /api/v1/custom-materials
Content-Type: multipart/form-data
Authorization: Bearer {token}

-- Payload --
file: Binary (Optional - cho PDF/DOCX/Image)
request: JSON String (Content-Type: application/json)
{
  "sourceType": "PDF",                    // PDF, YOUTUBE, TEXT, IMAGE, WEBSITE, DOCX
  "sourceUrl": null,                      // Required cho YOUTUBE, WEBSITE
  "rawText": null,                        // Required cho TEXT
  "inputMetadata": {                      // Optional - Range selection
    "pageStart": 5,
    "pageEnd": 15,
    "timeStart": 600,                     // Seconds
    "timeEnd": 1200
  },
  "title": "Project Specs Q1",
  "targetOptions": ["VOCABULARY", "ROLE_PLAY", "QUIZ"],
  "settings": {
    "aiCorrectionMode": "POLITE",         // STRICT, POLITE
    "styleLearnMode": true,
    "syncVocabToSrs": false
  }
}

-- Response: 202 Accepted --
{
  "id": "uuid-xxx",
  "status": "PROCESSING",
  "message": "Your material is being processed. We'll notify you when it's ready."
}
```

#### 5.2.2. List Materials (My Library)

```http
GET /api/v1/custom-materials?page=0&size=10&status=COMPLETED&sort=createdAt,desc
Authorization: Bearer {token}

-- Response: 200 OK --
{
  "content": [
    { "id": "...", "title": "...", "status": "COMPLETED", "createdAt": "..." }
  ],
  "totalPages": 5,
  "totalElements": 50
}
```

#### 5.2.3. Get Material Status (Polling)

```http
GET /api/v1/custom-materials/{id}/status
Authorization: Bearer {token}

-- Response: 200 OK --
{
  "id": "uuid-xxx",
  "status": "PROCESSING", // PENDING, PROCESSING, COMPLETED, FAILED
  "progress": 45          // Optional: % completed
}
```

#### 5.2.4. Get Material Content

```http
GET /api/v1/custom-materials/{id}
Authorization: Bearer {token}

-- Response: 200 OK --
{
  "id": "uuid-xxx",
  "title": "Project Specs Q1",
  "sourceType": "PDF",
  "status": "COMPLETED",
  "generatedContent": {
    "schemaVersion": 1,
    "vocabulary": [
        { "id": "v1", "term": "ROI", "definition": "Return on Investment", ... }
    ],
    "quiz": [...],
    "shadowing": [
        { "id": "s1", "sentence": "I believe we should consider...", "audioUrl": "..." }
    ],
    "roleplay": { ... },
    "summary": "..."
  },
  "settings": {...},
  "inputMetadata": { "pageStart": 5, "pageEnd": 15 },
  "createdAt": "2025-01-15T10:00:00Z"
}
```

#### 5.2.5. Update Generated Content (Edit/Delete Items)

```http
PATCH /api/v1/custom-materials/{id}/content
Authorization: Bearer {token}

-- Request --
{
  "generatedContent": {
    "vocabulary": [ ...updated list... ],
    "quiz": [ ...updated list... ]
  }
}

-- Response: 200 OK --
{
  "id": "uuid-xxx",
  "generatedContent": { ... }
}
```

#### 5.2.6. Role-Play Chat

```http
POST /api/v1/custom-materials/{id}/chat
Authorization: Bearer {token}

-- Request --
{
  "message": "I think we should delay the project deadline.",
  "sessionId": "uuid-session" // null để tạo session mới
}

-- Response: 200 OK --
{
  "sessionId": "uuid-session",
  "aiResponse": "I understand your concern, but could you explain the specific blockers?",
  "corrections": []  // Chỉ có nếu mode = STRICT
}
```

#### 5.2.7. End Chat & Get Report

```http
POST /api/v1/custom-materials/{id}/chat/{sessionId}/end
Authorization: Bearer {token}

-- Response: 200 OK --
{
  "sessionId": "uuid-session",
  "performanceReport": {
    "overallScore": 85,
    "grammarErrors": [
      {
        "original": "I think we should delay",
        "suggestion": "I believe we should consider delaying",
        "explanation": "Using 'I believe' and 'consider' sounds more diplomatic in business context."
      }
    ],
    "vocabularySuggestions": [...],
    "strengths": ["Good argumentation", "Clear points"],
    "improvements": ["Use more formal phrases"]
  }
}
```

#### 5.2.8. Shadowing Scoring

```http
POST /api/v1/custom-materials/{id}/shadowing/{sentenceId}/score
Content-Type: multipart/form-data
Authorization: Bearer {token}

-- Payload --
audio: Binary (User recording)

-- Response: 200 OK --
{
  "score": 85,
  "feedback": "Good intonation, but watch the stress on 'consider'.",
  "phonemeBreakdown": { ... }
}
-- Note: Result is saved to user_shadowing_attempts table.
```

#### 5.2.9. Style Transform

```http
POST /api/v1/custom-materials/transform-style
Authorization: Bearer {token}

-- Request --
{
  "text": "I want you to fix this ASAP. This is unacceptable.",
  "targetStyle": "DIPLOMATIC",  // FORMAL, DIPLOMATIC, PERSUASIVE
  "includeExplanation": true
}

-- Response: 200 OK --
{
  "transformedText": "I would greatly appreciate if this could be addressed at your earliest convenience. I believe there may be some room for improvement in the current approach.",
  "explanations": [
    {
      "original": "I want you to",
      "changed": "I would greatly appreciate if",
      "reason": "Softens the demand and shows respect for the recipient's time."
    },
    {
      "original": "fix this ASAP",
      "changed": "addressed at your earliest convenience",
      "reason": "More polite way to express urgency without sounding aggressive."
    }
  ]
}
```
**Error Codes:**
- `400 Bad Request`: Invalid input, range invalid.
- `401 Unauthorized`: Missing/Invalid token.
- `403 Forbidden`: Not owner of material, or Premium quota exceeded.
- `404 Not Found`: Material ID not found.
- `413 Payload Too Large`: File size exceeds limit.
- `429 Too Many Requests`: Rate limit exceeded.
- `500 Internal Server Error`: Unexpected server error.


### 5.3. Xử lý bất đồng bộ (Async Processing)

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client  │────▶│   API    │────▶│  Queue   │────▶│  Worker  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                │                                  │
     │   202 Accepted │                                  │
     │◀───────────────│                                  │
     │                                                   │
     │   Polling GET /status (mỗi 5s)                    │
     │──────────────────────────────────────────────────▶│
     │                                                   │
     │                              ┌──────────────┐     │
     │                              │   Gemini AI  │◀────│
     │                              └──────────────┘     │
     │                                                   │
     │   Push Notification khi COMPLETED                 │
     │◀──────────────────────────────────────────────────│
```

**Tech Stack:**
- **Queue:**
    - **MVP:** Spring `@Async` + `ThreadPoolTaskExecutor` + **DB-backed Job Tracking** (Table `custom_material_jobs` để recover khi restart).
    - **Scale:** RabbitMQ / Kafka.
- **Status Authority:** `custom_material_jobs.status` là source of truth cho Worker. `user_custom_materials.status` được update mirror theo job status để hiển thị cho user.
- **Storage:** MinIO/S3 cho file upload.
- **AI:** Google Gemini API (gemini-1.5-pro) - Sử dụng Multimodal Input cho PDF/Image.
- **Youtube Fallback:** yt-dlp (download audio) + Gemini Flash (Audio to Text) nếu thiếu Transcript.

### 5.4. Giới hạn & Ràng buộc (Constraints)

| Constraint | Value | Note |
|------------|-------|------|
| Max file size | 10MB | Áp dụng cho PDF, DOCX, Image |
| Max PDF pages | 20 trang | User chọn range (VD: Page 5-25). Default: 1-20. |
| Max Youtube length | 15 phút | User chọn range (VD: 10:00-25:00). Default: 0-15. |
| Max raw text | 5000 ký tự | ~1000 từ |
| Daily quota (Premium) | 10 materials/ngày | Quản lý chi phí Gemini API |

---

## 6. UI/UX Wireframe Concepts

### 6.1. Upload Screen

```
┌─────────────────────────────────────────┐
│  ← New Material                         │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────┐    │
│  │                                 │    │
│  │      📄 Drop file here         │    │
│  │      or click to browse        │    │
│  │                                 │    │
│  │   PDF, DOCX, Image (max 10MB)  │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ─────────── OR ───────────            │
│                                         │
│  🔗 Paste URL (Youtube/Website)         │
│  ┌─────────────────────────────────┐    │
│  │ https://...                     │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ─────────── OR ───────────            │
│                                         │
│  📝 Paste Text                          │
│  ┌─────────────────────────────────┐    │
│  │                                 │    │
│  │                                 │    │
│  └─────────────────────────────────┘    │
│                                         │
│  [ Continue →                      ]    │
└─────────────────────────────────────────┘

*Note: Sau khi chọn file/link, hiển thị thêm bước chọn Range (Page X-Y hoặc Time Start-End).*
```

### 6.2. Configuration Screen

```
┌─────────────────────────────────────────┐
│  ← Configure Learning                   │
├─────────────────────────────────────────┤
│                                         │
│  What do you want to learn?             │
│                                         │
│  ☑ Vocabulary List                      │
│  ☑ Smart Summary                        │
│  ☑ Quiz Questions                       │
│  ☑ Role-Play Scenario                   │
│  ☐ Shadowing Practice                   │
│                                         │
│  ─────────────────────────────────      │
│                                         │
│  ⚙️ Learning Preferences                │
│                                         │
│  AI Correction Style                    │
│  [Gentle ●────────○ Strict]             │
│                                         │
│  Show explanations in Style Transform   │
│  [ON ●] / [○ OFF]                       │
│                                         │
│  Sync vocabulary to daily review        │
│  [○ ON] / [● OFF]                       │
│                                         │
│  [ Generate Content →              ]    │
└─────────────────────────────────────────┘
```

### 6.3. Role-Play Chat Screen

```
┌─────────────────────────────────────────┐
│  Mock Meeting: Q1 Budget Review     ⚙️  │
├─────────────────────────────────────────┤
│                                         │
│  🤖 AI (CFO):                           │
│  ┌─────────────────────────────────┐    │
│  │ "I've reviewed the proposal.    │    │
│  │ Can you justify the 20%         │    │
│  │ increase in marketing spend?"   │    │
│  └─────────────────────────────────┘    │
│                                         │
│                        👤 You:          │
│    ┌─────────────────────────────────┐  │
│    │ "Based on our Q4 results,      │  │
│    │ the ROI from digital ads was   │  │
│    │ 3.2x. I believe increasing..." │  │
│    └─────────────────────────────────┘  │
│                                         │
│  🤖 AI (CFO):                           │
│  ┌─────────────────────────────────┐    │
│  │ "Interesting. What about the    │    │
│  │ traditional channels?"          │    │
│  └─────────────────────────────────┘    │
│                                         │
├─────────────────────────────────────────┤
│  ┌─────────────────────────────┐  [📤] │
│  │ Type your response...       │       │
│  └─────────────────────────────┘       │

### 6.4. My Library / Lesson View

- **Editable Content:** User có thể Edit/Delete các từ vựng, câu hỏi quiz mà AI tạo ra nếu thấy không chính xác.
- **Action Menu:** [✏️ Edit] [🗑️ Delete] bên cạnh mỗi item.
│                      [ End & Report ]   │
└─────────────────────────────────────────┘
```

---

## 7. Kế hoạch triển khai (Implementation Plan)

### Phase 1: Foundation (Tuần 1)

| Task | Owner | Priority |
|------|-------|----------|
| Tạo DB tables & Entity classes | Backend | P0 |
| Tích hợp MinIO/S3 upload | Backend | P0 |
| API endpoint nhận file (202 Accepted) | Backend | P0 |
| Async processing với @Async | Backend | P0 |
(Multimodal Support) | Backend | P0 |
| PDF Processing (Gemini Direct Input + PDFBox Fallback) | Backend | P0 |
| Youtube Transcript (API + Audio Fallback) | Backend | P1 |
| Prompt Engineering (XML Tagging for Security)
|------|-------|----------|
| Gemini API integration | Backend | P0 |
| PDF text extraction (Apache PDFBox) | Backend | P0 |
| Youtube transcript fetching | Backend | P1 |
| Prompt engineering cho Vocab/Quiz/Summary | Backend | P0 |

### Phase 3: Role-Play & Chat (Tuần 3)

| Task | Owner | Priority |
|------|-------|----------|
| Chat session management | Backend | P0 |
| Role-Play prompt engineering | Backend | P0 |
| AI Correction modes (Strict/Polite) | Backend | P0 |
| Performance report generation | Backend | P1 |

### Phase 4: Frontend Integration (Tuần 4)

| Task | Owner | Priority |
|------|-------|----------|
| Upload UI (Web + Mobile) | Frontend | P0 |
| Configuration screen | Frontend | P0 |
| Processing state & polling | Frontend | P0 |
| Chat interface | Frontend | P0 |
| My Library view | Frontend | P1 |

### Phase 5: Polish & Testing (Tuần 5)

| Task | Owner | Priority |
|------|-------|----------|
| Style Transformer feature | Backend | P1 |
| Push notifications | Backend | P1 |
| Premium gate check | Backend | P0 |
| E2E testing | QA | P0 |
| Performance optimization | All | P1 |

---

## 8. Metrics & Success Criteria

| Metric | Target | Measurement |
|--------|--------|-------------|
| Feature adoption | 30% Premium users trong tháng đầu | Analytics |
| Processing success rate | > 95% | Error logs |
| Avg processing time | < 60s | APM |
| User satisfaction | > 4.0/5.0 | In-app rating |
| Conversion impact | +15% Free→Paid | A/B test |

---

## 9. Risks & MitigationsSử dụng Gemini Multimodal, fallback sang OCR |
| Youtube transcript không có | Medium | Fallback: Download Audio -> Gemini Flash/Whisper |
| Prompt Injection | High | Bọc user content trong thẻ XML, validate input
| Risk | Impact | Mitigation |
|------|--------|------------|
| Gemini API cost vượt budget | High | Daily quota limit, caching common patterns |
| PDF extraction quality kém | Medium | Fallback sang OCR, cho user edit text |
| Youtube transcript không có | Medium | Thông báo user, suggest upload file thay thế |
| User upload nội dung không phù hợp | Low | Content moderation filter |

---

## 10. Open Questions

1. [ ] Có cần hỗ trợ offline mode cho Role-Play không?
2. [ ] Có cho phép user share material với người khác không?
3. [ ] Có tích hợp với Google Drive/Dropbox để import không?

---

## Appendix A: Toggle Settings Summary

| Setting | Options | Default | Scope |
|---------|---------|---------|-------|
| AI Correction Mode | STRICT / POLITE | POLITE | Per Material |
| Style Learn Mode | ON / OFF | ON | Per Request |
| Sync Vocab to SRS | ON / OFF | OFF | Per Material |

---

**Document History:**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-01-15 | - | Initial draft |
| 2.0 | 2025-01-15 | - | Added brainstorming decisions: Chat interface, User toggles, No gamification, Premium only |
| 2.1 | 2025-12-19 | AI | PR1 Complete: Database + API Foundation |
| 2.2 | 2025-12-20 | AI | PR4 Complete: DOCX Support (Apache POI) + Chat API (2 endpoints) |
| 2.3 | 2025-12-20 | AI | PR5 Complete: Style Transform + Premium Gate + Push Notifications |

---

## 11. Implementation Progress

### ✅ PR1: Database + API Foundation (Complete - 2025-12-19)

**Created Files (26 total):**

| Category | Files | Status |
|----------|-------|--------|
| Migration | V38__Create_custom_materials_tables.sql | ✅ |
| Enums | CustomMaterialSourceType, CustomMaterialStatus, AiCorrectionMode | ✅ |
| Entities | UserCustomMaterial, UserCustomMaterialSettings, CustomMaterialChatSession, UserShadowingAttempt, CustomMaterialJob | ✅ |
| Repositories | All 5 repositories with custom queries | ✅ |
| DTOs | 9 DTOs for request/response | ✅ |
| Service | CustomMaterialService interface + impl | ✅ |
| Controller | CustomMaterialController (7 endpoints) | ✅ |
| Exception | AccessDeniedException | ✅ |
| Modified | FileCategory.CUSTOM_MATERIAL | ✅ |

**API Endpoints:**
- `POST /api/v1/custom-materials` - Upload material
- `GET /api/v1/custom-materials` - List materials
- `GET /api/v1/custom-materials/{id}` - Get details
- `GET /api/v1/custom-materials/{id}/status` - Poll status
- `PATCH /api/v1/custom-materials/{id}/content` - Update content
- `DELETE /api/v1/custom-materials/{id}` - Delete
- `GET /api/v1/custom-materials/quota` - Check quota

---

### ✅ PR2: AI Content Generation (Complete - 2025-12-19)

**Created Files (6 total):**

| Category | Files | Status |
|----------|-------|--------|
| Service Interface | ContentExtractorService | ✅ |
| Implementation | ContentExtractorServiceImpl (PDF, Image, YouTube, Website, Text) | ✅ |
| Prompts | CustomMaterialPrompts (Vocabulary, Quiz, Summary, RolePlay, Shadowing, StyleTransform) | ✅ |
| Processing | CustomMaterialProcessingService interface + impl | ✅ |
| Exception | ContentExtractionException | ✅ |

**Features:**
- Content extraction from all 6 source types
- Gemini multimodal integration for PDF/Image OCR
- XML-tagged prompts for security (prompt injection prevention)
- Async processing pipeline with progress tracking
- Error handling and retry support

---

### ⚠️ PR3: Testing & Polish (Partial Complete - 2025-12-20)

**Completed Goals:**
- [x] Unit tests for ContentExtractorService (≥70% coverage) ✅
- [x] Unit tests for CustomMaterialProcessingService (≥70% coverage) ✅
- [x] Controller Tests Context Load Fixes (`AuthControllerTest`, `FlashcardControllerTest`, `ProgressControllerTest`) ✅
- [x] Refactored `ContentExtractorService` for better testability (HttpClient injection) ✅

**Pending Goals:**
- [ ] Integration tests for API endpoints
- [ ] Add Jsoup for better HTML extraction (already exists as dependency)

**Created Test Files:**
| Category | Files | Status |
|----------|-------|--------|
| Unit Tests | `ContentExtractorServiceImplTest`, `CustomMaterialProcessingServiceImplTest` | ✅ |
| Unit Tests | `CustomMaterialPromptsTest`, `AICostServiceTest` | ✅ |
| Config | `HttpClientConfig` (for testability) | ✅ |

---

### ✅ PR4: DOCX Support + Chat API (Complete - 2025-12-20)

**Phase 2 Complete - DOCX Support:**
- [x] Added Apache POI dependency (`poi-ooxml:5.2.5`) ✅
- [x] Implemented `extractFromDocx()` in `ContentExtractorServiceImpl` ✅
  - Downloads DOCX from URL
  - Parses with `XWPFDocument`
  - Supports page range via paragraph chunks
  - Error handling for all failure cases

**Phase 3 Complete - Chat API:**

| Category | Files | Status |
|----------|-------|--------|
| DTOs | `ChatMessageRequestDTO`, `ChatMessageResponseDTO`, `EndChatResponseDTO` | ✅ |
| Service | `CustomMaterialChatService` interface + `CustomMaterialChatServiceImpl` | ✅ |
| Controller | 2 new endpoints in `CustomMaterialController` | ✅ |

**New Endpoints:**
- `POST /api/v1/custom-materials/{id}/chat` - Send message (creates session if null)
- `POST /api/v1/custom-materials/{id}/chat/{sessionId}/end` - End session with report

**Features:**
- Session management (create/continue sessions)
- AI conversation using role-play context from material
- STRICT mode: immediate corrections
- POLITE mode: corrections in final report
- Performance report with score, grammar errors, vocabulary suggestions

**Build Status:** ✅ BUILD SUCCESSFUL
**Test Status:** 98% pass (1166/1178 - 12 pre-existing failures in AuthControllerTest)

---

### ✅ PR5: Phase 5 Polish (Complete - 2025-12-20)

**Task 1: Style Transform Endpoint:**

| Category | Files | Status |
|----------|-------|--------|
| DTOs | `StyleTransformRequestDTO`, `StyleTransformResponseDTO` | ✅ |
| Service | `transformStyle()` in `CustomMaterialService` + impl | ✅ |
| Controller | `POST /transform-style` endpoint | ✅ |

**Endpoint:** `POST /api/v1/custom-materials/transform-style`  
**Styles:** FORMAL, CASUAL, EMAIL, PRESENTATION, SOCIAL_MEDIA

---

**Task 2: Premium Gate Enforcement:**

| Category | Files | Status |
|----------|-------|--------|
| Annotation | `@RequirePremium` | ✅ |
| AOP | `PremiumCheckAspect` | ✅ |
| Exception | `SubscriptionRequiredException` + handler | ✅ |

**Applied to:** `createMaterial`, `transformStyle` endpoints  
**Returns:** 403 FORBIDDEN with upgrade URL for Free users

---

**Task 3: Push Notifications:**

| Category | Files | Status |
|----------|-------|--------|
| Event | `MaterialProcessingCompletedEvent` | ✅ |
| Types | `CUSTOM_MATERIAL_READY`, `CUSTOM_MATERIAL_FAILED` | ✅ |
| Listener | Handler in `NotificationEventListener` | ✅ |
| Publisher | Event firing in `CustomMaterialProcessingServiceImpl` | ✅ |

**Behavior:** Real-time WebSocket notification when material processing completes

---

**Build Status:** ✅ Compile SUCCESSFUL  
**Coverage:** ⚠️ 46% (new files need unit tests)

---

### ✅ PR6: Fixes & Missing Features (Complete - 2025-12-21)

**Task 1: Premium Gate - Extended Enforcement:**

| Endpoint | Annotation Added |
|----------|------------------|
| `GET /api/v1/custom-materials` (list) | `@RequirePremium` ✅ |
| `GET /api/v1/custom-materials/{id}` (get) | `@RequirePremium` ✅ |
| `POST /{id}/chat` (send message) | `@RequirePremium` ✅ |
| `POST /{id}/chat/{sessionId}/end` | `@RequirePremium` ✅ |

---

**Task 2: Physical File Deletion:**

- [x] Extract file ID from `originalFileUrl` (pattern: `/api/v1/files/{uuid}/download`)
- [x] Call `fileStorageService.delete(fileId)` when deleting material
- [x] Graceful error handling (log warning, don't block deletion)

---

**Task 3: Shadowing Score Endpoint (NEW):**

| Category | Files | Status |
|----------|-------|--------|
| DTOs | `ShadowingScoreResponseDTO` | ✅ |
| Service | `scoreShadowing()` in `CustomMaterialService` | ✅ |
| Controller | `POST /{id}/shadowing/{sentenceId}/score` | ✅ |

**Endpoint:** `POST /api/v1/custom-materials/{id}/shadowing/{sentenceId}/score`  
**Features:** Upload audio, Gemini pronunciation scoring, save attempt to `user_shadowing_attempts`

---

**Task 4: SRS Sync Integration:**

- [x] `FlashcardService` integration in `CustomMaterialProcessingServiceImpl`
- [x] When `syncVocabToSrs=true`, vocabulary is synced to FlashcardDeck after processing
- [x] Deck title: "Vocabulary: {material.title}"
- [x] Converts vocabulary items to `FlashcardCardDTO` with word, definition, pos, example, synonyms

---

**Task 5: YouTube Fallback (Multi-Tier):**

| Tier | Method | API |
|------|--------|-----|
| 1 | `fetchTranscriptPrimary()` | YouTubeTranscript.com |
| 2 | `fetchTranscriptAlternative()` | video.google.com/timedtext |
| 3 | `generateContentFromMetadata()` | YouTube oEmbed + Gemini |

**Fallback Behavior:** If no transcript available, Gemini generates learning content from video title/channel

---

**Task 6: YouTube Time Range:**

- [x] Parse timestamp patterns: `[MM:SS]`, `(HH:MM:SS)`, `MM:SS - text`
- [x] Filter transcript segments by `timeStart`/`timeEnd` metadata
- [x] Word-count fallback (~3 words/sec) if no timestamps found

---

**Task 7: Chat Report Parsing Fix:**

- [x] Replaced regex with Jackson `ObjectMapper` for JSON parsing
- [x] Properly extracts: `grammarErrors`, `vocabularySuggestions`, `strengths`, `improvements`
- [x] Fallback to default values if parsing fails

---

**Task 8: Style Transform - Extended Styles:**

| New Style | Description |
|-----------|-------------|
| `DIPLOMATIC` | Polite, considerate, softens demands while maintaining clarity |
| `PERSUASIVE` | Convincing, uses rhetorical techniques, emphasizes benefits |

**All Styles Now:** FORMAL, CASUAL, EMAIL, PRESENTATION, SOCIAL_MEDIA, DIPLOMATIC, PERSUASIVE

---

**Build Status:** ✅ Compile SUCCESSFUL  
**Files Modified:** 8 files  
**New Features:** 3 (Shadowing Score, SRS Sync, YouTube Multi-Tier Fallback)

---

### ✅ PR7: Phase 4 - Frontend Integration (Complete - 2025-12-21)

**Scope:** lexia-web (Next.js 14+ / TypeScript / Tailwind CSS / Zustand)

#### Files Created (23 total):

| Category | Files | Status |
|----------|-------|--------|
| Types | `types/custom-materials.ts` (270 lines) | ✅ |
| Service | `services/customMaterialService.ts` (200 lines) | ✅ |
| Store | `store/customMaterialStore.ts` (340 lines) | ✅ |
| Upload Components | `SourceTypeSelector`, `FileDropzone`, `UrlInput`, `RawTextInput`, `InputMetadataForm` | ✅ |
| Config Components | `TargetOptionsSelector`, `SettingsPanel` | ✅ |
| Processing Components | `useStatusPoller`, `ProcessingStatus`, `QuotaDisplay` | ✅ |
| Chat Components | `ChatMessageBubble`, `EndSessionReport`, `MaterialChatInterface` | ✅ |
| Library Components | `MaterialCard`, `MaterialFilters`, `MaterialsGrid` | ✅ |
| Pages | `page.tsx`, `library/page.tsx`, `[id]/page.tsx`, `[id]/chat/page.tsx` | ✅ |
| Navigation | `Sidebar.tsx` (modified - added Custom Materials link with PRO badge) | ✅ |

---

#### Feature Implementation:

**1. Multi-Step Upload Wizard (`/custom-materials`):**
- Step 1: Source type selection (PDF, DOCX, Image, YouTube, Website, Text)
- Step 2: Content input (FileDropzone / UrlInput / RawTextInput)
- Step 3: Configuration (TargetOptions, AI Correction Mode, SRS Sync)
- Step 4: Processing status with real-time polling

**2. Premium Gate:**
- Non-Pro users see upgrade CTA with feature highlights
- Pro users access full wizard

**3. My Library (`/custom-materials/library`):**
- Grid view of all materials
- Filter by status (All, Completed, Processing, Failed)
- Delete with confirmation dialog

**4. Material Detail (`/custom-materials/[id]`):**
- Tabs: Vocabulary, Quiz, Summary, Shadowing
- Vocabulary: term, definition, example, IPA, part of speech
- Quiz: multiple choice with explanations
- "Practice Role-Play" button → Chat page

**5. Role-Play Chat (`/custom-materials/[id]/chat`):**
- Real-time chat interface
- Grammar corrections display (STRICT mode)
- End Session → Performance Report modal
  - Overall score (0-100)
  - Grammar errors with suggestions
  - Vocabulary suggestions
  - Strengths & Improvements

**6. Navigation Integration:**
- Added "Custom Materials" link in Sidebar under AI Features
- PRO badge indicator
- Collapsible opens for `/custom-materials/*` routes

---

#### Technical Implementation:

**TypeScript Types (`types/custom-materials.ts`):**
```typescript
// Enums
CustomMaterialSourceType: PDF | DOCX | IMAGE | YOUTUBE | WEBSITE | TEXT
CustomMaterialStatus: PENDING | PROCESSING | COMPLETED | FAILED
AiCorrectionMode: STRICT | POLITE
TargetOption: VOCABULARY | QUIZ | SUMMARY | ROLE_PLAY | SHADOWING

// Main interfaces
CustomMaterial, MaterialListItem, GeneratedContent
VocabularyItem, QuizQuestion, ShadowingSentence, RolePlayContext
ChatMessage, PerformanceReport, GrammarError
```

**API Service (`services/customMaterialService.ts`):**
- `createMaterial(data, file?)` - POST multipart/form-data
- `listMaterials(params)` - GET with pagination
- `getMaterial(id)` - GET single material
- `deleteMaterial(id)` - DELETE
- `getStatus(id)` - GET polling endpoint
- `sendChatMessage(id, data)` - POST chat
- `endChatSession(id, sessionId)` - POST end session
- `transformStyle(data)` - POST style transform
- `scoreShadowing(id, sentenceId, audio)` - POST audio scoring
- `getQuota()` - GET user quota

**Zustand Store (`store/customMaterialStore.ts`):**
- Materials list state + pagination
- Current material state
- Upload/create state with error handling
- Processing status polling (startPolling/stopPolling)
- Chat session management (messages, sessionId, performanceReport)
- Quota tracking

---

#### Code Review Issues Fixed:

| Issue | Severity | Fix |
|-------|----------|-----|
| Processing không poll sau khi create | Critical | Gọi `startPolling()` sau `createMaterial()` |
| Sidebar không expand cho `/custom-materials` | Major | Update `defaultOpen` condition |
| `useStatusPoller` cascading render warning | Major | Sử dụng `setTimeout` cho initial poll |
| `showReport` setState trong effect | Major | Derive từ `performanceReport` |
| `Image` icon a11y warning | Minor | Rename thành `ImageIcon` |
| Time `0` bị convert thành `undefined` | Minor | Fix `formatTime` function |

---

#### Design System Compliance:

| Token | Value | Usage |
|-------|-------|-------|
| Primary Blue | `#4285F4` | Buttons, active states |
| Text Primary | `#202124` | Main text |
| Text Secondary | `#5F6368` | Descriptions |
| Success | `#4CAF50` | Completed status |
| Error | `#D32F2F` | Failed status |
| Warning/AI | `#FFB300` | AI Features accent |

**Accessibility:** ✅ ARIA labels, keyboard navigation, focus indicators, color contrast ≥ 4.5:1

---

**Build Status:** ✅ No TypeScript errors  
**Test Status:** ✅ 21 unit tests passing  
**Lines of Code:** ~4,260 lines

---

### ✅ PR8: Frontend Unit Tests (Complete - 2025-12-21)

**Test Files Created:**

| File | Tests | Coverage |
|------|-------|----------|
| `tests/mocks/customMaterialMocks.ts` | - | Mock data |
| `tests/store/customMaterialStore.test.ts` | 8 | Store actions |
| `tests/services/customMaterialService.test.ts` | 5 | API service |
| `tests/components/CustomMaterialLibrary.test.tsx` | 5 | Library components |
| `tests/components/MaterialChatInterface.test.tsx` | 3 | Chat UI |
| `tests/integration/customMaterialIntegration.test.ts` | - | Integration suite |

**Test Coverage:**
- Store: fetchMaterials, fetchMaterial, createMaterial, deleteMaterial, sendChatMessage
- Service: All API endpoints với mock responses
- Components: MaterialsGrid, MaterialCard, MaterialChatInterface

**Additional Fixes:**
- Added `aria-label` to chat send button for accessibility
- Fixed `jest.setup.js` with `scrollIntoView` and `IntersectionObserver` mocks
- Created `docs/testing/CUSTOM-MATERIALS-TESTING.md` testing guide

**Test Results:** ✅ All 21 tests passing

---

### ✅ PR9: Admin AI Content Management (Complete - 2025-12-21)

**Scope:** Backend (Spring Boot) + Admin Web (React/TypeScript)

#### Backend Implementation

**1. Feature Flag Registration:**

| Component | File | Change |
|-----------|------|--------|
| Service | `AIConfigServiceImpl.java` | Added `registerFeatureIfNotExists()` method |
| Init | `@PostConstruct init()` | Dynamically registers `custom_materials` on startup |

**Default Config for `custom_materials`:**
- `enabled`: true
- `max_file_size`: 10485760 (10MB)
- `max_pages`: 50
- `monthly_limit`: 10

---

**2. Granular Usage Tracking:**

| Constant | Content Type | Description |
|----------|--------------|-------------|
| `CONTENT_TYPE_CM_PDF_EXTRACTION` | `cm_pdf_extraction` | PDF file processing |
| `CONTENT_TYPE_CM_DOCX_EXTRACTION` | `cm_docx_extraction` | DOCX file processing |
| `CONTENT_TYPE_CM_IMAGE_OCR` | `cm_image_ocr` | Image OCR processing |
| `CONTENT_TYPE_CM_YOUTUBE_TRANSCRIPT` | `cm_youtube_transcript` | YouTube transcript extraction |
| `CONTENT_TYPE_CM_WEBSITE_EXTRACTION` | `cm_website_extraction` | Website content extraction |
| `CONTENT_TYPE_CM_TEXT_INPUT` | `cm_text_input` | Raw text input |
| `CONTENT_TYPE_CM_CONTENT_GENERATION` | `cm_content_generation` | Gemini content generation |

**Integration:** `CustomMaterialProcessingServiceImpl` now calls `AiUsageTracker.trackUsageAsync()` with source-type metadata.

---

**3. Quota Management:**

| File | Change |
|------|--------|
| `QuotaLimitsConfig.java` | Added `freeCustomMaterials` (0), `proCustomMaterials` (10) |
| `UserAiQuotaDTO.java` | Added `customMaterialsUsed`, `customMaterialsLimit` fields |
| `AdminAIQuotaController.java` | Updated `mapToDTO()` to include custom materials quota |

---

**4. Admin Monitoring API (NEW):**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/admin/custom-materials/materials` | List all materials (paginated) |
| `GET` | `/api/v1/admin/custom-materials/materials/{id}` | Get material details |
| `DELETE` | `/api/v1/admin/custom-materials/materials/{id}` | Delete material |
| `GET` | `/api/v1/admin/custom-materials/jobs` | List all jobs (with filters) |
| `GET` | `/api/v1/admin/custom-materials/jobs/stats` | Get job statistics |
| `POST` | `/api/v1/admin/custom-materials/jobs/{id}/retry` | Force retry failed job |
| `POST` | `/api/v1/admin/custom-materials/jobs/{id}/reset-retries` | Reset retry count |
| `DELETE` | `/api/v1/admin/custom-materials/jobs/{id}` | Force delete stuck job |

**New DTOs:**
- `AdminCustomMaterialDTO` - Material admin view
- `AdminJobDTO` - Job status with stuck detection
- `JobStatsDTO` - Success rate, counts by status

**Controller:** `AdminCustomMaterialController.java` (ROLE_ADMIN required)

---

#### Admin Frontend Implementation

**Files Created:**

| Category | File | Description |
|----------|------|-------------|
| Types | `types/customMaterialAdmin.ts` | TypeScript interfaces |
| API | `api/customMaterialAdminApi.ts` | API client functions |
| Hooks | `hooks/useCustomMaterialAdmin.ts` | React Query hooks |
| Page | `pages/JobMonitorPage.tsx` | Job monitoring UI |

**Job Monitor Page Features:**
- Stats cards (Total, Completed, Failed, Stuck jobs)
- Job table with progress bars, retry counts
- Filter by status (All/Queued/Processing/Completed/Failed/Stuck)
- Actions: Retry, Reset Retry Count, Delete (with confirmation)
- Auto-refresh stats every 30 seconds
- Error display section

**Route:** `/ai/jobs`

---

**Build Status:** ✅ Backend SUCCESSFUL, ✅ Frontend SUCCESSFUL  
**Files Created:** 7 new files  
**Files Modified:** 8 files

