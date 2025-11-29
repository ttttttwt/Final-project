package com.lexia.backend.service;

import com.lexia.backend.dto.ActivityDTO;
import com.lexia.backend.dto.AdminDashboardDTO;
import com.lexia.backend.entity.AdminActivityLog;
import com.lexia.backend.repository.AdminActivityLogRepository;
import com.lexia.backend.repository.CourseRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for admin dashboard statistics and recent activities.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final AdminActivityLogRepository adminActivityLogRepository;

    private static final List<String> CEFR_LEVELS = List.of("A1", "A2", "B1", "B2", "C1", "C2");
    private static final int RECENT_ACTIVITY_LIMIT = 10;

    /**
     * Get comprehensive admin dashboard statistics.
     *
     * @return AdminDashboardDTO with all dashboard data
     */
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardStats() {
        log.debug("Fetching admin dashboard statistics");

        // Basic counts
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long publishedCourses = courseRepository.countByIsPublishedTrue();
        long totalLessons = lessonRepository.count();

        // Course counts by CEFR level
        Map<String, Long> coursesByLevel = getCoursesByLevel();

        // Recent activities from activity log
        List<ActivityDTO> recentActivities = getRecentActivities();

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .totalLessons(totalLessons)
                .coursesByLevel(coursesByLevel)
                .recentActivities(recentActivities)
                .build();
    }

    /**
     * Get course counts by CEFR level.
     */
    private Map<String, Long> getCoursesByLevel() {
        Map<String, Long> coursesByLevel = new LinkedHashMap<>();
        for (String level : CEFR_LEVELS) {
            coursesByLevel.put(level, courseRepository.countByCefrLevel(level));
        }
        return coursesByLevel;
    }

    /**
     * Get recent admin activities from activity log.
     */
    private List<ActivityDTO> getRecentActivities() {
        List<AdminActivityLog> activityLogs = adminActivityLogRepository
                .findRecentActivities(PageRequest.of(0, RECENT_ACTIVITY_LIMIT));

        return activityLogs.stream()
                .map(this::mapToActivityDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map AdminActivityLog to ActivityDTO.
     */
    private ActivityDTO mapToActivityDTO(AdminActivityLog log) {
        String link = generateLink(log);

        return ActivityDTO.builder()
                .id(log.getId().toString())
                .type(log.getAction().name())
                .description(log.getDescription())
                .timestamp(log.getCreatedAt())
                .link(link)
                .userName(log.getUserName())
                .courseName(log.getEntityType() == AdminActivityLog.EntityType.COURSE
                        ? log.getEntityName()
                        : null)
                .build();
    }

    /**
     * Generate link based on entity type and action.
     */
    private String generateLink(AdminActivityLog log) {
        if (log.getAction() == AdminActivityLog.ActionType.COURSE_DELETED ||
                log.getAction() == AdminActivityLog.ActionType.SECTION_DELETED ||
                log.getAction() == AdminActivityLog.ActionType.LESSON_DELETED) {
            return null; // No link for deleted entities
        }

        return switch (log.getEntityType()) {
            case COURSE -> "/courses/" + log.getEntityId() + "/edit";
            case SECTION -> "/courses/" + log.getEntityId() + "/edit"; // Section links to course edit
            case LESSON -> "/lessons/" + log.getEntityId() + "/edit";
        };
    }
}