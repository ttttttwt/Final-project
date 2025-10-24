package com.lexia.backend.repository;

import com.lexia.backend.entity.RefreshToken;
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

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token hash
     * 
     * @param tokenHash the hashed token
     * @return Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Find all refresh tokens for a user
     * 
     * @param user the user
     * @return list of refresh tokens
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find all refresh tokens for a user by user ID
     * 
     * @param userId the user ID
     * @return list of refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId")
    List<RefreshToken> findByUserId(@Param("userId") UUID userId);

    /**
     * Find refresh tokens by family
     * 
     * @param family the token family
     * @return list of refresh tokens in the family
     */
    List<RefreshToken> findByFamily(String family);

    /**
     * Delete expired refresh tokens
     * 
     * @param currentTime the current time
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Delete all refresh tokens for a user
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Delete refresh tokens by family (for token rotation)
     * 
     * @param family the token family
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.family = :family")
    void deleteByFamily(@Param("family") String family);

    /**
     * Revoke refresh token by setting revoked flag and revoked_at timestamp
     * 
     * @param tokenHash the token hash
     * @param revokedAt the revocation timestamp
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.tokenHash = :tokenHash")
    void revokeToken(@Param("tokenHash") String tokenHash, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Count active refresh tokens for a user
     * 
     * @param userId the user ID
     * @return count of active tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiresAt > :currentTime")
    long countActiveTokensByUserId(@Param("userId") UUID userId, @Param("currentTime") LocalDateTime currentTime);
}