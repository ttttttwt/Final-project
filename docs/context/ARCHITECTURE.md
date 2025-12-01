# LEXIA - System Architecture

## High-Level Diagram

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                                 Client Layer                                   │
├────────────────────┬────────────────────────┬───────────────────────────────────┤
│ Next.js Web App    │ React Native Mobile App│ React Admin Panel (Vite + shadcn) │
│ (Dashboard, CMS)   │ (Daily practice)       │ (Ops, moderation, insights)       │
└────────────┬───────┴──────────────┬────────┴───────────────────────────────────┘
             │                      │
             │ REST API (JWT + Rotating Refresh Tokens)
             └──────────────┬────────────────────────────────────────────────────┘
                            │
                 ┌──────────▼───────────┐
                 │  Spring Boot 3.x     │
                 │  Domain Services     │
                 │  - Auth & Profile    │
                 │  - Course & Lesson   │
                 │  - Learning Path     │
                 │  - Enrollment        │
                 │  - Progress & Streak │
                 │  - Dashboard         │
                 │  - Admin & Audit     │
                 │  - Gemini AI Adapter │
                 └──────────┬───────────┘
                            │
            ┌───────────────┼────────────────────────┬───────────────────────┐
            │               │                        │                       │
      ┌─────▼────┐    ┌─────▼─────┐            ┌──────▼─────┐          ┌─────▼─────┐
      │PostgreSQL │    │ Redis     │            │ ObjectStore│          │ Gemini AI │
      │(JSONB +   │    │ Cache     │            │ (Uploads)  │          │ (Content) │
      │analytics) │    └───────────┘            └────────────┘          └───────────┘
      └──────────┘            │
                              │
                        ┌─────▼────────┐
                        │ Background   │
                        │ Analytics &  │
                        │ Batch Jobs   │
                        └──────────────┘
```

## Architecture Patterns

- **API Pattern**: RESTful with JWT authentication, versioned `/api/v1`, HATEOAS-lite links for enrollments
- **Auth Pattern**: JWT access + rotating refresh tokens, family-based refresh storage, device-aware revoke logic
- **Domain Pattern**: Modular service layer (Auth, Course, Learning Path, Enrollment, Progress, Admin) with DTO mappers, validation, and transaction boundaries per aggregate
- **Database Pattern**: Relational (PostgreSQL) with JSONB for adaptive lesson content plus analytics tables (`lesson_progress`, `user_learning_paths`, `dashboard_snapshots`)
- **Caching**: Redis-backed query caching + rate-limit buckets for high-read mobile dashboards
- **AI Integration**: GeminiService adapter with circuit breaker + fallback prompts, auditable usage logs surfaced in admin panel

## Key Design Decisions

| Decision               | Choice                                                | Why                                                                       |
| ---------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------- |
| Backend Framework      | Spring Boot 3.x                                       | Enterprise-grade, security, scalability, community                        |
| Frontend Web           | Next.js                                               | SSR, SEO, modern DX, Vercel deployment                                    |
| Frontend Mobile        | React Native                                          | Code sharing with web team, iOS+Android                                   |
| Admin Panel            | React + Vite + shadcn/ui                              | Fast builds, modern UI components, TanStack Query                         |
| Database               | PostgreSQL                                            | JSONB for AI-generated content, powerful features                         |
| Authentication         | JWT + Rotating Tokens                                 | Stateless, secure, multi-device support                                   |
| AI Provider            | Google Gemini                                         | Cost-effective, fast, reliable                                            |
| Learning Path Modeling | Dedicated `learning_paths` + junction tables          | Enables curated curriculum with ordered course sequencing                 |
| Progress Analytics     | DashboardService over `lesson_progress` + Redis cache | Keeps dashboard APIs <500ms while persisting detailed telemetry           |
| Admin & Observability  | React (Vite) admin app + `/admin/**` APIs + Actuator  | Provides moderation, AI-usage insights, and runtime health in one surface |
| Audit Logging          | Separate audit_logs + admin_activity_logs tables      | Compliance tracking with field-level changes and actor accountability     |

## Component Responsibilities

- **Frontend**: UI rendering, user interactions, API calls
- **Backend**: Business logic, data validation, API contracts
- **Database**: Data persistence, ACID compliance
- **AI Service**: Content generation, personalization
- **Auth**: Security, token management, user sessions

## Domain Service Breakdown

- **AuthService**: Registration, login, refresh-token rotation, password hashing (BCrypt 12), device-aware revocation ledger.
- **CourseService / LessonService**: CRUD with drafting + publish flows, JSONB content validation, lesson ordering.
- **SectionService**: Manages course sections including CRUD operations and section reordering within courses.
- **LearningPathService**: Recommends curated paths based on CEFR level + goals, hydrates ordered list of courses/sections.
- **EnrollmentService**: Maintains `course_enrollments`, ensures idempotent enroll/start operations, provides enrollment-aware access control.
- **ProgressService**: Records lesson completions, streak math, XP totals, and writes granular telemetry rows used by analytics.
- **DashboardService**: Aggregates streak, goals, recommendations, and activity feed data for mobile dashboard (<500ms SLA) with Redis caching.
- **AdminService**: Surfaces AI usage logs, audit trails, and broadcast tooling for admin panel; enforces role-based guards.
- **AdminActivityLogService**: Records content management actions (course/section/lesson CRUD) for admin activity tracking and compliance.
- **AuditLogService**: Tracks user profile changes for security compliance; records field-level changes with IP and user agent.
- **UserProfileService**: Manages user profiles including personal info, preferences, avatar management with audit logging.
- **NotificationService**: Manages user notifications, preferences, and admin broadcasts. Supports in-app, email, and push channels.
- **FileStorageService**: Handles file uploads, metadata storage, and secure retrieval with access control. Supports local and cloud storage strategies.

## Learning Path & Progress Flow

1. **Discovery**: Mobile app calls `/learning-paths/recommend`, which evaluates profile data (CEFR level, goals, preferred skills) against curated paths stored in PostgreSQL.
2. **Enrollment**: `/learning-paths/{id}/start` orchestrates creation of `user_learning_paths` + course enrollments, ensuring transactions stay atomic.
3. **Lesson Consumption**: When users complete lessons, `/progress/lessons/{lessonId}/complete` persists analytics (duration, accuracy, sentiment) into `lesson_progress` JSONB columns.
4. **Aggregation**: Nightly background jobs roll up per-user stats (daily streaks, XP, goal status) into snapshot tables while Redis caches hot aggregates for real-time dashboard calls.
5. **Surfacing Insights**: `/progress/dashboard` and `/progress/summary` stitch together enrollment info, streaks, goals, and AI recommendations so the mobile dashboard can render a single payload.

## Admin & Observability

- **Admin Panel**: React + Vite SPA calling `/admin/**` endpoints for user management, AI usage analytics, and audit log reviews (ROLE_ADMIN enforced server-side).
- **AI Usage Tracking**: Every GeminiService invocation writes to `ai_usage_logs`, which the admin UI paginates and summarizes via `/admin/ai-usage/stats`.
- **Admin Activity Logs**: Content management actions (course/section/lesson CRUD, publish/unpublish) are logged to `admin_activity_logs` with actor info, timestamp, and change details. Accessible via `/admin/activity-logs` with filters, export to CSV.
- **User Audit Logs**: Profile changes tracked in `audit_logs` table with field-level diff, IP address, and user agent. Accessible via `/admin/audit-logs` with filters, export to CSV.
- **Runtime Monitoring**: Spring Boot Actuator exposed at `/actuator/**` with health/info/metrics plus Prometheus scraping; health root remains unauthenticated for load balancers, detailed nodes require ADMIN.
- **Alerting Hooks**: NotificationService uses the same audit data to dispatch broadcasts through WebSocket + push providers.
