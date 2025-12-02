package com.lexia.backend.email.service;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.dto.EmailQueueDTO;
import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.dto.EmailSendResult;
import com.lexia.backend.email.entity.EmailQueue;
import com.lexia.backend.email.enums.EmailPriority;
import com.lexia.backend.email.enums.EmailStatus;
import com.lexia.backend.email.enums.EmailType;
import com.lexia.backend.email.repository.EmailLogRepository;
import com.lexia.backend.email.repository.EmailQueueRepository;
import com.lexia.backend.email.service.impl.EmailServiceImpl;
import com.lexia.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailServiceImpl.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EmailService Unit Tests")
class EmailServiceImplTest {

    @Mock
    private EmailQueueRepository emailQueueRepository;

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailTemplateService templateService;

    @Mock
    private EmailProviderService emailProvider;

    @Mock
    private EmailConfig emailConfig;

    @InjectMocks
    private EmailServiceImpl emailService;

    private EmailRequest testEmailRequest;
    private EmailQueue testEmailQueue;

    @BeforeEach
    void setUp() {
        testEmailRequest = EmailRequest.builder()
                .recipientEmail("test@example.com")
                .recipientName("Test User")
                .emailType(EmailType.EMAIL_VERIFICATION)
                .subject("Test Subject")
                .templateData(Map.of("userName", "Test User", "verificationUrl", "http://test.com/verify"))
                .locale("en")
                .build();

        testEmailQueue = EmailQueue.builder()
                .id(UUID.randomUUID())
                .recipientEmail("test@example.com")
                .recipientName("Test User")
                .emailType(EmailType.EMAIL_VERIFICATION)
                .subject("Test Subject")
                .templateName("auth/verification")
                .templateData(Map.of("userName", "Test User"))
                .status(EmailStatus.PENDING)
                .priority(EmailPriority.CRITICAL)
                .attempts(0)
                .maxAttempts(5)
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Queue Email Tests")
    class QueueEmailTests {

        @Test
        @DisplayName("Should queue email successfully")
        void shouldQueueEmailSuccessfully() {
            // Given
            when(userRepository.findById(any())).thenReturn(Optional.empty());
            when(templateService.getSubject(any(), any(), any())).thenReturn("Email Verification");
            when(emailQueueRepository.save(any(EmailQueue.class))).thenAnswer(invocation -> {
                EmailQueue saved = invocation.getArgument(0);
                saved.setId(UUID.randomUUID());
                return saved;
            });

            // When
            UUID emailId = emailService.queueEmail(testEmailRequest);

            // Then
            assertThat(emailId).isNotNull();

            ArgumentCaptor<EmailQueue> captor = ArgumentCaptor.forClass(EmailQueue.class);
            verify(emailQueueRepository).save(captor.capture());

            EmailQueue saved = captor.getValue();
            assertThat(saved.getRecipientEmail()).isEqualTo("test@example.com");
            assertThat(saved.getEmailType()).isEqualTo(EmailType.EMAIL_VERIFICATION);
            assertThat(saved.getStatus()).isEqualTo(EmailStatus.PENDING);
        }

        @Test
        @DisplayName("Should queue bulk emails successfully")
        void shouldQueueBulkEmailsSuccessfully() {
            // Given
            EmailRequest request2 = EmailRequest.builder()
                    .recipientEmail("test2@example.com")
                    .recipientName("Test User 2")
                    .emailType(EmailType.WELCOME)
                    .locale("en")
                    .templateData(new java.util.HashMap<>())
                    .build();

            when(userRepository.findById(any())).thenReturn(Optional.empty());
            when(templateService.getSubject(any(), any(), any())).thenReturn("Subject");
            when(emailQueueRepository.save(any(EmailQueue.class))).thenAnswer(invocation -> {
                EmailQueue saved = invocation.getArgument(0);
                saved.setId(UUID.randomUUID());
                return saved;
            });

            // When
            List<UUID> emailIds = emailService.queueBulkEmails(List.of(testEmailRequest, request2));

            // Then
            assertThat(emailIds).hasSize(2);
            verify(emailQueueRepository, times(2)).save(any(EmailQueue.class));
        }
    }

    @Nested
    @DisplayName("Send Email Sync Tests")
    class SendEmailSyncTests {

        @Test
        @DisplayName("Should send email synchronously successfully")
        void shouldSendEmailSyncSuccessfully() {
            // Given
            doNothing().when(templateService).addCommonVariables(any(), any());
            when(templateService.renderTemplate(anyString(), any(), any())).thenReturn("<html>Test</html>");
            when(templateService.renderPlainText(anyString(), any(), any())).thenReturn("Test");
            when(templateService.getSubject(any(), any(), any())).thenReturn("Test Subject");
            when(emailConfig.getFromAddress()).thenReturn("noreply@test.com");
            when(emailConfig.getFromName()).thenReturn("Test");
            when(emailConfig.getReplyToAddress()).thenReturn(null);
            when(emailProvider.send(any())).thenReturn(EmailSendResult.success("msg-123"));
            when(emailProvider.getProviderName()).thenReturn("SMTP");
            when(emailLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            boolean result = emailService.sendEmailSync(testEmailRequest);

            // Then
            assertThat(result).isTrue();
            verify(emailProvider).send(any());
            verify(emailLogRepository).save(any()); // Verify logging
        }

        @Test
        @DisplayName("Should return false when send fails")
        void shouldReturnFalseWhenSendFails() {
            // Given
            doNothing().when(templateService).addCommonVariables(any(), any());
            when(templateService.renderTemplate(anyString(), any(), any())).thenReturn("<html>Test</html>");
            when(templateService.renderPlainText(anyString(), any(), any())).thenReturn("Test");
            when(templateService.getSubject(any(), any(), any())).thenReturn("Test Subject");
            when(emailConfig.getFromAddress()).thenReturn("noreply@test.com");
            when(emailConfig.getFromName()).thenReturn("Test");
            when(emailConfig.getReplyToAddress()).thenReturn(null);
            when(emailProvider.send(any())).thenReturn(EmailSendResult.failure("SMTP error"));
            when(emailProvider.getProviderName()).thenReturn("SMTP");
            when(emailLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            boolean result = emailService.sendEmailSync(testEmailRequest);

            // Then
            assertThat(result).isFalse();
            verify(emailLogRepository).save(any()); // Verify failure is logged
        }
    }

    @Nested
    @DisplayName("Email Status Tests")
    class EmailStatusTests {

        @Test
        @DisplayName("Should get email status by ID")
        void shouldGetEmailStatusById() {
            // Given
            when(emailQueueRepository.findById(testEmailQueue.getId()))
                    .thenReturn(Optional.of(testEmailQueue));

            // When
            Optional<EmailQueueDTO> result = emailService.getEmailStatus(testEmailQueue.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getRecipientEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(emailQueueRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When
            Optional<EmailQueueDTO> result = emailService.getEmailStatus(unknownId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cancel and Retry Tests")
    class CancelAndRetryTests {

        @Test
        @DisplayName("Should cancel pending email")
        void shouldCancelPendingEmail() {
            // Given
            when(emailQueueRepository.findById(testEmailQueue.getId()))
                    .thenReturn(Optional.of(testEmailQueue));
            when(emailQueueRepository.save(any())).thenReturn(testEmailQueue);

            // When
            boolean result = emailService.cancelEmail(testEmailQueue.getId());

            // Then
            assertThat(result).isTrue();
            verify(emailQueueRepository).save(any());
        }

        @Test
        @DisplayName("Should not cancel non-pending email")
        void shouldNotCancelNonPendingEmail() {
            // Given
            testEmailQueue.setStatus(EmailStatus.SENT);
            when(emailQueueRepository.findById(testEmailQueue.getId()))
                    .thenReturn(Optional.of(testEmailQueue));

            // When
            boolean result = emailService.cancelEmail(testEmailQueue.getId());

            // Then
            assertThat(result).isFalse();
            verify(emailQueueRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should retry failed email")
        void shouldRetryFailedEmail() {
            // Given
            testEmailQueue.setStatus(EmailStatus.FAILED);
            when(emailQueueRepository.findById(testEmailQueue.getId()))
                    .thenReturn(Optional.of(testEmailQueue));
            when(emailQueueRepository.save(any())).thenReturn(testEmailQueue);

            // When
            boolean result = emailService.retryEmail(testEmailQueue.getId());

            // Then
            assertThat(result).isTrue();
            verify(emailQueueRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("Should get pending count")
        void shouldGetPendingCount() {
            // Given
            when(emailQueueRepository.countPending()).thenReturn(10L);

            // When
            long count = emailService.getPendingCount();

            // Then
            assertThat(count).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should get failed count")
        void shouldGetFailedCount() {
            // Given
            when(emailQueueRepository.countFailed()).thenReturn(5L);

            // When
            long count = emailService.getFailedCount();

            // Then
            assertThat(count).isEqualTo(5L);
        }
    }
}
