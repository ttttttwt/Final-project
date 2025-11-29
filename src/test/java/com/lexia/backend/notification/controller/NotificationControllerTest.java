package com.lexia.backend.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.common.GlobalExceptionHandler;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.notification.dto.NotificationDTO;
import com.lexia.backend.notification.dto.NotificationPreferencesDTO;
import com.lexia.backend.notification.dto.UnreadCountDTO;
import com.lexia.backend.notification.entity.Notification.NotificationPriority;
import com.lexia.backend.notification.entity.Notification.NotificationType;
import com.lexia.backend.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for NotificationController.
 * Tests all notification endpoints with various scenarios.
 * 
 * <p>
 * Uses @AutoConfigureMockMvc(addFilters = false) to disable security filters
 * and SecurityContextHolder to provide authenticated user
 * for @AuthenticationPrincipal.
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { NotificationController.class, GlobalExceptionHandler.class })
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UUID userId;
    private UUID notificationId;
    private User testUser;
    private NotificationDTO mockNotificationDTO;
    private NotificationPreferencesDTO mockPreferencesDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@lexia.app")
                .passwordHash("$2a$12$hashedpassword")
                .isActive(true)
                .build();

        // Set up Security Context with authenticated user for @AuthenticationPrincipal
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockNotificationDTO = NotificationDTO.builder()
                .id(notificationId)
                .type(NotificationType.COURSE_COMPLETED)
                .title("Congratulations! 🎉")
                .message("You completed Business English Basics")
                .data(Map.of("courseId", "123", "courseTitle", "Business English Basics"))
                .priority(NotificationPriority.HIGH)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();

        mockPreferencesDTO = NotificationPreferencesDTO.builder()
                .inAppEnabled(true)
                .emailEnabled(true)
                .pushEnabled(false)
                .learningEnabled(true)
                .achievementsEnabled(true)
                .remindersEnabled(true)
                .systemEnabled(true)
                .quietHoursStart(LocalTime.of(22, 0))
                .quietHoursEnd(LocalTime.of(7, 0))
                .quietHoursTimezone("Asia/Ho_Chi_Minh")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========== GET /api/v1/notifications Tests ==========

    @Nested
    @DisplayName("GET /api/v1/notifications")
    class GetNotificationsTests {

        @Test
        @DisplayName("Should return paginated notifications")
        void getNotifications_ReturnsOk() throws Exception {
            // Given
            Page<NotificationDTO> page = new PageImpl<>(
                    List.of(mockNotificationDTO),
                    PageRequest.of(0, 20),
                    1);
            when(notificationService.getNotifications(any(UUID.class), any())).thenReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/notifications")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].title").value("Congratulations! 🎉"))
                    .andExpect(jsonPath("$.content[0].type").value("COURSE_COMPLETED"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(notificationService).getNotifications(eq(userId), any());
        }

        @Test
        @DisplayName("Should return empty page when no notifications")
        void getNotifications_Empty() throws Exception {
            // Given
            Page<NotificationDTO> emptyPage = new PageImpl<>(
                    Collections.emptyList(),
                    PageRequest.of(0, 20),
                    0);
            when(notificationService.getNotifications(any(UUID.class), any())).thenReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/v1/notifications")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    // ========== GET /api/v1/notifications/unread-count Tests ==========

    @Nested
    @DisplayName("GET /api/v1/notifications/unread-count")
    class GetUnreadCountTests {

        @Test
        @DisplayName("Should return unread count")
        void getUnreadCount_ReturnsOk() throws Exception {
            // Given
            UnreadCountDTO countDTO = UnreadCountDTO.builder()
                    .unreadCount(5)
                    .highPriorityCount(2)
                    .build();
            when(notificationService.getUnreadCount(any(UUID.class))).thenReturn(countDTO);

            // When & Then
            mockMvc.perform(get("/api/v1/notifications/unread-count")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount").value(5))
                    .andExpect(jsonPath("$.highPriorityCount").value(2));

            verify(notificationService).getUnreadCount(eq(userId));
        }

        @Test
        @DisplayName("Should return zero when no unread notifications")
        void getUnreadCount_Zero() throws Exception {
            // Given
            UnreadCountDTO countDTO = UnreadCountDTO.builder()
                    .unreadCount(0)
                    .highPriorityCount(0)
                    .build();
            when(notificationService.getUnreadCount(any(UUID.class))).thenReturn(countDTO);

            // When & Then
            mockMvc.perform(get("/api/v1/notifications/unread-count")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount").value(0))
                    .andExpect(jsonPath("$.highPriorityCount").value(0));
        }
    }

    // ========== GET /api/v1/notifications/{id} Tests ==========

    @Nested
    @DisplayName("GET /api/v1/notifications/{id}")
    class GetNotificationByIdTests {

        @Test
        @DisplayName("Should return notification when found")
        void getNotificationById_Found() throws Exception {
            // Given
            when(notificationService.getNotificationById(any(UUID.class), eq(notificationId)))
                    .thenReturn(mockNotificationDTO);

            // When & Then
            mockMvc.perform(get("/api/v1/notifications/{id}", notificationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(notificationId.toString()))
                    .andExpect(jsonPath("$.title").value("Congratulations! 🎉"))
                    .andExpect(jsonPath("$.type").value("COURSE_COMPLETED"));

            verify(notificationService).getNotificationById(eq(userId), eq(notificationId));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void getNotificationById_NotFound() throws Exception {
            // Given
            when(notificationService.getNotificationById(any(UUID.class), eq(notificationId)))
                    .thenThrow(new ResourceNotFoundException("Notification", notificationId));

            // When & Then
            mockMvc.perform(get("/api/v1/notifications/{id}", notificationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== PUT /api/v1/notifications/{id}/read Tests ==========

    @Nested
    @DisplayName("PUT /api/v1/notifications/{id}/read")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should mark notification as read")
        void markAsRead_Success() throws Exception {
            // Given
            doNothing().when(notificationService).markAsRead(any(UUID.class), eq(notificationId));

            // When & Then
            mockMvc.perform(put("/api/v1/notifications/{id}/read", notificationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Notification marked as read"));

            verify(notificationService).markAsRead(eq(userId), eq(notificationId));
        }

        @Test
        @DisplayName("Should return 404 when notification not found")
        void markAsRead_NotFound() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("Notification", notificationId))
                    .when(notificationService).markAsRead(any(UUID.class), eq(notificationId));

            // When & Then
            mockMvc.perform(put("/api/v1/notifications/{id}/read", notificationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== PUT /api/v1/notifications/read-all Tests ==========

    @Nested
    @DisplayName("PUT /api/v1/notifications/read-all")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("Should mark all notifications as read")
        void markAllAsRead_Success() throws Exception {
            // Given
            when(notificationService.markAllAsRead(any(UUID.class))).thenReturn(5);

            // When & Then
            mockMvc.perform(put("/api/v1/notifications/read-all")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("5 notifications marked as read"));

            verify(notificationService).markAllAsRead(eq(userId));
        }

        @Test
        @DisplayName("Should return zero when no notifications to mark")
        void markAllAsRead_None() throws Exception {
            // Given
            when(notificationService.markAllAsRead(any(UUID.class))).thenReturn(0);

            // When & Then
            mockMvc.perform(put("/api/v1/notifications/read-all")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("0 notifications marked as read"));
        }
    }

    // ========== DELETE /api/v1/notifications/{id} Tests ==========

    @Nested
    @DisplayName("DELETE /api/v1/notifications/{id}")
    class DeleteNotificationTests {

        @Test
        @DisplayName("Should delete notification")
        void deleteNotification_Success() throws Exception {
            // Given
            doNothing().when(notificationService).deleteNotification(any(UUID.class), eq(notificationId));

            // When & Then
            mockMvc.perform(delete("/api/v1/notifications/{id}", notificationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(notificationService).deleteNotification(eq(userId), eq(notificationId));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void deleteNotification_NotFound() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("Notification", notificationId))
                    .when(notificationService).deleteNotification(any(UUID.class), eq(notificationId));

            // When & Then
            mockMvc.perform(delete("/api/v1/notifications/{id}", notificationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== DELETE /api/v1/notifications Tests ==========

    @Nested
    @DisplayName("DELETE /api/v1/notifications")
    class DeleteAllReadNotificationsTests {

        @Test
        @DisplayName("Should delete all read notifications")
        void deleteAllReadNotifications_Success() throws Exception {
            // Given
            when(notificationService.deleteAllReadNotifications(any(UUID.class))).thenReturn(10);

            // When & Then
            mockMvc.perform(delete("/api/v1/notifications")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("10 read notifications deleted"));

            verify(notificationService).deleteAllReadNotifications(eq(userId));
        }

        @Test
        @DisplayName("Should return zero when no read notifications to delete")
        void deleteAllReadNotifications_None() throws Exception {
            // Given
            when(notificationService.deleteAllReadNotifications(any(UUID.class))).thenReturn(0);

            // When & Then
            mockMvc.perform(delete("/api/v1/notifications")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("0 read notifications deleted"));
        }
    }

    // ========== GET /api/v1/notifications/preferences Tests ==========

    @Nested
    @DisplayName("GET /api/v1/notifications/preferences")
    class GetPreferencesTests {

        @Test
        @DisplayName("Should return preferences")
        void getPreferences_Success() throws Exception {
            // Given
            when(notificationService.getPreferences(any(UUID.class))).thenReturn(mockPreferencesDTO);

            // When & Then
            mockMvc.perform(get("/api/v1/notifications/preferences")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inAppEnabled").value(true))
                    .andExpect(jsonPath("$.emailEnabled").value(true))
                    .andExpect(jsonPath("$.pushEnabled").value(false))
                    .andExpect(jsonPath("$.quietHoursTimezone").value("Asia/Ho_Chi_Minh"));

            verify(notificationService).getPreferences(eq(userId));
        }
    }

    // ========== PUT /api/v1/notifications/preferences Tests ==========

    @Nested
    @DisplayName("PUT /api/v1/notifications/preferences")
    class UpdatePreferencesTests {

        @Test
        @DisplayName("Should update preferences")
        void updatePreferences_Success() throws Exception {
            // Given
            when(notificationService.updatePreferences(any(UUID.class), any(NotificationPreferencesDTO.class)))
                    .thenReturn(mockPreferencesDTO);

            // When & Then
            mockMvc.perform(put("/api/v1/notifications/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mockPreferencesDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inAppEnabled").value(true))
                    .andExpect(jsonPath("$.emailEnabled").value(true));

            verify(notificationService).updatePreferences(eq(userId), any(NotificationPreferencesDTO.class));
        }

        @Test
        @DisplayName("Should update preferences with quiet hours disabled")
        void updatePreferences_QuietHoursDisabled() throws Exception {
            // Given
            NotificationPreferencesDTO noQuietHours = NotificationPreferencesDTO.builder()
                    .inAppEnabled(true)
                    .emailEnabled(false)
                    .pushEnabled(true)
                    .learningEnabled(true)
                    .achievementsEnabled(true)
                    .remindersEnabled(false)
                    .systemEnabled(true)
                    .quietHoursStart(null)
                    .quietHoursEnd(null)
                    .quietHoursTimezone(null)
                    .build();

            when(notificationService.updatePreferences(any(UUID.class), any(NotificationPreferencesDTO.class)))
                    .thenReturn(noQuietHours);

            // When & Then
            mockMvc.perform(put("/api/v1/notifications/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(noQuietHours)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inAppEnabled").value(true))
                    .andExpect(jsonPath("$.emailEnabled").value(false))
                    .andExpect(jsonPath("$.pushEnabled").value(true))
                    .andExpect(jsonPath("$.remindersEnabled").value(false));
        }
    }
}
