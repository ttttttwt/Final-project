package com.lexia.backend.auth;

import com.lexia.backend.dto.ChangePasswordDTO;
import com.lexia.backend.dto.ForgotPasswordDTO;
import com.lexia.backend.dto.LoginDTO;
import com.lexia.backend.dto.LoginResponseDTO;
import com.lexia.backend.dto.RefreshTokenDTO;
import com.lexia.backend.dto.RefreshTokenResponseDTO;
import com.lexia.backend.dto.RegisterDTO;
import com.lexia.backend.dto.ResetPasswordDTO;
import com.lexia.backend.dto.UserDTO;
import com.lexia.backend.entity.PasswordResetToken;
import com.lexia.backend.entity.RefreshToken;
import com.lexia.backend.entity.Role;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.entity.UserRole;
import com.lexia.backend.exception.InvalidPasswordException;
import com.lexia.backend.exception.InvalidTokenException;
import com.lexia.backend.exception.UserAlreadyExistsException;
import com.lexia.backend.repository.PasswordResetTokenRepository;
import com.lexia.backend.repository.RefreshTokenRepository;
import com.lexia.backend.repository.RoleRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.AuthEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentication Service for LEXIA.
 * Handles user registration, login, and token management.
 *
 * Security features:
 * - BCrypt password hashing with cost factor 12
 * - Comprehensive input validation
 * - Transactional operations for data consistency
 * - Proper error handling and logging
 */
@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthEmailService authEmailService;

    @Value("${lexia.auth.password-reset.token-expiry-hours:1}")
    private int passwordResetTokenExpiryHours;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            AuthEmailService authEmailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authEmailService = authEmailService;
        // BCrypt with cost factor 12 for strong password hashing
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    /**
     * Registers a new user account.
     *
     * @param registerDTO the registration data
     * @return the created user entity (without sensitive data)
     * @throws UserAlreadyExistsException if email already exists
     */
    @Transactional
    public User register(RegisterDTO registerDTO) {
        LOG.info("Attempting to register user with email: {}", registerDTO.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            LOG.warn("Registration failed: Email already exists: {}", registerDTO.getEmail());
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Hash the password using BCrypt with cost factor 12
        String hashedPassword = passwordEncoder.encode(registerDTO.getPassword());
        LOG.debug("Password hashed successfully for user: {}", registerDTO.getEmail());

        // Create user entity
        User user = User.builder()
                .email(registerDTO.getEmail())
                .passwordHash(hashedPassword)
                .authProvider(User.AuthProvider.EMAIL)
                .isActive(true)
                .build();

        // Create user profile
        UserProfile profile = UserProfile.builder()
                .fullName(registerDTO.getFullName())
                .user(user)
                .build();
        user.setProfile(profile);

        // Assign default LEARNER role
        Optional<Role> learnerRole = roleRepository.findByName("LEARNER");
        if (learnerRole.isPresent()) {
            UserRole userRole = new UserRole(user, learnerRole.get());
            user.getUserRoles().add(userRole);
            LOG.debug("Assigned LEARNER role to user: {}", registerDTO.getEmail());
        } else {
            LOG.warn("LEARNER role not found in database. User registered without role assignment.");
        }

        // Save user (cascades to profile and roles)
        User savedUser = userRepository.save(user);
        LOG.info("User registered successfully: {}", savedUser.getEmail());

        return savedUser;
    }

    /**
     * Validates a password against its hash.
     * Used during login authentication.
     *
     * @param rawPassword    the plain text password
     * @param hashedPassword the BCrypt hashed password
     * @return true if password matches, false otherwise
     */
    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Authenticates a user by email and password.
     * Uses findByEmailWithRoles to eagerly fetch roles and profile
     * to avoid LazyInitializationException when building UserDTO.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return Optional containing the user if authentication successful
     */
    public Optional<User> authenticateUser(String email, String password) {
        LOG.debug("Attempting to authenticate user: {}", email);

        // Use findByEmailWithRoles to eagerly fetch roles and profile
        Optional<User> userOpt = userRepository.findByEmailWithRoles(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check if user is active
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                LOG.warn("Authentication failed: User account is inactive: {}", email);
                return Optional.empty();
            }
            if (validatePassword(password, user.getPasswordHash())) {
                LOG.info("User authenticated successfully: {}", email);
                return Optional.of(user);
            } else {
                LOG.warn("Authentication failed: Invalid password for user: {}", email);
            }
        } else {
            LOG.warn("Authentication failed: User not found: {}", email);
        }

        return Optional.empty();
    }

    /**
     * Handles user login flow.
     * Authenticates user credentials and generates JWT tokens.
     *
     * @param loginDTO the login credentials
     * @return LoginResponseDTO containing access token, refresh token, and user
     *         data
     * @throws UserAlreadyExistsException if user not found or credentials invalid
     */
    @Transactional
    public LoginResponseDTO login(LoginDTO loginDTO) {
        LOG.info("Attempting to login user with email: {}", loginDTO.getEmail());

        // Authenticate user
        Optional<User> userOpt = authenticateUser(loginDTO.getEmail(), loginDTO.getPassword());
        if (userOpt.isEmpty()) {
            LOG.warn("Login failed: Invalid credentials for email: {}", loginDTO.getEmail());
            throw new UserAlreadyExistsException("Invalid email or password");
        }

        User user = userOpt.get();

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        String refreshTokenHash = jwtTokenProvider.hashToken(refreshToken);

        // Store refresh token in database
        RefreshToken storedRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenHash)
                .family(UUID.randomUUID().toString()) // Token family for rotation
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(storedRefreshToken);
        LOG.debug("Refresh token stored for user: {}", loginDTO.getEmail());

        // Build response
        LoginResponseDTO response = LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserDTO.fromEntity(user))
                .tokenType("Bearer")
                .expiresIn(ACCESS_TOKEN_EXPIRATION_MS)
                .build();

        LOG.info("User logged in successfully: {}", loginDTO.getEmail());
        return response;
    }

    /**
     * Refreshes access token using a valid refresh token.
     * Implements token rotation: invalidates old refresh token and creates a new
     * one.
     *
     * @param refreshTokenDTO the refresh token request
     * @return RefreshTokenResponseDTO containing new access token and new refresh
     *         token
     * @throws InvalidTokenException if refresh token is invalid, expired, or
     *                               revoked
     */
    @Transactional
    public RefreshTokenResponseDTO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        String refreshTokenValue = refreshTokenDTO.getRefreshToken();
        LOG.info("Attempting to refresh access token");

        if (!StringUtils.hasText(refreshTokenValue)) {
            LOG.warn("Invalid refresh token: value missing or blank");
            throw new InvalidTokenException("Refresh token is required");
        }

        // Validate refresh token format
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            LOG.warn("Invalid refresh token format");
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        // Hash the refresh token to look it up in database
        String tokenHash = jwtTokenProvider.hashToken(refreshTokenValue);

        // Find refresh token in database
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    LOG.warn("Refresh token not found in database");
                    return new InvalidTokenException("Invalid refresh token");
                });

        // Check if token is revoked
        if (storedToken.isRevoked()) {
            LOG.warn("Refresh token is revoked. Possible token theft detected. Invalidating token family: {}",
                    storedToken.getFamily());
            // Revoke all tokens in the same family (security measure against token theft)
            refreshTokenRepository.deleteByFamily(storedToken.getFamily());
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        // Check if token is expired
        if (storedToken.isExpired()) {
            LOG.warn("Refresh token is expired");
            throw new InvalidTokenException("Refresh token has expired");
        }

        // Get user from database
        User user = storedToken.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            LOG.warn("User account is inactive: {}", user.getEmail());
            throw new InvalidTokenException("User account is inactive");
        }

        // Revoke old refresh token
        refreshTokenRepository.revokeToken(tokenHash, LocalDateTime.now());
        LOG.debug("Old refresh token revoked for user: {}", user.getEmail());

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());

        // Generate new refresh token (token rotation)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        String newRefreshTokenHash = jwtTokenProvider.hashToken(newRefreshToken);

        // Store new refresh token in database (same family for rotation tracking)
        RefreshToken newStoredToken = RefreshToken.builder()
                .user(user)
                .tokenHash(newRefreshTokenHash)
                .family(storedToken.getFamily()) // Keep same family for rotation tracking
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newStoredToken);
        LOG.debug("New refresh token stored for user: {}", user.getEmail());

        // Build response
        RefreshTokenResponseDTO response = RefreshTokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(ACCESS_TOKEN_EXPIRATION_MS)
                .build();

        LOG.info("Access token refreshed successfully for user: {}", user.getEmail());
        return response;
    }

    /**
     * Logs out the authenticated user by revoking all active refresh tokens
     * associated with the provided access token. Requires a valid Authorization
     * header in the format "Bearer <access_token>".
     *
     * @param authorizationHeader the Authorization header containing the bearer
     *                            token
     * @throws InvalidTokenException if the header is missing, malformed, or the
     *                               token is invalid
     */
    @Transactional
    public void logout(String authorizationHeader) {
        LOG.info("Attempting to log out user");

        if (!StringUtils.hasText(authorizationHeader)) {
            LOG.warn("Logout failed: Missing Authorization header");
            throw new InvalidTokenException("Authorization header is required");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            LOG.warn("Logout failed: Authorization header does not start with Bearer");
            throw new InvalidTokenException("Authorization header must start with 'Bearer '");
        }

        String accessToken = authorizationHeader.substring(7).trim();

        if (!StringUtils.hasText(accessToken)) {
            LOG.warn("Logout failed: Access token missing in Authorization header");
            throw new InvalidTokenException("Access token is required");
        }

        if (!jwtTokenProvider.validateToken(accessToken)) {
            LOG.warn("Logout failed: Invalid access token");
            throw new InvalidTokenException("Invalid or expired access token");
        }

        UUID userId;

        try {
            userId = jwtTokenProvider.getUserIdFromToken(accessToken);
        } catch (IllegalArgumentException ex) {
            LOG.warn("Logout failed: Unable to parse user ID from token");
            throw new InvalidTokenException("Invalid access token", ex);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    LOG.warn("Logout failed: User not found for token subject: {}", userId);
                    return new InvalidTokenException("User associated with token does not exist");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            LOG.warn("Logout failed: User {} is inactive", user.getEmail());
            throw new InvalidTokenException("User account is inactive");
        }

        refreshTokenRepository.deleteByUserId(userId);
        LOG.info("User {} logged out successfully. All refresh tokens revoked.", user.getEmail());
    }

    /**
     * Changes the password for an authenticated user.
     * Validates current password, ensures new password meets requirements,
     * and revokes all refresh tokens for security.
     *
     * @param userId            the authenticated user's ID
     * @param changePasswordDTO the password change request
     * @throws InvalidTokenException      if user not found or inactive
     * @throws UserAlreadyExistsException if current password is incorrect
     * @throws IllegalArgumentException   if new password validation fails
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordDTO changePasswordDTO) {
        LOG.info("Attempting to change password for user: {}", userId);

        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    LOG.warn("Change password failed: User not found: {}", userId);
                    return new InvalidTokenException("User not found");
                });

        // Check if user is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            LOG.warn("Change password failed: User account is inactive: {}", user.getEmail());
            throw new InvalidTokenException("User account is inactive");
        }

        // Validate current password
        if (!validatePassword(changePasswordDTO.getCurrentPassword(), user.getPasswordHash())) {
            LOG.warn("Change password failed: Invalid current password for user: {}", user.getEmail());
            throw new InvalidPasswordException("Current password is incorrect");
        }

        // Validate new password != current password
        if (changePasswordDTO.getCurrentPassword().equals(changePasswordDTO.getNewPassword())) {
            LOG.warn("Change password failed: New password same as current for user: {}", user.getEmail());
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Validate new password matches confirmation
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmNewPassword())) {
            LOG.warn("Change password failed: Password confirmation mismatch for user: {}", user.getEmail());
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Hash the new password using BCrypt with cost factor 12
        String newHashedPassword = passwordEncoder.encode(changePasswordDTO.getNewPassword());
        LOG.debug("New password hashed successfully for user: {}", user.getEmail());

        // Update user's password
        user.setPasswordHash(newHashedPassword);
        userRepository.save(user);
        LOG.info("Password updated successfully for user: {}", user.getEmail());

        // Revoke all refresh tokens for security (force re-login on all devices)
        refreshTokenRepository.deleteByUserId(userId);
        LOG.info("All refresh tokens revoked for user: {} after password change", user.getEmail());

        // Send password changed notification email
        authEmailService.sendPasswordChangedEmail(user);
    }

    // ========== Password Reset Methods ==========

    /**
     * Initiates password reset by generating a token and sending reset email.
     * For security, always returns success even if email doesn't exist.
     *
     * @param forgotPasswordDTO the forgot password request
     */
    @Transactional
    public void requestPasswordReset(ForgotPasswordDTO forgotPasswordDTO) {
        String email = forgotPasswordDTO.getEmail().toLowerCase().trim();
        LOG.info("Password reset requested for email: {}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // Don't reveal if email exists - just log and return
            LOG.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOpt.get();

        // Check if user is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            LOG.warn("Password reset requested for inactive user: {}", email);
            return;
        }

        // Invalidate any existing reset tokens for this user
        passwordResetTokenRepository.invalidateAllForUser(user.getId(), LocalDateTime.now());

        // Generate secure random token
        String plainToken = generateSecureToken();
        String tokenHash = hashToken(plainToken);

        // Create and save token entity
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(passwordResetTokenExpiryHours))
                .build();

        passwordResetTokenRepository.save(resetToken);
        LOG.debug("Password reset token created for user: {}", user.getId());

        // Send reset email with plain token
        authEmailService.sendPasswordResetEmail(user, plainToken);
        LOG.info("Password reset email sent for user: {}", user.getId());
    }

    /**
     * Resets password using a valid reset token.
     *
     * @param resetPasswordDTO the reset password request
     * @throws InvalidTokenException    if token is invalid, expired, or already
     *                                  used
     * @throws IllegalArgumentException if passwords don't match
     */
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        LOG.info("Attempting to reset password with token");

        // Validate passwords match
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            LOG.warn("Password reset failed: Password confirmation mismatch");
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Hash the provided token and look it up
        String tokenHash = hashToken(resetPasswordDTO.getToken());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    LOG.warn("Password reset failed: Invalid token");
                    return new InvalidTokenException("Invalid or expired reset token");
                });

        // Check if token is valid
        if (!resetToken.isValid()) {
            if (resetToken.isUsed()) {
                LOG.warn("Password reset failed: Token already used");
                throw new InvalidTokenException("This reset link has already been used");
            } else {
                LOG.warn("Password reset failed: Token expired");
                throw new InvalidTokenException("This reset link has expired. Please request a new one.");
            }
        }

        User user = resetToken.getUser();

        // Check if user is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            LOG.warn("Password reset failed: User account is inactive: {}", user.getEmail());
            throw new InvalidTokenException("User account is inactive");
        }

        // Hash and update password
        String newHashedPassword = passwordEncoder.encode(resetPasswordDTO.getNewPassword());
        user.setPasswordHash(newHashedPassword);
        userRepository.save(user);
        LOG.info("Password reset successfully for user: {}", user.getEmail());

        // Mark token as used
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens (force re-login on all devices)
        refreshTokenRepository.deleteByUserId(user.getId());
        LOG.info("All refresh tokens revoked for user: {} after password reset", user.getEmail());

        // Send password changed notification
        authEmailService.sendPasswordChangedEmail(user);
    }

    // ========== Helper Methods ==========

    /**
     * Generates a cryptographically secure random token.
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Hashes a token using SHA-256.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}