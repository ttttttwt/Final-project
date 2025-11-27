package com.lexia.backend.specification;

import com.lexia.backend.entity.Role;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.entity.UserRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> withSearchAndRole(String search, String roleName) {
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

            // Distinct to avoid duplicates due to joins
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
