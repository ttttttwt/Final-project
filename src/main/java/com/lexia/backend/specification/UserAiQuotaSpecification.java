package com.lexia.backend.specification;

import com.lexia.backend.entity.UserAiQuota;
import com.lexia.backend.entity.User;
import com.lexia.backend.enums.PlanType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for filtering UserAiQuota queries.
 */
public class UserAiQuotaSpecification {

    /**
     * Creates a specification for filtering quotas by search term and plan type.
     *
     * @param search   search term (matches user email or full name)
     * @param planType plan type filter (FREE, PRO, or null for all)
     * @return the combined specification
     */
    public static Specification<UserAiQuota> withSearchAndPlan(String search, String planType) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // Search by user email or full name using a subquery
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase().trim() + "%";
                
                Subquery<User> userSubquery = query.subquery(User.class);
                Root<User> userRoot = userSubquery.from(User.class);
                
                Predicate emailMatch = cb.like(cb.lower(userRoot.get("email")), searchPattern);
                Predicate fullNameMatch = cb.like(
                    cb.lower(userRoot.get("profile").get("fullName")), 
                    searchPattern
                );
                
                userSubquery.select(userRoot)
                    .where(cb.and(
                        cb.equal(userRoot.get("id"), root.get("userId")),
                        cb.or(emailMatch, fullNameMatch)
                    ));
                
                predicate = cb.and(predicate, cb.exists(userSubquery));
            }

            // Filter by plan type
            if (planType != null && !planType.isEmpty() && !"ALL".equals(planType)) {
                if ("PRO".equals(planType)) {
                    // Pro users have MONTHLY or YEARLY plan
                    predicate = cb.and(predicate, root.get("planType").in(PlanType.MONTHLY, PlanType.YEARLY));
                } else if ("FREE".equals(planType)) {
                    // Free users have FREE plan or null
                    predicate = cb.and(predicate, cb.or(
                        cb.equal(root.get("planType"), PlanType.FREE),
                        cb.isNull(root.get("planType"))
                    ));
                }
            }

            return predicate;
        };
    }
}
