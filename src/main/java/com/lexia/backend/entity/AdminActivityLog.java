package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for tracking admin/content manager activities.
 * Records actions like course create/update/publish/delete for dashboard
 * display.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@Entity
@Table(name = "admin_activity_logs", indexes = {
        @Index(name = "idx_admin_activity_created_at", columnList = "created_at DESC"),
        @Index(name = "idx_admin_activity_user", columnList = "user_id"),
        @Index(name = "idx_admin_activity_action", columnList = "action")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The admin/content manager who performed the action.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Display name of the user who performed the action.
     */
    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    /**
     * The type of action performed.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActionType action;

    /**
     * The entity type that was modified.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    /**
     * The ID of the entity that was modified (as String for flexibility).
     */
    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    /**
     * Name/title of the entity for display purposes.
     */
    @Column(name = "entity_name", nullable = false, length = 255)
    private String entityName;

    /**
     * Human-readable description of the action.
     */
    @Column(nullable = false, length = 500)
    private String description;

    /**
     * Additional details about the change (JSON format, optional).
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * Timestamp when the action was performed.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Action types for admin activities.
     */
    public enum ActionType {
        COURSE_CREATED,
        COURSE_UPDATED,
        COURSE_PUBLISHED,
        COURSE_UNPUBLISHED,
        COURSE_DELETED,
        SECTION_CREATED,
        SECTION_UPDATED,
        SECTION_DELETED,
        LESSON_CREATED,
        LESSON_UPDATED,
        LESSON_DELETED
    }

    /**
     * Entity types that can be logged.
     */
    public enum EntityType {
        COURSE,
        SECTION,
        LESSON
    }
}
