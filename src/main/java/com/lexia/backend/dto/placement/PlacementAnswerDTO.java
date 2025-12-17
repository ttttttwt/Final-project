package com.lexia.backend.dto.placement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementAnswerDTO {
    @NotNull
    private UUID questionId;
    @NotBlank
    private String selectedOption; // A, B, C, or D
}
