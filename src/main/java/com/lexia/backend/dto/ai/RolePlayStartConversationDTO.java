package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for starting a new role-play conversation.
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to start a new role-play conversation")
public class RolePlayStartConversationDTO {

    @NotNull(message = "Scenario ID is required")
    @Schema(
        description = "The scenario ID to start the conversation with",
        example = "550e8400-e29b-41d4-a716-446655440000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID scenarioId;

    @Pattern(regexp = "^(immersive|learning)$", message = "Mode must be 'immersive' or 'learning'")
    @Schema(
        description = "Conversation mode: 'immersive' for fast chat-only, 'learning' for chat with feedback",
        example = "immersive",
        allowableValues = {"immersive", "learning"},
        defaultValue = "immersive"
    )
    private String mode = "immersive";
}
