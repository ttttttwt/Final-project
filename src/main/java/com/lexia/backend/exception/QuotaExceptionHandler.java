package com.lexia.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception handler for AI quota-related exceptions.
 * Handles QuotaExceededException and returns proper 429 responses.
 */
@RestControllerAdvice
@Order(1) // Higher priority than generic handlers
@Slf4j
public class QuotaExceptionHandler {

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<Map<String, Object>> handleQuotaExceededException(QuotaExceededException ex) {
        log.info("Quota exceeded: feature={}, used={}, limit={}, plan={}",
                ex.getFeatureType(), ex.getUsed(), ex.getLimit(), ex.getPlanType());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "QUOTA_EXCEEDED");
        response.put("message", ex.getMessage());
        response.put("featureType", ex.getFeatureType());
        response.put("used", ex.getUsed());
        response.put("limit", ex.getLimit());
        response.put("planType", ex.getPlanType() != null ? ex.getPlanType().name() : "FREE");
        response.put("canUpgrade", ex.canUpgradeToResolve());

        if (ex.canUpgradeToResolve()) {
            response.put("upgradeMessage",
                    "Nâng cấp lên Pro để nhận quota cao hơn 3-5 lần!");
            response.put("upgradeUrl", "/subscription");
        }

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "86400") // 24 hours
                .body(response);
    }

    @ExceptionHandler(SubscriptionRequiredException.class)
    public ResponseEntity<Map<String, Object>> handleSubscriptionRequired(SubscriptionRequiredException ex) {
        log.info("Premium subscription required: feature={}", ex.getFeatureName());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "SUBSCRIPTION_REQUIRED");
        response.put("message", ex.getMessage());
        response.put("featureName", ex.getFeatureName());
        response.put("upgradeUrl", "/subscription");
        response.put("upgradeMessage", "Upgrade to Pro to unlock this feature");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }
}
