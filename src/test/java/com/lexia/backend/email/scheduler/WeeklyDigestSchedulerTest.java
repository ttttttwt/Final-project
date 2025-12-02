package com.lexia.backend.email.scheduler;

import com.lexia.backend.dto.ProgressSummaryDTO;
import com.lexia.backend.dto.StreakDTO;
import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.service.EmailService;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.notification.entity.NotificationPreferences;
import com.lexia.backend.notification.repository.NotificationPreferencesRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WeeklyDigestScheduler.
 * Tests digest generation, user filtering, and template data building.
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklyDigestScheduler Tests")
class WeeklyDigestSchedulerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private WeeklyDigestScheduler scheduler;

    @Captor
    private ArgumentCaptor<EmailRequest> emailRequestCaptor;

    private User activeUser;
    private User inactiveUser;
    private User optedOutUser;
    private UserProfile testProfile;
    private NotificationPreferences enabledPreferences;
    private NotificationPreferences disabledPreferences;
    private ProgressSummaryDTO progressSummary;
    private StreakDTO streakDTO;

    @BeforeEach
    void setUp() {
        UUID activeUserId = UUID.randomUUID();
        UUID inactiveUserId = UUID.randomUUID();
        UUID optedOutUserId = UUID.randomUUID();

        // Active user with profile
        testProfile = new UserProfile();
        testProfile.setFullName("John Doe");
        testProfile.setFirstName("John");
        testProfile.setLastName("Doe");
        testProfile.setLanguage("en");

        activeUser = new User();
        activeUser.setId(activeUserId);
        activeUser.setEmail("active@example.com");
        activeUser.setIsActive(true);
        activeUser.setProfile(testProfile);

        // Inactive user
        inactiveUser = new User();
        inactiveUser.setId(inactiveUserId);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setIsActive(false);

        // User who opted out
        optedOutUser = new User();
        optedOutUser.setId(optedOutUserId);
        optedOutUser.setEmail("optedout@example.com");
        optedOutUser.setIsActive(true);

        // Enabled preferences
        enabledPreferences = new NotificationPreferences();
        enabledPreferences.setUserId(activeUserId);
        enabledPreferences.setEmailEnabled(true);
        enabledPreferences.setRemindersEnabled(true);

        // Disabled preferences
        disabledPreferences = new NotificationPreferences();
        disabledPreferences.setUserId(optedOutUserId);
        disabledPreferences.setEmailEnabled(false);
        disabledPreferences.setRemindersEnabled(false);

        // Progress summary
        progressSummary = ProgressSummaryDTO.builder()
                .totalLessonsCompleted(15)
                .totalTimeSpentMinutes(180)
                .activeDays(5)
                .build();

        // Streak DTO
        streakDTO = StreakDTO.builder()
                .currentStreak(10)
                .longestStreak(30)
                .build();
    }

    @Nested
    @DisplayName("Send Weekly Digests Tests")
    class SendWeeklyDigestsTests {

        @Test
        @DisplayName("Should send digest only to active users with enabled preferences")
        void shouldSendDigestOnlyToActiveUsersWithEnabledPreferences() {
            // Given
            when(userRepository.findAll()).thenReturn(List.of(activeUser, inactiveUser, optedOutUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(preferencesRepository.findByUserId(optedOutUser.getId())).thenReturn(Optional.of(disabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService, times(1)).queueEmail(any(EmailRequest.class));
        }

        @Test
        @DisplayName("Should use default preferences when none set")
        void shouldUseDefaultPreferencesWhenNoneSet() {
            // Given
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.empty());
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService, times(1)).queueEmail(any(EmailRequest.class));
        }

        @Test
        @DisplayName("Should skip users with global email disabled")
        void shouldSkipUsersWithGlobalEmailDisabled() {
            // Given
            enabledPreferences.setEmailEnabled(false);
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService, never()).queueEmail(any());
        }

        @Test
        @DisplayName("Should skip users with reminders disabled")
        void shouldSkipUsersWithRemindersDisabled() {
            // Given
            enabledPreferences.setRemindersEnabled(false);
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService, never()).queueEmail(any());
        }

        @Test
        @DisplayName("Should handle errors for individual users without stopping")
        void shouldHandleErrorsForIndividualUsersWithoutStopping() {
            // Given
            User anotherActiveUser = new User();
            anotherActiveUser.setId(UUID.randomUUID());
            anotherActiveUser.setEmail("another@example.com");
            anotherActiveUser.setIsActive(true);
            anotherActiveUser.setProfile(testProfile);

            when(userRepository.findAll()).thenReturn(List.of(activeUser, anotherActiveUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(preferencesRepository.findByUserId(anotherActiveUser.getId())).thenReturn(Optional.empty());
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenThrow(new RuntimeException("Error"));
            when(progressService.getProgressSummary(eq(anotherActiveUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(anotherActiveUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then - One user errored, but the other should still get email
            verify(emailService, times(1)).queueEmail(any(EmailRequest.class));
        }
    }

    @Nested
    @DisplayName("Template Data Building Tests")
    class TemplateDataBuildingTests {

        @Test
        @DisplayName("Should build correct template data")
        void shouldBuildCorrectTemplateData() {
            // Given
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            EmailRequest request = emailRequestCaptor.getValue();

            assertEquals(EmailType.WEEKLY_PROGRESS, request.getEmailType());
            assertEquals("active@example.com", request.getRecipientEmail());
            assertEquals("en", request.getLocale());

            Map<String, Object> data = request.getTemplateData();
            assertEquals("John Doe", data.get("userName"));
            assertEquals(15L, data.get("lessonsCompleted"));
            assertEquals(180L, data.get("totalMinutes"));
            assertEquals(5, data.get("activeDays"));
            assertEquals(10, data.get("currentStreak"));
            assertEquals(30, data.get("longestStreak"));
            assertTrue((Boolean) data.get("streakActive"));
            assertTrue((Boolean) data.get("hasActivity"));
            assertNotNull(data.get("motivationalMessage"));
            assertNotNull(data.get("suggestedGoal"));
        }

        @Test
        @DisplayName("Should handle null streak")
        void shouldHandleNullStreak() {
            // Given
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(null);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            Map<String, Object> data = emailRequestCaptor.getValue().getTemplateData();

            assertEquals(0, data.get("currentStreak"));
            assertEquals(0, data.get("longestStreak"));
            assertFalse((Boolean) data.get("streakActive"));
        }

        @Test
        @DisplayName("Should format hours correctly")
        void shouldFormatHoursCorrectly() {
            // Given
            progressSummary = ProgressSummaryDTO.builder()
                    .totalLessonsCompleted(10)
                    .totalTimeSpentMinutes(90)
                    .activeDays(3)
                    .build();

            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            Map<String, Object> data = emailRequestCaptor.getValue().getTemplateData();

            assertEquals("1 hr 30 min", data.get("totalHours"));
        }

        @Test
        @DisplayName("Should calculate average minutes per day")
        void shouldCalculateAverageMinutesPerDay() {
            // Given
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            Map<String, Object> data = emailRequestCaptor.getValue().getTemplateData();

            // 180 minutes / 5 days = 36 minutes per day
            assertEquals(36L, data.get("averageMinutesPerDay"));
        }
    }

    @Nested
    @DisplayName("Motivational Message Tests")
    class MotivationalMessageTests {

        @Test
        @DisplayName("Should show no activity message when no lessons completed")
        void shouldShowNoActivityMessage() {
            // Given
            progressSummary = ProgressSummaryDTO.builder()
                    .totalLessonsCompleted(0)
                    .totalTimeSpentMinutes(0)
                    .activeDays(0)
                    .build();

            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(null);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            String message = (String) emailRequestCaptor.getValue().getTemplateData().get("motivationalMessage");

            assertTrue(message.contains("journey begins"));
        }

        @Test
        @DisplayName("Should show streak celebration for 7+ day streak")
        void shouldShowStreakCelebration() {
            // Given
            streakDTO = StreakDTO.builder().currentStreak(7).longestStreak(7).build();

            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            String message = (String) emailRequestCaptor.getValue().getTemplateData().get("motivationalMessage");

            assertTrue(message.contains("streak") || message.contains("week"));
        }
    }

    @Nested
    @DisplayName("User Display Name Tests")
    class UserDisplayNameTests {

        @Test
        @DisplayName("Should use full name when available")
        void shouldUseFullNameWhenAvailable() {
            // Given
            testProfile.setFullName("Jonathan Doe");
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            assertEquals("Jonathan Doe", emailRequestCaptor.getValue().getTemplateData().get("userName"));
        }

        @Test
        @DisplayName("Should fallback to email prefix when no profile")
        void shouldFallbackToEmailPrefix() {
            // Given
            activeUser.setProfile(null);
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            assertEquals("active", emailRequestCaptor.getValue().getTemplateData().get("userName"));
        }
    }

    @Nested
    @DisplayName("User Locale Tests")
    class UserLocaleTests {

        @Test
        @DisplayName("Should use Vietnamese locale")
        void shouldUseVietnameseLocale() {
            // Given
            testProfile.setLanguage("vi");
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            assertEquals("vi", emailRequestCaptor.getValue().getLocale());
        }

        @Test
        @DisplayName("Should default to English locale")
        void shouldDefaultToEnglishLocale() {
            // Given
            testProfile.setLanguage(null);
            when(userRepository.findAll()).thenReturn(List.of(activeUser));
            when(preferencesRepository.findByUserId(activeUser.getId())).thenReturn(Optional.of(enabledPreferences));
            when(progressService.getProgressSummary(eq(activeUser), eq(7))).thenReturn(progressSummary);
            when(progressService.getStreak(eq(activeUser))).thenReturn(streakDTO);

            // When
            scheduler.sendWeeklyDigests();

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            assertEquals("en", emailRequestCaptor.getValue().getLocale());
        }
    }
}
