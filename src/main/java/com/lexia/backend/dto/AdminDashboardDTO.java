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
public class AdminDashboardDTO {
    private long totalUsers;
    private long totalCourses;
    private long totalLessons;
    private long activeEnrollments;
    private List<ActivityDTO> recentActivities;
}
