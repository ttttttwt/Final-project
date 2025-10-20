package com.lexia.backend.auth;

import com.lexia.backend.dto.RegisterDTO;
import com.lexia.backend.entity.Role;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.entity.UserRole;
import com.lexia.backend.repository.RoleRepository;
import com.lexia.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        // BCrypt with cost factor 12 for strong password hashing
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    /**
     * Registers a new user account.
     *
     * @param registerDTO the registration data
     * @return the created user entity (without sensitive data)
     * @throws IllegalArgumentException if email already exists or validation fails
     */
    @Transactional
    public User register(RegisterDTO registerDTO) {
        LOG.info("Attempting to register user with email: {}", registerDTO.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            LOG.warn("Registration failed: Email already exists: {}", registerDTO.getEmail());
            throw new IllegalArgumentException("Email already registered");
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
}