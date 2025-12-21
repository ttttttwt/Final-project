package com.lexia.backend.exception;

/**
 * Exception thrown when a Premium subscription is required but user is Free.
 *
 * @since Sprint 5
 */
public class SubscriptionRequiredException extends RuntimeException {

    private final String featureName;

    public SubscriptionRequiredException(String message) {
        super(message);
        this.featureName = "premium_feature";
    }

    public SubscriptionRequiredException(String message, String featureName) {
        super(message);
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }
}
