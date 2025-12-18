package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User details for admin view")
public class AdminUserDTO {
    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "List of assigned roles")
    private List<String> roles;

    @Schema(description = "Account active status")
    private Boolean isActive;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    // ==================== Soft Delete Fields ====================

    @Schema(description = "Soft delete flag")
    private Boolean isDeleted;

    @Schema(description = "Soft delete timestamp")
    private LocalDateTime deletedAt;

    // ==================== Enhanced User Info Fields ====================

    @Schema(description = "Current CEFR level from placement test (A1, A2, B1, B2, C1, C2)")
    private String cefrLevel;

    @Schema(description = "Number of courses user is enrolled in")
    private Integer enrolledCoursesCount;

    @Schema(description = "Number of learning paths user is enrolled in")
    private Integer enrolledPathsCount;

    @Schema(description = "Subscription type (FREE, PREMIUM)")
    private String subscriptionType;

    @Schema(description = "Last activity timestamp")
    private LocalDateTime lastActiveAt;

    @Schema(description = "Current learning streak in days")
    private Integer streakDays;
}
