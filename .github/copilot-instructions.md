# GitHub Copilot Instructions - LEXIA Backend

## 🎯 Project Identity

**LEXIA** - AI English Learning Platform for Working Professionals  
**Stack**: Spring Boot 3.x | Java 17 | PostgreSQL | JWT | Gemini API

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

### Package Structure

```
com.lexia.api/
├── auth/         # JWT, login
├── user/         # User management
├── course/       # Courses/lessons
├── ai/           # Gemini integration
│   ├── roleplay/
│   ├── grammar/
│   └── flashcard/
├── common/       # Shared utilities
└── config/       # Spring config
```

### Security Rules (CRITICAL)

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

### JWT Token Handling

- Access: 15 min, Refresh: 7 days
- Hash tokens before DB (SHA-256)
- Family-based rotation for multi-device
- Never log tokens

### Gemini API Integration

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

### Testing Requirements

```java
// Unit Test
@Test
void testRegisterUser_WithValidEmail_Success() {
    // Arrange
    RegisterDTO dto = new RegisterDTO("test@lexia.com", "Pass123!");

    // Act
    User result = userService.register(dto);

    // Assert
    assertNotNull(result);
    assertTrue(passwordEncoder.matches("Pass123!", result.getPasswordHash()));
}

// Integration Test
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Test
void testLogin_WithValidCredentials_ReturnsToken() throws Exception {
    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"test@lexia.com\",\"password\":\"Pass123!\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists());
}
```

---

## 🚫 Never Do

- ❌ Edit `docs/context/` without approval
- ❌ Skip tests (70% minimum)
- ❌ Commit failing tests
- ❌ Log passwords/tokens/keys
- ❌ Store plain-text tokens
- ❌ Work on multiple tasks at once
- ❌ Use generic `Exception` catches

## ✅ Always Do

- ✅ Bcrypt passwords (cost 12)
- ✅ Hash tokens (SHA-256)
- ✅ Validate all inputs
- ✅ Use DTOs for APIs
- ✅ Handle Gemini timeouts
- ✅ Update daily-log.md
- ✅ Conventional commits

---

## 📌 Daily Checklist

**Start Session**:

1. Read `docs/plan/current-sprint-status.md`
2. Check `docs/implement/sprint-X/daily-log.md`
3. Pick next task

**During Dev**:

1. Code + Tests
2. `./gradlew test` (pass)
3. Update daily-log.md

**End Session**:

1. Commit changes
2. Update sprint status
3. Create session summary ONLY when user explicitly requests it

---

## 📊 Success Metrics

- ✅ Tests pass (100%)
- ✅ Coverage ≥ 70%
- ✅ No security issues
- ✅ API response < 500ms
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
2. **Security First**: Bcrypt, hash, validate
3. **Test-Driven**: Tests alongside code
4. **Document Daily**: Update logs every session
5. **Focus**: One task at a time

---

**Quick Start**:

1. `docs/plan/current-sprint-status.md`
2. Pick task
3. Code + Test + Document
4. Commit + Update
