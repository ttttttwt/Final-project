package com.lexia.backend.repository;

import com.lexia.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

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

        /**
         * Find user by ID and fetch roles eagerly.
         * Used for WebSocket authentication to avoid LazyInitializationException.
         *
         * @param id the user ID to search for
         * @return Optional containing the user with roles if found
         */
        @Query("""
                        SELECT DISTINCT u FROM User u
                        LEFT JOIN FETCH u.userRoles r
                        LEFT JOIN FETCH r.role
                        LEFT JOIN FETCH u.profile
                        WHERE u.id = :id
                        """)
        Optional<User> findByIdWithRoles(@Param("id") UUID id);

        /**
         * Find recently registered users.
         * Used for admin dashboard recent activity.
         * 
         * @param pageable pagination info (use PageRequest.of(0, N) to get top N)
         * @return list of recently registered users
         */
        @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
        java.util.List<User> findRecentlyRegistered(org.springframework.data.domain.Pageable pageable);

        // ==================== Soft Delete Support ====================

        /**
         * Find user by email among non-deleted users only.
         *
         * @param email the email to search for
         * @return Optional containing the active user if found
         */
        Optional<User> findByEmailAndIsDeletedFalse(String email);

        /**
         * Check if email exists among non-deleted users.
         *
         * @param email the email to check
         * @return true if a non-deleted user with this email exists
         */
        boolean existsByEmailAndIsDeletedFalse(String email);

        /**
         * Find all deleted users for trash management.
         *
         * @param pageable pagination info
         * @return page of deleted users
         */
        org.springframework.data.domain.Page<User> findByIsDeletedTrue(
                        org.springframework.data.domain.Pageable pageable);

        /**
         * Find all active (non-deleted) users.
         *
         * @param pageable pagination info
         * @return page of active users
         */
        org.springframework.data.domain.Page<User> findByIsDeletedFalse(
                        org.springframework.data.domain.Pageable pageable);
}