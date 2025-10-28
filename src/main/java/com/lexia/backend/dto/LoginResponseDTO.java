package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful login.
 * Contains JWT tokens and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing JWT tokens and user information after successful authentication")
public class LoginResponseDTO {

    /**
     * JWT access token for API requests.
     * Valid for 15 minutes.
     */
    @Schema(description = "JWT access token for authenticating API requests (15-minute expiration)", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBsZXhpYS5jb20iLCJpYXQiOjE3Mjk5MDAwMDAsImV4cCI6MTcyOTkwMDkwMH0.signature", accessMode = Schema.AccessMode.READ_ONLY)
    private String accessToken;

    /**
     * JWT refresh token for obtaining new access tokens.
     * Valid for 7 days.
     */
    @Schema(description = "JWT refresh token for obtaining new access tokens without re-authentication (7-day expiration)", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBsZXhpYS5jb20iLCJ0b2tlbklkIjoiYWJjZGVmZ2giLCJpYXQiOjE3Mjk5MDAwMDAsImV4cCI6MTczMDUwNDgwMH0.signature", accessMode = Schema.AccessMode.READ_ONLY)
    private String refreshToken;

    /**
     * User information from successful login.
     */
    @Schema(description = "User account and profile information", accessMode = Schema.AccessMode.READ_ONLY)
    private UserDTO user;

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
