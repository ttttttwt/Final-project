package com.lexia.backend.repository;

import com.lexia.backend.entity.UserShadowingAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserShadowingAttempt entity.
 * 
 * @since Sprint 5
 */
@Repository
public interface UserShadowingAttemptRepository extends JpaRepository<UserShadowingAttempt, UUID> {

    /**
     * Finds all attempts for a material and user.
     * 
     * @param materialId the material ID
     * @param userId     the user ID
     * @return list of attempts
     */
    List<UserShadowingAttempt> findByMaterialIdAndUserIdOrderByCreatedAtDesc(UUID materialId, UUID userId);

    /**
     * Finds attempts for a specific sentence.
     * 
     * @param materialId the material ID
     * @param userId     the user ID
     * @param sentenceId the sentence ID
     * @return list of attempts for the sentence
     */
    List<UserShadowingAttempt> findByMaterialIdAndUserIdAndSentenceIdOrderByCreatedAtDesc(
            UUID materialId,
            UUID userId,
            String sentenceId);

    /**
     * Gets the best attempt for each sentence in a material.
     * 
     * @param materialId the material ID
     * @param userId     the user ID
     * @return list of best attempts grouped by sentence
     */
    @Query("SELECT a FROM UserShadowingAttempt a " +
            "WHERE a.material.id = :materialId AND a.userId = :userId " +
            "AND a.score = (SELECT MAX(a2.score) FROM UserShadowingAttempt a2 " +
            "               WHERE a2.material.id = :materialId AND a2.userId = :userId " +
            "               AND a2.sentenceId = a.sentenceId)")
    List<UserShadowingAttempt> findBestAttemptsBySentence(
            @Param("materialId") UUID materialId,
            @Param("userId") UUID userId);

    /**
     * Gets the average score for a user on a material.
     * 
     * @param materialId the material ID
     * @param userId     the user ID
     * @return average score or null if no scored attempts
     */
    @Query("SELECT AVG(a.score) FROM UserShadowingAttempt a " +
            "WHERE a.material.id = :materialId AND a.userId = :userId " +
            "AND a.score IS NOT NULL")
    Double getAverageScore(
            @Param("materialId") UUID materialId,
            @Param("userId") UUID userId);

    /**
     * Counts attempts for a sentence.
     * 
     * @param materialId the material ID
     * @param userId     the user ID
     * @param sentenceId the sentence ID
     * @return attempt count
     */
    long countByMaterialIdAndUserIdAndSentenceId(UUID materialId, UUID userId, String sentenceId);

    /**
     * Finds a specific attempt with ownership check.
     * 
     * @param id     the attempt ID
     * @param userId the user ID
     * @return the attempt if found and owned
     */
    Optional<UserShadowingAttempt> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Finds all attempts for a user.
     * 
     * @param userId   the user ID
     * @param pageable pagination info
     * @return page of attempts
     */
    Page<UserShadowingAttempt> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
