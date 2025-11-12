# GitHub Copilot Instructions - LEXIA

## 🎯 Project Identity

**LEXIA** - AI English Learning Platform for Working Professionals  
**Backend**: Spring Boot 3.x | Java 17 | PostgreSQL | JWT  
**Frontend**: Next.js 14+ | TypeScript | Tailwind CSS | Zustand

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
- `docs/context/CODE-STANDARDS.md` - Coding conventions
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

## 💻 Code Standards

### Backend (Spring Boot)

**Package Structure**:

```
com.lexia.api/
├── auth/         # JWT, login
├── user/         # User management
├── course/       # Courses/lessons
├── ai/           # Gemini integration
└── common/       # Shared utilities
```

**Security Rules (CRITICAL)**

```java
// ✅ DO
@PostMapping("/register")
public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterDTO dto) {
    String hashed = passwordEncoder.encode(dto.getPassword()); // Bcrypt cost 12
    User user = userService.create(dto.getEmail(), hashed);
    LOG.info("User registered: {}", user.getEmail());
    return ResponseEntity.ok(UserMapper.toDTO(user));
}

// ❌ NEVER
- Plain-text passwords/tokens
- Log sensitive data
- Expose entities directly
- Hardcode secrets
```

**JWT Token Handling**:

- Access: 15 min, Refresh: 7 days
- Set httpOnly cookies (HttpOnly; Secure; SameSite=Strict)
- Hash tokens before DB (SHA-256)
- Never log tokens

**Gemini API Integration**

```java
@Retryable(maxAttempts = 3)
public RolePlayDTO generateRolePlay(String context) {
    try {
        String response = geminiClient.generate(prompt);
        aiUsageLogRepository.save(new AIUsageLog(...)); // Track cost
        return parseResponse(response);
    } catch (TimeoutException e) {
        return getFallbackScenario(); // Always have fallback
    }
}
```

**Testing**: Unit + Integration, 70%+ coverage, Services ≥ 80%

---

### Frontend (Next.js)

**Project Structure**:

```
src/
├── app/           # Next.js App Router pages
├── components/    # React components
│   ├── ui/        # shadcn/ui components
│   ├── auth/      # Auth components
│   ├── courses/   # Course components
│   └── layout/    # Layout components
├── lib/           # Utilities, API client
├── services/      # API service functions
├── store/         # Zustand stores
└── types/         # TypeScript types
```

**Security Rules (CRITICAL)**:

```typescript
// ✅ DO: httpOnly cookies (XSS protected)
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  withCredentials: true, // Sends cookies automatically
});

// Backend sets: Set-Cookie: accessToken=...; HttpOnly; Secure; SameSite=Strict

// ❌ NEVER: localStorage for tokens (XSS vulnerable)
// ❌ localStorage.setItem("accessToken", token);
```

**API Error Handling**:

```typescript
// ✅ Comprehensive error handling
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Network errors
    if (error.code === "ERR_NETWORK") {
      toast.error("No internet connection");
      return Promise.reject({ code: "NETWORK" });
    }

    // Timeout
    if (error.code === "ECONNABORTED") {
      toast.error("Request timeout");
      return retryRequest(error.config, 3); // Retry 3 times
    }

    // Server errors
    if (error.response?.status >= 500) {
      toast.error("Server error");
      return retryRequest(error.config, 3);
    }

    // 401 - Refresh token
    if (error.response?.status === 401) {
      return handleTokenRefresh(error);
    }

    return Promise.reject(error);
  }
);
```

**Component Standards**:

```typescript
// ✅ DO: Type-safe components with validation
interface LoginFormProps {
  onSuccess: () => void;
}

const loginSchema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(8, "Min 8 characters"),
});

export function LoginForm({ onSuccess }: LoginFormProps) {
  const form = useForm<z.infer<typeof loginSchema>>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: z.infer<typeof loginSchema>) => {
    try {
      await authService.login(data);
      toast.success("Login successful");
      onSuccess();
    } catch (error) {
      toast.error(error.message);
    }
  };

  return <form onSubmit={form.handleSubmit(onSubmit)}>...</form>;
}
```

**Responsive Design**:

```typescript
// ✅ Mobile-first, test all breakpoints
// 320px (Mobile S), 375px (Mobile M), 768px (Tablet)
// 1024px (Desktop S), 1280px (Desktop M), 1920px (Desktop L)

<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  {/* Mobile: 1 column, Tablet: 2 columns, Desktop: 3 columns */}
</div>
```

**Accessibility (WCAG AA)**:

```typescript
// ✅ ARIA labels, keyboard nav, contrast
<button aria-label="Close dialog" onClick={onClose}>
  <X className="h-4 w-4" aria-hidden="true" />
</button>

<input
  aria-describedby="email-error"
  aria-invalid={errors.email ? "true" : "false"}
/>
{errors.email && <span id="email-error">{errors.email.message}</span>}
```

**Testing (Jest + RTL)**:

```typescript
// ✅ 60%+ coverage (global), 80%+ (services)
describe("LoginForm", () => {
  it("validates email format", async () => {
    render(<LoginForm onSuccess={jest.fn()} />);

    const emailInput = screen.getByLabelText(/email/i);
    await userEvent.type(emailInput, "invalid");

    const submitBtn = screen.getByRole("button", { name: /login/i });
    await userEvent.click(submitBtn);

    expect(screen.getByText(/invalid email/i)).toBeInTheDocument();
  });
});
```

---

## 🚫 Never Do

**Backend**:

- ❌ Store plain-text passwords/tokens
- ❌ Log sensitive data
- ❌ Skip tests (70% minimum)
- ❌ Use generic `Exception` catches

**Frontend**:

- ❌ Store tokens in localStorage (use httpOnly cookies)
- ❌ Skip form validation (use Zod)
- ❌ Ignore error states (network, timeout, server)
- ❌ Forget loading states/skeletons
- ❌ Skip accessibility (ARIA, keyboard nav)
- ❌ Use `<img>` (use Next.js `<Image>`)

**Both**:

- ❌ Edit `docs/context/` without approval
- ❌ Commit failing tests
- ❌ Work on multiple tasks at once

## ✅ Always Do

**Backend**:

- ✅ Bcrypt passwords (cost 12)
- ✅ Set httpOnly cookies for JWT
- ✅ Hash tokens before DB (SHA-256)
- ✅ Validate all inputs
- ✅ Use DTOs for APIs

**Frontend**:

- ✅ httpOnly cookies (not localStorage)
- ✅ Validate forms (React Hook Form + Zod)
- ✅ Handle all errors (network, timeout, 500)
- ✅ Add retry logic (3 attempts)
- ✅ Test responsive (320px - 1920px)
- ✅ ARIA labels + keyboard nav
- ✅ TypeScript strict mode

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
2. **Backend**: `./gradlew test` | **Frontend**: `npm test`
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

**Frontend**:

- ✅ Tests pass (100%)
- ✅ Coverage ≥ 60% (Services ≥ 80%)
- ✅ No XSS vulnerabilities (httpOnly cookies)
- ✅ Responsive (320px - 1920px)
- ✅ WCAG AA compliant

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
- [ ] JWT in httpOnly cookies (HttpOnly; Secure; SameSite=Strict)
- [ ] Tokens hashed in DB (SHA-256)
- [ ] All inputs validated
- [ ] No secrets in logs/code

**Frontend**:

- [ ] httpOnly cookies (NOT localStorage)
- [ ] axios withCredentials: true
- [ ] Form validation (Zod)
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

## 📱 Responsive Checklist

- [ ] 320px - Mobile S (iPhone SE)
- [ ] 375px - Mobile M (iPhone 12/13)
- [ ] 768px - Tablet (iPad)
- [ ] 1024px - Desktop S
- [ ] 1280px - Desktop M (MacBook)
- [ ] 1920px - Desktop L (Full HD)
- [ ] No horizontal scroll
- [ ] Touch targets ≥ 44px

---

**Quick Start**:

1. `docs/plan/current-sprint-status.md`
2. Pick task
3. Code + Test + Document
4. Commit + Update
