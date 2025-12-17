package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO containing detailed context information for a role-play scenario.
 * Provides background info to help users understand the situation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed context for a role-play scenario")
public class RolePlayContextDetailsDTO {

    @Schema(description = "Where this scenario takes place", example = "Modern tech company office")
    private String setting;

    @Schema(description = "What's happening and why", example = "Weekly sprint review meeting")
    private String situation;

    @Schema(description = "Key facts the user should know", example = "[\"Project deadline is next Friday\", \"Budget is limited\"]")
    private List<String> keyInfo;

    @Schema(description = "What the user should try to achieve", example = "Get status updates from all team members")
    private String yourGoal;

    @Schema(description = "Helpful tips for this scenario", example = "[\"Start with a brief agenda overview\"]")
    private List<String> tips;
}
