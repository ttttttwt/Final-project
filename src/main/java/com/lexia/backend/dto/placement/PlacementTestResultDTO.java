package com.lexia.backend.dto.placement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementTestResultDTO {
    private Integer score;
    private Integer totalQuestions;
    private String assignedLevel;
    private String message;
    private Long assignedLearningPathId;
    private String assignedLearningPathName;
}
