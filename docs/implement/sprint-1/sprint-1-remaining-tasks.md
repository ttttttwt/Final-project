# Sprint 1 - Remaining Tasks Checklist

**Status**: 🔄 In Progress (85% Complete)  
**Last Updated**: October 25, 2025

---

## 🎯 Overview

Sprint 1 has successfully completed 11/13 tasks. Two major features remain:

1. **User Profile Management** - Full CRUD operations for user profiles
2. **API Documentation (Swagger)** - Complete OpenAPI documentation

**Estimated Time**: 8-10 hours total

- User Profile Management: 5-6 hours
- API Documentation: 3-4 hours

---

## ✅ Completed (11 tasks)

- [x] Project setup + Git infrastructure
- [x] Database schema migration
- [x] JPA entities (User, UserProfile, Role, UserRole, RefreshToken)
- [x] Repository interfaces
- [x] JWT token provider implementation
- [x] AuthService with BCrypt password hashing
- [x] User registration endpoint (POST /auth/register)
- [x] User login endpoint (POST /auth/login)
- [x] Token refresh endpoint (POST /auth/refresh)
- [x] Global exception handling
- [x] Comprehensive testing (71% coverage)

---

## 📋 Task 1: User Profile Management

**Priority**: HIGH  
**Status**: 🔵 Not Started  
**Estimated Time**: 5-6 hours

### 1.1 Service Layer (1.5 hours)

- [ ] **Create UserProfileService interface**

  - Method: `UserProfileDTO getProfile(Long userId)`
  - Method: `UserProfileDTO getCurrentUserProfile()` (from SecurityContext)
  - Method: `UserProfileDTO updateProfile(Long userId, UpdateProfileDTO dto)`
  - Method: `void updateAvatar(Long userId, String avatarUrl)`
  - Method: `void deleteAvatar(Long userId)`

- [ ] **Implement UserProfileServiceImpl**

  - Inject UserRepository, UserProfileRepository
  - Implement getProfile() - fetch user profile or throw UserNotFoundException
  - Implement getCurrentUserProfile() - get from SecurityContextHolder
  - Implement updateProfile() - validate + update fields
  - Implement updateAvatar() - update avatarUrl field
  - Implement deleteAvatar() - set avatarUrl to null
  - Add @Transactional annotation
  - Add proper logging

- [ ] **Add validation logic**
  - Phone number format: `^[+]?[0-9]{10,15}$` (optional)
  - Timezone: validate against ZoneId.getAvailableZoneIds()
  - Language: validate against ISO 639-1 codes (en, vi, es, fr, etc.)
  - Bio max length: 500 characters
  - Throw InvalidInputException for invalid data

### 1.2 DTOs & Mappers (45 minutes)

- [ ] **Create UserProfileDTO** (in `com.lexia.api.user.dto`)

  ```java
  - Long userId
  - String email
  - String firstName
  - String lastName
  - String bio (nullable)
  - String phoneNumber (nullable)
  - String avatarUrl (nullable)
  - String timezone (default: UTC)
  - String language (default: en)
  - Instant createdAt
  - Instant updatedAt
  ```

- [ ] **Create UpdateProfileDTO**

  ```java
  - @NotBlank String firstName
  - @NotBlank String lastName
  - @Size(max=500) String bio
  - @Pattern(regexp="^[+]?[0-9]{10,15}$") String phoneNumber
  - @NotBlank String timezone
  - @NotBlank String language
  ```

- [ ] **Create UserProfileMapper utility class**
  - Static method: `toDTO(UserProfile profile)`
  - Static method: `updateEntityFromDTO(UserProfile entity, UpdateProfileDTO dto)`

### 1.3 REST Controller (1 hour)

- [ ] **Create UserProfileController** (in `com.lexia.api.user`)

  - Base path: `/api/v1/users`
  - Inject UserProfileService
  - Add @RestController, @RequestMapping, @RequiredArgsConstructor

- [ ] **Implement endpoints**

  - GET `/profile` - Get current user's profile (200 OK)
  - PUT `/profile` - Update current user's profile (200 OK)
  - POST `/profile/avatar` - Upload avatar URL (200 OK)
  - DELETE `/profile/avatar` - Remove avatar (204 No Content)

- [ ] **Add security annotations**

  - @PreAuthorize("isAuthenticated()") on all methods
  - Extract userId from SecurityContext (JWT token)

- [ ] **Add validation & error handling**

  - @Valid on request bodies
  - Handle validation errors (GlobalExceptionHandler)
  - Return proper HTTP status codes

- [ ] **Add JavaDoc documentation**
  - Describe each endpoint purpose
  - Document request/response formats
  - List possible error responses

### 1.4 Security Configuration (30 minutes)

- [ ] **Update SecurityConfig**

  - Add `/api/v1/users/**` to protected paths
  - Ensure JWT filter processes these endpoints
  - Configure CORS for profile endpoints

- [ ] **Implement authorization logic**

  - Users can only access their own profile
  - Add check: `if (!userId.equals(currentUserId)) throw ForbiddenException()`

- [ ] **Add audit logging**
  - Log profile updates with userId, timestamp
  - Log avatar changes
  - Use SLF4J logger

### 1.5 Testing (2 hours)

- [ ] **Unit tests - UserProfileServiceTest**

  - Test getProfile() - success with valid userId
  - Test getProfile() - throws UserNotFoundException
  - Test getCurrentUserProfile() - success
  - Test updateProfile() - success with valid data
  - Test updateProfile() - throws InvalidInputException for bad data
  - Test updateAvatar() - success
  - Test deleteAvatar() - success
  - Mock UserRepository, UserProfileRepository
  - Use @ExtendWith(MockitoExtension.class)

- [ ] **Unit tests - UserProfileControllerTest**

  - Test GET /profile - 200 OK with valid token
  - Test GET /profile - 401 Unauthorized without token
  - Test PUT /profile - 200 OK with valid data
  - Test PUT /profile - 400 Bad Request with invalid data
  - Test POST /avatar - 200 OK
  - Test DELETE /avatar - 204 No Content
  - Use MockMvc, @WebMvcTest

- [ ] **Integration tests - UserProfileIntegrationTest**

  - Test complete flow: register → login → get profile → update profile
  - Test with actual JWT authentication
  - Verify database changes with JPA repositories
  - Use @SpringBootTest, TestRestTemplate

- [ ] **Run coverage report**
  - Execute: `./gradlew test jacocoTestReport`
  - Verify UserProfileService coverage ≥ 80%
  - Verify UserProfileController coverage ≥ 70%
  - Total coverage should remain ≥ 70%

---

## 📋 Task 2: API Documentation (Swagger)

**Priority**: HIGH  
**Status**: 🔵 Not Started  
**Estimated Time**: 3-4 hours

### 2.1 Swagger Setup (1 hour)

- [ ] **Add dependency to build.gradle**

  ```gradle
  implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
  ```

- [ ] **Create OpenApiConfig.java** (in `com.lexia.api.config`)

  ```java
  @Configuration
  @OpenAPIDefinition(
    info = @Info(
      title = "LEXIA API",
      version = "1.0.0",
      description = "AI English Learning Platform for Working Professionals",
      contact = @Contact(name = "LEXIA Team", email = "support@lexia.com"),
      license = @License(name = "MIT License")
    ),
    servers = {
      @Server(url = "http://localhost:8080", description = "Development Server")
    }
  )
  @SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
  )
  public class OpenApiConfig {
    // Additional configuration if needed
  }
  ```

- [ ] **Configure paths in application.properties**

  ```properties
  springdoc.api-docs.path=/api-docs
  springdoc.swagger-ui.path=/swagger-ui.html
  springdoc.swagger-ui.enabled=true
  springdoc.swagger-ui.operationsSorter=method
  ```

- [ ] **Update SecurityConfig**

  - Allow public access to `/api-docs/**` and `/swagger-ui/**`
  - Add to permitAll() list

- [ ] **Test access**
  - Start application
  - Visit: http://localhost:8080/swagger-ui.html
  - Verify UI loads correctly

### 2.2 Document Authentication Endpoints (1 hour)

- [ ] **Update AuthController with annotations**

- [ ] **Add class-level annotation**

  ```java
  @Tag(name = "Authentication", description = "User authentication and authorization endpoints")
  ```

- [ ] **Document POST /auth/register**

  ```java
  @Operation(
    summary = "Register new user",
    description = "Creates a new user account with email and password"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "User created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "409", description = "Email already exists")
  })
  ```

- [ ] **Document POST /auth/login**

  ```java
  @Operation(
    summary = "User login",
    description = "Authenticates user and returns JWT tokens"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Login successful"),
    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
    @ApiResponse(responseCode = "400", description = "Invalid input format")
  })
  ```

- [ ] **Document POST /auth/refresh**
  ```java
  @Operation(
    summary = "Refresh access token",
    description = "Generates new access token using refresh token"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Token refreshed"),
    @ApiResponse(responseCode = "401", description = "Invalid/expired refresh token")
  })
  @SecurityRequirement(name = "Bearer Authentication")
  ```

### 2.3 Document User Profile Endpoints (45 minutes)

- [ ] **Update UserProfileController with annotations**

- [ ] **Add class-level annotation**

  ```java
  @Tag(name = "User Profile", description = "User profile management endpoints")
  @SecurityRequirement(name = "Bearer Authentication")
  ```

- [ ] **Document GET /users/profile**

  ```java
  @Operation(
    summary = "Get current user profile",
    description = "Retrieves profile information for authenticated user"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Profile retrieved"),
    @ApiResponse(responseCode = "401", description = "Not authenticated"),
    @ApiResponse(responseCode = "404", description = "Profile not found")
  })
  ```

- [ ] **Document PUT /users/profile**

  ```java
  @Operation(
    summary = "Update user profile",
    description = "Updates profile fields for authenticated user"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Profile updated"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "401", description = "Not authenticated")
  })
  ```

- [ ] **Document avatar endpoints**
  - POST /profile/avatar - Upload avatar URL
  - DELETE /profile/avatar - Remove avatar

### 2.4 Add DTO Schema Documentation (45 minutes)

- [ ] **Update RegisterDTO**

  ```java
  @Schema(description = "User registration request")
  public class RegisterDTO {
    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Password (min 8 chars, 1 uppercase, 1 lowercase, 1 digit)",
            example = "SecurePass123")
    private String password;

    @Schema(description = "User first name", example = "John")
    private String firstName;

    @Schema(description = "User last name", example = "Doe")
    private String lastName;
  }
  ```

- [ ] **Update LoginDTO**

  ```java
  @Schema(description = "User login request")
  public class LoginDTO {
    @Schema(description = "User email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User password", example = "SecurePass123")
    private String password;
  }
  ```

- [ ] **Update UserProfileDTO**

  - Add @Schema annotations to all fields
  - Add example values

- [ ] **Update UpdateProfileDTO**

  - Add @Schema annotations with descriptions
  - Add validation constraints in descriptions

- [ ] **Update ErrorResponse**
  ```java
  @Schema(description = "Error response format")
  public class ErrorResponse {
    @Schema(description = "Error timestamp", example = "2025-10-25T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Validation Error")
    private String error;

    @Schema(description = "Error message", example = "Invalid input data")
    private String message;

    @Schema(description = "Request path", example = "/api/v1/auth/register")
    private String path;
  }
  ```

### 2.5 Testing & Validation (30 minutes)

- [ ] **Manual testing via Swagger UI**

  - Start application
  - Open http://localhost:8080/swagger-ui.html
  - Test POST /auth/register - create test user
  - Test POST /auth/login - get JWT token
  - Click "Authorize" button - paste access token
  - Test GET /users/profile - verify token works
  - Test PUT /users/profile - update profile
  - Verify all responses match documentation

- [ ] **Export OpenAPI specification**

  - Visit: http://localhost:8080/api-docs
  - Save JSON response as `openapi.json`
  - Store in `docs/api/openapi.json`
  - Optionally convert to YAML format

- [ ] **Create API documentation README**

  - Create `docs/api/README.md`
  - Add instructions: "How to access Swagger UI"
  - Add authentication flow example
  - Add common error codes table
  - Add link to OpenAPI spec file

- [ ] **Verify documentation completeness**
  - All endpoints documented
  - All request/response examples present
  - Security scheme configured
  - Error responses documented
  - DTOs have schema descriptions

---

## 🎯 Completion Checklist

### User Profile Management Complete When:

- [ ] All 5 subtasks (1.1-1.5) completed
- [ ] All endpoints functional and tested
- [ ] Tests pass: `./gradlew test` (100% pass rate)
- [ ] Coverage ≥ 70% overall, ≥ 80% for UserProfileService
- [ ] Security properly configured (JWT required)
- [ ] Code reviewed against CODE-STANDARDS.md
- [ ] JavaDoc added to all public methods
- [ ] Committed with message: `feat: implement user profile management API`

### API Documentation Complete When:

- [ ] All 6 subtasks (2.1-2.6) completed
- [ ] Swagger UI accessible at `/swagger-ui.html`
- [ ] All endpoints documented with examples
- [ ] JWT authentication scheme configured
- [ ] Manual tests pass via Swagger UI
- [ ] OpenAPI spec exported to `docs/api/openapi.json`
- [ ] API README created in `docs/api/README.md`
- [ ] Committed with message: `docs: add comprehensive Swagger API documentation`

### Sprint 1 Complete When:

- [ ] Both tasks above completed
- [ ] All tests pass: `./gradlew test`
- [ ] Coverage report ≥ 70%: `./gradlew jacocoTestReport`
- [ ] No compilation errors
- [ ] No security vulnerabilities
- [ ] daily-log.md updated
- [ ] Session summary created (session-5-profile-and-docs.md)
- [ ] SPRINT-1-SUMMARY.md updated
- [ ] current-sprint-status.md marked as complete

---

## 📝 Work Sessions Planning

### Session 1: User Profile Service & DTOs (2 hours)

1. Create UserProfileService interface
2. Implement UserProfileServiceImpl
3. Create DTOs (UserProfileDTO, UpdateProfileDTO)
4. Create UserProfileMapper
5. Write unit tests for service layer
6. **Output**: Service layer complete with 80%+ test coverage

### Session 2: User Profile Controller & Security (2 hours)

1. Create UserProfileController
2. Implement all CRUD endpoints
3. Add security configuration
4. Add authorization checks
5. Write controller unit tests
6. **Output**: REST API complete with tests

### Session 3: Integration Testing (1 hour)

1. Write integration tests
2. Test complete user flows
3. Verify database operations
4. Run full test suite
5. Generate coverage report
6. **Output**: All tests passing, 70%+ coverage

### Session 4: Swagger Configuration (1.5 hours)

1. Add SpringDoc dependency
2. Create OpenApiConfig
3. Update SecurityConfig for docs access
4. Test Swagger UI accessibility
5. Configure paths and settings
6. **Output**: Swagger UI working

### Session 5: API Documentation (1.5 hours)

1. Document AuthController endpoints
2. Document UserProfileController endpoints
3. Add DTO schema annotations
4. Add request/response examples
5. Test via Swagger UI
6. **Output**: Complete API documentation

### Session 6: Final Testing & Documentation (1 hour)

1. Manual testing via Swagger
2. Export OpenAPI spec
3. Create API README
4. Update all project docs
5. Create session summaries
6. Final commit
7. **Output**: Sprint 1 complete! 🎉

---

## 📊 Success Metrics

- **Code Quality**: All tests pass, 70%+ coverage
- **Security**: JWT properly configured, no vulnerabilities
- **Documentation**: Complete Swagger docs with examples
- **Standards**: Code follows CODE-STANDARDS.md
- **Performance**: API response < 500ms
- **User Experience**: Clear error messages, proper validation

---

## 🚀 Next Steps After Sprint 1

Once Sprint 1 is complete:

1. Create SPRINT-1-SUMMARY.md with all achievements
2. Update project-roadmap.md
3. Plan Sprint 2: Course/Lesson management API
4. Celebrate the milestone! 🎉

---

**Remember**:

- One task at a time
- Test as you go
- Update docs daily
- Commit frequently with conventional messages
- Ask for help if stuck

Good luck! 💪
