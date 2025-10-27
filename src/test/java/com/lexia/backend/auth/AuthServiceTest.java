package com.lexia.backend.auth;

import com.lexia.backend.dto.RegisterDTO;
import com.lexia.backend.entity.Role;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.UserAlreadyExistsException;
import com.lexia.backend.repository.RefreshTokenRepository;
import com.lexia.backend.repository.RoleRepository;
import com.lexia.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;
    private RegisterDTO validRegisterDTO;
    private Role learnerRole;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);

        validRegisterDTO = RegisterDTO.builder()
                .email("test@lexia.com")
                .password("Password123")
                .confirmPassword("Password123")
                .fullName("Test User")
                .build();

        learnerRole = Role.builder()
                .id(1)
                .name("LEARNER")
                .build();
    }

    @Test
    void testRegisterUser_WithValidData_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("LEARNER")).thenReturn(Optional.of(learnerRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // Act
        User result = authService.register(validRegisterDTO);

        // Assert
        assertNotNull(result);
        assertEquals("test@lexia.com", result.getEmail());
        assertNotNull(result.getPasswordHash());
        assertNotEquals("Password123", result.getPasswordHash()); // Password should be hashed
        assertTrue(passwordEncoder.matches("Password123", result.getPasswordHash())); // But should match when verified
        assertEquals(User.AuthProvider.EMAIL, result.getAuthProvider());
        assertTrue(result.getIsActive());
        assertNotNull(result.getProfile());
        assertEquals("Test User", result.getProfile().getFullName());

        // Verify repository interactions
        verify(userRepository).existsByEmail("test@lexia.com");
        verify(roleRepository).findByName("LEARNER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_WithExistingEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail("test@lexia.com")).thenReturn(true);

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(validRegisterDTO);
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).existsByEmail("test@lexia.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_WithLearnerRoleNotFound_SuccessWithoutRole() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("LEARNER")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // Act
        User result = authService.register(validRegisterDTO);

        // Assert
        assertNotNull(result);
        assertTrue(result.getUserRoles().isEmpty()); // No roles assigned
        verify(roleRepository).findByName("LEARNER");
    }

    @Test
    void testValidatePassword_WithCorrectPassword_ReturnsTrue() {
        // Arrange
        String rawPassword = "Password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean result = authService.validatePassword(rawPassword, hashedPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidatePassword_WithIncorrectPassword_ReturnsFalse() {
        // Arrange
        String rawPassword = "Password123";
        String wrongPassword = "WrongPassword123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean result = authService.validatePassword(wrongPassword, hashedPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    void testAuthenticateUser_WithValidCredentials_ReturnsUser() {
        // Arrange
        String email = "test@lexia.com";
        String password = "Password123";
        String hashedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(hashedPassword)
                .isActive(true)
                .build();

        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = authService.authenticateUser(email, password);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByEmailAndIsActive(email, true);
    }

    @Test
    void testAuthenticateUser_WithInvalidPassword_ReturnsEmpty() {
        // Arrange
        String email = "test@lexia.com";
        String wrongPassword = "WrongPassword123";
        String correctPassword = "Password123";
        String hashedPassword = passwordEncoder.encode(correctPassword);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(hashedPassword)
                .isActive(true)
                .build();

        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = authService.authenticateUser(email, wrongPassword);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmailAndIsActive(email, true);
    }

    @Test
    void testAuthenticateUser_WithNonExistentUser_ReturnsEmpty() {
        // Arrange
        String email = "nonexistent@lexia.com";
        String password = "Password123";

        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = authService.authenticateUser(email, password);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmailAndIsActive(email, true);
    }

    @Test
    void testAuthenticateUser_WithInactiveUser_ReturnsEmpty() {
        // Arrange
        String email = "inactive@lexia.com";
        String password = "Password123";

        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = authService.authenticateUser(email, password);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmailAndIsActive(email, true);
    }

    @Test
    void testPasswordHashingUsesBCryptCost12() {
        // This test verifies that the password encoder is configured with cost factor
        // 12
        // by checking that the hash starts with $2a$12$ (BCrypt format with cost 12)

        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("LEARNER")).thenReturn(Optional.of(learnerRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // Act
        User result = authService.register(validRegisterDTO);

        // Assert
        String hash = result.getPasswordHash();
        assertTrue(hash.startsWith("$2a$12$"), "Password hash should use BCrypt with cost factor 12");
        assertTrue(hash.length() > 50, "BCrypt hash should be sufficiently long");
    }

    // ========== Login Tests ==========

    @Test
    void testLogin_WithValidCredentials_ReturnsLoginResponse() {
        // Arrange
        String email = "test@lexia.com";
        String password = "Password123";
        String hashedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .email(email)
                .passwordHash(hashedPassword)
                .isActive(true)
                .build();

        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(any(java.util.UUID.class), anyString()))
                .thenReturn("mock.access.token");
        when(jwtTokenProvider.generateRefreshToken(any(java.util.UUID.class), anyString()))
                .thenReturn("mock.refresh.token");
        when(jwtTokenProvider.hashToken(anyString())).thenReturn("hashed.token");
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var loginDTO = com.lexia.backend.dto.LoginDTO.builder()
                .email(email)
                .password(password)
                .build();

        var result = authService.login(loginDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(900000L, result.getExpiresIn()); // 15 minutes
        assertEquals(email, result.getUser().getEmail());

        // Verify refresh token was stored
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void testLogin_WithInvalidEmail_ThrowsException() {
        // Arrange
        String email = "nonexistent@lexia.com";
        String password = "Password123";

        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.empty());

        var loginDTO = com.lexia.backend.dto.LoginDTO.builder()
                .email(email)
                .password(password)
                .build();

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.login(loginDTO);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLogin_WithInvalidPassword_ThrowsException() {
        // Arrange
        String email = "test@lexia.com";
        String correctPassword = "Password123";
        String wrongPassword = "WrongPassword123";
        String hashedPassword = passwordEncoder.encode(correctPassword);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(hashedPassword)
                .isActive(true)
                .build();

        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.of(user));

        var loginDTO = com.lexia.backend.dto.LoginDTO.builder()
                .email(email)
                .password(wrongPassword)
                .build();

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.login(loginDTO);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }
}