package com.lexia.backend.repository;

import com.lexia.backend.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Find user profile by user ID
     * 
     * @param userId the user ID
     * @return Optional containing the user profile if found
     */
    Optional<UserProfile> findByUserId(UUID userId);
}