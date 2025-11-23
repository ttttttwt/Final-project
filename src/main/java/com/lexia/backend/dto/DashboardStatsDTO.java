package com.lexia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private int enrolledCourses;
    private int completedLessons;
    private int totalLessons;
    private int currentStreak;
    private int longestStreak;
    private int totalStudyMinutes; // New field
    private double averageScore;   // New field
}
