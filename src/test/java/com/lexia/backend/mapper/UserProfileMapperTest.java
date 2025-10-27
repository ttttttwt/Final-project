package com.lexia.backend.mapper;

import com.lexia.backend.dto.UpdateProfileDTO;
import com.lexia.backend.dto.UserProfileDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserProfileMapper.
 */
class UserProfileMapperTest {

    @Test
    void testToDTO_WithValidProfile_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@lexia.com")
                .build();

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .user(user)
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .bio("Software developer")
                .phoneNumber("+1234567890")
                .avatarUrl("https://example.com/avatar.jpg")
                .timezone("America/New_York")
                .language("en")
                .currentLevel("B1")
                .learningGoal("Improve business English")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        UserProfileDTO dto = UserProfileMapper.toDTO(profile);

        // Assert
        assertNotNull(dto);
        assertEquals(userId, dto.getUserId());
        assertEquals("test@lexia.com", dto.getEmail());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("Software developer", dto.getBio());
        assertEquals("+1234567890", dto.getPhoneNumber());
        assertEquals("https://example.com/avatar.jpg", dto.getAvatarUrl());
        assertEquals("America/New_York", dto.getTimezone());
        assertEquals("en", dto.getLanguage());
        assertEquals("B1", dto.getCurrentLevel());
        assertEquals("Improve business English", dto.getLearningGoal());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
    }

    @Test
    void testToDTO_WithNullProfile_ReturnsNull() {
        // Act
        UserProfileDTO dto = UserProfileMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    void testToDTO_WithProfileWithoutUser_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .user(null) // No user relationship loaded
                .firstName("Jane")
                .lastName("Smith")
                .timezone("UTC")
                .language("vi")
                .build();

        // Act
        UserProfileDTO dto = UserProfileMapper.toDTO(profile);

        // Assert
        assertNotNull(dto);
        assertEquals(userId, dto.getUserId());
        assertNull(dto.getEmail()); // Email should be null when user is not loaded
        assertEquals("Jane", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
    }

    @Test
    void testUpdateEntityFromDTO_WithValidData_Success() {
        // Arrange
        UserProfile profile = UserProfile.builder()
                .userId(UUID.randomUUID())
                .firstName("Old")
                .lastName("Name")
                .fullName("Old Name")
                .bio("Old bio")
                .phoneNumber("+9876543210")
                .timezone("UTC")
                .language("en")
                .build();

        UpdateProfileDTO dto = UpdateProfileDTO.builder()
                .firstName("New")
                .lastName("Name")
                .bio("New bio")
                .phoneNumber("+1234567890")
                .timezone("Asia/Ho_Chi_Minh")
                .language("vi")
                .build();

        // Act
        UserProfileMapper.updateEntityFromDTO(profile, dto);

        // Assert
        assertEquals("New", profile.getFirstName());
        assertEquals("Name", profile.getLastName());
        assertEquals("New Name", profile.getFullName()); // Should be updated
        assertEquals("New bio", profile.getBio());
        assertEquals("+1234567890", profile.getPhoneNumber());
        assertEquals("Asia/Ho_Chi_Minh", profile.getTimezone());
        assertEquals("vi", profile.getLanguage());
    }

    @Test
    void testUpdateEntityFromDTO_WithNullProfile_ThrowsException() {
        // Arrange
        UpdateProfileDTO dto = UpdateProfileDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .timezone("UTC")
                .language("en")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            UserProfileMapper.updateEntityFromDTO(null, dto);
        });
        assertEquals("UserProfile entity cannot be null", exception.getMessage());
    }

    @Test
    void testUpdateEntityFromDTO_WithNullDTO_ThrowsException() {
        // Arrange
        UserProfile profile = UserProfile.builder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            UserProfileMapper.updateEntityFromDTO(profile, null);
        });
        assertEquals("UpdateProfileDTO cannot be null", exception.getMessage());
    }

    @Test
    void testToEntity_WithValidDTO_Success() {
        // Arrange
        UpdateProfileDTO dto = UpdateProfileDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .bio("Software engineer")
                .phoneNumber("+1234567890")
                .timezone("America/New_York")
                .language("en")
                .build();

        // Act
        UserProfile profile = UserProfileMapper.toEntity(dto);

        // Assert
        assertNotNull(profile);
        assertEquals("John", profile.getFirstName());
        assertEquals("Doe", profile.getLastName());
        assertEquals("John Doe", profile.getFullName());
        assertEquals("Software engineer", profile.getBio());
        assertEquals("+1234567890", profile.getPhoneNumber());
        assertEquals("America/New_York", profile.getTimezone());
        assertEquals("en", profile.getLanguage());
    }

    @Test
    void testToEntity_WithNullDTO_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            UserProfileMapper.toEntity(null);
        });
        assertEquals("UpdateProfileDTO cannot be null", exception.getMessage());
    }

    @Test
    void testToEntity_WithMinimalDTO_Success() {
        // Arrange
        UpdateProfileDTO dto = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .timezone("UTC")
                .language("vi")
                .build();

        // Act
        UserProfile profile = UserProfileMapper.toEntity(dto);

        // Assert
        assertNotNull(profile);
        assertEquals("Jane", profile.getFirstName());
        assertEquals("Smith", profile.getLastName());
        assertEquals("Jane Smith", profile.getFullName());
        assertNull(profile.getBio());
        assertNull(profile.getPhoneNumber());
        assertEquals("UTC", profile.getTimezone());
        assertEquals("vi", profile.getLanguage());
    }

    @Test
    void testMapperConstructor_ThrowsException() {
        // Act & Assert
        try {
            // Use reflection to access private constructor
            java.lang.reflect.Constructor<UserProfileMapper> constructor = UserProfileMapper.class
                    .getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Expected UnsupportedOperationException to be thrown");
        } catch (Exception e) {
            // Verify the cause is UnsupportedOperationException
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
            assertEquals("Utility class cannot be instantiated", e.getCause().getMessage());
        }
    }
}
