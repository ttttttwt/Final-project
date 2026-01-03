package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive user details DTO for admin detail view.
 * Aggregates data from multiple entities for a complete user overview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Comprehensive user details for admin view")
public class AdminUserDetailDTO {

    // ==================== Basic Info ====================

    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "Avatar URL")
    private String avatarUrl;

    @Schema(description = "Phone number")
    private String phoneNumber;

    @Schema(description = "Bio")
    private String bio;

    @Schema(description = "Timezone")
    private String timezone;

    @Schema(description = "Preferred language")
    private String language;

    @Schema(description = "List of assigned roles")
    private List<String> roles;

    @Schema(description = "Account active status")
    private Boolean isActive;

    @Schema(description = "Soft delete flag")
    private Boolean isDeleted;

    @Schema(description = "Soft delete timestamp")
    private LocalDateTime deletedAt;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last activity timestamp")
    private LocalDateTime lastActiveAt;

    @Schema(description = "Authentication provider (LOCAL, GOOGLE)")
    private String authProvider;

    // ==================== CEFR Level & Placement ====================

    @Schema(description = "Current CEFR level")
    private String currentCefrLevel;

    @Schema(description = "Learning goal set by user")
    private String learningGoal;

    @Schema(description = "Placement test history")
    private List<PlacementResultDTO> placementHistory;

    // ==================== Subscription ====================

    @Schema(description = "Subscription information")
    private SubscriptionInfoDTO subscription;

    // ==================== Learning Progress ====================

    @Schema(description = "Enrolled courses with progress")
    private List<EnrollmentSummaryDTO> enrolledCourses;

    @Schema(description = "Enrolled learning paths with progress")
    private List<LearningPathSummaryDTO> enrolledPaths;

    @Schema(description = "Learning statistics")
    private LearningStatsDTO learningStats;

    // ==================== AI Usage ====================

    @Schema(description = "AI quota summary")
    private AiQuotaSummaryDTO aiQuota;

    @Schema(description = "Recent AI usage logs")
    private List<AiUsageDTO> recentAiUsage;

    // ==================== Activity Log ====================

    @Schema(description = "Recent activities")
    private List<ActivityLogDTO> recentActivities;

    // ==================== Nested DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlacementResultDTO {
        private UUID id;
        private Integer score;
        private Integer totalQuestions;
        private String assignedLevel;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionInfoDTO {
        private String planType; // FREE, PREMIUM
        private String status; // ACTIVE, CANCELLED, EXPIRED
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String stripeCustomerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollmentSummaryDTO {
        private Long courseId;
        private String courseName;
        private String courseThumbnail;
        private String cefrLevel;
        private Integer progressPercentage;
        private Boolean isCompleted;
        private LocalDateTime enrolledAt;
        private LocalDateTime completedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningPathSummaryDTO {
        private Long pathId;
        private String pathName;
        private String pathDescription;
        private Integer currentCourseIndex;
        private Integer totalCourses;
        private BigDecimal overallProgress;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningStatsDTO {
        private Integer totalEnrolledCourses;
        private Integer completedCourses;
        private Integer totalEnrolledPaths;
        private Integer completedPaths;
        private Integer completedLessons;
        private Integer currentStreak;
        private Integer bestStreak;
        private Long totalStudyTimeMinutes;
        private Integer rolePlaySessions;
        private Integer flashcardDecksCreated;
        private Integer grammarExercisesCompleted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiQuotaSummaryDTO {
        private Boolean isPremium;

        // ========== Subscription-Based Quota (NEW) ==========

        /** User's subscription plan: FREE, MONTHLY, YEARLY */
        private String planType;

        /** Date when monthly quota resets */
        private LocalDate quotaResetDate;

        /** Days until next quota reset */
        private Integer daysUntilReset;

        /** Role play sessions used/limit for current month */
        private Integer roleplaySessionsUsed;
        private Integer roleplaySessionsLimit;

        /** Flashcard decks created used/limit */
        private Integer flashcardDecksUsed;
        private Integer flashcardDecksLimit;

        /** Grammar exercises generated used/limit for current month */
        private Integer grammarExercisesUsed;
        private Integer grammarExercisesLimit;

        /** Custom materials created used/limit for current month */
        private Integer customMaterialsUsed;
        private Integer customMaterialsLimit;

        /** Total AI requests used/limit for current month */
        private Integer totalRequestsUsed;
        private Integer totalRequestsLimit;

        // ========== Legacy Fields (kept for backwards compatibility) ==========

        private Integer dailyLimit;
        private Integer dailyUsed;
        private Integer monthlyLimit;
        private Integer monthlyUsed;
        private Map<String, FeatureQuotaDTO> featureQuotas; // roleplay, grammar, flashcard
        private LocalDateTime lastResetAt;
        private Boolean isSuspended;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureQuotaDTO {
        private Integer dailyLimit;
        private Integer dailyUsed;
        private Integer monthlyLimit;
        private Integer monthlyUsed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiUsageDTO {
        private Long id;
        private String contentType; // roleplay, grammar, flashcard
        private String modelId;
        private Integer inputTokens;
        private Integer outputTokens;
        private BigDecimal estimatedCostUsd;
        private Integer responseTimeMs;
        private Boolean success;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLogDTO {
        private Long id;
        private String action; // LOGIN, LESSON_COMPLETE, TEST_SUBMIT, etc.
        private String description;
        private String ipAddress;
        private Map<String, Object> metadata;
        private LocalDateTime createdAt;
    }
}
