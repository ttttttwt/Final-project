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
     * Checks both legacy featureName and new contentType fields.
     */
    public static Specification<AIUsageLog> hasFeatureName(String featureName) {
        return (root, query, criteriaBuilder) -> {
            if (featureName == null) {
                return null;
            }
            // Check legacy featureName (exact match)
            var legacyMatch = criteriaBuilder.equal(root.get("featureName"), featureName);
            
            // Check new contentType (case-insensitive match)
            var contentTypeMatch = criteriaBuilder.equal(
                criteriaBuilder.lower(root.get("contentType")), 
                featureName.toLowerCase()
            );
            
            return criteriaBuilder.or(legacyMatch, contentTypeMatch);
        };
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
