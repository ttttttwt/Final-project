package com.lexia.backend.dto.placement;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementTestSubmissionDTO {
    @NotNull
    private List<PlacementAnswerDTO> answers;
}
