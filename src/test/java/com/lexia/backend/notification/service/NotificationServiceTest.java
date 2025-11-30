package com.lexia.backend.notification.service;

import com.lexia.backend.entity.User;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.notification.dto.*;
import com.lexia.backend.notification.entity.Notification;
import com.lexia.backend.notification.entity.Notification.NotificationPriority;
import com.lexia.backend.notification.entity.Notification.NotificationType;
import com.lexia.backend.notification.entity.NotificationPreferences;
import com.lexia.backend.notification.repository.NotificationPreferencesRepository;
import com.lexia.backend.notification.repository.NotificationRepository;
import com.lexia.backend.notification.service.impl.NotificationServiceImpl;
import com.lexia.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 * Tests all CRUD operations, preferences, and notification delivery.
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID userId;
    private UUID notificationId;
    private User testUser;
    private Notification testNotification;
    private NotificationPreferences testPreferences;
    private CreateNotificationRequest createRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@lexia.app")
                .isActive(true)
                .build();

        testNotification = Notification.builder()
                .id(notificationId)
                .user(testUser)
                .type(NotificationType.COURSE_COMPLETED)
                .title("Congratulations! 🎉")
                .message("You completed Business English Basics")
                .data(Map.of("courseId", "123", "courseTitle", "Business English Basics"))
                .priority(NotificationPriority.HIGH)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(30))
                .build();

        testPreferences = NotificationPreferences.builder()
                .userId(userId)
                .user(testUser)
                .inAppEnabled(true)
                .emailEnabled(true)
                .pushEnabled(true)
                .learningEnabled(true)
                .achievementsEnabled(true)
                .remindersEnabled(true)
                .systemEnabled(true)
                .quietHoursTimezone("UTC")
                .build();

        createRequest = CreateNotificationRequest.builder()
                .type(NotificationType.COURSE_COMPLETED)
                .title("Congratulations! 🎉")
                .message("You completed Business English Basics")
                .data(Map.of("courseId", "123", "courseTitle", "Business English Basics"))
                .priority(NotificationPriority.HIGH)
                .build();
    }

    // ========== GET NOTIFICATIONS TESTS ==========

    @Nested
    @DisplayName("getNotifications Tests")
    class GetNotificationsTests {

        @Test
        @DisplayName("Should return paginated notifications for user")
        void testGetNotifications_Success() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            List<Notification> notifications = List.of(testNotification);
            Page<Notification> page = new PageImpl<>(notifications, pageable, 1);

            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(page);

            // Act
            Page<NotificationDTO> result = notificationService.getNotifications(userId, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Congratulations! 🎉", result.getContent().get(0).getTitle());
            verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no notifications")
        void testGetNotifications_Empty() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<Notification> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(emptyPage);

            // Act
            Page<NotificationDTO> result = notificationService.getNotifications(userId, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }
    }

    // ========== GET NOTIFICATION BY ID TESTS ==========

    @Nested
    @DisplayName("getNotificationById Tests")
    class GetNotificationByIdTests {

        @Test
        @DisplayName("Should return notification when found")
        void testGetNotificationById_Found() {
            // Arrange
            when(notificationRepository.findByIdAndUserId(notificationId, userId))
                    .thenReturn(Optional.of(testNotification));

            // Act
            NotificationDTO result = notificationService.getNotificationById(userId, notificationId);

            // Assert
            assertNotNull(result);
            assertEquals(notificationId, result.getId());
            assertEquals("Congratulations! 🎉", result.getTitle());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void testGetNotificationById_NotFound() {
            // Arrange
            when(notificationRepository.findByIdAndUserId(notificationId, userId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> notificationService.getNotificationById(userId, notificationId));
        }
    }

    // ========== MARK AS READ TESTS ==========

    @Nested
    @DisplayName("markAsRead Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should mark notification as read")
        void testMarkAsRead_Success() {
            // Arrange
            when(notificationRepository.findByIdAndUserId(notificationId, userId))
                    .thenReturn(Optional.of(testNotification));
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            // Act
            notificationService.markAsRead(userId, notificationId);

            // Assert
            assertTrue(testNotification.getIsRead());
            assertNotNull(testNotification.getReadAt());
            verify(notificationRepository).save(testNotification);
        }

        @Test
        @DisplayName("Should not update already read notification")
        void testMarkAsRead_AlreadyRead() {
            // Arrange
            testNotification.setIsRead(true);
            testNotification.setReadAt(OffsetDateTime.now().minusHours(1));

            when(notificationRepository.findByIdAndUserId(notificationId, userId))
                    .thenReturn(Optional.of(testNotification));

            // Act
            notificationService.markAsRead(userId, notificationId);

            // Assert
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when notification not found")
        void testMarkAsRead_NotFound() {
            // Arrange
            when(notificationRepository.findByIdAndUserId(notificationId, userId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> notificationService.markAsRead(userId, notificationId));
        }
    }

    // ========== MARK ALL AS READ TESTS ==========

    @Nested
    @DisplayName("markAllAsRead Tests")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("Should mark all notifications as read")
        void testMarkAllAsRead_Success() {
            // Arrange
            when(notificationRepository.markAllAsReadByUserId(eq(userId), any(OffsetDateTime.class)))
                    .thenReturn(5);

            // Act
            int count = notificationService.markAllAsRead(userId);

            // Assert
            assertEquals(5, count);
            verify(notificationRepository).markAllAsReadByUserId(eq(userId), any(OffsetDateTime.class));
        }
    }

    // ========== DELETE NOTIFICATION TESTS ==========

    @Nested
    @DisplayName("deleteNotification Tests")
    class DeleteNotificationTests {

        @Test
        @DisplayName("Should delete notification when found")
        void testDeleteNotification_Success() {
            // Arrange
            when(notificationRepository.findByIdAndUserId(notificationId, userId))
                    .thenReturn(Optional.of(testNotification));

            // Act
            notificationService.deleteNotification(userId, notificationId);

            // Assert
            verify(notificationRepository).delete(testNotification);
        }

        @Test
        @DisplayName("Should throw exception when notification not found")
        void testDeleteNotification_NotFound() {
            // Arrange
            when(notificationRepository.findByIdAndUserId(notificationId, userId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> notificationService.deleteNotification(userId, notificationId));
        }
    }

    // ========== DELETE ALL READ NOTIFICATIONS TESTS ==========

    @Nested
    @DisplayName("deleteAllReadNotifications Tests")
    class DeleteAllReadNotificationsTests {

        @Test
        @DisplayName("Should delete all read notifications")
        void testDeleteAllReadNotifications_Success() {
            // Arrange
            when(notificationRepository.deleteAllReadByUserId(userId)).thenReturn(10);

            // Act
            int count = notificationService.deleteAllReadNotifications(userId);

            // Assert
            assertEquals(10, count);
            verify(notificationRepository).deleteAllReadByUserId(userId);
        }
    }

    // ========== UNREAD COUNT TESTS ==========

    @Nested
    @DisplayName("getUnreadCount Tests")
    class GetUnreadCountTests {

        @Test
        @DisplayName("Should return unread count")
        void testGetUnreadCount_Success() {
            // Arrange
            when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(5L);
            when(notificationRepository.countHighPriorityUnreadByUserId(userId)).thenReturn(2L);

            // Act
            UnreadCountDTO result = notificationService.getUnreadCount(userId);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.getUnreadCount());
            assertEquals(2, result.getHighPriorityCount());
        }
    }

    // ========== CREATE NOTIFICATION TESTS ==========

    @Nested
    @DisplayName("createNotification Tests")
    class CreateNotificationTests {

        @Test
        @DisplayName("Should always create notification regardless of preferences")
        void testCreateNotification_Success() {
            // Arrange - preferences don't matter for persistence anymore
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            // Act
            NotificationDTO result = notificationService.createNotification(userId, createRequest);

            // Assert
            assertNotNull(result);
            assertEquals("Congratulations! 🎉", result.getTitle());
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should create notification even when in-app disabled (preferences only affect real-time)")
        void testCreateNotification_InAppDisabledStillSaves() {
            // Arrange - in-app disabled but notification should still be saved
            testPreferences.setInAppEnabled(false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            // Act
            NotificationDTO result = notificationService.createNotification(userId, createRequest);

            // Assert - notification should still be created
            assertNotNull(result);
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void testCreateNotification_UserNotFound() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> notificationService.createNotification(userId, createRequest));
        }
    }

    // ========== PREFERENCES TESTS ==========

    @Nested
    @DisplayName("Preferences Tests")
    class PreferencesTests {

        @Test
        @DisplayName("Should return preferences when exist")
        void testGetPreferences_Exists() {
            // Arrange
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // Act
            NotificationPreferencesDTO result = notificationService.getPreferences(userId);

            // Assert
            assertNotNull(result);
            assertTrue(result.getInAppEnabled());
            assertTrue(result.getLearningEnabled());
        }

        @Test
        @DisplayName("Should return default preferences when not exist")
        void testGetPreferences_Default() {
            // Arrange
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // Act
            NotificationPreferencesDTO result = notificationService.getPreferences(userId);

            // Assert
            assertNotNull(result);
            assertTrue(result.getInAppEnabled());
            assertTrue(result.getLearningEnabled());
            assertEquals("UTC", result.getQuietHoursTimezone());
        }

        @Test
        @DisplayName("Should update preferences")
        void testUpdatePreferences_Success() {
            // Arrange
            NotificationPreferencesDTO updateDTO = NotificationPreferencesDTO.builder()
                    .inAppEnabled(true)
                    .emailEnabled(false)
                    .pushEnabled(false)
                    .learningEnabled(true)
                    .achievementsEnabled(true)
                    .remindersEnabled(false)
                    .systemEnabled(true)
                    .quietHoursStart(LocalTime.of(22, 0))
                    .quietHoursEnd(LocalTime.of(7, 0))
                    .quietHoursTimezone("Asia/Ho_Chi_Minh")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any(NotificationPreferences.class))).thenReturn(testPreferences);

            // Act
            NotificationPreferencesDTO result = notificationService.updatePreferences(userId, updateDTO);

            // Assert
            assertNotNull(result);
            verify(preferencesRepository).save(any(NotificationPreferences.class));
        }
    }

    // ========== SHOULD NOTIFY TESTS (For Real-time Delivery) ==========

    @Nested
    @DisplayName("shouldNotify Tests (Real-time delivery only)")
    class ShouldNotifyTests {

        @Test
        @DisplayName("Should return true when no preferences exist (default to send real-time)")
        void testShouldNotify_NoPreferences() {
            // Arrange
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // Act
            boolean result = notificationService.shouldNotify(userId, createRequest);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when in-app disabled (no real-time, but still persisted)")
        void testShouldNotify_InAppDisabled() {
            // Arrange
            testPreferences.setInAppEnabled(false);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // Act
            boolean result = notificationService.shouldNotify(userId, createRequest);

            // Assert - false means no real-time delivery, but notification is still saved
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when achievements disabled for achievement notification")
        void testShouldNotify_AchievementsDisabled() {
            // Arrange
            testPreferences.setAchievementsEnabled(false);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // Course completed is an achievement type
            CreateNotificationRequest achievementRequest = CreateNotificationRequest.builder()
                    .type(NotificationType.ACHIEVEMENT_UNLOCKED)
                    .title("Achievement!")
                    .message("Test")
                    .build();

            // Act
            boolean result = notificationService.shouldNotify(userId, achievementRequest);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when category is enabled")
        void testShouldNotify_CategoryEnabled() {
            // Arrange
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // Act
            boolean result = notificationService.shouldNotify(userId, createRequest);

            // Assert
            assertTrue(result);
        }
    }

    // ========== BROADCAST TESTS ==========

    @Nested
    @DisplayName("broadcastNotification Tests")
    class BroadcastNotificationTests {

        @Test
        @DisplayName("Should broadcast to topic and persist for all active users")
        void testBroadcastNotification_Success() {
            // Arrange
            BroadcastNotificationRequest broadcastRequest = BroadcastNotificationRequest.builder()
                    .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                    .title("System Update")
                    .message("New features available!")
                    .priority(NotificationPriority.HIGH)
                    .build();

            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            List<UUID> activeUserIds = List.of(user1, user2);

            when(notificationRepository.findAllActiveUserIds()).thenReturn(activeUserIds);
            // New implementation uses saveAll for batch processing
            when(notificationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            int count = notificationService.broadcastNotification(broadcastRequest);

            // Assert
            assertEquals(2, count);
            // Should persist notifications in batch (saveAll instead of individual save)
            verify(notificationRepository, atLeastOnce()).saveAll(anyList());
            // Should broadcast to topic once (not individual WebSocket per user)
            verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/announcements"), any(NotificationDTO.class));
            // Should NOT send individual real-time notifications (no double delivery)
            verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should persist notifications for all active users using batch processing")
        void testBroadcastNotification_BatchProcessing() {
            // Arrange
            BroadcastNotificationRequest broadcastRequest = BroadcastNotificationRequest.builder()
                    .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                    .title("System Update")
                    .message("New features available!")
                    .priority(NotificationPriority.HIGH)
                    .build();

            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            List<UUID> activeUserIds = List.of(user1, user2);

            when(notificationRepository.findAllActiveUserIds()).thenReturn(activeUserIds);
            // New implementation uses saveAll for batch processing - doesn't need
            // userRepository.findById
            when(notificationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            int count = notificationService.broadcastNotification(broadcastRequest);

            // Assert - all users should be persisted since we use reference-only User
            // objects
            assertEquals(2, count);
            // Verify batch save was used (not individual saves)
            verify(notificationRepository, atLeastOnce()).saveAll(anyList());
            // Verify no individual User lookups were made (optimization)
            verify(userRepository, never()).findById(any());
        }
    }

    // ========== SEND TO USERS TESTS ==========

    @Nested
    @DisplayName("sendToUsers Tests")
    class SendToUsersTests {

        @Test
        @DisplayName("Should send to specific users and deliver real-time based on preferences")
        void testSendToUsers_Success() {
            // Arrange
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            SendNotificationRequest sendRequest = SendNotificationRequest.builder()
                    .userIds(List.of(user1, user2))
                    .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                    .title("Important Update")
                    .message("Check this out!")
                    .priority(NotificationPriority.NORMAL)
                    .build();

            when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            // Act
            int count = notificationService.sendToUsers(sendRequest);

            // Assert - notifications should be persisted for both users
            assertEquals(2, count);
            verify(notificationRepository, times(2)).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should skip users not found")
        void testSendToUsers_UserNotFound() {
            // Arrange
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            SendNotificationRequest sendRequest = SendNotificationRequest.builder()
                    .userIds(List.of(user1, user2))
                    .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                    .title("Important Update")
                    .message("Check this out!")
                    .priority(NotificationPriority.NORMAL)
                    .build();

            when(userRepository.findById(user1)).thenReturn(Optional.of(testUser));
            when(userRepository.findById(user2)).thenReturn(Optional.empty());
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            // Act
            int count = notificationService.sendToUsers(sendRequest);

            // Assert
            assertEquals(1, count);
        }
    }

    // ========== DELETE EXPIRED TESTS ==========

    @Nested
    @DisplayName("deleteExpiredNotifications Tests")
    class DeleteExpiredNotificationsTests {

        @Test
        @DisplayName("Should delete expired notifications")
        void testDeleteExpiredNotifications_Success() {
            // Arrange
            when(notificationRepository.deleteExpiredNotifications(any(OffsetDateTime.class)))
                    .thenReturn(100);

            // Act
            int count = notificationService.deleteExpiredNotifications();

            // Assert
            assertEquals(100, count);
            verify(notificationRepository).deleteExpiredNotifications(any(OffsetDateTime.class));
        }
    }
}
