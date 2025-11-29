package com.lexia.backend.specification;

import com.lexia.backend.entity.AdminActivityLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Specifications for filtering admin activity logs.
 * Used by the Log Management feature for ADMIN users.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
public class AdminActivityLogSpecification {

    private AdminActivityLogSpecification() {
        // Utility class
    }

    /**
     * Filter by action type.
     */
    public static Specification<AdminActivityLog> hasAction(AdminActivityLog.ActionType action) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("action"), action);
    }

    /**
     * Filter by entity type.
     */
    public static Specification<AdminActivityLog> hasEntityType(AdminActivityLog.EntityType entityType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("entityType"), entityType);
    }

    /**
     * Filter by user ID.
     */
    public static Specification<AdminActivityLog> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    /**
     * Filter by user name containing.
     */
    public static Specification<AdminActivityLog> userNameContains(String userName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("userName")),
                "%" + userName.toLowerCase() + "%");
    }

    /**
     * Filter by entity name containing.
     */
    public static Specification<AdminActivityLog> entityNameContains(String entityName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("entityName")),
                "%" + entityName.toLowerCase() + "%");
    }

    /**
     * Filter by created after date.
     */
    public static Specification<AdminActivityLog> createdAfter(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    /**
     * Filter by created before date.
     */
    public static Specification<AdminActivityLog> createdBefore(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }

    /**
     * Search by description containing.
     */
    public static Specification<AdminActivityLog> descriptionContains(String search) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")),
                "%" + search.toLowerCase() + "%");
    }
}
