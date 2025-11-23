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
public class ProgressSummaryDTO {
    private long totalLessonsCompleted;
    private long totalTimeSpentMinutes;
    private long averageTimePerLesson;
    private int activeDays;
    private List<DailyActivityDTO> dailyActivities;
}
