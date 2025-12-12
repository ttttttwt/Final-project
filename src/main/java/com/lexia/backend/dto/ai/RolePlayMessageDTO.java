package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO representing a single message in a role-play conversation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role-play conversation message")
public class RolePlayMessageDTO {

    @Schema(description = "Message role", example = "user", allowableValues = { "user", "ai" })
    private String role;

    @Schema(description = "Message content", example = "Could you share the latest project update?")
    private String content;

    @Schema(description = "Timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant timestamp;

    @Schema(description = "Optional feedback metadata (learning mode)", nullable = true)
    private Map<String, Object> feedback;
}
