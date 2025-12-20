package com.lexia.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a chat session for custom material role-play.
 * 
 * <p>
 * Users can have multiple chat sessions per material, practicing
 * different scenarios or re-doing conversations.
 * </p>
 * 
 * <p>
 * Table: custom_material_chat_sessions (V38 migration)
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 * @see UserCustomMaterial
 */
@Entity
@Table(name = "custom_material_chat_sessions", indexes = {
        @Index(name = "idx_chat_sessions_material_id", columnList = "material_id"),
        @Index(name = "idx_chat_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_chat_sessions_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "chatHistory", "performanceReport" })
public class CustomMaterialChatSession {

    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ABANDONED = "abandoned";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private UserCustomMaterial material;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * JSON array of chat messages.
     * Structure: [{ "role": "user"|"ai", "content": "...", "timestamp": "..." }]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chat_history", columnDefinition = "TEXT")
    @Builder.Default
    private List<Map<String, Object>> chatHistory = new ArrayList<>();

    /**
     * Performance report generated at end of session (for POLITE mode).
     * Contains grammar errors, vocabulary suggestions, overall score.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "performance_report", columnDefinition = "TEXT")
    private Map<String, Object> performanceReport;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_IN_PROGRESS;

    @Column(name = "started_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = Instant.now();
        }
        if (chatHistory == null) {
            chatHistory = new ArrayList<>();
        }
    }

    // ===== Helper Methods =====

    /**
     * Adds a message to the chat history.
     * 
     * @param role    "user" or "ai"
     * @param content message content
     */
    public void addMessage(String role, String content) {
        if (chatHistory == null) {
            chatHistory = new ArrayList<>();
        }
        chatHistory.add(Map.of(
                "role", role,
                "content", content,
                "timestamp", Instant.now().toString()));
    }

    /**
     * Ends the session with a performance report.
     * 
     * @param report the performance report data
     */
    public void endSession(Map<String, Object> report) {
        this.status = STATUS_COMPLETED;
        this.performanceReport = report;
        this.endedAt = Instant.now();
    }

    /**
     * Abandons the session without a report.
     */
    public void abandonSession() {
        this.status = STATUS_ABANDONED;
        this.endedAt = Instant.now();
    }

    /**
     * Checks if the session is still active.
     * 
     * @return true if in progress
     */
    public boolean isActive() {
        return STATUS_IN_PROGRESS.equals(status);
    }

    /**
     * Gets the message count.
     * 
     * @return number of messages in chat history
     */
    public int getMessageCount() {
        return chatHistory != null ? chatHistory.size() : 0;
    }
}
