package com.lexia.backend.email.controller;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.entity.EmailLog;
import com.lexia.backend.email.repository.EmailLogRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailTrackingController.
 * Tests token generation/validation, tracking pixel, click tracking, and
 * unsubscribe.
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailTrackingController Tests")
class EmailTrackingControllerTest {

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @Mock
    private EmailConfig emailConfig;

    @InjectMocks
    private EmailTrackingController controller;

    private UUID userId;
    private UUID emailLogId;
    private EmailLog testEmailLog;
    private NotificationPreferences testPreferences;
    private static final String TEST_SECRET = "test-secret-key-for-hmac-signing";
    private static final String TEST_BASE_URL = "https://lexia.app";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        emailLogId = UUID.randomUUID();

        testEmailLog = new EmailLog();
        testEmailLog.setId(emailLogId);
        testEmailLog.setOpenedAt(null);
        testEmailLog.setClickedAt(null);

        testPreferences = new NotificationPreferences();
        testPreferences.setUserId(userId);
        testPreferences.setEmailEnabled(true);
        testPreferences.setLearningEnabled(true);
        testPreferences.setAchievementsEnabled(true);
        testPreferences.setRemindersEnabled(true);
        testPreferences.setSystemEnabled(true);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid tracking token")
        void shouldGenerateValidTrackingToken() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);

            // When
            String token = controller.generateTrackingToken(emailLogId);

            // Then
            assertNotNull(token);
            assertTrue(token.contains("."));
            String[] parts = token.split("\\.");
            assertEquals(2, parts.length); // payload.signature
        }

        @Test
        @DisplayName("Should generate valid unsubscribe token")
        void shouldGenerateValidUnsubscribeToken() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);

            // When
            String token = controller.generateUnsubscribeToken(userId);

            // Then
            assertNotNull(token);
            assertTrue(token.contains("."));
        }
    }

    @Nested
    @DisplayName("Track Open Tests")
    class TrackOpenTests {

        @Test
        @DisplayName("Should return tracking pixel on valid token")
        void shouldReturnTrackingPixelOnValidToken() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            String token = controller.generateTrackingToken(emailLogId);
            when(emailLogRepository.findById(emailLogId)).thenReturn(Optional.of(testEmailLog));
            when(emailLogRepository.save(any())).thenReturn(testEmailLog);

            // When
            ResponseEntity<byte[]> response = controller.trackOpen(token);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("image/gif", response.getHeaders().getContentType().toString());
            verify(emailLogRepository).save(any(EmailLog.class));
        }

        @Test
        @DisplayName("Should return pixel even on invalid token")
        void shouldReturnPixelEvenOnInvalidToken() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);

            // When
            ResponseEntity<byte[]> response = controller.trackOpen("invalid.token");

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(emailLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not update if already opened")
        void shouldNotUpdateIfAlreadyOpened() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            String token = controller.generateTrackingToken(emailLogId);
            testEmailLog.setOpenedAt(java.time.Instant.now());
            when(emailLogRepository.findById(emailLogId)).thenReturn(Optional.of(testEmailLog));

            // When
            ResponseEntity<byte[]> response = controller.trackOpen(token);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(emailLogRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Track Click Tests")
    class TrackClickTests {

        @Test
        @DisplayName("Should redirect to target URL and record click")
        void shouldRedirectToTargetUrlAndRecordClick() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            String token = controller.generateTrackingToken(emailLogId);
            String targetUrl = "https://example.com/article";
            when(emailLogRepository.findById(emailLogId)).thenReturn(Optional.of(testEmailLog));
            when(emailLogRepository.save(any())).thenReturn(testEmailLog);

            // When
            ResponseEntity<Void> response = controller.trackClick(token, targetUrl);

            // Then
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertEquals(targetUrl, response.getHeaders().getLocation().toString());
            verify(emailLogRepository).save(any(EmailLog.class));
        }

        @Test
        @DisplayName("Should also mark as opened on click")
        void shouldAlsoMarkAsOpenedOnClick() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            String token = controller.generateTrackingToken(emailLogId);
            when(emailLogRepository.findById(emailLogId)).thenReturn(Optional.of(testEmailLog));
            when(emailLogRepository.save(any())).thenAnswer(invocation -> {
                EmailLog saved = invocation.getArgument(0);
                // Verify both fields are set
                assertNotNull(saved.getOpenedAt());
                assertNotNull(saved.getClickedAt());
                return saved;
            });

            // When
            controller.trackClick(token, "https://example.com");

            // Then
            verify(emailLogRepository).save(any(EmailLog.class));
        }

        @Test
        @DisplayName("Should redirect to base URL when target URL is blank")
        void shouldRedirectToBaseUrlWhenTargetUrlBlank() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            when(emailConfig.getBaseUrl()).thenReturn(TEST_BASE_URL);
            String token = controller.generateTrackingToken(emailLogId);
            when(emailLogRepository.findById(emailLogId)).thenReturn(Optional.empty());

            // When
            ResponseEntity<Void> response = controller.trackClick(token, "");

            // Then
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertEquals(TEST_BASE_URL, response.getHeaders().getLocation().toString());
        }
    }

    @Nested
    @DisplayName("Unsubscribe Tests")
    class UnsubscribeTests {

        @Test
        @DisplayName("Should unsubscribe user with valid token")
        void shouldUnsubscribeUserWithValidToken() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            when(emailConfig.getBaseUrl()).thenReturn(TEST_BASE_URL);
            String token = controller.generateUnsubscribeToken(userId);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any())).thenReturn(testPreferences);

            // When
            ResponseEntity<Void> response = controller.unsubscribe(token);

            // Then
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertTrue(response.getHeaders().getLocation().toString().contains("success=true"));
            verify(preferencesRepository).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should redirect to error page on invalid token")
        void shouldRedirectToErrorPageOnInvalidToken() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            when(emailConfig.getBaseUrl()).thenReturn(TEST_BASE_URL);

            // When
            ResponseEntity<Void> response = controller.unsubscribe("invalid.token");

            // Then
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertTrue(response.getHeaders().getLocation().toString().contains("error=invalid"));
        }
    }

    @Nested
    @DisplayName("Unsubscribe From Category Tests")
    class UnsubscribeFromCategoryTests {

        @Test
        @DisplayName("Should unsubscribe from learning category")
        void shouldUnsubscribeFromLearningCategory() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            when(emailConfig.getBaseUrl()).thenReturn(TEST_BASE_URL);
            String token = controller.generateUnsubscribeToken(userId);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any())).thenAnswer(invocation -> {
                NotificationPreferences saved = invocation.getArgument(0);
                assertFalse(saved.getLearningEnabled());
                return saved;
            });

            // When
            ResponseEntity<Void> response = controller.unsubscribeFromCategory(token, "learning");

            // Then
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertTrue(response.getHeaders().getLocation().toString().contains("category=learning"));
            verify(preferencesRepository).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should unsubscribe from achievements category")
        void shouldUnsubscribeFromAchievementsCategory() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            when(emailConfig.getBaseUrl()).thenReturn(TEST_BASE_URL);
            String token = controller.generateUnsubscribeToken(userId);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any())).thenAnswer(invocation -> {
                NotificationPreferences saved = invocation.getArgument(0);
                assertFalse(saved.getAchievementsEnabled());
                return saved;
            });

            // When
            ResponseEntity<Void> response = controller.unsubscribeFromCategory(token, "achievements");

            // Then
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Should unsubscribe from reminders category")
        void shouldUnsubscribeFromRemindersCategory() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            when(emailConfig.getBaseUrl()).thenReturn(TEST_BASE_URL);
            String token = controller.generateUnsubscribeToken(userId);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any())).thenAnswer(invocation -> {
                NotificationPreferences saved = invocation.getArgument(0);
                assertFalse(saved.getRemindersEnabled());
                return saved;
            });

            // When
            ResponseEntity<Void> response = controller.unsubscribeFromCategory(token, "reminders");

            // Then
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle unknown category gracefully")
        void shouldHandleUnknownCategoryGracefully() {
            // Given
            when(emailConfig.getUnsubscribeSecret()).thenReturn(TEST_SECRET);
            when(emailConfig.getBaseUrl()).thenReturn(TEST_BASE_URL);
            String token = controller.generateUnsubscribeToken(userId);
            when(preferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
            when(preferencesRepository.save(any())).thenReturn(testPreferences);

            // When
            ResponseEntity<Void> response = controller.unsubscribeFromCategory(token, "unknown");

            // Then
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            // No exception thrown, category change logged as warning
            verify(preferencesRepository).save(any(NotificationPreferences.class));
        }
    }
}
