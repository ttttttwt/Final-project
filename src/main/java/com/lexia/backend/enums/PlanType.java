package com.lexia.backend.enums;

public enum PlanType {
    FREE,
    MONTHLY, // Pro tier
    YEARLY; // Pro tier

    /**
     * Checks if this plan type is a Pro tier (MONTHLY or YEARLY subscription).
     * 
     * @return true if Pro tier, false if Free
     */
    public boolean isPro() {
        return this == MONTHLY || this == YEARLY;
    }
}
