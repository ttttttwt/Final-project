package com.lexia.backend.service;

import com.lexia.backend.dto.AdminUserDTO;
import com.lexia.backend.dto.CreateUserDTO;
import com.lexia.backend.dto.UpdateUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {
    Page<AdminUserDTO> getAllUsers(String search, String role, Pageable pageable);

    AdminUserDTO getUserById(UUID id);

    AdminUserDTO createUser(CreateUserDTO createUserDTO);

    AdminUserDTO updateUser(UUID id, UpdateUserDTO updateUserDTO);

    void deleteUser(UUID id);
}
