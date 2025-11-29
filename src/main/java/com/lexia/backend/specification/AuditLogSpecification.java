package com.lexia.backend.specification;

import com.lexia.backend.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Specifications for filtering audit logs.
 * Used by the Log Management feature for ADMIN users.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
public class AuditLogSpecification {

    private AuditLogSpecification() {
        // Utility class
    }

    /**
     * Filter by action type.
     */
    public static Specification<AuditLog> hasAction(String action) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("action"), action);
    }

    /**
     * Filter by entity type.
     */
    public static Specification<AuditLog> hasEntityType(String entityType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("entityType"), entityType);
    }

    /**
     * Filter by user ID.
     */
    public static Specification<AuditLog> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    /**
     * Filter by user email containing.
     */
    public static Specification<AuditLog> userEmailContains(String email) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("user").get("email")),
                "%" + email.toLowerCase() + "%");
    }

    /**
     * Filter by IP address.
     */
    public static Specification<AuditLog> hasIpAddress(String ipAddress) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ipAddress"), ipAddress);
    }

    /**
     * Filter by created after date.
     */
    public static Specification<AuditLog> createdAfter(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    /**
     * Filter by created before date.
     */
    public static Specification<AuditLog> createdBefore(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }

    /**
     * Search by changes containing.
     */
    public static Specification<AuditLog> changesContains(String search) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("changes")),
                "%" + search.toLowerCase() + "%");
    }
}
