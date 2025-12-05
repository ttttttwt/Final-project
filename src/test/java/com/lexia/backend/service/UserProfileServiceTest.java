package com.lexia.backend.service;

import com.lexia.backend.dto.UpdateProfileDTO;
import com.lexia.backend.dto.UserProfileDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.exception.InvalidInputException;
import com.lexia.backend.exception.UserNotFoundException;
import com.lexia.backend.repository.UserProfileRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserProfileService.
 * Tests all CRUD operations, validation rules, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private com.lexia.backend.file.service.FileStorageService fileStorageService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private UUID testUserId;
    private User testUser;
    private UserProfile testProfile;
    private UpdateProfileDTO validUpdateDTO;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        // Create test user
        testUser = User.builder()
                .id(testUserId)
                .email("test@lexia.com")
                .passwordHash("$2a$12$hashedpassword")
                .authProvider(User.AuthProvider.EMAIL)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create test profile
        testProfile = UserProfile.builder()
                .userId(testUserId)
                .user(testUser)
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .bio("Software engineer with passion for learning")
                .phoneNumber("+84987654321")
                .avatarUrl("https://example.com/avatar.jpg")
                .timezone("Asia/Ho_Chi_Minh")
                .language("en")
                .currentLevel("B1")
                .learningGoal("Improve business English")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser.setProfile(testProfile);

        // Create valid update DTO
        validUpdateDTO = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .bio("Updated bio")
                .phoneNumber("+84912345678")
                .timezone("UTC")
                .language("vi")
                .build();
    }

    // ========== getProfile() Tests ==========

    @Test
    void testGetProfile_WithValidUserId_ReturnsUserProfileDTO() {
        // Arrange
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));

        // Act
        UserProfileDTO result = userProfileService.getProfile(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals("test@lexia.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Software engineer with passion for learning", result.getBio());
        assertEquals("+84987654321", result.getPhoneNumber());
        assertEquals("https://example.com/avatar.jpg", result.getAvatarUrl());
        assertEquals("Asia/Ho_Chi_Minh", result.getTimezone());
        assertEquals("en", result.getLanguage());
        assertEquals("B1", result.getCurrentLevel());
        assertEquals("Improve business English", result.getLearningGoal());

        verify(userProfileRepository).findByUserId(testUserId);
    }

    @Test
    void testGetProfile_WithNullUserId_ThrowsInvalidInputException() {
        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.getProfile(null);
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testGetProfile_WithNonExistentUserId_ThrowsUserNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userProfileService.getProfile(nonExistentId);
        });

        assertTrue(exception.getMessage().contains("User profile not found"));
        verify(userProfileRepository).findByUserId(nonExistentId);
    }

    // ========== getCurrentUserProfile() Tests ==========

    @Test
    void testGetCurrentUserProfile_WithAuthenticatedUser_ReturnsUserProfileDTO() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@lexia.com");
        when(userRepository.findByEmail("test@lexia.com")).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));

        // Act
        UserProfileDTO result = userProfileService.getCurrentUserProfile();

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals("test@lexia.com", result.getEmail());
        assertEquals("John", result.getFirstName());

        verify(userRepository).findByEmail("test@lexia.com");
        verify(userProfileRepository).findByUserId(testUserId);

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserProfile_WithNoAuthentication_ThrowsUserNotFoundException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userProfileService.getCurrentUserProfile();
        });

        assertEquals("No authenticated user found", exception.getMessage());

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserProfile_WithUnauthenticatedUser_ThrowsUserNotFoundException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userProfileService.getCurrentUserProfile();
        });

        assertEquals("No authenticated user found", exception.getMessage());

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserProfile_WithNonExistentUserEmail_ThrowsUserNotFoundException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistent@lexia.com");
        when(userRepository.findByEmail("nonexistent@lexia.com")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userProfileService.getCurrentUserProfile();
        });

        assertTrue(exception.getMessage().contains("User not found for email"));

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserProfile_WithUserPrincipal_ReturnsUserProfileDTO() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser); // Principal is User object
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));

        // Act
        UserProfileDTO result = userProfileService.getCurrentUserProfile();

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());

        // Verify we didn't look up by email because we got ID from principal
        verify(userRepository, never()).findByEmail(anyString());
        verify(userProfileRepository).findByUserId(testUserId);

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    // ========== updateProfile() Tests ==========

    @Test
    void testUpdateProfile_WithValidData_ReturnsUpdatedProfile() {
        // Arrange
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile profile = invocation.getArgument(0);
            profile.setUpdatedAt(LocalDateTime.now());
            return profile;
        });

        // Act
        UserProfileDTO result = userProfileService.updateProfile(testUserId, validUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Updated bio", result.getBio());
        assertEquals("+84912345678", result.getPhoneNumber());
        assertEquals("UTC", result.getTimezone());
        assertEquals("vi", result.getLanguage());

        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void testUpdateProfile_WithNullUserId_ThrowsInvalidInputException() {
        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateProfile(null, validUpdateDTO);
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateProfile_WithNullDTO_ThrowsInvalidInputException() {
        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateProfile(testUserId, null);
        });

        assertEquals("Update profile data cannot be null", exception.getMessage());
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateProfile_WithNonExistentUser_ThrowsUserNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userProfileService.updateProfile(nonExistentId, validUpdateDTO);
        });

        assertTrue(exception.getMessage().contains("User profile not found"));
        verify(userProfileRepository).findByUserId(nonExistentId);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile_WithInvalidTimezone_ThrowsInvalidInputException() {
        // Arrange
        UpdateProfileDTO invalidDTO = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .bio("Updated bio")
                .phoneNumber("+84912345678")
                .timezone("Invalid/Timezone")
                .language("en")
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateProfile(testUserId, invalidDTO);
        });

        assertTrue(exception.getMessage().contains("Invalid timezone"));
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateProfile_WithInvalidLanguageCode_ThrowsInvalidInputException() {
        // Arrange
        UpdateProfileDTO invalidDTO = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .bio("Updated bio")
                .phoneNumber("+84912345678")
                .timezone("UTC")
                .language("eng") // Invalid: should be 2 letters
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateProfile(testUserId, invalidDTO);
        });

        assertTrue(exception.getMessage().contains("Language code must be a 2-letter ISO 639-1 code"));
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateProfile_WithInvalidPhoneNumber_ThrowsInvalidInputException() {
        // Arrange
        UpdateProfileDTO invalidDTO = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .bio("Updated bio")
                .phoneNumber("123") // Invalid: too short
                .timezone("UTC")
                .language("en")
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateProfile(testUserId, invalidDTO);
        });

        assertTrue(exception.getMessage().contains("Phone number must be 10-20 digits"));
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateProfile_WithTooLongBio_ThrowsInvalidInputException() {
        // Arrange
        String longBio = "a".repeat(501); // Exceeds 500 character limit
        UpdateProfileDTO invalidDTO = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .bio(longBio)
                .phoneNumber("+84912345678")
                .timezone("UTC")
                .language("en")
                .build();

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateProfile(testUserId, invalidDTO);
        });

        assertTrue(exception.getMessage().contains("Bio must not exceed 500 characters"));
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateProfile_WithNullPhoneNumber_Success() {
        // Arrange
        UpdateProfileDTO dtoWithoutPhone = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .bio("Updated bio")
                .phoneNumber(null) // Phone is optional
                .timezone("UTC")
                .language("en")
                .build();

        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfileDTO result = userProfileService.updateProfile(testUserId, dtoWithoutPhone);

        // Assert
        assertNotNull(result);
        assertNull(result.getPhoneNumber());
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void testUpdateProfile_WithEmptyPhoneNumber_Success() {
        // Arrange
        UpdateProfileDTO dtoWithEmptyPhone = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .bio("Updated bio")
                .phoneNumber("") // Empty phone is treated as null
                .timezone("UTC")
                .language("en")
                .build();

        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfileDTO result = userProfileService.updateProfile(testUserId, dtoWithEmptyPhone);

        // Assert
        assertNotNull(result);
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void testUpdateProfile_UpdatesFullNameField() {
        // Arrange
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile profile = invocation.getArgument(0);
            // Verify fullName is updated correctly
            assertEquals("Jane Smith", profile.getFullName());
            return profile;
        });

        // Act
        userProfileService.updateProfile(testUserId, validUpdateDTO);

        // Assert
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    // ========== updateAvatar() Tests ==========

    @Test
    void testUpdateAvatar_WithValidUrl_Success() {
        // Arrange
        String newAvatarUrl = "https://example.com/new-avatar.jpg";
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userProfileService.updateAvatar(testUserId, newAvatarUrl);

        // Assert
        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository).save(argThat(profile -> profile.getAvatarUrl().equals(newAvatarUrl)));
    }

    @Test
    void testUpdateAvatar_WithHttpUrl_Success() {
        // Arrange
        String httpAvatarUrl = "http://example.com/avatar.jpg";
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userProfileService.updateAvatar(testUserId, httpAvatarUrl);

        // Assert
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void testUpdateAvatar_WithNullUserId_ThrowsInvalidInputException() {
        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateAvatar(null, "https://example.com/avatar.jpg");
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateAvatar_WithNullUrl_ThrowsInvalidInputException() {
        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateAvatar(testUserId, null);
        });

        assertEquals("Avatar URL cannot be null or empty", exception.getMessage());
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateAvatar_WithEmptyUrl_ThrowsInvalidInputException() {
        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateAvatar(testUserId, "   ");
        });

        assertEquals("Avatar URL cannot be null or empty", exception.getMessage());
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateAvatar_WithInvalidUrl_ThrowsInvalidInputException() {
        // Arrange
        String invalidUrl = "not-a-valid-url";

        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.updateAvatar(testUserId, invalidUrl);
        });

        assertTrue(exception.getMessage().contains("Avatar URL must be a valid HTTP or HTTPS URL"));
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testUpdateAvatar_WithNonExistentUser_ThrowsUserNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userProfileService.updateAvatar(nonExistentId, "https://example.com/avatar.jpg");
        });

        assertTrue(exception.getMessage().contains("User profile not found"));
        verify(userProfileRepository, never()).save(any());
    }

    // ========== deleteAvatar() Tests ==========

    @Test
    void testDeleteAvatar_WithValidUserId_Success() {
        // Arrange
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userProfileService.deleteAvatar(testUserId);

        // Assert
        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository).save(argThat(profile -> profile.getAvatarUrl() == null));
    }

    @Test
    void testDeleteAvatar_WithNullUserId_ThrowsInvalidInputException() {
        // Act & Assert
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            userProfileService.deleteAvatar(null);
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(userProfileRepository, never()).findByUserId(any());
    }

    @Test
    void testDeleteAvatar_WithNonExistentUser_ThrowsUserNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userProfileService.deleteAvatar(nonExistentId);
        });

        assertTrue(exception.getMessage().contains("User profile not found"));
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void testDeleteAvatar_WhenAvatarAlreadyNull_Success() {
        // Arrange
        testProfile.setAvatarUrl(null); // Avatar already null
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userProfileService.deleteAvatar(testUserId);

        // Assert
        verify(userProfileRepository).save(argThat(profile -> profile.getAvatarUrl() == null));
    }
}
