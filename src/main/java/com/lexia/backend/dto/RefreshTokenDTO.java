package com.lexia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for refresh token requests.
 * Used to refresh access tokens without re-authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDTO {

    /**
     * The refresh token issued during login.
     * Used to obtain a new access token without re-authentication.
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
