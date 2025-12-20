package com.lexia.backend.repository;

import com.lexia.backend.entity.CustomMaterialChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CustomMaterialChatSession entity.
 * 
 * @since Sprint 5
 */
@Repository
public interface CustomMaterialChatSessionRepository extends JpaRepository<CustomMaterialChatSession, UUID> {

    /**
     * Finds all chat sessions for a material.
     * 
     * @param materialId the material ID
     * @return list of sessions
     */
    List<CustomMaterialChatSession> findByMaterialIdOrderByStartedAtDesc(UUID materialId);

    /**
     * Finds chat sessions for a user.
     * 
     * @param userId   the user ID
     * @param pageable pagination info
     * @return page of sessions
     */
    Page<CustomMaterialChatSession> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    /**
     * Finds a session ensuring user ownership.
     * 
     * @param id     the session ID
     * @param userId the user ID
     * @return the session if found and owned by user
     */
    Optional<CustomMaterialChatSession> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Finds active (in-progress) sessions for a material.
     * 
     * @param materialId the material ID
     * @param status     the session status
     * @return list of active sessions
     */
    List<CustomMaterialChatSession> findByMaterialIdAndStatus(UUID materialId, String status);

    /**
     * Counts sessions for a material.
     * 
     * @param materialId the material ID
     * @return session count
     */
    long countByMaterialId(UUID materialId);

    /**
     * Checks if user owns the session.
     * 
     * @param id     the session ID
     * @param userId the user ID
     * @return true if user owns the session
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
