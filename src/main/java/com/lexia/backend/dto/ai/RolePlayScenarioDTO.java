package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a role-play scenario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role-play scenario")
public class RolePlayScenarioDTO {

    @Schema(description = "Scenario ID", example = "550e8400-e29b-41d4-a716-446655440000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Title", example = "Project Status Meeting")
    private String title;

    @Schema(description = "Scenario context", example = "You are in a weekly team meeting...")
    private String context;

    @Schema(description = "User role", example = "Team Member")
    private String yourRole;

    @Schema(description = "AI role", example = "Project Manager (Sarah)")
    private String aiRole;

    @Schema(description = "CEFR level", example = "B1", allowableValues = { "A1", "A2", "B1", "B2", "C1", "C2" })
    private String cefrLevel;

    @Schema(description = "Domain", example = "meetings")
    private String domain;

    @Schema(description = "Industry", example = "technology", nullable = true)
    private String industry;

    @Schema(description = "Learning objectives")
    private List<String> objectives;

    @Schema(description = "Key vocabulary hints")
    private List<RolePlayVocabularyItemDTO> keyVocabulary;

    @Schema(description = "Opening line for the AI to start the conversation")
    private String openingLine;

    @Schema(description = "Suggested duration in minutes", example = "10")
    private Integer suggestedDuration;

    @Schema(description = "True if this is pre-seeded fallback content", example = "false")
    private Boolean isFallback;

    @Schema(description = "Created timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;
}
