package com.lexia.backend.email.event;

import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.service.EmailService;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.notification.entity.NotificationPreferences;
import com.lexia.backend.notification.event.*;
import com.lexia.backend.notification.repository.NotificationPreferencesRepository;
import com.lexia.backend.repository.UserRepository;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailEventListener.
 * Tests event handling, preference checking, and email queueing.
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailEventListener Tests")
class EmailEventListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @InjectMocks
    private EmailEventListener emailEventListener;

    @Captor
    private ArgumentCaptor<EmailRequest> emailRequestCaptor;

    private User testUser;
    private UserProfile testProfile;
    private NotificationPreferences testPreferences;
    private UUID userId;
    private Object eventSource;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventSource = this;

        testProfile = new UserProfile();
        testProfile.setFirstName("John");
        testProfile.setLastName("Doe");
        testProfile.setLanguage("en");

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("john.doe@example.com");
        testUser.setProfile(testProfile);

        testPreferences = new NotificationPreferences();
        testPreferences.setUserId(userId);
        testPreferences.setEmailEnabled(true);
        testPreferences.setLearningEnabled(true);
        testPreferences.setAchievementsEnabled(true);
        testPreferences.setRemindersEnabled(true);
        testPreferences.setSystemEnabled(true);
    }

    @Nested
    @DisplayName("Course Completed Event Tests")
    class CourseCompletedEventTests {

        @Test
        @DisplayName("Should queue email when course completed and preferences allow")
        void shouldQueueEmailWhenCourseCompleted() {
            // Given
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "English Basics", 120);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            EmailRequest request = emailRequestCaptor.getValue();

            assertEquals(userId, request.getRecipientId());
            assertEquals("john.doe@example.com", request.getRecipientEmail());
            assertEquals(EmailType.COURSE_COMPLETED, request.getEmailType());
            assertEquals("John Doe", request.getRecipientName());
            assertEquals("English Basics", request.getTemplateData().get("courseTitle"));
        }

        @Test
        @DisplayName("Should not queue email when user preferences disable achievements")
        void shouldNotQueueEmailWhenAchievementsDisabled() {
            // Given
            testPreferences.setAchievementsEnabled(false);
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "English Basics", 120);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService, never()).queueEmail(any());
        }

        @Test
        @DisplayName("Should not queue email when user not found")
        void shouldNotQueueEmailWhenUserNotFound() {
            // Given
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "English Basics", 120);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService, never()).queueEmail(any());
        }
    }

    @Nested
    @DisplayName("Enrollment Confirmed Event Tests")
    class EnrollmentConfirmedEventTests {

        @Test
        @DisplayName("Should queue email when enrollment confirmed")
        void shouldQueueEmailWhenEnrollmentConfirmed() {
            // Given
            EnrollmentConfirmedEvent event = new EnrollmentConfirmedEvent(
                    eventSource, userId, 1L, "Business English", "B1", "https://example.com/thumb.jpg");
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleEnrollmentConfirmed(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            EmailRequest request = emailRequestCaptor.getValue();

            assertEquals(EmailType.ENROLLMENT_CONFIRMATION, request.getEmailType());
            assertEquals("Business English", request.getTemplateData().get("courseTitle"));
            assertEquals("B1", request.getTemplateData().get("cefrLevel"));
        }

        @Test
        @DisplayName("Should not queue email when learning category disabled")
        void shouldNotQueueEmailWhenLearningDisabled() {
            // Given
            testPreferences.setLearningEnabled(false);
            EnrollmentConfirmedEvent event = new EnrollmentConfirmedEvent(
                    eventSource, userId, 1L, "Business English", "B1", "https://example.com/thumb.jpg");
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleEnrollmentConfirmed(event);

            // Then
            verify(emailService, never()).queueEmail(any());
        }
    }

    @Nested
    @DisplayName("Streak Milestone Event Tests")
    class StreakMilestoneEventTests {

        @Test
        @DisplayName("Should queue email when streak milestone reached")
        void shouldQueueEmailWhenStreakMilestoneReached() {
            // Given
            StreakMilestoneEvent event = new StreakMilestoneEvent(eventSource, userId, 30);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleStreakMilestone(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            EmailRequest request = emailRequestCaptor.getValue();

            assertEquals(EmailType.STREAK_MILESTONE, request.getEmailType());
            assertEquals(30, request.getTemplateData().get("streakDays"));
            assertEquals(60, request.getTemplateData().get("nextMilestone"));
        }

        @Test
        @DisplayName("Should calculate correct next milestone")
        void shouldCalculateCorrectNextMilestone() {
            // Given - User with 90-day streak
            StreakMilestoneEvent event = new StreakMilestoneEvent(eventSource, userId, 90);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleStreakMilestone(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            EmailRequest request = emailRequestCaptor.getValue();

            assertEquals(180, request.getTemplateData().get("nextMilestone"));
        }
    }

    @Nested
    @DisplayName("Streak Lost Event Tests")
    class StreakLostEventTests {

        @Test
        @DisplayName("Should queue email when streak lost")
        void shouldQueueEmailWhenStreakLost() {
            // Given
            StreakLostEvent event = new StreakLostEvent(eventSource, userId, 15);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleStreakLost(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            EmailRequest request = emailRequestCaptor.getValue();

            assertEquals(EmailType.STREAK_LOST, request.getEmailType());
            assertEquals(15, request.getTemplateData().get("previousStreak"));
        }
    }

    @Nested
    @DisplayName("Level Up Event Tests")
    class LevelUpEventTests {

        @Test
        @DisplayName("Should queue email when user levels up")
        void shouldQueueEmailWhenLevelUp() {
            // Given
            LevelUpEvent event = new LevelUpEvent(eventSource, userId, "A1", "A2");
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleLevelUp(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            EmailRequest request = emailRequestCaptor.getValue();

            assertEquals(EmailType.LEVEL_UP, request.getEmailType());
            assertEquals("A1", request.getTemplateData().get("previousLevel"));
            assertEquals("A2", request.getTemplateData().get("newLevel"));
            assertNotNull(request.getTemplateData().get("levelDescription"));
        }

        @Test
        @DisplayName("Should include correct level descriptions")
        void shouldIncludeCorrectLevelDescriptions() {
            // Given
            LevelUpEvent event = new LevelUpEvent(eventSource, userId, "B1", "B2");
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleLevelUp(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            EmailRequest request = emailRequestCaptor.getValue();

            String levelDescription = (String) request.getTemplateData().get("levelDescription");
            assertTrue(levelDescription.contains("Upper Intermediate"));
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
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "Test", 60);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            assertEquals("Jonathan Doe", emailRequestCaptor.getValue().getRecipientName());
        }

        @Test
        @DisplayName("Should use first name + last name when full name not set")
        void shouldUseFirstAndLastName() {
            // Given
            testProfile.setFullName(null);
            testProfile.setFirstName("John");
            testProfile.setLastName("Doe");
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "Test", 60);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            assertEquals("John Doe", emailRequestCaptor.getValue().getRecipientName());
        }

        @Test
        @DisplayName("Should fallback to email prefix when no profile")
        void shouldFallbackToEmailPrefix() {
            // Given
            testUser.setProfile(null);
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "Test", 60);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            assertEquals("john.doe", emailRequestCaptor.getValue().getRecipientName());
        }
    }

    @Nested
    @DisplayName("User Locale Tests")
    class UserLocaleTests {

        @Test
        @DisplayName("Should use Vietnamese locale when profile language is vi")
        void shouldUseVietnameseLocale() {
            // Given
            testProfile.setLanguage("vi");
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "Test", 60);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            assertEquals("vi", emailRequestCaptor.getValue().getLocale());
        }

        @Test
        @DisplayName("Should default to English locale")
        void shouldDefaultToEnglishLocale() {
            // Given
            testProfile.setLanguage(null);
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "Test", 60);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService).queueEmail(emailRequestCaptor.capture());
            assertEquals("en", emailRequestCaptor.getValue().getLocale());
        }
    }

    @Nested
    @DisplayName("Global Email Toggle Tests")
    class GlobalEmailToggleTests {

        @Test
        @DisplayName("Should not send email when global email is disabled")
        void shouldNotSendEmailWhenGlobalEmailDisabled() {
            // Given
            testPreferences.setEmailEnabled(false);
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "Test", 60);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService, never()).queueEmail(any());
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should send email when no preferences exist (default behavior)")
        void shouldSendEmailWhenNoPreferencesExist() {
            // Given
            CourseCompletedEvent event = new CourseCompletedEvent(eventSource, userId, 1L, "Test", 60);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            emailEventListener.handleCourseCompleted(event);

            // Then
            verify(emailService).queueEmail(any());
        }
    }
}
