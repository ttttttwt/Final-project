package com.lexia.backend.repository;

import com.lexia.backend.entity.UserCustomMaterial;
import com.lexia.backend.enums.CustomMaterialStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserCustomMaterial entity.
 * 
 * @since Sprint 5
 */
@Repository
public interface UserCustomMaterialRepository extends JpaRepository<UserCustomMaterial, UUID> {

    /**
     * Finds all materials for a user, ordered by creation date (newest first).
     * 
     * @param userId   the user ID
     * @param pageable pagination info
     * @return page of materials
     */
    Page<UserCustomMaterial> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Finds materials for a user with specific status.
     * 
     * @param userId   the user ID
     * @param status   the material status
     * @param pageable pagination info
     * @return page of materials
     */
    Page<UserCustomMaterial> findByUserIdAndStatusOrderByCreatedAtDesc(
            UUID userId,
            CustomMaterialStatus status,
            Pageable pageable);

    /**
     * Finds a material by ID ensuring user ownership.
     * 
     * @param id     the material ID
     * @param userId the user ID
     * @return the material if found and owned by user
     */
    Optional<UserCustomMaterial> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Counts materials created by user after a given timestamp.
     * Used for monthly quota enforcement.
     * 
     * @param userId the user ID
     * @param after  the timestamp (start of month)
     * @return count of materials
     */
    long countByUserIdAndCreatedAtAfter(UUID userId, Instant after);

    /**
     * Counts materials created by user in the current month.
     * 
     * @param userId the user ID
     * @return count of materials this month
     */
    @Query("SELECT COUNT(m) FROM UserCustomMaterial m " +
            "WHERE m.userId = :userId " +
            "AND m.createdAt >= :monthStart")
    long countByUserIdThisMonth(
            @Param("userId") UUID userId,
            @Param("monthStart") Instant monthStart);

    /**
     * Finds materials with pending or processing status.
     * Used for job recovery after server restart.
     * 
     * @return list of materials still processing
     */
    @Query("SELECT m FROM UserCustomMaterial m " +
            "WHERE m.status IN ('PENDING', 'PROCESSING') " +
            "ORDER BY m.createdAt ASC")
    Page<UserCustomMaterial> findProcessingMaterials(Pageable pageable);

    /**
     * Checks if user owns the material.
     * 
     * @param id     the material ID
     * @param userId the user ID
     * @return true if user owns the material
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
