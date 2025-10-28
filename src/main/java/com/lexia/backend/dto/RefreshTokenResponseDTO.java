package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response containing new access token and optionally a new refresh token after successful refresh")
public class RefreshTokenResponseDTO {

    /**
     * New JWT access token for API requests.
     * Valid for 15 minutes.
     */
    @Schema(description = "New JWT access token for authenticating API requests (15-minute expiration)", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBsZXhpYS5jb20iLCJpYXQiOjE3Mjk5MDAwMDAsImV4cCI6MTcyOTkwMDkwMH0.signature", accessMode = Schema.AccessMode.READ_ONLY)
    private String accessToken;

    /**
     * New JWT refresh token (optional - returned if rotation is enabled).
     * Valid for 7 days.
     */
    @Schema(description = "New refresh token (returned when rotation is enabled, 7-day expiration)", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBsZXhpYS5jb20iLCJ0b2tlbklkIjoibmV3dG9rZW4iLCJpYXQiOjE3Mjk5MDAwMDAsImV4cCI6MTczMDUwNDgwMH0.signature", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private String refreshToken;

    /**
     * Token type (typically "Bearer").
     */
    @Schema(description = "Token type for Authorization header", example = "Bearer", accessMode = Schema.AccessMode.READ_ONLY)
    private String tokenType;

    /**
     * Access token expiration time in milliseconds.
     */
    @Schema(description = "Access token expiration time in milliseconds (900000 ms = 15 minutes)", example = "900000", accessMode = Schema.AccessMode.READ_ONLY)
    private long expiresIn;
}
