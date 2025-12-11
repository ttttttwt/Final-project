package com.lexia.backend.email.service.impl;

import com.lexia.backend.email.config.EmailConfig;
import com.lexia.backend.email.repository.EmailLogRepository;
import com.lexia.backend.email.service.EmailTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Implementation of EmailTrackingService.
 * Handles email open and click tracking.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailTrackingServiceImpl implements EmailTrackingService {

    private final EmailLogRepository emailLogRepository;
    private final EmailConfig emailConfig;

    @Override
    public void recordOpen(UUID logId, String ipAddress, String userAgent) {
        emailLogRepository.findById(logId).ifPresent(emailLog -> {
            if (emailLog.getOpenedAt() == null) {
                emailLog.recordOpen(ipAddress, userAgent);
                emailLogRepository.save(emailLog);
                log.debug("Recorded email open: logId={}, ip={}", logId, ipAddress);
            }
        });
    }

    @Override
    public void recordClick(UUID logId, String url, String ipAddress, String userAgent) {
        emailLogRepository.findById(logId).ifPresent(emailLog -> {
            emailLog.recordClick(ipAddress, userAgent);
            emailLogRepository.save(emailLog);
            log.debug("Recorded email click: logId={}, url={}, ip={}", logId, url, ipAddress);
        });
    }

    @Override
    public void recordBounce(String providerMessageId, String bounceType, String errorMessage) {
        emailLogRepository.findByProviderMessageId(providerMessageId).ifPresent(emailLog -> {
            emailLog.recordBounce(errorMessage);
            emailLogRepository.save(emailLog);
            log.warn("Recorded email bounce: messageId={}, type={}, error={}",
                    providerMessageId, bounceType, errorMessage);
        });
    }

    @Override
    public void recordComplaint(String providerMessageId) {
        emailLogRepository.findByProviderMessageId(providerMessageId).ifPresent(emailLog -> {
            emailLog.recordComplaint();
            emailLogRepository.save(emailLog);
            log.warn("Recorded spam complaint: messageId={}", providerMessageId);
        });
    }

    @Override
    public String generateTrackingPixelUrl(UUID logId) {
        if (!emailConfig.isTrackingEnabled()) {
            return null;
        }
        return emailConfig.getBaseUrl() + "/api/v1/emails/track/open/" + logId;
    }

    @Override
    public String generateTrackedLinkUrl(UUID logId, String originalUrl) {
        if (!emailConfig.isTrackingEnabled()) {
            return originalUrl;
        }
        String encodedUrl = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
        return emailConfig.getBaseUrl() + "/api/v1/emails/track/click/" + logId + "?url=" + encodedUrl;
    }
}
