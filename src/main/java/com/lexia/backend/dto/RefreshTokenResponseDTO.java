package com.lexia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful token refresh.
 * Contains new access token and optionally a new refresh token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponseDTO {

    /**
     * New JWT access token for API requests.
     * Valid for 15 minutes.
     */
    private String accessToken;

    /**
     * New JWT refresh token (optional - returned if rotation is enabled).
     * Valid for 7 days.
     */
    private String refreshToken;

    /**
     * Token type (typically "Bearer").
     */
    private String tokenType;

    /**
     * Access token expiration time in milliseconds.
     */
    private long expiresIn;
}
