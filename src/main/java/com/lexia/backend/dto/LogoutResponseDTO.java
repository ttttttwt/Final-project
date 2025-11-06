package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO used to return confirmation message for logout operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponseDTO {

    @Schema(description = "Logout confirmation message", example = "Logged out successfully")
    private String message;
}
