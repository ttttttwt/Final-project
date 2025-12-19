package com.lexia.backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user activity history (AI usage).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDTO {
    private UUID userId;
    private String userEmail;
    private List<ActivityItem> activities;
    private ActivitySummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private Long id;
        private String contentType; // roleplay, grammar, flashcard
        private String description;
        private Instant timestamp;
        private Integer tokensUsed;
        private Boolean success;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySummary {
        private int totalRequests;
        private int roleplayRequests;
        private int grammarRequests;
        private int flashcardRequests;
        private int successfulRequests;
        private int failedRequests;
    }
}
