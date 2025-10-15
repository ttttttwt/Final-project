Dưới đây là phiên bản viết lại chuẩn, rõ ràng và chuyên nghiệp hơn, giữ nguyên nội dung gốc nhưng cải thiện ngữ pháp, trình bày và nhất quán Markdown:

```markdown
# LEXIA – Quick Start for AI Context

## Identity
- **Project**: LEXIA (AI-Powered English Learning Platform)  
- **Vision**: Deliver adaptive, AI-integrated English learning for working professionals  
- **Version**: 1.0  
- **Last Updated**: 2025-01-15  

## Tech Stack
- **Backend**: Spring Boot 3.x (Java 17) + PostgreSQL + JWT  
- **Frontend**: Next.js + React Native  
- **AI Integration**: Google Gemini API  
- **Build Tool**: Gradle (Kotlin DSL)  

## Key Features (in Priority Order)
1. ⭐ **EPIC 3:** AI Role-Play *(Highest Priority)*  
2. ⭐ **EPIC 4:** Grammar Sandbox *(High Priority)*  
3. **EPIC 1:** Placement Test  
4. **EPIC 2:** Structured Learning Path  
5. **EPIC 5:** Gamification  

## Core Architecture
```

Frontend (Next.js / React Native)
↓  (API calls secured with JWT)
Backend (Spring Boot REST API)
↓  (via JPA / Hibernate)
PostgreSQL (JSONB for AI content)
↓  (through GeminiService)
Google Gemini API

```

## Constraints & Rules
- ✅ Maintain **≥70% unit test coverage**  
- ✅ Use **JWT tokens** — Access: 15 minutes | Refresh: 7 days  
- ✅ Support **multi-device login** with token rotation  
- ✅ **Hash all tokens** before storing (never store in plain text)  
- ❌ Do **not** generate code without proper error handling  
- ❌ **Never commit** `.env` or other sensitive configuration files  

## Quick Links to Full Documentation
- [Project Overview](/docs/00_context/01_PROJECT-OVERVIEW.md)  
- [Architecture Decisions](/docs/00_context/02_ARCHITECTURE.md)  
- [API Specification](/docs/00_context/04_API-SPECIFICATION.md)  
- [Code Standards](/docs/00_context/05_CODE-STANDARDS.md)
```

Bạn có muốn tôi viết thêm phần **mục tiêu kỹ thuật (Technical Objectives)** hoặc **project setup instructions** (cách khởi chạy nhanh) để phần “Quick Start” đúng nghĩa hơn không?
