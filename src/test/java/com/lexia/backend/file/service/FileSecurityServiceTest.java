package com.lexia.backend.file.service;

import com.lexia.backend.entity.Role;
import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserRole;
import com.lexia.backend.file.entity.FileEntity;
import com.lexia.backend.file.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FileSecurityService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileSecurityService Tests")
class FileSecurityServiceTest {

    @Mock
    private FileRepository fileRepository;

    private FileSecurityService fileSecurityService;

    @BeforeEach
    void setUp() {
        fileSecurityService = new FileSecurityService(fileRepository);
    }

    private User createUserWithRole(UUID userId, String roleName) {
        User user = new User();
        user.setId(userId);

        if (roleName != null) {
            Role role = new Role();
            role.setName(roleName);

            UserRole userRole = new UserRole();
            userRole.setRole(role);

            Set<UserRole> userRoles = new HashSet<>();
            userRoles.add(userRole);
            user.setUserRoles(userRoles);
        } else {
            user.setUserRoles(new HashSet<>());
        }

        return user;
    }

    @Nested
    @DisplayName("isOwner Tests")
    class IsOwnerTests {

        @Test
        @DisplayName("Should return true when user is the owner")
        void shouldReturnTrueWhenUserIsOwner() {
            UUID fileId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            User owner = new User();
            owner.setId(userId);

            FileEntity file = FileEntity.builder()
                    .id(fileId)
                    .uploadedBy(owner)
                    .build();

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

            assertTrue(fileSecurityService.isOwner(fileId, userId));
        }

        @Test
        @DisplayName("Should return false when user is not the owner")
        void shouldReturnFalseWhenUserIsNotOwner() {
            UUID fileId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID differentUserId = UUID.randomUUID();

            User owner = new User();
            owner.setId(userId);

            FileEntity file = FileEntity.builder()
                    .id(fileId)
                    .uploadedBy(owner)
                    .build();

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

            assertFalse(fileSecurityService.isOwner(fileId, differentUserId));
        }

        @Test
        @DisplayName("Should return false when file has no owner")
        void shouldReturnFalseWhenFileHasNoOwner() {
            UUID fileId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            FileEntity file = FileEntity.builder()
                    .id(fileId)
                    .uploadedBy(null)
                    .build();

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

            assertFalse(fileSecurityService.isOwner(fileId, userId));
        }

        @Test
        @DisplayName("Should return false when file not found")
        void shouldReturnFalseWhenFileNotFound() {
            UUID fileId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

            assertFalse(fileSecurityService.isOwner(fileId, userId));
        }

        @Test
        @DisplayName("Should return false when fileId is null")
        void shouldReturnFalseWhenFileIdIsNull() {
            assertFalse(fileSecurityService.isOwner(null, UUID.randomUUID()));
        }

        @Test
        @DisplayName("Should return false when userId is null")
        void shouldReturnFalseWhenUserIdIsNull() {
            assertFalse(fileSecurityService.isOwner(UUID.randomUUID(), null));
        }
    }

    @Nested
    @DisplayName("canAccess Tests")
    class CanAccessTests {

        @Test
        @DisplayName("Should grant access to public files without authentication")
        void shouldGrantAccessToPublicFilesWithoutAuth() {
            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .isPublic(true)
                    .build();

            assertTrue(fileSecurityService.canAccess(file, null));
        }

        @Test
        @DisplayName("Should grant access to public files with authentication")
        void shouldGrantAccessToPublicFilesWithAuth() {
            UUID userId = UUID.randomUUID();
            User user = createUserWithRole(userId, "LEARNER");

            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .isPublic(true)
                    .build();

            assertTrue(fileSecurityService.canAccess(file, user));
        }

        @Test
        @DisplayName("Should deny access to private files without authentication")
        void shouldDenyAccessToPrivateFilesWithoutAuth() {
            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .isPublic(false)
                    .build();

            assertFalse(fileSecurityService.canAccess(file, null));
        }

        @Test
        @DisplayName("Should grant access to owner for private files")
        void shouldGrantAccessToOwnerForPrivateFiles() {
            UUID userId = UUID.randomUUID();
            User owner = createUserWithRole(userId, "LEARNER");

            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .isPublic(false)
                    .uploadedBy(owner)
                    .build();

            assertTrue(fileSecurityService.canAccess(file, owner));
        }

        @Test
        @DisplayName("Should grant access to admin for any file")
        void shouldGrantAccessToAdminForAnyFile() {
            UUID adminId = UUID.randomUUID();
            User admin = createUserWithRole(adminId, "ADMIN");

            UUID ownerId = UUID.randomUUID();
            User owner = new User();
            owner.setId(ownerId);

            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .isPublic(false)
                    .uploadedBy(owner)
                    .build();

            assertTrue(fileSecurityService.canAccess(file, admin));
        }

        @Test
        @DisplayName("Should deny access to non-owner for private files")
        void shouldDenyAccessToNonOwnerForPrivateFiles() {
            UUID userId = UUID.randomUUID();
            User user = createUserWithRole(userId, "LEARNER");

            UUID ownerId = UUID.randomUUID();
            User owner = new User();
            owner.setId(ownerId);

            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .isPublic(false)
                    .uploadedBy(owner)
                    .build();

            assertFalse(fileSecurityService.canAccess(file, user));
        }

        @Test
        @DisplayName("Should return false when file is null")
        void shouldReturnFalseWhenFileIsNull() {
            User user = createUserWithRole(UUID.randomUUID(), "ADMIN");
            assertFalse(fileSecurityService.canAccess(null, user));
        }
    }

    @Nested
    @DisplayName("canDelete Tests")
    class CanDeleteTests {

        @Test
        @DisplayName("Should allow owner to delete file")
        void shouldAllowOwnerToDelete() {
            UUID userId = UUID.randomUUID();
            User owner = createUserWithRole(userId, "LEARNER");

            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .uploadedBy(owner)
                    .build();

            assertTrue(fileSecurityService.canDelete(file, owner));
        }

        @Test
        @DisplayName("Should allow admin to delete any file")
        void shouldAllowAdminToDeleteAnyFile() {
            UUID adminId = UUID.randomUUID();
            User admin = createUserWithRole(adminId, "ADMIN");

            UUID ownerId = UUID.randomUUID();
            User owner = new User();
            owner.setId(ownerId);

            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .uploadedBy(owner)
                    .build();

            assertTrue(fileSecurityService.canDelete(file, admin));
        }

        @Test
        @DisplayName("Should deny non-owner from deleting file")
        void shouldDenyNonOwnerFromDeleting() {
            UUID userId = UUID.randomUUID();
            User user = createUserWithRole(userId, "LEARNER");

            UUID ownerId = UUID.randomUUID();
            User owner = new User();
            owner.setId(ownerId);

            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .uploadedBy(owner)
                    .build();

            assertFalse(fileSecurityService.canDelete(file, user));
        }

        @Test
        @DisplayName("Should return false when file is null")
        void shouldReturnFalseWhenFileIsNull() {
            User user = createUserWithRole(UUID.randomUUID(), "ADMIN");
            assertFalse(fileSecurityService.canDelete(null, user));
        }

        @Test
        @DisplayName("Should return false when user is null")
        void shouldReturnFalseWhenUserIsNull() {
            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .build();
            assertFalse(fileSecurityService.canDelete(file, null));
        }
    }
}
