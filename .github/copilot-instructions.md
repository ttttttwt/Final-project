# GitHub Copilot Instructions - LEXIA

## 🎯 Project Identity

**LEXIA** - AI English Learning Platform for Working Professionals  
**Backend**: Spring Boot 3.x | Java 17 | PostgreSQL | JWT  
**Frontend Web**: Next.js 14+ | TypeScript | Tailwind CSS | Zustand  
**Frontend Mobile**: React Native (Expo) | TypeScript | React Navigation | Zustand

---

## 📚 Context Documents (Read First Each Session)

**Mandatory**:

1. `docs/context/QUICK-START.md` - Project overview & rules
2. `docs/plan/current-sprint-status.md` - Current tasks
3. `docs/implement/sprint-X/session-X-*.md` - Previous session summaries

**Reference**:

- `docs/context/ARCHITECTURE.md` - System design
- `docs/context/DATABASE-SCHEMA.md` - Database structure
- `docs/context/API-SPECIFICATION.md` - API contracts
- `docs/context/CODING-STANDARDS.md` - Coding conventions & standards (1)
- `docs/context/CODE-STANDARDS.md` - Coding conventions (2)
- `docs/context/FRONTEND-DESIGN-REQUIREMENTS.md` - ✨ **Medium-inspired UI Design**

---

## 🔄 Task Workflow

```
1. Read current-sprint-status.md
2. Read previous session summaries (docs/implement/sprint-X/session-X-*.md)
3. Pick ONE task
4. Implement + Tests (70%+ coverage)
5. Run: ./gradlew test (must PASS)
6. Update daily-log.md
7. Commit (conventional format)
8. Update sprint status
9. Create session summary ONLY when user explicitly requests it
```

---

## ✅ Quality Gates (All Must Pass)

- [ ] Code compiles
- [ ] All tests PASS (./gradlew test)
- [ ] Coverage ≥ 70% (Services ≥ 80%)
- [ ] No secrets in code
- [ ] JavaDoc added
- [ ] daily-log.md updated
- [ ] Conventional commit

---

## 💻 Project Structure

> **Note**: For detailed coding standards, examples, and best practices, see `docs/context/CODING-STANDARDS.md`

### Backend (Spring Boot)

```
com.lexia.backend/
├── auth/              # JWT, Security filters
├── controller/        # REST API endpoints
├── service/           # Business logic
│   ├── impl/          # Service implementations
├── repository/        # JPA repositories
├── entity/            # JPA entities
├── dto/               # Data Transfer Objects
├── mapper/            # Entity-DTO mappers
├── config/            # Configuration classes
├── exception/         # Custom exceptions
├── validation/        # Custom validators
├── specification/     # JPA Specifications
├── util/              # Utility classes
├── converter/         # Custom converters
├── seeder/            # Database seeders
└── common/            # Shared utilities
```

### Frontend Web (Next.js)

```
lexia-web/
├── app/                    # Next.js App Router pages
├── components/            # React components
├── services/             # API service functions
├── store/                # Zustand stores
├── types/                # TypeScript types
├── lib/                  # Utilities, helpers
├── hooks/                # Custom React hooks
├── tests/                # Test files
└── public/               # Static assets
```

### Mobile (React Native + Expo)

```
lexia-mobile/
├── app/                    # Screen components
│   ├── auth/              # Auth screens
│   ├── tabs/              # Tab navigation screens
│   ├── courses/           # Course screens
│   └── lessons/           # Lesson screens
├── components/            # Reusable components
├── services/             # API service functions
├── store/                # Zustand stores
├── types/                # TypeScript types
├── assets/               # Static assets
├── App.tsx               # Root component
├── index.ts              # Entry point
└── app.json              # Expo configuration
```

---

## 🚫 Never Do

**Backend**:

- ❌ Store plain-text passwords/refresh tokens
- ❌ Log sensitive data (passwords, tokens)
- ❌ Skip tests (70% minimum)
- ❌ Use generic `Exception` catches

**Frontend (Web)**:

- ❌ Store passwords or sensitive data in localStorage
- ❌ Skip form validation (use Zod)
- ❌ Ignore error states (network, timeout, server)
- ❌ Forget loading states/skeletons
- ❌ Skip accessibility (ARIA, keyboard nav)
- ❌ Use `<img>` (use Next.js `<Image>`)

**Mobile**:

- ❌ Store sensitive data in plain AsyncStorage (except tokens)
- ❌ Skip platform-specific handling (iOS/Android)
- ❌ Ignore navigation type safety
- ❌ Forget error boundaries
- ❌ Skip accessibility labels
- ❌ Use inline styles without StyleSheet

**Both**:

- ❌ Edit `docs/context/` without approval
- ❌ Commit failing tests
- ❌ Work on multiple tasks at once

## ✅ Always Do

**Backend**:

- ✅ Bcrypt passwords (cost 12)
- ✅ Return JWT in response body
- ✅ Hash refresh tokens before DB (SHA-256)
- ✅ Validate all inputs
- ✅ Use DTOs for APIs

**Frontend (Web)**:

- ✅ localStorage for tokens (temporary, will migrate to httpOnly cookies)
- ✅ Validate forms (React Hook Form + Zod)
- ✅ Handle all errors (network, timeout, 500)
- ✅ Add retry logic (3 attempts)
- ✅ Test responsive (320px - 1920px)
- ✅ ARIA labels + keyboard nav
- ✅ TypeScript strict mode

**Mobile**:

- ✅ AsyncStorage for tokens with proper encryption consideration
- ✅ Validate all inputs (manual validation or libraries)
- ✅ Handle all errors (network, timeout, server)
- ✅ Add retry logic (3 attempts)
- ✅ Test on iOS & Android
- ✅ Accessibility labels (accessible, accessibilityLabel)
- ✅ TypeScript strict mode
- ✅ Use React Native Paper for consistent UI
- ✅ Type-safe navigation (React Navigation types)

**Both**:

- ✅ Update daily-log.md
- ✅ Conventional commits
- ✅ Tests pass (Backend: ./gradlew test, Frontend: npm test)

---

## 📌 Daily Checklist

**Start Session**:

1. Read `docs/plan/current-sprint-status.md`
2. Check `docs/implement/sprint-X/daily-log.md`
3. Pick next task

**During Dev**:

1. Code + Tests
2. **Backend**: `./gradlew test` | **Frontend Web**: `npm test` | **Mobile**: `npm test` (in lexia-mobile)
3. Update daily-log.md

**End Session**:

1. Commit changes (conventional format)
2. Update sprint status
3. Create session summary ONLY when user explicitly requests it

---

## 📊 Success Metrics

**Backend**:

- ✅ Tests pass (100%)
- ✅ Coverage ≥ 70% (Services ≥ 80%)
- ✅ API response < 500ms

**Frontend (Web)**:

- ✅ Tests pass (100%)
- ✅ Coverage ≥ 60% (Services ≥ 80%)
- ✅ Token management implemented correctly
- ✅ Responsive (320px - 1920px)
- ✅ WCAG AA compliant

**Mobile**:

- ✅ Tests pass (100%)
- ✅ Coverage ≥ 60% (Services ≥ 80%)
- ✅ Token management with AsyncStorage
- ✅ Works on iOS & Android
- ✅ Accessibility labels present

**Both**:

- ✅ No security issues
- ✅ Docs updated

---

## 📝 Session Documentation

### Session Summary Requirements

Each development session must create a comprehensive summary in `docs/implement/sprint-X/session-X-topic.md` with:

**Required Sections**:

1. **What We Accomplished** - Detailed task completion list
2. **Code Generated** - Files created/modified with LOC metrics
3. **Key Decisions** - Top 3 architectural decisions made
4. **Challenges Faced** - Problems and solutions implemented
5. **Quality Assessment** - 1-10 rating with detailed explanation
6. **Best Prompts Used** - Effective prompts for future reuse
7. **Next Steps** - Clear roadmap for subsequent session

**Why Required**:

- **Knowledge Preservation**: Document decisions and solutions for team reference
- **Progress Tracking**: Maintain clear development history
- **Quality Assurance**: Ensure comprehensive implementation coverage
- **Learning Tool**: Capture challenges and solutions for future sessions
- **Onboarding Aid**: Help new developers understand project evolution

---

## 🔍 Decision Framework

1. Check `docs/context/ARCHITECTURE.md` first
2. Security-first mindset
3. Keep it simple
4. Ask if unsure

---

## 💡 Key Principles

1. **Context First**: Read docs before coding
2. **Security First**: httpOnly cookies, Bcrypt, hash, validate
3. **Test-Driven**: Tests alongside code (TDD)
4. **User Experience**: Loading states, error handling, responsive
5. **Accessibility**: ARIA, keyboard nav, WCAG AA
6. **Document Daily**: Update logs every session
7. **Focus**: One task at a time

---

## 🔐 Security Checklist

**Backend**:

- [ ] Passwords Bcrypt (cost 12)
- [ ] JWT returned in response body
- [ ] Refresh tokens hashed in DB (SHA-256)
- [ ] All inputs validated
- [ ] No secrets in logs/code

**Frontend (Web)**:

- [ ] localStorage for tokens with Authorization header
- [ ] Clear tokens on logout
- [ ] Form validation (Zod)
- [ ] Error handling (network, timeout, 500)
- [ ] Retry logic (3 attempts)
- [ ] No sensitive data in client code

**Mobile**:

- [ ] AsyncStorage for tokens with Authorization header
- [ ] Clear tokens on logout (AsyncStorage.multiRemove)
- [ ] Input validation
- [ ] Error handling (network, timeout, 500)
- [ ] Retry logic (3 attempts)
- [ ] No sensitive data in client code

---

## ♿ Accessibility Checklist

- [ ] ARIA labels on interactive elements
- [ ] Semantic HTML (`<nav>`, `<main>`, `<article>`)
- [ ] Keyboard navigation (Tab, Enter, Escape)
- [ ] Focus indicators visible
- [ ] Color contrast ≥ 4.5:1 (WCAG AA)
- [ ] Error messages linked (`aria-describedby`)

---

## 📱 Responsive Checklist (Web)

- [ ] 320px - Mobile S (iPhone SE)
- [ ] 375px - Mobile M (iPhone 12/13)
- [ ] 768px - Tablet (iPad)
- [ ] 1024px - Desktop S
- [ ] 1280px - Desktop M (MacBook)
- [ ] 1920px - Desktop L (Full HD)
- [ ] No horizontal scroll
- [ ] Touch targets ≥ 44px

## 📱 Mobile Testing Checklist

- [ ] iOS Simulator (iPhone 14/15)
- [ ] Android Emulator (Pixel 6)
- [ ] Physical devices (if available)
- [ ] Portrait & Landscape orientations
- [ ] Touch targets ≥ 44px
- [ ] Safe area insets (notch/home indicator)
- [ ] Keyboard handling (avoid overlapping inputs)
- [ ] Pull-to-refresh (where applicable)
- [ ] Network error states (offline mode)

---

**Quick Start**:

1. `docs/plan/current-sprint-status.md`
2. Pick task
3. Code + Test + Document
4. Commit + Update
