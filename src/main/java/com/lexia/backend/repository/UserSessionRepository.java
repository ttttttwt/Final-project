package com.lexia.backend.repository;

import com.lexia.backend.entity.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserSession entity operations.
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

        /**
         * Find all active sessions for a user
         */
        List<UserSession> findByUserIdAndIsActiveTrue(UUID userId);

        /**
         * Find session history for a user (ordered by login time desc)
         */
        Page<UserSession> findByUserIdOrderByLoginTimeDesc(UUID userId, Pageable pageable);

        /**
         * Count currently active sessions
         */
        long countByIsActiveTrue();

        /**
         * Count distinct active users (unique users with at least one active session)
         */
        @Query("SELECT COUNT(DISTINCT s.userId) FROM UserSession s WHERE s.isActive = true")
        long countDistinctActiveUsers();

        /**
         * Find all active sessions (with pagination)
         */
        Page<UserSession> findByIsActiveTrueOrderByLastActivityTimeDesc(Pageable pageable);

        /**
         * Cleanup old inactive sessions
         */
        @Modifying
        @Query("""
                        DELETE FROM UserSession s
                        WHERE s.isActive = false
                        AND s.logoutTime < :cutoffDate
                        """)
        void deleteOldInactiveSessions(@Param("cutoffDate") Instant cutoffDate);

        /**
         * Find latest active session for a user
         */
        Optional<UserSession> findFirstByUserIdAndIsActiveTrueOrderByLoginTimeDesc(UUID userId);

        /**
         * Find sessions that have been inactive for a specified duration
         */
        @Query("SELECT s FROM UserSession s WHERE s.isActive = true AND s.lastActivityTime < :threshold")
        List<UserSession> findInactiveSessions(@Param("threshold") Instant threshold);

        /**
         * Deactivate stale sessions (no activity for X minutes)
         */
        @Modifying
        @Query("UPDATE UserSession s SET s.isActive = false, s.logoutTime = :now WHERE s.isActive = true AND s.lastActivityTime < :threshold")
        int deactivateStaleSessions(@Param("threshold") Instant threshold, @Param("now") Instant now);

        /**
         * Find session by token hash
         */
        Optional<UserSession> findBySessionTokenHashAndIsActiveTrue(String tokenHash);

        /**
         * Deactivate all sessions for a user
         */
        @Modifying
        @Query("UPDATE UserSession s SET s.isActive = false, s.logoutTime = :now WHERE s.userId = :userId AND s.isActive = true")
        int deactivateAllUserSessions(@Param("userId") UUID userId, @Param("now") Instant now);

        /**
         * Count active sessions by IP address (for anomaly detection)
         */
        @Query("SELECT COUNT(s) FROM UserSession s WHERE s.isActive = true AND s.ipAddress = :ip")
        long countActiveSessionsByIp(@Param("ip") String ipAddress);

        /**
         * Find users with multiple active sessions from different IPs
         */
        @Query("""
                        SELECT DISTINCT s.userId FROM UserSession s
                        WHERE s.isActive = true
                        GROUP BY s.userId
                        HAVING COUNT(DISTINCT s.ipAddress) > :threshold
                        """)
        List<UUID> findUsersWithMultipleIpSessions(@Param("threshold") int threshold);

        /**
         * Get recent active sessions with user details (for monitoring dashboard)
         */
        @Query("""
                        SELECT s FROM UserSession s
                        WHERE s.isActive = true
                        ORDER BY s.lastActivityTime DESC
                        """)
        Page<UserSession> findRecentActiveSessions(Pageable pageable);

        /**
         * Count sessions by device type for a user
         */
        @Query("""
                        SELECT s.deviceType, COUNT(s) FROM UserSession s
                        WHERE s.userId = :userId
                        GROUP BY s.deviceType
                        """)
        List<Object[]> countSessionsByDeviceType(@Param("userId") UUID userId);

        /**
         * Get only the latest session for each active user (to show unique users)
         */
        @Query("""
                        SELECT s FROM UserSession s
                        WHERE s.isActive = true
                        AND s.lastActivityTime = (
                            SELECT MAX(s2.lastActivityTime) FROM UserSession s2
                            WHERE s2.userId = s.userId AND s2.isActive = true
                        )
                        ORDER BY s.lastActivityTime DESC
                        """)
        Page<UserSession> findLatestSessionPerActiveUser(Pageable pageable);
}
