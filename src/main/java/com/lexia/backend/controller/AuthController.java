package com.lexia.backend.controller;

import com.lexia.backend.auth.AuthService;
import com.lexia.backend.auth.AuthenticatedUserDetails;
import com.lexia.backend.dto.ChangePasswordDTO;
import com.lexia.backend.dto.LoginDTO;
import com.lexia.backend.dto.LoginResponseDTO;
import com.lexia.backend.dto.LogoutResponseDTO;
import com.lexia.backend.dto.RefreshTokenDTO;
import com.lexia.backend.dto.RefreshTokenResponseDTO;
import com.lexia.backend.dto.RegisterDTO;
import com.lexia.backend.dto.UserDTO;
import com.lexia.backend.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller for LEXIA.
 * Handles user registration, login, and token management endpoints.
 *
 * Base path: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Endpoints for user authentication, registration, and token management")
public class AuthController {

  private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * Register a new user account.
   *
   * @param registerDTO the registration data
   * @return ResponseEntity with user data or error message
   */
  @Operation(summary = "Register a new user", description = "Creates a new user account with email, password, and full name. "
      +
      "Password must be at least 8 characters and contain both letters and numbers. " +
      "Email must be unique.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration information", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RegisterDTO.class), examples = @ExampleObject(name = "Registration Example", value = """
          {
            "email": "john.doe@example.com",
            "password": "SecurePass123",
            "confirmPassword": "SecurePass123",
            "fullName": "John Doe"
          }
          """))))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDTO.class), examples = @ExampleObject(name = "Success Response", value = """
          {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "email": "john.doe@example.com",
            "authProvider": "LOCAL",
            "isActive": true,
            "createdAt": "2025-10-28T19:30:00",
            "updatedAt": "2025-10-28T19:30:00",
            "fullName": "John Doe",
            "avatarUrl": null,
            "currentLevel": "BEGINNER",
            "learningGoal": null
          }
          """))),
      @ApiResponse(responseCode = "400", description = "Invalid input data - validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Validation Error", value = """
          {
            "timestamp": "2025-10-28T19:30:00",
            "status": 400,
            "error": "Bad Request",
            "message": "Validation failed",
            "path": "/api/v1/auth/register",
            "errors": [
              {
                "field": "email",
                "message": "Email must be valid"
              },
              {
                "field": "password",
                "message": "Password must contain at least one letter and one number"
              }
            ]
          }
          """))),
      @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Conflict Error", value = """
          {
            "timestamp": "2025-10-28T19:30:00",
            "status": 409,
            "error": "Conflict",
            "message": "User with this email already exists",
            "path": "/api/v1/auth/register"
          }
          """)))
  })
  @PostMapping("/register")
  public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterDTO registerDTO) {
    LOG.info("Received registration request for email: {}", registerDTO.getEmail());

    // Register the user using AuthService
    User registeredUser = authService.register(registerDTO);

    // Convert to DTO for response (excludes sensitive data)
    UserDTO userDTO = UserDTO.fromEntity(registeredUser);

    LOG.info("User registered successfully with ID: {}", registeredUser.getId());

    // Return 201 Created with user data
    return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
  }

  /**
   * Authenticate user and generate JWT tokens.
   *
   * @param loginDTO the login credentials (email and password)
   * @return ResponseEntity with access token, refresh token, and user data
   */
  @Operation(summary = "Authenticate user", description = "Authenticates a user with email and password. " +
      "Returns JWT access token (valid for 15 minutes) and refresh token (valid for 7 days). " +
      "Use the access token in Authorization header as 'Bearer <token>' for protected endpoints.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User login credentials", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoginDTO.class), examples = @ExampleObject(name = "Login Example", value = """
          {
            "email": "john.doe@example.com",
            "password": "SecurePass123"
          }
          """))))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login successful - returns JWT tokens and user data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoginResponseDTO.class), examples = @ExampleObject(name = "Success Response", value = """
          {
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "user": {
              "id": "550e8400-e29b-41d4-a716-446655440000",
              "email": "john.doe@example.com",
              "authProvider": "LOCAL",
              "isActive": true,
              "createdAt": "2025-10-28T19:30:00",
              "updatedAt": "2025-10-28T19:30:00",
              "fullName": "John Doe",
              "avatarUrl": null,
              "currentLevel": "BEGINNER",
              "learningGoal": null
            },
            "tokenType": "Bearer",
            "expiresIn": 900000
          }
          """))),
      @ApiResponse(responseCode = "400", description = "Invalid input data - validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Validation Error", value = """
          {
            "timestamp": "2025-10-28T19:30:00",
            "status": 400,
            "error": "Bad Request",
            "message": "Validation failed",
            "path": "/api/v1/auth/login",
            "errors": [
              {
                "field": "email",
                "message": "Email must be valid"
              }
            ]
          }
          """))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials - email or password is incorrect", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Authentication Error", value = """
          {
            "timestamp": "2025-10-28T19:30:00",
            "status": 401,
            "error": "Unauthorized",
            "message": "Invalid email or password",
            "path": "/api/v1/auth/login"
          }
          """)))
  })
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody LoginDTO loginDTO) {
    LOG.info("Received login request for email: {}", loginDTO.getEmail());

    // Authenticate user and generate tokens
    LoginResponseDTO loginResponse = authService.login(loginDTO);

    LOG.info("User logged in successfully: {}", loginDTO.getEmail());

    // Return 200 OK with tokens and user data
    return ResponseEntity.ok(loginResponse);
  }

  /**
   * Refresh access token using a valid refresh token.
   * Implements token rotation for security.
   *
   * @param refreshTokenDTO the refresh token request
   * @return ResponseEntity with new access token and new refresh token
   */
  @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token. "
      +
      "Implements token rotation: old refresh token is revoked and a new one is issued. " +
      "Both the old refresh token and family tokens are invalidated if suspicious activity is detected. "
      +
      "New access token is valid for 15 minutes, new refresh token is valid for 7 days.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Refresh token from login response", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RefreshTokenDTO.class), examples = @ExampleObject(name = "Refresh Token Example", value = """
          {
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
          }
          """))))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Token refresh successful - returns new access and refresh tokens", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RefreshTokenResponseDTO.class), examples = @ExampleObject(name = "Success Response", value = """
          {
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "tokenType": "Bearer",
            "expiresIn": 900000
          }
          """))),
      @ApiResponse(responseCode = "401", description = "Invalid, expired, or revoked refresh token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Invalid Token Error", value = """
          {
            "timestamp": "2025-10-28T19:30:00",
            "status": 401,
            "error": "Unauthorized",
            "message": "Invalid or expired refresh token",
            "path": "/api/v1/auth/refresh"
          }
          """)))
  })
  @PostMapping("/refresh")
  public ResponseEntity<RefreshTokenResponseDTO> refreshToken(
      @Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
    LOG.info("Received token refresh request");

    // Refresh access token and rotate refresh token
    RefreshTokenResponseDTO refreshResponse = authService.refreshToken(refreshTokenDTO);

    LOG.info("Access token refreshed successfully");

    // Return 200 OK with new tokens
    return ResponseEntity.ok(refreshResponse);
  }

  /**
   * Logout endpoint that invalidates all active refresh tokens for the
   * authenticated user.
   *
   * @param authorizationHeader the Authorization header containing the bearer
   *                            access token
   * @return confirmation message
   */
  @Operation(summary = "Logout user", description = "Revokes all active refresh tokens for the authenticated user. "
      +
      "Requires a valid access token in the Authorization header. " +
      "After logout, all refresh tokens associated with the user are invalidated " +
      "and must be reissued by logging in again.", security = {
          @SecurityRequirement(name = "bearerAuth") })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Logout successful - refresh tokens invalidated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LogoutResponseDTO.class), examples = @ExampleObject(name = "Success Response", value = """
          {
            "message": "Logged out successfully"
          }
          """))),
      @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Invalid Token Error", value = """
          {
            "timestamp": "2025-10-28T19:30:00",
            "status": 401,
            "error": "Invalid Token",
            "message": "Invalid or expired access token",
            "path": "/api/v1/auth/logout"
          }
          """)))
  })
  @PostMapping("/logout")
  public ResponseEntity<LogoutResponseDTO> logoutUser(
      @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
    LOG.info("Received logout request");

    authService.logout(authorizationHeader);

    LogoutResponseDTO response = LogoutResponseDTO.builder()
        .message("Logged out successfully")
        .build();

    LOG.info("User logged out successfully");
    return ResponseEntity.ok(response);
  }

  /**
   * Change password for authenticated user.
   * Requires valid current password and validates new password strength.
   *
   * @param changePasswordDTO the password change data
   * @param userDetails       the authenticated user
   * @return confirmation message
   */
  @Operation(summary = "Change password", description = "Changes the password for the authenticated user. "
      +
      "Requires the current password for verification. " +
      "New password must be at least 8 characters and contain uppercase, lowercase, and number. " +
      "After password change, all refresh tokens are revoked (user must re-login on all devices).", security = {
          @SecurityRequirement(name = "bearerAuth") }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Password change data", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChangePasswordDTO.class), examples = @ExampleObject(name = "Change Password Example", value = """
              {
                "currentPassword": "OldPassword123",
                "newPassword": "NewSecurePass456",
                "confirmNewPassword": "NewSecurePass456"
              }
              """))))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Password changed successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LogoutResponseDTO.class), examples = @ExampleObject(name = "Success Response", value = """
          {
            "message": "Password changed successfully. Please login again."
          }
          """))),
      @ApiResponse(responseCode = "400", description = "Invalid input data - validation errors or password mismatch", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Validation Error", value = """
          {
            "timestamp": "2025-10-28T19:30:00",
            "status": 400,
            "error": "Bad Request",
            "message": "Current password is incorrect",
            "path": "/api/v1/auth/change-password"
          }
          """))),
      @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Authentication Error", value = """
          {
            "timestamp": "2025-10-28T19:30:00",
            "status": 401,
            "error": "Unauthorized",
            "message": "Authentication required",
            "path": "/api/v1/auth/change-password"
          }
          """)))
  })
  @PostMapping("/change-password")
  public ResponseEntity<LogoutResponseDTO> changePassword(
      @Valid @RequestBody ChangePasswordDTO changePasswordDTO,
      @AuthenticationPrincipal AuthenticatedUserDetails userDetails) {
    LOG.info("Received change password request for user: {}", userDetails.getUsername());

    authService.changePassword(userDetails.getUser().getId(), changePasswordDTO);

    LogoutResponseDTO response = LogoutResponseDTO.builder()
        .message("Password changed successfully. Please login again.")
        .build();

    LOG.info("Password changed successfully for user: {}", userDetails.getUsername());
    return ResponseEntity.ok(response);
  }
}