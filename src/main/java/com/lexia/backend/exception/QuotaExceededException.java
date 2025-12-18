package com.lexia.backend.exception;

import com.lexia.backend.enums.PlanType;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user exceeds their AI quota limit.
 * Returns HTTP 429 Too Many Requests.
 */
@Getter
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class QuotaExceededException extends RuntimeException {

    private final String featureType;
    private final int limit;
    private final int used;
    private final PlanType planType;

    public QuotaExceededException(String featureType, int limit, int used, PlanType planType) {
        super(buildMessage(featureType, limit, used, planType));
        this.featureType = featureType;
        this.limit = limit;
        this.used = used;
        this.planType = planType;
    }

    private static String buildMessage(String featureType, int limit, int used, PlanType planType) {
        String planName = planType != null && planType.isPro() ? "Pro" : "Free";
        return String.format(
                "Quota exceeded for %s: used %d/%d on %s plan. Upgrade to continue!",
                featureType, used, limit, planName);
    }

    /**
     * Checks if upgrading would help (i.e., user is on Free plan).
     * 
     * @return true if user is on Free plan and could benefit from upgrading
     */
    public boolean canUpgradeToResolve() {
        return planType == null || planType == PlanType.FREE;
    }
}
