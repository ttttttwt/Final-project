package com.lexia.backend.service.impl;

import com.lexia.backend.dto.LearningPathDTO;
import com.lexia.backend.dto.UserPathProgressDTO;
import com.lexia.backend.entity.LearningPath;
import com.lexia.backend.entity.LearningPathCourse;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserLearningPath;
import com.lexia.backend.exception.LearningPathNotFoundException;
import com.lexia.backend.mapper.LearningPathMapper;
import com.lexia.backend.repository.LearningPathRepository;
import com.lexia.backend.repository.UserLearningPathRepository;
import com.lexia.backend.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of LearningPathService for managing learning paths.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathServiceImpl implements LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final UserLearningPathRepository userLearningPathRepository;
    private final com.lexia.backend.repository.EnrollmentRepository enrollmentRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<LearningPathDTO> getAllPaths() {
        log.debug("Fetching all learning paths");

        List<LearningPath> paths = learningPathRepository.findAll();
        log.info("Retrieved {} learning paths", paths.size());

        return LearningPathMapper.toDTOList(paths);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public LearningPathDTO getPathById(Long id) {
        log.debug("Fetching learning path with ID: {}", id);

        LearningPath path = learningPathRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Learning path not found with ID: {}", id);
                    return new LearningPathNotFoundException("Learning path not found with ID: " + id);
                });

        log.info("Successfully retrieved learning path: {}", path.getName());
        return LearningPathMapper.toDTO(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public LearningPathDTO getRecommendedPath(User user) {
        log.debug("Getting recommended learning path for user: {}", user.getEmail());

        // Step 1: Check if user has completed any learning paths
        String recommendedLevel = findNextRecommendedLevel(user);

        // Step 2: Find default path for recommended level
        LearningPath path = learningPathRepository.findByCefrLevelAndIsDefaultTrue(recommendedLevel)
                .orElseThrow(() -> {
                    log.error("No default learning path found for CEFR level: {}", recommendedLevel);
                    return new LearningPathNotFoundException(
                            "No default learning path found for CEFR level: " + recommendedLevel);
                });

        log.info("Recommended learning path '{}' for user {} (CEFR level: {})",
                path.getName(), user.getEmail(), recommendedLevel);

        return LearningPathMapper.toDTO(path);
    }

    /**
     * Find the next recommended CEFR level for a user based on completion history.
     * 
     * <p>
     * Logic:
     * <ul>
     * <li>If user has completed any paths → recommend next level after highest completed</li>
     * <li>If user has no completed paths → use current level from profile</li>
     * <li>If user has no level set → default to A1 (beginner)</li>
     * <li>If user completed C2 → recommend C2 advanced paths</li>
     * </ul>
     * </p>
     * 
     * @param user the user
     * @return the recommended CEFR level string
     */
    private String findNextRecommendedLevel(User user) {
        // Get all completed learning paths
        List<UserLearningPath> completedPaths = userLearningPathRepository.findByUserId(user.getId())
                .stream()
                .filter(ulp -> ulp.getCompletedAt() != null)
                .collect(java.util.stream.Collectors.toList());

        if (!completedPaths.isEmpty()) {
            log.debug("User {} has completed {} learning paths", user.getEmail(), completedPaths.size());

            // Find highest completed CEFR level
            java.util.List<com.lexia.backend.enums.CEFRLevel> completedLevels = completedPaths.stream()
                    .map(ulp -> ulp.getLearningPath().getCefrLevel())
                    .filter(level -> level != null && !level.isEmpty())
                    .map(level -> {
                        try {
                            return com.lexia.backend.enums.CEFRLevel.fromString(level);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid CEFR level '{}' found in completed path, ignoring", level);
                            return null;
                        }
                    })
                    .filter(level -> level != null)
                    .collect(java.util.stream.Collectors.toList());

            if (!completedLevels.isEmpty()) {
                com.lexia.backend.enums.CEFRLevel highestCompleted = com.lexia.backend.enums.CEFRLevel.max(completedLevels);
                com.lexia.backend.enums.CEFRLevel nextLevel = highestCompleted.getNextLevel();

                log.info("User {} completed highest level {}, recommending next level {}",
                        user.getEmail(), highestCompleted, nextLevel);

                return nextLevel.name();
            }
        }

        // Fallback to current level from profile
        String userLevel = null;
        if (user.getProfile() != null && user.getProfile().getCurrentLevel() != null) {
            userLevel = user.getProfile().getCurrentLevel();
        }

        // Default to A1 if no level set
        final String cefrLevel = (userLevel == null || userLevel.isEmpty()) ? "A1" : userLevel;

        if (userLevel == null || userLevel.isEmpty()) {
            log.info("User {} has no CEFR level set and no completed paths, defaulting to A1", user.getEmail());
        } else {
            log.info("User {} has no completed paths, using current profile level: {}", user.getEmail(), cefrLevel);
        }

        return cefrLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserPathProgressDTO startPath(User user, Long pathId) {
        log.debug("User {} starting learning path with ID: {}", user.getEmail(), pathId);

        // Check if path exists
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> {
                    log.warn("Learning path not found with ID: {}", pathId);
                    return new LearningPathNotFoundException("Learning path not found with ID: " + pathId);
                });

        // Check if user already started this path
        if (userLearningPathRepository.existsByUserIdAndPathId(user.getId(), pathId)) {
            log.warn("User {} already started learning path: {}", user.getEmail(), path.getName());
            throw new IllegalStateException("You have already started this learning path");
        }

        // Get first course in the path
        if (path.getLearningPathCourses() == null || path.getLearningPathCourses().isEmpty()) {
            log.error("Learning path {} has no courses", path.getName());
            throw new IllegalStateException("Learning path has no courses");
        }

        // Sort by orderIndex and get first course
        LearningPathCourse firstPathCourse = path.getLearningPathCourses().stream()
                .min((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                .orElseThrow(() -> new IllegalStateException("Unable to determine first course"));

        // Create user learning path enrollment
        UserLearningPath userLearningPath = UserLearningPath.builder()
                .user(user)
                .learningPath(path)
                .currentCourse(firstPathCourse.getCourse())
                .build();

        UserLearningPath savedEnrollment = userLearningPathRepository.save(userLearningPath);
        log.info("User {} successfully started learning path: {}", user.getEmail(), path.getName());

        // Return progress DTO (0 courses completed initially)
        return LearningPathMapper.toProgressDTO(savedEnrollment, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserPathProgressDTO> getMyProgress(User user) {
        log.debug("Fetching learning path progress for user: {}", user.getEmail());

        List<UserLearningPath> userPaths = userLearningPathRepository.findByUserId(user.getId());
        log.info("User {} has {} learning path enrollments", user.getEmail(), userPaths.size());

        // Calculate courses completed for each path using real enrollment data
        Map<Long, Integer> coursesCompletedMap = new HashMap<>();
        for (UserLearningPath ulp : userPaths) {
            LearningPath path = ulp.getLearningPath();
            
            // Guard against null or empty course list
            if (path.getLearningPathCourses() == null || path.getLearningPathCourses().isEmpty()) {
                log.warn("Learning path {} has no courses, setting completed count to 0", path.getName());
                coursesCompletedMap.put(path.getId(), 0);
                continue;
            }
            
            // Extract course IDs from the learning path
            List<Long> courseIds = path.getLearningPathCourses().stream()
                    .map(lpc -> lpc.getCourse().getId())
                    .collect(java.util.stream.Collectors.toList());
            
            // Count completed courses in this path for this user
            long completedCount = enrollmentRepository.countCompletedByUserIdAndCourseIdIn(
                    user.getId(), 
                    courseIds
            );
            
            coursesCompletedMap.put(path.getId(), (int) completedCount);
            log.debug("User {} completed {}/{} courses in path: {}", 
                    user.getEmail(), completedCount, courseIds.size(), path.getName());
        }

        return LearningPathMapper.toProgressDTOList(userPaths, coursesCompletedMap);
    }
}
