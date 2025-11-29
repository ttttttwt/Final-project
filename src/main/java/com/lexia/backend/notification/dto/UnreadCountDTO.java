package com.lexia.backend.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for unread notification count response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Unread notification count")
public class UnreadCountDTO {

    @Schema(description = "Total unread notifications count", example = "5")
    private long unreadCount;

    @Schema(description = "High priority unread notifications count", example = "2")
    private long highPriorityCount;
}
