package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for user learning streak information.
 * Tracks consecutive days of lesson completion.
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User learning streak information")
public class StreakDTO {

    @Schema(description = "Current consecutive days streak", example = "7", minimum = "0", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer currentStreak;

    @Schema(description = "Longest consecutive days streak achieved", example = "15", minimum = "0", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer longestStreak;

    @Schema(description = "Date of last lesson completion", example = "2025-11-05", accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDate lastActivityDate;

    @Schema(description = "Whether user completed a lesson today", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isActiveToday;

    @Schema(description = "Total number of days with activity", example = "42", minimum = "0", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer totalActiveDays;
}
