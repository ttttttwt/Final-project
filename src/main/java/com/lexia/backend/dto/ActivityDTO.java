package com.lexia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    private String id;
    private String type; // e.g., "LESSON_COMPLETED", "QUIZ_PASSED"
    private String description;
    private LocalDateTime timestamp;
    private String link; // URL to view details
    private int score;
}
