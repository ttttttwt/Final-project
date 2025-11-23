package com.lexia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {
    private String id;
    private String title;
    private String description;
    private String type; // e.g., "COURSE", "LESSON", "TIP"
    private String link;
    private String reason; // e.g., "Based on your recent grammar score"
    private String imageUrl;
}
