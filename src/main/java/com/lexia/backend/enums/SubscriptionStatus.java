package com.lexia.backend.enums;

public enum SubscriptionStatus {
    ACTIVE,
    CANCELED,
    PAST_DUE,
    INCOMPLETE,
    TRIALING,
    EXPIRED // Subscription period ended without renewal
}
