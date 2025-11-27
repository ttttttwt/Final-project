package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for creating a new user")
public class CreateUserDTO {
    @Email
    @NotBlank
    @Schema(description = "User email", example = "newuser@example.com")
    private String email;

    @NotBlank
    @Size(min = 8)
    @Schema(description = "Password (min 8 chars)", example = "Password123!")
    private String password;

    @NotBlank
    @Schema(description = "First name", example = "John")
    private String firstName;

    @NotBlank
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @NotBlank
    @Schema(description = "Role name (ADMIN, CONTENT_MANAGER, LEARNER)", example = "LEARNER")
    private String role;
}
