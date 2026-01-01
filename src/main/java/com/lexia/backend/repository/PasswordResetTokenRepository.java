package com.lexia.backend.repository;

import com.lexia.backend.entity.PasswordResetToken;
import com.lexia.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PasswordResetToken entity.
 * 
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Find a token by its hash.
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Find all unused tokens for a user.
     */
    List<PasswordResetToken> findByUserAndUsedAtIsNull(User user);

    /**
     * Find all unused and non-expired tokens for a user.
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user = :user AND t.usedAt IS NULL AND t.expiresAt > :now")
    List<PasswordResetToken> findValidTokensForUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Delete all expired tokens.
     * Used by cleanup scheduler.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :time")
    int deleteByExpiresAtBefore(@Param("time") LocalDateTime time);

    /**
     * Invalidate (mark as used) all tokens for a user.
     * Called when password is reset or user requests new token.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :now WHERE t.user.id = :userId AND t.usedAt IS NULL")
    int invalidateAllForUser(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Count pending (valid) reset requests for a user.
     * Used for rate limiting.
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.user.id = :userId AND t.usedAt IS NULL AND t.expiresAt > :now")
    long countPendingForUser(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Delete all tokens for a user.
     */
    @Modifying
    void deleteByUser(User user);
}
