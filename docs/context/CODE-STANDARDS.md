# LEXIA - Code Standards & Conventions

## Backend (Java/Spring Boot)

### Naming Conventions
- Classes: PascalCase (`UserService`, `JwtTokenProvider`)
- Methods: camelCase (`generateToken`, `validateUser`)
- Constants: UPPER_SNAKE_CASE (`JWT_EXPIRATION_MS`)
- Packages: lowercase with domain-driven structure
  ```
  com.lexia.api
  ├── auth
  ├── course
  ├── lesson
  ├── user
  └── ai
  ```

### Code Organization
```java
@Service
public class MyService {
    // 1. Constants
    private static final Logger LOG = LoggerFactory.getLogger(...);
    
    // 2. Autowired dependencies
    @Autowired
    private MyRepository repository;
    
    // 3. Lifecycle methods
    @PostConstruct
    public void init() { }
    
    // 4. Public methods
    public void publicMethod() { }
    
    // 5. Private helper methods
    private void privateHelper() { }
}
```

### Error Handling
✅ **DO:**
```java
try {
    // code
} catch (SpecificException e) {
    LOG.error("Specific error message", e);
    throw new CustomException("User-friendly message", e);
}
```

❌ **DON'T:**
```java
catch (Exception e) {
    e.printStackTrace();  // Never use this
}
```

### Security Practices
- ✅ Use Bcrypt for passwords
- ✅ Hash tokens before storing
- ✅ Validate all inputs
- ✅ Use HTTPS only
- ✅ Set JWT expiration
- ❌ Don't log sensitive data (tokens, passwords)
- ❌ Don't commit API keys to git

### Testing Requirements
- Minimum 70% code coverage
- Unit tests for services
- Integration tests for APIs
- Test naming: `test[Method][Scenario][Expected]`
  ```java
  @Test
  void testGenerateTokenWithValidUser_ReturnsValidToken() { }
  ```

### Documentation
- JavaDoc for public methods
- Meaningful variable names
- Comments for non-obvious logic
  ```java
  /**
   * Generates JWT access token for authenticated user.
   * 
   * @param userId user ID
   * @return JWT token valid for 15 minutes
   * @throws InvalidUserException if user not found
   */
  public String generateAccessToken(UUID userId) { }
  ```

## Frontend (TypeScript/React)

### Component Structure
```
components/
├── [FeatureName]/
│   ├── index.ts              # Barrel export
│   ├── [FeatureName].tsx     # Component
│   ├── [FeatureName].module.css
│   └── [FeatureName].test.tsx
```

### Naming
- Components: PascalCase (`UserProfile`, `RolePlayModal`)
- Files: kebab-case (`user-profile.tsx`)
- Functions/variables: camelCase

### Code Style
- Use TypeScript (no `any` type)
- Functional components with hooks
- Props interface for each component
- ESLint + Prettier configured

## Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

Examples:
```
feat(auth): implement JWT refresh token rotation
fix(role-play): handle Gemini API timeout gracefully
docs(api): add role-play endpoints documentation
chore(deps): upgrade Spring Boot to 3.3.1
test(grammar): add unit tests for sandbox service
```

## Code Review Checklist
- [ ] Follows coding standards
- [ ] Tests included (70%+ coverage)
- [ ] No security vulnerabilities
- [ ] Performance considered
- [ ] Error handling present
- [ ] Documentation complete
- [ ] No hardcoded values/secrets



