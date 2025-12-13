package com.lexia.backend.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending a message in a role-play conversation.
 * 
 * <p>Used by all message endpoints (immersive, learning, streaming, fallback).</p>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send a message in a role-play conversation")
public class RolePlaySendMessageDTO {

    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 500, message = "Message content cannot exceed 500 characters")
    @Schema(
        description = "The user's message content",
        example = "Good morning! I wanted to discuss the project timeline with you.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 500
    )
    private String content;
}
