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

        // Get user's CEFR level from profile
        String userLevel = null;
        if (user.getProfile() != null && user.getProfile().getCurrentLevel() != null) {
            userLevel = user.getProfile().getCurrentLevel();
        }

        // Default to A1 if no level set
        final String cefrLevel = (userLevel == null || userLevel.isEmpty()) ? "A1" : userLevel;

        if (userLevel == null || userLevel.isEmpty()) {
            log.info("User {} has no CEFR level set, defaulting to A1", user.getEmail());
        }

        // Find default path for user's level
        LearningPath path = learningPathRepository.findByCefrLevelAndIsDefaultTrue(cefrLevel)
                .orElseThrow(() -> {
                    log.error("No default learning path found for CEFR level: {}", cefrLevel);
                    return new LearningPathNotFoundException(
                            "No default learning path found for CEFR level: " + cefrLevel);
                });

        log.info("Recommended learning path '{}' for user {} (CEFR level: {})",
                path.getName(), user.getEmail(), cefrLevel);

        return LearningPathMapper.toDTO(path);
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

        // Calculate courses completed for each path
        // TODO: In Sprint 3, integrate with enrollment/progress tables
        // For now, return 0 completed courses as placeholder
        Map<Long, Integer> coursesCompletedMap = new HashMap<>();
        for (UserLearningPath ulp : userPaths) {
            coursesCompletedMap.put(ulp.getLearningPath().getId(), 0);
        }

        return LearningPathMapper.toProgressDTOList(userPaths, coursesCompletedMap);
    }
}
