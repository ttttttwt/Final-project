package com.lexia.backend.email.controller;

import com.lexia.backend.email.dto.EmailPreferencesDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.notification.entity.NotificationPreferences;
import com.lexia.backend.notification.repository.NotificationPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailPreferencesController.
 * Tests GET/PUT preferences, reset, and unsubscribe-all endpoints.
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailPreferencesController Tests")
class EmailPreferencesControllerTest {

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @InjectMocks
    private EmailPreferencesController controller;

    private User testUser;
    private NotificationPreferences testPreferences;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testPreferences = new NotificationPreferences();
        testPreferences.setUserId(userId);
        testPreferences.setEmailEnabled(true);
        testPreferences.setLearningEnabled(true);
        testPreferences.setAchievementsEnabled(true);
        testPreferences.setRemindersEnabled(true);
        testPreferences.setSystemEnabled(true);
        testPreferences.setUpdatedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/v1/users/me/email-preferences")
    class GetEmailPreferencesTests {

        @Test
        @DisplayName("Should return existing preferences")
        void shouldReturnExistingPreferences() {
            // Given
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.getEmailPreferences(testUser);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(userId, response.getBody().getUserId());
            assertTrue(response.getBody().isEmailVerificationEnabled()); // mandatory
            assertTrue(response.getBody().isSecurityAlertsEnabled()); // mandatory
            assertTrue(response.getBody().isWeeklyDigestEnabled());
            assertTrue(response.getBody().isStreakRemindersEnabled());
            assertTrue(response.getBody().isAchievementsEnabled());
            assertTrue(response.getBody().isCourseUpdatesEnabled());
            assertTrue(response.getBody().isAnnouncementsEnabled());
        }

        @Test
        @DisplayName("Should create default preferences when none exist")
        void shouldCreateDefaultPreferencesWhenNoneExist() {
            // Given
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.save(any())).thenAnswer(invocation -> {
                NotificationPreferences saved = invocation.getArgument(0);
                saved.setUpdatedAt(OffsetDateTime.now());
                return saved;
            });

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.getEmailPreferences(testUser);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmailVerificationEnabled());
            assertTrue(response.getBody().isSecurityAlertsEnabled());
            verify(preferencesRepository).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should return disabled preferences when email disabled")
        void shouldReturnDisabledPreferencesWhenEmailDisabled() {
            // Given
            testPreferences.setEmailEnabled(false);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.getEmailPreferences(testUser);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            // Mandatory emails always enabled
            assertTrue(response.getBody().isEmailVerificationEnabled());
            assertTrue(response.getBody().isSecurityAlertsEnabled());
            // Non-mandatory respect global setting
            assertFalse(response.getBody().isWeeklyDigestEnabled());
            assertFalse(response.getBody().isAchievementsEnabled());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/me/email-preferences")
    class UpdateEmailPreferencesTests {

        @Test
        @DisplayName("Should update preferences successfully")
        void shouldUpdatePreferencesSuccessfully() {
            // Given
            EmailPreferencesDTO updateRequest = EmailPreferencesDTO.builder()
                    .weeklyDigestEnabled(false)
                    .streakRemindersEnabled(true)
                    .achievementsEnabled(true)
                    .courseUpdatesEnabled(false)
                    .announcementsEnabled(true)
                    .build();

            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.updateEmailPreferences(testUser, updateRequest);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isCourseUpdatesEnabled());
            verify(preferencesRepository).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should create preferences if not exist on update")
        void shouldCreatePreferencesIfNotExistOnUpdate() {
            // Given
            EmailPreferencesDTO updateRequest = EmailPreferencesDTO.builder()
                    .weeklyDigestEnabled(true)
                    .streakRemindersEnabled(true)
                    .achievementsEnabled(true)
                    .courseUpdatesEnabled(true)
                    .announcementsEnabled(true)
                    .build();

            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.save(any())).thenAnswer(invocation -> {
                NotificationPreferences saved = invocation.getArgument(0);
                saved.setUpdatedAt(OffsetDateTime.now());
                return saved;
            });

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.updateEmailPreferences(testUser, updateRequest);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            verify(preferencesRepository, times(2)).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should disable all categories when all set to false")
        void shouldDisableAllCategoriesWhenAllSetToFalse() {
            // Given
            EmailPreferencesDTO updateRequest = EmailPreferencesDTO.builder()
                    .weeklyDigestEnabled(false)
                    .streakRemindersEnabled(false)
                    .achievementsEnabled(false)
                    .courseUpdatesEnabled(false)
                    .announcementsEnabled(false)
                    .build();

            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.updateEmailPreferences(testUser, updateRequest);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            // Mandatory still enabled
            assertTrue(response.getBody().isEmailVerificationEnabled());
            assertTrue(response.getBody().isSecurityAlertsEnabled());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/me/email-preferences/reset")
    class ResetEmailPreferencesTests {

        @Test
        @DisplayName("Should reset preferences to defaults")
        void shouldResetPreferencesToDefaults() {
            // Given
            testPreferences.setEmailEnabled(false);
            testPreferences.setLearningEnabled(false);
            testPreferences.setAchievementsEnabled(false);

            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.resetEmailPreferences(testUser);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isWeeklyDigestEnabled());
            assertTrue(response.getBody().isAchievementsEnabled());
            assertTrue(response.getBody().isCourseUpdatesEnabled());
            verify(preferencesRepository).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should create preferences if not exist on reset")
        void shouldCreatePreferencesIfNotExistOnReset() {
            // Given
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.save(any())).thenAnswer(invocation -> {
                NotificationPreferences saved = invocation.getArgument(0);
                saved.setUpdatedAt(OffsetDateTime.now());
                return saved;
            });

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.resetEmailPreferences(testUser);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            verify(preferencesRepository, times(2)).save(any(NotificationPreferences.class));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/me/email-preferences/unsubscribe-all")
    class UnsubscribeAllTests {

        @Test
        @DisplayName("Should unsubscribe from all emails")
        void shouldUnsubscribeFromAllEmails() {
            // Given
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.unsubscribeAll(testUser);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isWeeklyDigestEnabled());
            assertFalse(response.getBody().isStreakRemindersEnabled());
            assertFalse(response.getBody().isAchievementsEnabled());
            assertFalse(response.getBody().isCourseUpdatesEnabled());
            assertFalse(response.getBody().isAnnouncementsEnabled());
            // Mandatory emails still enabled
            assertTrue(response.getBody().isEmailVerificationEnabled());
            assertTrue(response.getBody().isSecurityAlertsEnabled());
            verify(preferencesRepository).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should create preferences if not exist on unsubscribe")
        void shouldCreatePreferencesIfNotExistOnUnsubscribe() {
            // Given
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.save(any())).thenAnswer(invocation -> {
                NotificationPreferences saved = invocation.getArgument(0);
                saved.setUpdatedAt(OffsetDateTime.now());
                return saved;
            });

            // When
            ResponseEntity<EmailPreferencesDTO> response = controller.unsubscribeAll(testUser);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            verify(preferencesRepository, times(2)).save(any(NotificationPreferences.class));
        }
    }
}
