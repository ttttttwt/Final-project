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

1. ⭐ **EPIC 3:** AI Role-Play _(Highest Priority)_
2. ⭐ **EPIC 4:** Grammar Sandbox _(High Priority)_
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

- [Project Overview](/docs/context/PROJECT-OVERVIEW.md)
- [Architecture Decisions](/docs/context/ARCHITECTURE.md)
- [API Specification](/docs/context/API-SPECIFICATION.md)
- [Code Standards](/docs/context/CODE-STANDARDS.md)
- [Frontend Design Requirements](/docs/context/FRONTEND-DESIGN-REQUIREMENTS.md) ✨ **NEW**
- [Database Schema](/docs/context/DATABASE-SCHEMA.md)
- [Security Requirements](/docs/context/SECURITY-REQUIREMENTS.md)

```

```
