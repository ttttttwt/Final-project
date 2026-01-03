package com.lexia.backend.specification;

import com.lexia.backend.entity.Role;
import com.lexia.backend.entity.Subscription;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.entity.UserRole;
import com.lexia.backend.enums.PlanType;
import com.lexia.backend.enums.SubscriptionStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> withSearchAndRole(String search, String roleName) {
        return withSearchRoleAndPlan(search, roleName, null);
    }
    
    public static Specification<User> withSearchRoleAndPlan(String search, String roleName, String planType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join with Profile for name search
            Join<User, UserProfile> profileJoin = root.join("profile", JoinType.LEFT);

            // Search by email, first name, or last name
            if (StringUtils.hasText(search)) {
                String searchLower = "%" + search.toLowerCase() + "%";
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchLower);
                Predicate firstNamePredicate = cb.like(cb.lower(profileJoin.get("firstName")), searchLower);
                Predicate lastNamePredicate = cb.like(cb.lower(profileJoin.get("lastName")), searchLower);
                predicates.add(cb.or(emailPredicate, firstNamePredicate, lastNamePredicate));
            }

            // Filter by Role
            if (StringUtils.hasText(roleName)) {
                Join<User, UserRole> userRoleJoin = root.join("userRoles", JoinType.INNER);
                Join<UserRole, Role> roleJoin = userRoleJoin.join("role", JoinType.INNER);
                predicates.add(cb.equal(roleJoin.get("name"), roleName));
            }
            
            // Filter by Plan Type (FREE or PRO)
            if (StringUtils.hasText(planType)) {
                // Use subquery to check subscription plan type with active status
                Subquery<Long> subscriptionSubquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<Subscription> subRoot = subscriptionSubquery.from(Subscription.class);
                
                if ("PRO".equalsIgnoreCase(planType)) {
                    // Pro users have ACTIVE MONTHLY or YEARLY subscription that hasn't expired
                    subscriptionSubquery.select(cb.literal(1L))
                        .where(
                            cb.equal(subRoot.get("user").get("id"), root.get("id")),
                            subRoot.get("planType").in(PlanType.MONTHLY, PlanType.YEARLY),
                            cb.equal(subRoot.get("status"), SubscriptionStatus.ACTIVE),
                            cb.or(
                                cb.isNull(subRoot.get("currentPeriodEnd")),
                                cb.greaterThan(subRoot.get("currentPeriodEnd"), LocalDateTime.now())
                            )
                        );
                    predicates.add(cb.exists(subscriptionSubquery));
                } else if ("FREE".equalsIgnoreCase(planType)) {
                    // Free users either have no active Pro subscription
                    subscriptionSubquery.select(cb.literal(1L))
                        .where(
                            cb.equal(subRoot.get("user").get("id"), root.get("id")),
                            subRoot.get("planType").in(PlanType.MONTHLY, PlanType.YEARLY),
                            cb.equal(subRoot.get("status"), SubscriptionStatus.ACTIVE),
                            cb.or(
                                cb.isNull(subRoot.get("currentPeriodEnd")),
                                cb.greaterThan(subRoot.get("currentPeriodEnd"), LocalDateTime.now())
                            )
                        );
                    predicates.add(cb.not(cb.exists(subscriptionSubquery)));
                }
            }

            // Distinct to avoid duplicates due to joins
            query.distinct(true);

            // Filter out soft-deleted users by default
            predicates.add(cb.equal(root.get("isDeleted"), false));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
