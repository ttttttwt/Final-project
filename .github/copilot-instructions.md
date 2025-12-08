# GitHub Copilot Instructions - LEXIA

## 🎯 Project Identity
**LEXIA** - AI English Learning Platform for Working Professionals  
**Backend**: Spring Boot 3.x | Java 17 | PostgreSQL | JWT  
**Frontend Web**: Next.js 14+ | TypeScript | Tailwind CSS | Zustand  
**Mobile**: React Native (Expo) | TypeScript | React Navigation | Zustand  
**Admin**: React 18+ | Vite | shadcn/ui | TanStack Query

---

## 📚 Read First Each Session
**Mandatory**:
1. `docs/context/QUICK-START.md` - Overview & rules
2. `docs/plan/current-sprint-status.md` - Current tasks
3. `docs/implement/sprint-X/session-X-*.md` - Previous sessions

**Reference**: `docs/context/ARCHITECTURE.md`, `DATABASE-SCHEMA.md`, `API-SPECIFICATION.md`, `CODING-STANDARDS.md`, `FRONTEND-DESIGN-REQUIREMENTS.md`

---

## 🔄 Task Workflow
```
1. Read current-sprint-status.md
2. Read previous sessions
3. Pick ONE task
4. Implement + Tests (70%+ coverage)
5. Run tests (must PASS)
6. Update daily-log.md
7. Commit (conventional format)
8. Update sprint status
9. Session summary ONLY when requested
```

---

## ✅ Quality Gates
- [ ] Code compiles
- [ ] Tests PASS (./gradlew test | npm test)
- [ ] Coverage ≥ 70% (Services ≥ 80%)
- [ ] No secrets
- [ ] JavaDoc/Comments added
- [ ] daily-log.md updated
- [ ] Conventional commit

---

## 💻 Project Structure

### Backend
```
com.lexia.backend/
├── auth/              # JWT, Security
├── controller/        # REST APIs
├── service/impl/      # Business logic
├── repository/        # JPA repos
├── entity/            # Database models
├── dto/               # Data Transfer Objects
├── mapper/            # Entity-DTO mappers
├── config/            # Spring configs
├── exception/         # Custom exceptions
└── validation/        # Validators
```

### Frontend Web
```
lexia-web/
├── app/              # Next.js pages
├── components/       # React components
├── services/         # API services
├── store/           # Zustand stores
├── types/           # TypeScript types
├── lib/             # Utilities
└── hooks/           # Custom hooks
```

### Mobile
```
lexia-mobile-2/
├── app/             # Screens (auth, tabs, courses, lessons)
├── app-example/     # Example screens or backup
├── components/      # Reusable components
├── services/        # API services
├── store/          # Zustand stores
├── types/          # TypeScript types
├── assets/         # Static files
├── constants/      # Constants
├── docs/           # Documentation
├── hooks/          # Custom hooks
├── lib/            # Utilities
└── utils/          # Utility functions
```

### Admin
```
lexia-admin/src/
├── main.tsx         # Entry point
├── router.tsx       # Routes
├── components/      # UI components (ui/, layout/, auth/, shared/)
├── features/        # Modules (auth, dashboard, users, courses, lessons)
├── lib/            # Utilities (api.ts)
├── store/          # Zustand stores
└── types/          # TypeScript types
```

---

## 🚫 Never Do
**Backend**:
- ❌ Plain-text passwords/tokens
- ❌ Log sensitive data
- ❌ Skip tests
- ❌ Generic `Exception` catches

**Frontend (All)**:
- ❌ Store passwords in localStorage/AsyncStorage
- ❌ Skip validation (use Zod)
- ❌ Ignore error/loading states
- ❌ Skip accessibility (ARIA, keyboard)
- ❌ Forget responsive design
- ❌ Use inline styles (Web: Tailwind, Mobile: StyleSheet)

**Admin**:
- ❌ Allow non-ADMIN to user management
- ❌ Skip RoleGuard
- ❌ Ignore TanStack Query cache invalidation

**All**:
- ❌ Edit `docs/context/` without approval
- ❌ Commit failing tests
- ❌ Work on multiple tasks

---

## ✅ Always Do
**Backend**:
- ✅ Bcrypt passwords (cost 12)
- ✅ Return JWT in response body
- ✅ Hash refresh tokens (SHA-256)
- ✅ Validate inputs
- ✅ Use DTOs

**Frontend Web**:
- ✅ localStorage for tokens (temp, will migrate to httpOnly)
- ✅ React Hook Form + Zod validation
- ✅ Handle errors (network, timeout, 500)
- ✅ Retry logic (3x)
- ✅ Test 320px-1920px
- ✅ Next.js `<Image>`

**Mobile**:
- ✅ AsyncStorage for tokens
- ✅ Validate inputs
- ✅ Error handling + retry (3x)
- ✅ Test iOS & Android
- ✅ Accessibility labels
- ✅ React Native Paper UI
- ✅ Type-safe navigation

**Admin**:
- ✅ TanStack Query + cache invalidation
- ✅ shadcn/ui components
- ✅ RoleGuard for protected routes
- ✅ Toast notifications

**All**:
- ✅ Update daily-log.md
- ✅ Conventional commits
- ✅ TypeScript strict mode

---

## 📌 Daily Checklist
**Start**: Read `current-sprint-status.md` → `daily-log.md` → Pick task  
**During**: Code + Tests → Run tests → Update daily-log.md  
**End**: Commit → Update sprint status → Session summary (if requested)

---

## 📊 Success Metrics
**Backend**: Tests 100%, Coverage ≥70% (Services ≥80%), API <500ms  
**Frontend Web**: Tests 100%, Coverage ≥60%, Responsive 320px-1920px, WCAG AA  
**Mobile**: Tests 100%, Coverage ≥60%, iOS+Android, Accessibility labels  
**Admin**: Tests 100%, Coverage ≥60%, RBAC working, TanStack Query optimized  
**All**: No security issues, Docs updated

---

## 📝 Session Documentation
**Create `session-X-topic.md` with**:
1. **Accomplished** - Task list
2. **Code Generated** - Files + LOC
3. **Key Decisions** - Top 3 choices
4. **Challenges** - Problems + solutions
5. **Quality** - 1-10 rating + why
6. **Best Prompts** - Reusable prompts
7. **Next Steps** - Roadmap

**Why**: Knowledge preservation, progress tracking, quality assurance, learning tool, onboarding aid

---

## 🔐 Security Checklist
**Backend**: Bcrypt passwords, JWT in response, hash refresh tokens, validate inputs, no secrets  
**Frontend**: localStorage/AsyncStorage tokens, clear on logout, Zod validation, error handling, retry logic  

---

## ♿ Accessibility
- [ ] ARIA labels
- [ ] Semantic HTML (`<nav>`, `<main>`)
- [ ] Keyboard nav (Tab, Enter, Esc)
- [ ] Focus indicators
- [ ] Color contrast ≥4.5:1
- [ ] Error messages (`aria-describedby`)

---

## 📱 Responsive (Web)
- [ ] 320px (Mobile S), 375px (Mobile M), 768px (Tablet)
- [ ] 1024px (Desktop S), 1280px (Desktop M), 1920px (Desktop L)
- [ ] No horizontal scroll, Touch targets ≥44px

## 📱 Mobile Testing
- [ ] iOS/Android simulators
- [ ] Portrait & Landscape
- [ ] Touch targets ≥44px
- [ ] Safe area insets
- [ ] Keyboard handling
- [ ] Network errors

---

## 🔍 Decision Framework
1. Check `ARCHITECTURE.md` first
2. Security-first
3. Keep simple
4. Ask if unsure

---

## 🎯 Key Principles
1. **Context First** - Read docs before coding
2. **Security First** - Bcrypt, hash, validate
3. **Test-Driven** - Tests alongside code
4. **User Experience** - Loading, errors, responsive
5. **Accessibility** - ARIA, keyboard, WCAG AA
6. **Document Daily** - Update logs every session
7. **Focus** - One task at a time

---

**Quick Start**: `current-sprint-status.md` → Pick task → Code+Test+Doc → Commit+Update