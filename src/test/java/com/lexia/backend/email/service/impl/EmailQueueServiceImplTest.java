package com.lexia.backend.email.service.impl;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.entity.EmailQueue;
import com.lexia.backend.email.repository.EmailLogRepository;
import com.lexia.backend.email.repository.EmailQueueRepository;
import com.lexia.backend.email.service.EmailProviderService;
import com.lexia.backend.email.service.EmailTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailQueueServiceImplTest {

    @Mock
    private EmailQueueRepository emailQueueRepository;

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private EmailTemplateService templateService;

    @Mock
    private EmailProviderService emailProvider;

    @Mock
    private EmailConfig emailConfig;

    @InjectMocks
    private EmailQueueServiceImpl emailQueueService;

    @BeforeEach
    void setUp() {
        // Setup common mocks if needed
    }

    @Test
    void processPendingEmails_NoEmails_ShouldNotLogAndReturnZero() {
        // Arrange
        int batchSize = 10;
        when(emailQueueRepository.findPendingEmailsReadyToProcess(any(Instant.class), eq(PageRequest.of(0, batchSize))))
                .thenReturn(Collections.emptyList());

        // Act
        int result = emailQueueService.processPendingEmails(batchSize);

        // Assert
        assertEquals(0, result);
        verify(emailQueueRepository).findPendingEmailsReadyToProcess(any(Instant.class), eq(PageRequest.of(0, batchSize)));
        // We can't easily verify logging with Mockito unless we mock the Logger, 
        // but we can verify that no further processing happened.
        verifyNoInteractions(templateService, emailProvider);
    }

    @Test
    void processRetryQueue_NoEmails_ShouldNotLogAndReturnZero() {
        // Arrange
        EmailConfig.QueueConfig queueConfig = mock(EmailConfig.QueueConfig.class);
        when(emailConfig.getQueue()).thenReturn(queueConfig);
        when(queueConfig.getBatchSize()).thenReturn(10);
        
        when(emailQueueRepository.findEmailsReadyForRetry(any(Instant.class), eq(PageRequest.of(0, 10))))
                .thenReturn(Collections.emptyList());

        // Act
        int result = emailQueueService.processRetryQueue();

        // Assert
        assertEquals(0, result);
        verify(emailQueueRepository).findEmailsReadyForRetry(any(Instant.class), eq(PageRequest.of(0, 10)));
        verifyNoInteractions(templateService, emailProvider);
    }
}
