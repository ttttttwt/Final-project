package com.lexia.backend.repository;

import com.lexia.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address
     * 
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     * 
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email and active status
     * 
     * @param email    the email to search for
     * @param isActive the active status
     * @return Optional containing the user if found and active
     */
    Optional<User> findByEmailAndIsActive(String email, Boolean isActive);

    /**
     * Count users by auth provider
     * 
     * @param authProvider the auth provider (EMAIL, GOOGLE, FACEBOOK)
     * @return count of users with the specified auth provider
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.authProvider = :authProvider")
    long countByAuthProvider(@Param("authProvider") User.AuthProvider authProvider);

    /**
     * Find user by email and fetch roles eagerly.
     *
     * @param email the email to search for
     * @return Optional containing the user with roles if found
     */
    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.userRoles r
            LEFT JOIN FETCH r.role
            LEFT JOIN FETCH u.profile
            WHERE u.email = :email
            """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);
}