package com.lexia.backend.dto.placement;

import com.lexia.backend.entity.PlacementTestQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementQuestionResponseDTO {
    private UUID id;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private PlacementTestQuestion.QuestionCategory category;
}
