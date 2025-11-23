package com.lexia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDTO {
    private DashboardStatsDTO stats;
    private List<GoalDTO> weeklyGoals;
    private List<ActivityDTO> recentActivities;
    private List<RecommendationDTO> recommendations;
}
