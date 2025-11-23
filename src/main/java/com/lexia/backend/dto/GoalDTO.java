package com.lexia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalDTO {
    private String id;
    private String title;
    private String description;
    private int currentProgress;
    private int targetProgress;
    private String unit; // e.g., "lessons", "points"
    private boolean isCompleted;
}
