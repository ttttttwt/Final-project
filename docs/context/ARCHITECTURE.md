# LEXIA - System Architecture

## High-Level Diagram
```
┌─────────────────────────────────────────────────────────────┐
│                     Client Layer                             │
├──────────────────────┬──────────────────────────────────────┤
│   Next.js Web App    │     React Native Mobile App          │
│   (Dashboard, Learn) │     (Daily Practice, Recording)      │
└──────────────┬───────┴──────────────────┬───────────────────┘
               │                          │
               │    REST API (JWT Auth)   │
               └──────────┬───────────────┘
                          │
                ┌─────────▼──────────┐
                │  Spring Boot 3.x   │
                │  Backend Services  │
                │  - AuthService     │
                │  - CourseService   │
                │  - LessonService   │
                │  - GeminiService   │
                └────────┬───────────┘
                         │
            ┌────────────┼────────────┐
            │            │            │
      ┌─────▼──┐   ┌────▼────┐  ┌───▼──────┐
      │PostgreSQL   │ Cache   │  │Gemini API│
      │(JSONB)      │(Redis)  │  │(AI Gen)  │
      └────────┘    └─────────┘  └──────────┘
```

## Architecture Patterns
- **API Pattern**: RESTful with JWT authentication
- **Auth Pattern**: JWT access + rotating refresh tokens
- **Database Pattern**: Relational (PostgreSQL) with JSONB for dynamic content
- **Caching**: Redis for frequently accessed data (lessons, user preferences)
- **AI Integration**: Service layer (GeminiService) with fallback handling

## Key Design Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Backend Framework | Spring Boot 3.x | Enterprise-grade, security, scalability, community |
| Frontend Web | Next.js | SSR, SEO, modern DX, Vercel deployment |
| Frontend Mobile | React Native | Code sharing with web team, iOS+Android |
| Database | PostgreSQL | JSONB for AI-generated content, powerful features |
| Authentication | JWT + Rotating Tokens | Stateless, secure, multi-device support |
| AI Provider | Google Gemini | Cost-effective, fast, reliable |

## Component Responsibilities
- **Frontend**: UI rendering, user interactions, API calls
- **Backend**: Business logic, data validation, API contracts
- **Database**: Data persistence, ACID compliance
- **AI Service**: Content generation, personalization
- **Auth**: Security, token management, user sessions
