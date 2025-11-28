package com.lexia.backend.specification;

import com.lexia.backend.entity.AIUsageLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Specifications for filtering AI usage logs.
 */
public class AIUsageLogSpecification {

    private AIUsageLogSpecification() {
        // Utility class
    }

    /**
     * Filter by feature name.
     */
    public static Specification<AIUsageLog> hasFeatureName(String featureName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("featureName"), featureName);
    }

    /**
     * Filter by user ID.
     */
    public static Specification<AIUsageLog> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    /**
     * Filter by created after date.
     */
    public static Specification<AIUsageLog> createdAfter(Instant startDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    /**
     * Filter by created before date.
     */
    public static Specification<AIUsageLog> createdBefore(Instant endDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }
}
