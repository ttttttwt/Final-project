package com.lexia.backend.email.controller;

import com.lexia.backend.email.dto.AdminEmailRequest;
import com.lexia.backend.email.dto.EmailQueueDTO;
import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.dto.EmailStatsDTO;
import com.lexia.backend.email.entity.EmailQueue;
import com.lexia.backend.email.enums.EmailPriority;
import com.lexia.backend.email.enums.EmailStatus;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.repository.EmailQueueRepository;
import com.lexia.backend.email.service.EmailService;
import com.lexia.backend.entity.User;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminEmailController.
 * Tests send, broadcast, queue retrieval, retry/cancel operations.
 *
 * @author LEXIA Team
 * @since Sprint 6
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminEmailController Tests")
class AdminEmailControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private EmailQueueRepository emailQueueRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminEmailController controller;

    @Captor
    private ArgumentCaptor<EmailRequest> emailRequestCaptor;

    @Captor
    private ArgumentCaptor<List<EmailRequest>> emailRequestListCaptor;

    private User testUser;
    private UUID userId;
    private EmailQueue testEmailQueue;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);

        testEmailQueue = new EmailQueue();
        testEmailQueue.setId(UUID.randomUUID());
        testEmailQueue.setRecipient(testUser);
        testEmailQueue.setRecipientEmail("test@example.com");
        testEmailQueue.setEmailType(EmailType.SYSTEM_ANNOUNCEMENT);
        testEmailQueue.setStatus(EmailStatus.PENDING);
        testEmailQueue.setSubject("Test Subject");
        testEmailQueue.setTemplateName("system/announcement");
    }

    @Nested
    @DisplayName("POST /api/v1/admin/emails/send")
    class SendEmailTests {

        @Test
        @DisplayName("Should send email to specified users")
        void shouldSendEmailToSpecifiedUsers() {
            // Given
            UUID userId2 = UUID.randomUUID();
            User testUser2 = new User();
            testUser2.setId(userId2);
            testUser2.setEmail("test2@example.com");

            AdminEmailRequest request = AdminEmailRequest.builder()
                    .userIds(List.of(userId, userId2))
                    .emailType(EmailType.SYSTEM_ANNOUNCEMENT)
                    .subject("Test Subject")
                    .priority(EmailPriority.NORMAL)
                    .templateData(Map.of("message", "Hello"))
                    .locale("en")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findById(userId2)).thenReturn(Optional.of(testUser2));
            when(emailService.queueEmail(any(EmailRequest.class))).thenReturn(UUID.randomUUID());

            // When
            ResponseEntity<Map<String, Object>> response = controller.sendEmail(request);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals("Email queued for 2 users", response.getBody().get("message"));
            assertEquals(2, response.getBody().get("queuedCount"));
            verify(emailService, times(2)).queueEmail(any(EmailRequest.class));
        }

        @Test
        @DisplayName("Should return error when user IDs are empty")
        void shouldReturnErrorWhenUserIdsEmpty() {
            // Given
            AdminEmailRequest request = AdminEmailRequest.builder()
                    .userIds(List.of())
                    .emailType(EmailType.SYSTEM_ANNOUNCEMENT)
                    .build();

            // When
            ResponseEntity<Map<String, Object>> response = controller.sendEmail(request);

            // Then
            assertTrue(response.getStatusCode().is4xxClientError());
            assertNotNull(response.getBody());
            assertEquals("User IDs are required", response.getBody().get("error"));
        }

        @Test
        @DisplayName("Should skip users not found")
        void shouldSkipUsersNotFound() {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();
            AdminEmailRequest request = AdminEmailRequest.builder()
                    .userIds(List.of(userId, nonExistentUserId))
                    .emailType(EmailType.SYSTEM_ANNOUNCEMENT)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
            when(emailService.queueEmail(any())).thenReturn(UUID.randomUUID());

            // When
            ResponseEntity<Map<String, Object>> response = controller.sendEmail(request);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().get("queuedCount"));
            verify(emailService, times(1)).queueEmail(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/emails/broadcast")
    class BroadcastEmailTests {

        @Test
        @DisplayName("Should broadcast to all active users")
        void shouldBroadcastToAllActiveUsers() {
            // Given
            User inactiveUser = new User();
            inactiveUser.setId(UUID.randomUUID());
            inactiveUser.setEmail("inactive@example.com");
            inactiveUser.setIsActive(false);

            User activeUser2 = new User();
            activeUser2.setId(UUID.randomUUID());
            activeUser2.setEmail("active2@example.com");
            activeUser2.setIsActive(true);

            AdminEmailRequest request = AdminEmailRequest.builder()
                    .emailType(EmailType.MAINTENANCE_NOTICE)
                    .subject("System Maintenance")
                    .priority(EmailPriority.HIGH)
                    .build();

            when(userRepository.findAll()).thenReturn(List.of(testUser, activeUser2, inactiveUser));
            when(emailService.queueBulkEmails(anyList())).thenReturn(List.of(UUID.randomUUID(), UUID.randomUUID()));

            // When
            ResponseEntity<Map<String, Object>> response = controller.broadcastEmail(request);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().get("queuedCount"));

            verify(emailService).queueBulkEmails(emailRequestListCaptor.capture());
            assertEquals(2, emailRequestListCaptor.getValue().size()); // Only active users
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/emails/queue")
    class GetEmailQueueTests {

        @Test
        @DisplayName("Should return all emails in queue")
        void shouldReturnAllEmailsInQueue() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<EmailQueue> page = new PageImpl<>(List.of(testEmailQueue));
            when(emailQueueRepository.findAll(pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<EmailQueueDTO>> response = controller.getEmailQueue(null, pageable);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getTotalElements());
        }

        @Test
        @DisplayName("Should filter by status")
        void shouldFilterByStatus() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<EmailQueue> page = new PageImpl<>(List.of(testEmailQueue));
            when(emailQueueRepository.findByStatusOrderByCreatedAtDesc(EmailStatus.PENDING, pageable)).thenReturn(page);

            // When
            ResponseEntity<Page<EmailQueueDTO>> response = controller.getEmailQueue(EmailStatus.PENDING, pageable);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            verify(emailQueueRepository).findByStatusOrderByCreatedAtDesc(EmailStatus.PENDING, pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/emails/stats")
    class GetEmailStatsTests {

        @Test
        @DisplayName("Should return email statistics")
        void shouldReturnEmailStatistics() {
            // Given
            Object[] stats = new Object[] { 10L, 5L, 100L, 95L, 5L, 2L }; // pending, processing, sent, delivered,
                                                                          // failed, bounced
            when(emailQueueRepository.getQueueStatistics()).thenReturn(stats);

            // When
            ResponseEntity<EmailStatsDTO> response = controller.getEmailStats(EmailStatsDTO.StatsPeriod.LAST_7_DAYS);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getSummary());
            assertEquals(195L, response.getBody().getSummary().getTotalSent());
            assertEquals(95L, response.getBody().getSummary().getDelivered());
            assertEquals(5L, response.getBody().getSummary().getFailed());
            assertEquals(2L, response.getBody().getSummary().getBounced());
        }

        @Test
        @DisplayName("Should handle null statistics values")
        void shouldHandleNullStatisticsValues() {
            // Given
            Object[] stats = new Object[] { null, null, null, null, null, null };
            when(emailQueueRepository.getQueueStatistics()).thenReturn(stats);

            // When
            ResponseEntity<EmailStatsDTO> response = controller.getEmailStats(EmailStatsDTO.StatsPeriod.LAST_24_HOURS);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(0L, response.getBody().getSummary().getTotalSent());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/emails/{emailId}/retry")
    class RetryEmailTests {

        @Test
        @DisplayName("Should retry email successfully")
        void shouldRetryEmailSuccessfully() {
            // Given
            UUID emailId = UUID.randomUUID();
            when(emailService.retryEmail(emailId)).thenReturn(true);

            // When
            ResponseEntity<Map<String, Object>> response = controller.retryEmail(emailId);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals("Email retry scheduled", response.getBody().get("message"));
            assertEquals(emailId, response.getBody().get("emailId"));
        }

        @Test
        @DisplayName("Should return 404 when email not found")
        void shouldReturn404WhenEmailNotFound() {
            // Given
            UUID emailId = UUID.randomUUID();
            when(emailService.retryEmail(emailId)).thenReturn(false);

            // When
            ResponseEntity<Map<String, Object>> response = controller.retryEmail(emailId);

            // Then
            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/emails/{emailId}/cancel")
    class CancelEmailTests {

        @Test
        @DisplayName("Should cancel email successfully")
        void shouldCancelEmailSuccessfully() {
            // Given
            UUID emailId = UUID.randomUUID();
            when(emailService.cancelEmail(emailId)).thenReturn(true);

            // When
            ResponseEntity<Map<String, Object>> response = controller.cancelEmail(emailId);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals("Email cancelled", response.getBody().get("message"));
        }

        @Test
        @DisplayName("Should return 404 when email cannot be cancelled")
        void shouldReturn404WhenEmailCannotBeCancelled() {
            // Given
            UUID emailId = UUID.randomUUID();
            when(emailService.cancelEmail(emailId)).thenReturn(false);

            // When
            ResponseEntity<Map<String, Object>> response = controller.cancelEmail(emailId);

            // Then
            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/emails/{emailId}")
    class GetEmailDetailsTests {

        @Test
        @DisplayName("Should return email details")
        void shouldReturnEmailDetails() {
            // Given
            UUID emailId = UUID.randomUUID();
            EmailQueueDTO dto = EmailQueueDTO.builder()
                    .id(emailId)
                    .recipientEmail("test@example.com")
                    .emailType(EmailType.SYSTEM_ANNOUNCEMENT)
                    .status(EmailStatus.PENDING)
                    .build();
            when(emailService.getEmailStatus(emailId)).thenReturn(Optional.of(dto));

            // When
            ResponseEntity<EmailQueueDTO> response = controller.getEmailDetails(emailId);

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(emailId, response.getBody().getId());
        }

        @Test
        @DisplayName("Should return 404 when email not found")
        void shouldReturn404WhenEmailNotFound() {
            // Given
            UUID emailId = UUID.randomUUID();
            when(emailService.getEmailStatus(emailId)).thenReturn(Optional.empty());

            // When
            ResponseEntity<EmailQueueDTO> response = controller.getEmailDetails(emailId);

            // Then
            assertEquals(404, response.getStatusCode().value());
        }
    }
}
