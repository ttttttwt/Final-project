package com.lexia.backend.service;

import com.lexia.backend.dto.LearningPathDTO;
import com.lexia.backend.dto.UserPathProgressDTO;
import com.lexia.backend.entity.User;
import com.lexia.backend.exception.LearningPathNotFoundException;

import java.util.List;

/**
 * Service interface for learning path management operations.
 * 
 * <p>
 * Handles learning path operations including:
 * </p>
 * <ul>
 * <li>Retrieving learning paths</li>
 * <li>Recommending paths based on user's CEFR level</li>
 * <li>Starting a learning path</li>
 * <li>Tracking user progress in learning paths</li>
 * </ul>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
public interface LearningPathService {

    /**
     * Retrieves all learning paths.
     * 
     * @return list of all LearningPathDTO
     */
    List<LearningPathDTO> getAllPaths();

    /**
     * Retrieves a learning path by ID.
     * 
     * @param id the learning path ID
     * @return LearningPathDTO containing the path data
     * @throws LearningPathNotFoundException if learning path not found
     */
    LearningPathDTO getPathById(Long id);

    /**
     * Recommends a learning path for a user based on their CEFR level.
     * 
     * <p>
     * Business rules:
     * </p>
     * <ul>
     * <li>Returns the default path matching user's CEFR level</li>
     * <li>Returns A1 beginner path if user has no level set</li>
     * <li>TODO: Add progressive recommendation in Sprint 3 (based on completed
     * paths)</li>
     * </ul>
     * 
     * @param user the user to recommend a path for
     * @return LearningPathDTO containing the recommended path
     * @throws LearningPathNotFoundException if no suitable path found
     */
    LearningPathDTO getRecommendedPath(User user);

    /**
     * Starts a learning path for a user.
     * 
     * <p>
     * Business rules:
     * </p>
     * <ul>
     * <li>Creates UserLearningPath record</li>
     * <li>Sets current_course_id to first course in the path</li>
     * <li>User can start multiple paths simultaneously</li>
     * <li>Throws exception if user already started this path</li>
     * </ul>
     * 
     * @param user   the user starting the path
     * @param pathId the learning path ID
     * @return UserPathProgressDTO containing the enrollment data
     * @throws LearningPathNotFoundException if learning path not found
     * @throws IllegalStateException         if user already started this path
     */
    UserPathProgressDTO startPath(User user, Long pathId);

    /**
     * Retrieves all learning path progress for a user.
     * 
     * <p>
     * Returns both active (incomplete) and completed paths.
     * </p>
     * 
     * @param user the user to get progress for
     * @return list of UserPathProgressDTO with progress information
     */
    List<UserPathProgressDTO> getMyProgress(User user);
}
