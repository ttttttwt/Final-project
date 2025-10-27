package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuditLog entity for tracking user profile changes.
 * Records who made what changes and when for compliance and security purposes.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The user who performed the action.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The type of action performed.
     * Examples: PROFILE_UPDATE, AVATAR_UPDATE, AVATAR_DELETE
     */
    @Column(nullable = false, length = 50)
    private String action;

    /**
     * The entity type that was modified.
     * Examples: UserProfile, User
     */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    /**
     * The ID of the entity that was modified.
     */
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    /**
     * Description of what changed.
     * JSON format: {"field": "firstName", "oldValue": "John", "newValue": "Jane"}
     */
    @Column(columnDefinition = "TEXT")
    private String changes;

    /**
     * IP address of the user who made the change.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent string from the request.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Timestamp when the action was performed.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
