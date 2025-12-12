package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO representing a role-play conversation session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role-play conversation")
public class RolePlayConversationDTO {

    @Schema(description = "Conversation ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "User ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    @Schema(description = "Scenario ID", nullable = true)
    private UUID scenarioId;

    @Schema(description = "Message history")
    private List<RolePlayMessageDTO> messages;

    @Schema(description = "AI-generated context summary of older messages", nullable = true)
    private String contextSummary;

    @Schema(description = "Conversation status", example = "in_progress", allowableValues = { "in_progress", "completed", "abandoned" })
    private String status;

    @Schema(description = "Conversation mode", example = "immersive", allowableValues = { "immersive", "learning" })
    private String mode;

    @Schema(description = "Conversation metrics (messageCount, word counts, scores)", nullable = true)
    private Map<String, Object> metrics;

    @Schema(description = "Conversation feedback summary (learning mode)", nullable = true)
    private Map<String, Object> feedbackSummary;

    @Schema(description = "Created timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(description = "Updated timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant updatedAt;
}
