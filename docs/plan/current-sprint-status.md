# LEXIA - Current Sprint Status

**Sprint**: 1 / 6  
**Duration**: October 16-25, 2025 (Extended)  
**Status**: ⏳ In Progress (91%)

## Sprint Goals

- [x] Environment setup complete
- [x] Database schema migration complete
- [x] JPA entities complete
- [x] Repository interfaces complete
- [x] JWT authentication complete
- [x] User registration API
- [x] User login API
- [x] Token refresh API
- [x] Unit tests (71% coverage - Target 70% ✅)
- [ ] User profile management API
- [ ] API documentation (Swagger)

## Story Breakdown

| Story                       | Status  | Owner | %Complete | Notes                                                                                          |
| --------------------------- | ------- | ----- | --------- | ---------------------------------------------------------------------------------------------- |
| Setup Spring Boot Project   | ✅ Done | You   | 100%      | All dependencies installed                                                                     |
| Database Schema Migration   | ✅ Done | You   | 100%      | Flyway scripts created and configured                                                          |
| JPA Entities                | ✅ Done | You   | 100%      | User, UserProfile, Role, UserRole, RefreshToken with validation & relationships                |
| Repository Interfaces       | ✅ Done | You   | 100%      | All repositories with custom queries for business logic                                        |
| Implement JWT Provider      | ✅ Done | You   | 100%      | JwtTokenProvider service with token generation, validation, and hashing                        |
| Implement AuthService       | ✅ Done | You   | 100%      | BCrypt password hashing (cost 12), register, login, token refresh with rotation                |
| User Registration Endpoint  | ✅ Done | You   | 100%      | POST /auth/register with validation, error handling, 201 Created response                      |
| Login Infrastructure        | ✅ Done | You   | 100%      | LoginDTO, LoginResponseDTO, AuthService.login() with JWT token generation                      |
| User Login Endpoint         | ✅ Done | You   | 100%      | POST /auth/login with JWT tokens (access 15min, refresh 7days), 200 OK response                |
| Token Refresh Endpoint      | ✅ Done | You   | 100%      | POST /auth/refresh with token rotation, family tracking, theft detection                       |
| Comprehensive Testing       | ✅ Done | You   | 100%      | 71% coverage achieved (AuthController, AuthService, GlobalExceptionHandler, PasswordValidator) |
| User Profile Management     | 🔵 Todo | You   | 40%       | Profile CRUD operations - See breakdown below                                                  |
| API Documentation (Swagger) | 🔵 Todo | You   | 0%        | OpenAPI 3.0 documentation - See breakdown below                                                |

---

## Task Breakdown: User Profile Management

### 1.1 User Profile Service Layer (Priority: HIGH) ✅ COMPLETE

- [x] Create `UserProfileService` interface
  - Define method signatures: `getProfile()`, `updateProfile()`, `updateAvatar()`, `deleteAvatar()`
- [x] Implement `UserProfileServiceImpl`
  - Get authenticated user's profile (from SecurityContext)
  - Update profile fields (firstName, lastName, bio, phoneNumber, timezone, language)
  - Validate input data
  - Handle avatar URL updates
  - Throw appropriate exceptions (UserNotFoundException, InvalidInputException)
- [x] Add business validation rules
  - Phone number format validation (optional field)
  - Timezone validation (valid IANA timezone)
  - Language code validation (ISO 639-1)
  - Bio max length: 500 characters

**Completed**: October 25, 2025
**Details**:

- Created database migration V3 with new profile fields
- Updated UserProfile entity with 6 new fields and validation
- Created UserNotFoundException and InvalidInputException
- Created UserProfileDTO and UpdateProfileDTO with validation
- Created ValidationUtils for business rule validation
- Implemented UserProfileService interface (5 methods)
- Implemented UserProfileServiceImpl with full business logic
- Updated GlobalExceptionHandler for new exceptions
- Build verification passed (zero compilation errors)

### 1.2 User Profile DTOs (Priority: HIGH) ✅ COMPLETE

- [x] Create `UserProfileDTO`
  - Fields: userId, email, firstName, lastName, bio, phoneNumber, avatarUrl, timezone, language, createdAt, updatedAt
  - Add validation annotations
- [x] Create `UpdateProfileDTO`
  - Fields: firstName, lastName, bio, phoneNumber, timezone, language
  - Add @NotBlank, @Size, @Pattern validations
- [x] Create `UserProfileMapper`
  - Map UserProfile entity to UserProfileDTO
  - Map UpdateProfileDTO to UserProfile entity

**Completed**: October 27, 2025
**Details**:

- Note: UserProfileDTO and UpdateProfileDTO were already created in Task 1.1
- Created UserProfileMapper.java utility class with 3 static methods
- Mapping methods: toDTO(), updateEntityFromDTO(), toEntity()
- Handles null safety and backward compatibility with fullName
- Created UserProfileMapperTest.java with 10 comprehensive unit tests
- All 89 tests pass (10 new mapper tests + 79 existing)
- UserProfileMapper: 100% coverage
- Refactored UserProfileServiceImpl to use centralized mapper
- Build verification passed (zero compilation errors)

### 1.3 User Profile REST Controller (Priority: HIGH)

- [ ] Create `UserProfileController` in `com.lexia.api.user` package
  - GET `/api/v1/users/profile` - Get current user profile
  - PUT `/api/v1/users/profile` - Update current user profile
  - POST `/api/v1/users/profile/avatar` - Upload avatar (future: multipart/form-data)
  - DELETE `/api/v1/users/profile/avatar` - Remove avatar
- [ ] Add @PreAuthorize annotations (require authentication)
- [ ] Add proper HTTP status codes (200 OK, 404 Not Found, 400 Bad Request)
- [ ] Add JavaDoc documentation

### 1.4 User Profile Security (Priority: HIGH)

- [ ] Configure JWT filter to work with profile endpoints
- [ ] Add security configuration for `/api/v1/users/**` paths
- [ ] Ensure users can only access/modify their own profile
- [ ] Add audit logging (who updated what, when)

### 1.5 User Profile Testing (Priority: CRITICAL) ✅ COMPLETE

- [x] Unit tests for `UserProfileService`
  - Test getProfile() - success case ✅
  - Test getProfile() - user not found ✅
  - Test getCurrentUserProfile() - success ✅
  - Test updateProfile() - success case ✅
  - Test updateProfile() - validation failures ✅
  - Test updateProfile() - unauthorized access ✅
- [ ] Unit tests for `UserProfileController` (Pending controller implementation)
  - Test GET /profile endpoint with valid token
  - Test GET /profile endpoint with invalid token
  - Test PUT /profile endpoint with valid data
  - Test PUT /profile endpoint with invalid data
  - Test avatar operations
- [ ] Integration tests (Pending controller implementation)
  - Test complete profile update flow
  - Test with actual JWT authentication
  - Verify database updates
- [x] Target: Maintain 70%+ coverage ✅ 75% achieved

**Completed**: October 27, 2025
**Details**:

- Created UserProfileServiceTest.java with 29 comprehensive unit tests
- All 79 tests pass (29 new + 50 existing)
- Coverage achieved: UserProfileServiceImpl 100%, ValidationUtils 84%, Overall 75%
- Test categories: success cases, validation, exceptions, edge cases
- Used Mockito for mocking, tested SecurityContext authentication
- Zero compilation errors, zero test failures

---

## Task Breakdown: API Documentation (Swagger)

### 2.1 Swagger Configuration (Priority: HIGH)

- [ ] Add SpringDoc OpenAPI dependency to `build.gradle`
  - `implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'`
- [ ] Create `OpenApiConfig.java` in `com.lexia.api.config` package
  - Configure OpenAPI info (title, version, description)
  - Add contact info (name, email)
  - Add license info (MIT License)
  - Configure server URLs (localhost:8080)
- [ ] Configure Swagger UI path: `/api-docs` and `/swagger-ui.html`

### 2.2 API Documentation - Authentication Endpoints (Priority: HIGH)

- [ ] Document `AuthController`
  - Add @Tag annotation: "Authentication API"
  - Add @Operation annotations for each endpoint
  - POST /auth/register
    - @ApiResponses: 201 Created, 400 Bad Request, 409 Conflict
    - Request body schema with examples
    - Response schema with examples
  - POST /auth/login
    - @ApiResponses: 200 OK, 401 Unauthorized, 400 Bad Request
    - Request/response examples
  - POST /auth/refresh
    - @ApiResponses: 200 OK, 401 Unauthorized
    - Security requirement: Bearer token

### 2.3 API Documentation - User Profile Endpoints (Priority: HIGH)

- [ ] Document `UserProfileController`
  - Add @Tag annotation: "User Profile API"
  - Add @Operation annotations for each endpoint
  - GET /users/profile
    - @ApiResponses: 200 OK, 401 Unauthorized, 404 Not Found
    - Security: JWT required
  - PUT /users/profile
    - @ApiResponses: 200 OK, 400 Bad Request, 401 Unauthorized
    - Request/response examples
  - Avatar endpoints documentation

### 2.4 DTO Schema Documentation (Priority: MEDIUM)

- [ ] Add @Schema annotations to all DTOs
  - RegisterDTO: Add field descriptions and examples
  - LoginDTO: Add field descriptions and examples
  - UserProfileDTO: Add field descriptions and examples
  - UpdateProfileDTO: Add field descriptions and examples
  - ErrorResponse: Add field descriptions
- [ ] Add @Schema(example = "...") for better documentation

### 2.5 Security Scheme Configuration (Priority: HIGH)

- [ ] Configure JWT Bearer authentication in OpenApiConfig
  - Add @SecurityScheme annotation
  - Type: HTTP Bearer
  - Scheme: bearer
  - Bearer format: JWT
- [ ] Add security requirements to protected endpoints
- [ ] Document token format and expiration

### 2.6 API Documentation Testing (Priority: MEDIUM)

- [ ] Verify Swagger UI is accessible at `/swagger-ui.html`
- [ ] Test all documented endpoints from Swagger UI
- [ ] Verify request/response examples are accurate
- [ ] Test authentication flow with JWT token
- [ ] Export OpenAPI JSON/YAML specification
- [ ] Create README section on how to access API docs

---

## Completion Criteria

**User Profile Management** is complete when:

- ✅ All 5 subtasks (1.1-1.5) are done
- ✅ Tests pass with 70%+ coverage
- ✅ All CRUD operations work correctly
- ✅ Security properly configured
- ✅ Code follows standards in CODE-STANDARDS.md

**API Documentation** is complete when:

- ✅ All 6 subtasks (2.1-2.6) are done
- ✅ Swagger UI accessible and functional
- ✅ All endpoints documented with examples
- ✅ JWT authentication documented
- ✅ OpenAPI spec exported

---

## Implementation Notes

### User Registration Endpoint Implementation ✅

- **Task**: Create REST controller endpoint for user registration with validation and error handling
- **Details**:
  - Created AuthController.java with POST /auth/register endpoint
  - Proper input validation using @Valid @RequestBody RegisterDTO
  - Comprehensive error handling for validation errors and business logic exceptions
  - Returns UserDTO in successful responses (201 Created)
  - Returns appropriate HTTP status codes (400 Bad Request for validation errors, 409 Conflict for duplicate emails)
  - Uses constructor injection following Spring Boot best practices
  - Added JavaDoc documentation for the endpoint
- **Endpoint Details**:
  - URL: POST /api/v1/auth/register
  - Request Body: RegisterDTO (email, password, firstName, lastName)
  - Response: 201 Created with UserDTO, or error responses
  - Validation: Email format, password strength, required fields
- **Error Handling**:
  - Validation errors return 400 with field-specific error messages
  - Duplicate email returns 409 Conflict with user-friendly message
  - Internal errors return 500 with generic message (security best practice)
- **Testing**: All tests pass, endpoint compiles successfully
- **Time Spent**: 25 minutes

### Enhanced Input Validation and Error Handling ✅

- **Task**: Add comprehensive input validation and centralized error handling
- **Details**:
  - Created ErrorResponse and ValidationError DTOs for consistent error formatting
  - Implemented GlobalExceptionHandler with @ControllerAdvice for centralized error management
  - Handles MethodArgumentNotValidException for @Valid validation errors with detailed field messages
  - Handles ConstraintViolationException for additional validation constraints
  - Custom exception handling for UserAlreadyExistsException (409 Conflict) and ResourceNotFoundException (404)
  - Authentication and access denied exception handling for future endpoints
  - Removed try-catch from AuthController since global handler manages all errors
  - Updated AuthService to throw UserAlreadyExistsException instead of generic IllegalArgumentException
  - Updated unit tests to expect the new exception types
- **Error Response Format**:
  - Consistent JSON structure with timestamp, status, error type, message, and path
  - Field-specific validation errors included for client-side error display
  - Security-conscious error messages (no sensitive data exposure)
- **Benefits**: Centralized error handling, consistent API responses, better client experience, improved maintainability
- **Testing**: All tests pass with updated exception handling
- **Time Spent**: 45 minutes

---
