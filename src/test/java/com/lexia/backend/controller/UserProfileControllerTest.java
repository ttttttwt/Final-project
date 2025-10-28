package com.lexia.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.common.GlobalExceptionHandler;
import com.lexia.backend.dto.UpdateProfileDTO;
import com.lexia.backend.dto.UserProfileDTO;
import com.lexia.backend.exception.InvalidInputException;
import com.lexia.backend.exception.UserNotFoundException;
import com.lexia.backend.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserProfileController.
 * Tests all profile management endpoints with various scenarios.
 */
@WebMvcTest(UserProfileController.class)
@ContextConfiguration(classes = { UserProfileController.class, GlobalExceptionHandler.class })
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UserProfileDTO mockProfileDTO;
    private UpdateProfileDTO updateProfileDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        mockProfileDTO = UserProfileDTO.builder()
                .userId(userId)
                .email("test@lexia.com")
                .firstName("John")
                .lastName("Doe")
                .bio("Test bio")
                .phoneNumber("+84123456789")
                .avatarUrl("https://example.com/avatar.jpg")
                .timezone("Asia/Ho_Chi_Minh")
                .language("en")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        updateProfileDTO = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .bio("Updated bio")
                .phoneNumber("+84987654321")
                .timezone("America/New_York")
                .language("vi")
                .build();
    }

    // ========== Controller Initialization Tests ==========

    @Test
    void contextLoads() {
        // Test that the controller bean is created successfully
        // If this test passes, it means the controller can be instantiated
    }

    @Test
    void controllerIsNotNull() {
        // Verify MockMvc is properly configured
        assert mockMvc != null : "MockMvc should be autowired";
    }

    // ========== GET /profile Tests ==========

    @Test
    @WithMockUser(username = "test@lexia.com")
    void getCurrentUserProfile_WithAuthenticatedUser_ReturnsOk() throws Exception {
        // Arrange
        when(userProfileService.getCurrentUserProfile()).thenReturn(mockProfileDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@lexia.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.bio").value("Test bio"))
                .andExpect(jsonPath("$.phoneNumber").value("+84123456789"))
                .andExpect(jsonPath("$.timezone").value("Asia/Ho_Chi_Minh"))
                .andExpect(jsonPath("$.language").value("en"));

        verify(userProfileService, times(1)).getCurrentUserProfile();
    }

    @Test
    void getCurrentUserProfile_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isUnauthorized());

        verify(userProfileService, never()).getCurrentUserProfile();
    }

    @Test
    @WithMockUser(username = "test@lexia.com")
    void getCurrentUserProfile_UserNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(userProfileService.getCurrentUserProfile())
                .thenThrow(new UserNotFoundException("User profile not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User profile not found"));

        verify(userProfileService, times(1)).getCurrentUserProfile();
    }

    // ========== PUT /profile Tests ==========

    @Test
    @WithMockUser(username = "test@lexia.com")
    void updateCurrentUserProfile_WithValidData_ReturnsOk() throws Exception {
        // Arrange
        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .userId(userId)
                .email("test@lexia.com")
                .firstName("Jane")
                .lastName("Smith")
                .bio("Updated bio")
                .phoneNumber("+84987654321")
                .timezone("America/New_York")
                .language("vi")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userProfileService.getCurrentUserProfile()).thenReturn(mockProfileDTO);
        when(userProfileService.updateProfile(eq(userId), any(UpdateProfileDTO.class)))
                .thenReturn(updatedProfile);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.phoneNumber").value("+84987654321"))
                .andExpect(jsonPath("$.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.language").value("vi"));

        verify(userProfileService, times(1)).getCurrentUserProfile();
        verify(userProfileService, times(1)).updateProfile(eq(userId), any(UpdateProfileDTO.class));
    }

    @Test
    @WithMockUser(username = "test@lexia.com")
    void updateCurrentUserProfile_WithInvalidTimezone_ReturnsBadRequest() throws Exception {
        // Arrange
        UpdateProfileDTO invalidDTO = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .timezone("Invalid/Timezone")
                .language("en")
                .build();

        when(userProfileService.getCurrentUserProfile()).thenReturn(mockProfileDTO);
        when(userProfileService.updateProfile(eq(userId), any(UpdateProfileDTO.class)))
                .thenThrow(new InvalidInputException("Invalid timezone: Invalid/Timezone"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid timezone: Invalid/Timezone"));

        verify(userProfileService, times(1)).getCurrentUserProfile();
    }

    @Test
    @WithMockUser(username = "test@lexia.com")
    void updateCurrentUserProfile_WithInvalidLanguage_ReturnsBadRequest() throws Exception {
        // Arrange
        UpdateProfileDTO invalidDTO = UpdateProfileDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .timezone("UTC")
                .language("invalid") // More than 2 characters, violates @Pattern
                .build();

        // Act & Assert - ValidationException should be thrown by @Valid annotation
        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        // Service should not be called due to validation failure
        verify(userProfileService, never()).updateProfile(any(), any());
    }

    @Test
    void updateCurrentUserProfile_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileDTO)))
                .andExpect(status().isUnauthorized());

        verify(userProfileService, never()).getCurrentUserProfile();
        verify(userProfileService, never()).updateProfile(any(), any());
    }

    // ========== POST /profile/avatar Tests ==========

    @Test
    @WithMockUser(username = "test@lexia.com")
    void updateAvatar_WithValidUrl_ReturnsOk() throws Exception {
        // Arrange
        UserProfileController.AvatarRequest avatarRequest = new UserProfileController.AvatarRequest(
                "https://example.com/new-avatar.jpg");

        when(userProfileService.getCurrentUserProfile()).thenReturn(mockProfileDTO);
        doNothing().when(userProfileService).updateAvatar(eq(userId), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/profile/avatar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avatarRequest)))
                .andExpect(status().isOk());

        verify(userProfileService, times(1)).getCurrentUserProfile();
        verify(userProfileService, times(1)).updateAvatar(userId, "https://example.com/new-avatar.jpg");
    }

    @Test
    @WithMockUser(username = "test@lexia.com")
    void updateAvatar_WithInvalidUrl_ReturnsBadRequest() throws Exception {
        // Arrange
        UserProfileController.AvatarRequest avatarRequest = new UserProfileController.AvatarRequest(
                "invalid-url");

        when(userProfileService.getCurrentUserProfile()).thenReturn(mockProfileDTO);
        doThrow(new InvalidInputException("Avatar URL must be a valid HTTP or HTTPS URL"))
                .when(userProfileService).updateAvatar(eq(userId), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/profile/avatar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avatarRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Avatar URL must be a valid HTTP or HTTPS URL"));

        verify(userProfileService, times(1)).getCurrentUserProfile();
    }

    @Test
    void updateAvatar_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Arrange
        UserProfileController.AvatarRequest avatarRequest = new UserProfileController.AvatarRequest(
                "https://example.com/avatar.jpg");

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/profile/avatar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avatarRequest)))
                .andExpect(status().isUnauthorized());

        verify(userProfileService, never()).getCurrentUserProfile();
        verify(userProfileService, never()).updateAvatar(any(), any());
    }

    // ========== DELETE /profile/avatar Tests ==========

    @Test
    @WithMockUser(username = "test@lexia.com")
    void deleteAvatar_WithAuthenticatedUser_ReturnsOk() throws Exception {
        // Arrange
        when(userProfileService.getCurrentUserProfile()).thenReturn(mockProfileDTO);
        doNothing().when(userProfileService).deleteAvatar(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/profile/avatar")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(userProfileService, times(1)).getCurrentUserProfile();
        verify(userProfileService, times(1)).deleteAvatar(userId);
    }

    @Test
    void deleteAvatar_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/profile/avatar")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userProfileService, never()).getCurrentUserProfile();
        verify(userProfileService, never()).deleteAvatar(any());
    }

    @Test
    @WithMockUser(username = "test@lexia.com")
    void deleteAvatar_UserNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(userProfileService.getCurrentUserProfile()).thenReturn(mockProfileDTO);
        doThrow(new UserNotFoundException("User profile not found"))
                .when(userProfileService).deleteAvatar(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/profile/avatar")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User profile not found"));

        verify(userProfileService, times(1)).getCurrentUserProfile();
        verify(userProfileService, times(1)).deleteAvatar(userId);
    }

    // ========== Integration Tests ==========

    @Test
    @WithMockUser(username = "test@lexia.com")
    void fullProfileUpdateFlow_Success() throws Exception {
        // 1. Get initial profile
        when(userProfileService.getCurrentUserProfile()).thenReturn(mockProfileDTO);

        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));

        // 2. Update profile
        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .userId(userId)
                .email("test@lexia.com")
                .firstName("Jane")
                .lastName("Smith")
                .bio("Updated bio")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userProfileService.updateProfile(eq(userId), any(UpdateProfileDTO.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/v1/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));

        // Verify all interactions
        verify(userProfileService, times(2)).getCurrentUserProfile();
        verify(userProfileService, times(1)).updateProfile(eq(userId), any(UpdateProfileDTO.class));
    }
}
