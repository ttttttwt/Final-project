package com.lexia.backend.repository;

import com.lexia.backend.entity.UserLearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserLearningPath entity.
 * 
 * <p>
 * Provides CRUD operations and custom query methods for managing user
 * enrollments
 * in learning paths.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 * @see UserLearningPath
 */
@Repository
public interface UserLearningPathRepository extends JpaRepository<UserLearningPath, Long> {

    /**
     * Find a user's enrollment in a specific learning path.
     * 
     * @param userId the user's UUID
     * @param pathId the learning path ID
     * @return Optional containing the user's path enrollment if found
     */
    @Query("SELECT ulp FROM UserLearningPath ulp WHERE ulp.user.id = :userId AND ulp.learningPath.id = :pathId")
    Optional<UserLearningPath> findByUserIdAndPathId(@Param("userId") UUID userId, @Param("pathId") Long pathId);

    /**
     * Find all learning paths a user is enrolled in.
     * 
     * @param userId the user's UUID
     * @return list of user's learning path enrollments
     */
    @Query("SELECT ulp FROM UserLearningPath ulp WHERE ulp.user.id = :userId ORDER BY ulp.startedAt DESC")
    List<UserLearningPath> findByUserId(@Param("userId") UUID userId);

    /**
     * Check if a user is already enrolled in a specific learning path.
     * 
     * @param userId the user's UUID
     * @param pathId the learning path ID
     * @return true if user is enrolled in the path, false otherwise
     */
    @Query("SELECT COUNT(ulp) > 0 FROM UserLearningPath ulp WHERE ulp.user.id = :userId AND ulp.learningPath.id = :pathId")
    boolean existsByUserIdAndPathId(@Param("userId") UUID userId, @Param("pathId") Long pathId);

    /**
     * Find all active (incomplete) learning path enrollments for a user.
     * 
     * @param userId the user's UUID
     * @return list of active learning path enrollments
     */
    @Query("SELECT ulp FROM UserLearningPath ulp WHERE ulp.user.id = :userId AND ulp.completedAt IS NULL ORDER BY ulp.startedAt DESC")
    List<UserLearningPath> findActiveByUserId(@Param("userId") UUID userId);

    /**
     * Find all completed learning path enrollments for a user.
     * 
     * @param userId the user's UUID
     * @return list of completed learning path enrollments
     */
    @Query("SELECT ulp FROM UserLearningPath ulp WHERE ulp.user.id = :userId AND ulp.completedAt IS NOT NULL ORDER BY ulp.completedAt DESC")
    List<UserLearningPath> findCompletedByUserId(@Param("userId") UUID userId);
}
