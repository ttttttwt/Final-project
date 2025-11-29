package com.lexia.backend.notification.repository;

import com.lexia.backend.notification.entity.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for NotificationPreferences entity.
 */
@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, UUID> {

    /**
     * Find notification preferences by user ID.
     */
    Optional<NotificationPreferences> findByUserId(UUID userId);

    /**
     * Check if preferences exist for a user.
     */
    boolean existsByUserId(UUID userId);
}
