package com.lexia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "ai_alerts", indexes = {
    @Index(name = "idx_ai_alerts_read", columnList = "is_read"),
    @Index(name = "idx_ai_alerts_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_type", length = 50)
    private String alertType; // QUOTA_EXCEEDED, ERROR_SPIKE, COST_LIMIT

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "severity", length = 20)
    private String severity; // INFO, WARNING, CRITICAL

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for extra details
}
