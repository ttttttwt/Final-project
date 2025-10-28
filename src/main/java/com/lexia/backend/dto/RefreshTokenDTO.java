package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request to refresh access token using a valid refresh token")
public class RefreshTokenDTO {

    /**
     * The refresh token issued during login.
     * Used to obtain a new access token without re-authentication.
     */
    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Valid refresh token issued during login (JWT format, 7-day expiration)", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBsZXhpYS5jb20iLCJ0b2tlbklkIjoiYWJjZGVmZ2giLCJpYXQiOjE3Mjk5MDAwMDAsImV4cCI6MTczMDUwNDgwMH0.signature", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
