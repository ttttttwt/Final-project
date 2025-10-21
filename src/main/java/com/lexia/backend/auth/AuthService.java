package com.lexia.backend.auth;

import com.lexia.backend.dto.LoginDTO;
import com.lexia.backend.dto.LoginResponseDTO;
import com.lexia.backend.dto.RefreshTokenDTO;
import com.lexia.backend.dto.RefreshTokenResponseDTO;
import com.lexia.backend.dto.RegisterDTO;
import com.lexia.backend.dto.UserDTO;
import com.lexia.backend.entity.RefreshToken;
import com.lexia.backend.entity.Role;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.entity.UserRole;
import com.lexia.backend.exception.InvalidTokenException;
import com.lexia.backend.exception.UserAlreadyExistsException;
import com.lexia.backend.repository.RefreshTokenRepository;
import com.lexia.backend.repository.RoleRepository;
import com.lexia.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
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
     *
     * @param email    the user's email
     * @param password the user's password
     * @return Optional containing the user if authentication successful
     */
    public Optional<User> authenticateUser(String email, String password) {
        LOG.debug("Attempting to authenticate user: {}", email);

        Optional<User> userOpt = userRepository.findByEmailAndIsActive(email, true);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (validatePassword(password, user.getPasswordHash())) {
                LOG.info("User authenticated successfully: {}", email);
                return Optional.of(user);
            } else {
                LOG.warn("Authentication failed: Invalid password for user: {}", email);
            }
        } else {
            LOG.warn("Authentication failed: User not found or inactive: {}", email);
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
        String accessToken = jwtTokenProvider.generateAccessToken(UUID.fromString(user.getId()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(UUID.fromString(user.getId()));
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
     * Implements token rotation: invalidates old refresh token and creates a new one.
     *
     * @param refreshTokenDTO the refresh token request
     * @return RefreshTokenResponseDTO containing new access token and new refresh token
     * @throws InvalidTokenException if refresh token is invalid, expired, or revoked
     */
    @Transactional
    public RefreshTokenResponseDTO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        String refreshTokenValue = refreshTokenDTO.getRefreshToken();
        LOG.info("Attempting to refresh access token");

        // Validate refresh token format
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            LOG.warn("Invalid refresh token format");
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        // Extract user ID from refresh token
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);

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
        if (!user.getIsActive()) {
            LOG.warn("User account is inactive: {}", user.getEmail());
            throw new InvalidTokenException("User account is inactive");
        }

        // Revoke old refresh token
        refreshTokenRepository.revokeToken(tokenHash, LocalDateTime.now());
        LOG.debug("Old refresh token revoked for user: {}", user.getEmail());

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(UUID.fromString(user.getId()));

        // Generate new refresh token (token rotation)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(UUID.fromString(user.getId()));
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
}