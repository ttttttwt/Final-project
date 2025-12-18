package com.lexia.backend.repository;

import com.lexia.backend.entity.UserPlacementResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPlacementResultRepository extends JpaRepository<UserPlacementResult, UUID> {
    List<UserPlacementResult> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find the most recent placement result for a user.
     *
     * @param userId the user's UUID
     * @return Optional containing the latest placement result
     */
    Optional<UserPlacementResult> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
