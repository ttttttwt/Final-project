package com.lexia.backend.service.impl;

import com.lexia.backend.dto.AdminUserDTO;
import com.lexia.backend.dto.CreateUserDTO;
import com.lexia.backend.dto.UpdateUserDTO;
import com.lexia.backend.entity.Role;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserProfile;
import com.lexia.backend.entity.UserRole;
import com.lexia.backend.exception.ResourceNotFoundException;
import com.lexia.backend.exception.UserAlreadyExistsException;
import com.lexia.backend.repository.RoleRepository;
import com.lexia.backend.repository.UserProfileRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.repository.UserRoleRepository;
import com.lexia.backend.service.AdminUserService;
import com.lexia.backend.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllUsers(String search, String role, Pageable pageable) {
        Specification<User> spec = UserSpecification.withSearchAndRole(search, role);
        Page<User> usersPage = userRepository.findAll(spec, pageable);

        return usersPage.map(this::mapToAdminUserDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToAdminUserDTO(user);
    }

    @Override
    @Transactional
    public AdminUserDTO createUser(CreateUserDTO createUserDTO) {
        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + createUserDTO.getEmail());
        }

        // 1. Create User
        User user = User.builder()
                .email(createUserDTO.getEmail())
                .passwordHash(passwordEncoder.encode(createUserDTO.getPassword()))
                .authProvider(User.AuthProvider.EMAIL)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // 2. Create Profile
        UserProfile profile = UserProfile.builder()
                .user(user)
                .firstName(createUserDTO.getFirstName())
                .lastName(createUserDTO.getLastName())
                .fullName(createUserDTO.getFirstName() + " " + createUserDTO.getLastName())
                .build();

        userProfileRepository.save(profile);
        user.setProfile(profile);

        // 3. Assign Role
        Role role = roleRepository.findByName(createUserDTO.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + createUserDTO.getRole()));

        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);

        // Refresh to get relationships
        return mapToAdminUserDTO(user);
    }

    @Override
    @Transactional
    public AdminUserDTO updateUser(UUID id, UpdateUserDTO updateUserDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update User fields
        if (updateUserDTO.getIsActive() != null) {
            user.setIsActive(updateUserDTO.getIsActive());
        }
        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(updateUserDTO.getPassword()));
        }
        userRepository.save(user);

        // Update Profile fields
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
        }

        boolean nameChanged = false;
        if (updateUserDTO.getFirstName() != null) {
            profile.setFirstName(updateUserDTO.getFirstName());
            nameChanged = true;
        }
        if (updateUserDTO.getLastName() != null) {
            profile.setLastName(updateUserDTO.getLastName());
            nameChanged = true;
        }
        if (nameChanged) {
            profile.setFullName(profile.getFirstName() + " " + profile.getLastName());
        }
        userProfileRepository.save(profile);

        // Update Role if provided
        if (updateUserDTO.getRole() != null) {
            // Remove existing roles
            userRoleRepository.deleteAll(user.getUserRoles());
            user.getUserRoles().clear();

            // Add new role
            Role role = roleRepository.findByName(updateUserDTO.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + updateUserDTO.getRole()));

            UserRole userRole = new UserRole(user, role);
            userRoleRepository.save(userRole);
        }

        return mapToAdminUserDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private AdminUserDTO mapToAdminUserDTO(User user) {
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        String firstName = user.getProfile() != null ? user.getProfile().getFirstName() : null;
        String lastName = user.getProfile() != null ? user.getProfile().getLastName() : null;

        return AdminUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
