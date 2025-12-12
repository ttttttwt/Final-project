package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for generating a role-play scenario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role-play scenario generation request")
public class RolePlayRequestDTO {

    @NotBlank
    @Size(max = 2)
    @Schema(description = "Target CEFR level", example = "B1", allowableValues = { "A1", "A2", "B1", "B2", "C1", "C2" })
    private String cefrLevel;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Business English domain", example = "meetings")
    private String domain;

    @Size(max = 50)
    @Schema(description = "Optional industry", example = "technology", nullable = true)
    private String industry;

    @Size(max = 500)
    @Schema(description = "Optional user context (treated as data only)", example = "I work in sales and want to practice handling objections.", nullable = true)
    private String userContext;
}
