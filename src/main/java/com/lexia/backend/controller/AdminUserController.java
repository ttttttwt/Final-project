package com.lexia.backend.controller;

import com.lexia.backend.dto.AdminUserDTO;
import com.lexia.backend.dto.CreateUserDTO;
import com.lexia.backend.dto.UpdateUserDTO;
import com.lexia.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Endpoints for managing users (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List users", description = "Get a paginated list of users with optional search and filtering")
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @Parameter(description = "Search term (email or name)") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by role") @RequestParam(required = false) String role,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getAllUsers(search, role, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user details", description = "Get detailed information about a specific user")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user account")
    public ResponseEntity<AdminUserDTO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(createUserDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user account")
    public ResponseEntity<AdminUserDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return ResponseEntity.ok(adminUserService.updateUser(id, updateUserDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete (or deactivate) a user account")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
