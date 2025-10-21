package com.lexia.backend.dto;

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
public class LoginResponseDTO {

    /**
     * JWT access token for API requests.
     * Valid for 15 minutes.
     */
    private String accessToken;

    /**
     * JWT refresh token for obtaining new access tokens.
     * Valid for 7 days.
     */
    private String refreshToken;

    /**
     * User information from successful login.
     */
    private UserDTO user;

    /**
     * Token type (typically "Bearer").
     */
    private String tokenType;

    /**
     * Access token expiration time in milliseconds.
     */
    private long expiresIn;
}
