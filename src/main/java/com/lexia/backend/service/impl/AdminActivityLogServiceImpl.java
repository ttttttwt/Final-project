package com.lexia.backend.service.impl;

import com.lexia.backend.entity.AdminActivityLog;
import com.lexia.backend.repository.AdminActivityLogRepository;
import com.lexia.backend.service.AdminActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of AdminActivityLogService.
 * Logs admin/content manager actions asynchronously to avoid blocking main
 * operations.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminActivityLogServiceImpl implements AdminActivityLogService {

    private final AdminActivityLogRepository adminActivityLogRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCourseCreated(UUID userId, String userName, Long courseId, String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.COURSE_CREATED)
                    .entityType(AdminActivityLog.EntityType.COURSE)
                    .entityId(courseId.toString())
                    .entityName(courseTitle)
                    .description(userName + " created course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged course creation: {} by {}", courseTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log course creation: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCourseUpdated(UUID userId, String userName, Long courseId, String courseTitle, String changes) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.COURSE_UPDATED)
                    .entityType(AdminActivityLog.EntityType.COURSE)
                    .entityId(courseId.toString())
                    .entityName(courseTitle)
                    .description(userName + " updated course \"" + courseTitle + "\"")
                    .details(changes)
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged course update: {} by {}", courseTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log course update: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCoursePublished(UUID userId, String userName, Long courseId, String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.COURSE_PUBLISHED)
                    .entityType(AdminActivityLog.EntityType.COURSE)
                    .entityId(courseId.toString())
                    .entityName(courseTitle)
                    .description(userName + " published course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged course publish: {} by {}", courseTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log course publish: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCourseUnpublished(UUID userId, String userName, Long courseId, String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.COURSE_UNPUBLISHED)
                    .entityType(AdminActivityLog.EntityType.COURSE)
                    .entityId(courseId.toString())
                    .entityName(courseTitle)
                    .description(userName + " unpublished course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged course unpublish: {} by {}", courseTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log course unpublish: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCourseDeleted(UUID userId, String userName, Long courseId, String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.COURSE_DELETED)
                    .entityType(AdminActivityLog.EntityType.COURSE)
                    .entityId(courseId.toString())
                    .entityName(courseTitle)
                    .description(userName + " deleted course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged course deletion: {} by {}", courseTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log course deletion: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSectionCreated(UUID userId, String userName, Long sectionId, String sectionTitle,
            String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.SECTION_CREATED)
                    .entityType(AdminActivityLog.EntityType.SECTION)
                    .entityId(sectionId.toString())
                    .entityName(sectionTitle)
                    .description(
                            userName + " created section \"" + sectionTitle + "\" in course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged section creation: {} by {}", sectionTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log section creation: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSectionUpdated(UUID userId, String userName, Long sectionId, String sectionTitle,
            String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.SECTION_UPDATED)
                    .entityType(AdminActivityLog.EntityType.SECTION)
                    .entityId(sectionId.toString())
                    .entityName(sectionTitle)
                    .description(
                            userName + " updated section \"" + sectionTitle + "\" in course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged section update: {} by {}", sectionTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log section update: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSectionDeleted(UUID userId, String userName, Long sectionId, String sectionTitle,
            String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.SECTION_DELETED)
                    .entityType(AdminActivityLog.EntityType.SECTION)
                    .entityId(sectionId.toString())
                    .entityName(sectionTitle)
                    .description(
                            userName + " deleted section \"" + sectionTitle + "\" from course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged section deletion: {} by {}", sectionTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log section deletion: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLessonCreated(UUID userId, String userName, Long lessonId, String lessonTitle, String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.LESSON_CREATED)
                    .entityType(AdminActivityLog.EntityType.LESSON)
                    .entityId(lessonId.toString())
                    .entityName(lessonTitle)
                    .description(userName + " created lesson \"" + lessonTitle + "\" in course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged lesson creation: {} by {}", lessonTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log lesson creation: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLessonUpdated(UUID userId, String userName, Long lessonId, String lessonTitle, String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.LESSON_UPDATED)
                    .entityType(AdminActivityLog.EntityType.LESSON)
                    .entityId(lessonId.toString())
                    .entityName(lessonTitle)
                    .description(userName + " updated lesson \"" + lessonTitle + "\" in course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged lesson update: {} by {}", lessonTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log lesson update: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLessonDeleted(UUID userId, String userName, Long lessonId, String lessonTitle, String courseTitle) {
        try {
            AdminActivityLog activityLog = AdminActivityLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AdminActivityLog.ActionType.LESSON_DELETED)
                    .entityType(AdminActivityLog.EntityType.LESSON)
                    .entityId(lessonId.toString())
                    .entityName(lessonTitle)
                    .description(
                            userName + " deleted lesson \"" + lessonTitle + "\" from course \"" + courseTitle + "\"")
                    .build();
            adminActivityLogRepository.save(activityLog);
            log.debug("Logged lesson deletion: {} by {}", lessonTitle, userName);
        } catch (Exception e) {
            log.error("Failed to log lesson deletion: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminActivityLog> getRecentActivities(int limit) {
        return adminActivityLogRepository.findRecentActivities(PageRequest.of(0, limit));
    }
}
