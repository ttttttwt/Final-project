package com.lexia.backend.service;

import com.lexia.backend.dto.AdminUserDTO;
import com.lexia.backend.dto.AdminUserDetailDTO;
import com.lexia.backend.dto.CreateUserDTO;
import com.lexia.backend.dto.UpdateUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {
    /**
     * Get all active (non-deleted) users with search and role filter.
     */
    Page<AdminUserDTO> getAllUsers(String search, String role, Pageable pageable);

    /**
     * Get user by ID.
     */
    AdminUserDTO getUserById(UUID id);

    /**
     * Get comprehensive user details by ID (for detail page).
     */
    AdminUserDetailDTO getUserDetailById(UUID id);

    /**
     * Create a new user.
     */
    AdminUserDTO createUser(CreateUserDTO createUserDTO);

    /**
     * Update an existing user.
     */
    AdminUserDTO updateUser(UUID id, UpdateUserDTO updateUserDTO);

    /**
     * Soft delete a user (mark as deleted, keep data).
     */
    void deleteUser(UUID id);

    // ==================== Soft Delete Management ====================

    /**
     * Hard delete a user (permanently remove from database).
     * Requires confirmation reason for audit trail.
     *
     * @param id     user ID
     * @param reason reason for permanent deletion
     */
    void hardDeleteUser(UUID id, String reason);

    /**
     * Restore a soft-deleted user.
     *
     * @param id user ID
     * @return the restored user
     */
    AdminUserDTO restoreUser(UUID id);

    /**
     * Get all soft-deleted users (trash view).
     *
     * @param pageable pagination info
     * @return page of deleted users
     */
    Page<AdminUserDTO> getDeletedUsers(Pageable pageable);
}
