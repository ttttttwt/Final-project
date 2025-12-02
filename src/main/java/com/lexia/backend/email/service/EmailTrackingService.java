package com.lexia.backend.email.service;

import java.util.UUID;

/**
 * Service for tracking email opens and clicks.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public interface EmailTrackingService {

    /**
     * Records an email open event.
     *
     * @param logId     the email log ID
     * @param ipAddress the IP address
     * @param userAgent the user agent
     */
    void recordOpen(UUID logId, String ipAddress, String userAgent);

    /**
     * Records an email click event.
     *
     * @param logId     the email log ID
     * @param url       the clicked URL
     * @param ipAddress the IP address
     * @param userAgent the user agent
     */
    void recordClick(UUID logId, String url, String ipAddress, String userAgent);

    /**
     * Records a bounce event from webhook.
     *
     * @param providerMessageId the provider's message ID
     * @param bounceType        the type of bounce (hard/soft)
     * @param errorMessage      the error message
     */
    void recordBounce(String providerMessageId, String bounceType, String errorMessage);

    /**
     * Records a spam complaint from webhook.
     *
     * @param providerMessageId the provider's message ID
     */
    void recordComplaint(String providerMessageId);

    /**
     * Generates a tracking pixel URL.
     *
     * @param logId the email log ID
     * @return the tracking pixel URL
     */
    String generateTrackingPixelUrl(UUID logId);

    /**
     * Generates a tracked link URL.
     *
     * @param logId       the email log ID
     * @param originalUrl the original URL
     * @return the tracked link URL
     */
    String generateTrackedLinkUrl(UUID logId, String originalUrl);
}
