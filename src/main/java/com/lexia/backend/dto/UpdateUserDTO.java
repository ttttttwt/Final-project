package com.lexia.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for updating an existing user")
public class UpdateUserDTO {
    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "New password (optional)")
    private String password;

    @Schema(description = "Account active status")
    private Boolean isActive;

    @Schema(description = "Role name")
    private String role;
}
