package com.lexia.backend.file.service;

import com.lexia.backend.entity.User;
import com.lexia.backend.file.entity.FileEntity;
import com.lexia.backend.file.enums.FileCategory;
import com.lexia.backend.file.exception.FileNotFoundException;
import com.lexia.backend.file.exception.FileStorageException;
import com.lexia.backend.file.exception.FileValidationException;
import com.lexia.backend.file.repository.FileRepository;
import com.lexia.backend.file.service.impl.LocalFileStorageService;
import com.lexia.backend.file.validator.FileValidator;
import com.lexia.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LocalFileStorageService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocalFileStorageService Tests")
class LocalFileStorageServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileValidator fileValidator;

    private LocalFileStorageService localFileStorageService;

    @TempDir
    Path tempDir;

    private static final byte[] JPEG_MAGIC_BYTES = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 };

    @BeforeEach
    void setUp() {
        localFileStorageService = new LocalFileStorageService(fileRepository, userRepository, fileValidator);
        ReflectionTestUtils.setField(localFileStorageService, "uploadDir", tempDir.toString());
        localFileStorageService.init();
    }

    @Nested
    @DisplayName("Store File Tests")
    class StoreFileTests {

        @Test
        @DisplayName("Should store file successfully")
        void shouldStoreFileSuccessfully() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setId(userId);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", JPEG_MAGIC_BYTES);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(fileValidator.sanitizeFilename("avatar.jpg")).thenReturn("avatar.jpg");
            when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> {
                FileEntity entity = invocation.getArgument(0);
                entity.setId(UUID.randomUUID());
                return entity;
            });

            // Act
            FileEntity result = localFileStorageService.store(file, FileCategory.AVATAR, userId);

            // Assert
            assertNotNull(result);
            assertEquals("avatar.jpg", result.getOriginalFilename());
            assertEquals("image/jpeg", result.getMimeType());
            assertEquals(FileCategory.AVATAR, result.getCategory());
            assertTrue(result.getIsPublic()); // Avatar is public by default
            assertNotNull(result.getStoragePath());
            assertTrue(result.getStoragePath().contains("avatars"));

            verify(fileValidator).validate(file, FileCategory.AVATAR);
            verify(fileRepository).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("Should store file without user")
        void shouldStoreFileWithoutUser() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", JPEG_MAGIC_BYTES);

            when(fileValidator.sanitizeFilename("avatar.jpg")).thenReturn("avatar.jpg");
            when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> {
                FileEntity entity = invocation.getArgument(0);
                entity.setId(UUID.randomUUID());
                return entity;
            });

            // Act
            FileEntity result = localFileStorageService.store(file, FileCategory.AVATAR, null);

            // Assert
            assertNotNull(result);
            assertNull(result.getUploadedBy());
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should call validator during store")
        void shouldCallValidatorDuringStore() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", JPEG_MAGIC_BYTES);

            when(fileValidator.sanitizeFilename("avatar.jpg")).thenReturn("avatar.jpg");
            when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> {
                FileEntity entity = invocation.getArgument(0);
                entity.setId(UUID.randomUUID());
                return entity;
            });

            // Act
            localFileStorageService.store(file, FileCategory.AVATAR, null);

            // Assert
            verify(fileValidator).validate(file, FileCategory.AVATAR);
        }

        @Test
        @DisplayName("Should throw exception when validation fails")
        void shouldThrowExceptionWhenValidationFails() {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file", "virus.exe", "application/octet-stream", new byte[10]);

            doThrow(new FileValidationException("Invalid file type"))
                    .when(fileValidator).validate(any(), any());

            // Act & Assert
            assertThrows(FileValidationException.class,
                    () -> localFileStorageService.store(file, FileCategory.AVATAR, null));
        }

        @Test
        @DisplayName("Should create file in correct directory structure")
        void shouldCreateFileInCorrectDirectoryStructure() throws IOException {
            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", JPEG_MAGIC_BYTES);

            when(fileValidator.sanitizeFilename("avatar.jpg")).thenReturn("avatar.jpg");
            ArgumentCaptor<FileEntity> entityCaptor = ArgumentCaptor.forClass(FileEntity.class);
            when(fileRepository.save(entityCaptor.capture())).thenAnswer(invocation -> {
                FileEntity entity = invocation.getArgument(0);
                entity.setId(UUID.randomUUID());
                return entity;
            });

            // Act
            localFileStorageService.store(file, FileCategory.AVATAR, null);

            // Assert
            FileEntity savedEntity = entityCaptor.getValue();
            Path filePath = tempDir.resolve(savedEntity.getStoragePath());
            assertTrue(Files.exists(filePath));
        }
    }

    @Nested
    @DisplayName("Load As Resource Tests")
    class LoadAsResourceTests {

        @Test
        @DisplayName("Should load file as resource")
        void shouldLoadFileAsResource() throws IOException {
            // Arrange
            UUID fileId = UUID.randomUUID();
            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .originalFilename("test.jpg")
                    .storagePath("avatars/2025-11/test.jpg")
                    .mimeType("image/jpeg")
                    .fileSize((long) JPEG_MAGIC_BYTES.length)
                    .category(FileCategory.AVATAR)
                    .build();

            // Create the file in temp directory
            Path filePath = tempDir.resolve("avatars/2025-11/test.jpg");
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, JPEG_MAGIC_BYTES);

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

            // Act
            Resource resource = localFileStorageService.loadAsResource(fileId);

            // Assert
            assertNotNull(resource);
            assertTrue(resource.exists());
            assertTrue(resource.isReadable());
        }

        @Test
        @DisplayName("Should throw exception when file not found in database")
        void shouldThrowExceptionWhenFileNotFoundInDatabase() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(FileNotFoundException.class,
                    () -> localFileStorageService.loadAsResource(fileId));
        }

        @Test
        @DisplayName("Should throw exception when file not found on disk")
        void shouldThrowExceptionWhenFileNotFoundOnDisk() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .originalFilename("missing.jpg")
                    .storagePath("avatars/2025-11/nonexistent.jpg")
                    .mimeType("image/jpeg")
                    .build();

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

            // Act & Assert
            assertThrows(FileNotFoundException.class,
                    () -> localFileStorageService.loadAsResource(fileId));
        }
    }

    @Nested
    @DisplayName("Delete File Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("Should delete file from disk and database")
        void shouldDeleteFileFromDiskAndDatabase() throws IOException {
            // Arrange
            UUID fileId = UUID.randomUUID();
            String storagePath = "avatars/2025-11/test.jpg";

            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .storagePath(storagePath)
                    .build();

            // Create test file
            Path filePath = tempDir.resolve(storagePath);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, JPEG_MAGIC_BYTES);

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

            // Act
            localFileStorageService.delete(fileId);

            // Assert
            assertFalse(Files.exists(filePath));
            verify(fileRepository).delete(fileEntity);
        }

        @Test
        @DisplayName("Should delete from database even if file not on disk")
        void shouldDeleteFromDatabaseEvenIfFileNotOnDisk() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .storagePath("avatars/2025-11/nonexistent.jpg")
                    .build();

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

            // Act
            localFileStorageService.delete(fileId);

            // Assert
            verify(fileRepository).delete(fileEntity);
        }
    }

    @Nested
    @DisplayName("Exists Tests")
    class ExistsTests {

        @Test
        @DisplayName("Should return true when file exists in database and on disk")
        void shouldReturnTrueWhenFileExists() throws IOException {
            // Arrange
            UUID fileId = UUID.randomUUID();
            String storagePath = "avatars/2025-11/test.jpg";

            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .storagePath(storagePath)
                    .build();

            // Create test file
            Path filePath = tempDir.resolve(storagePath);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, JPEG_MAGIC_BYTES);

            when(fileRepository.existsById(fileId)).thenReturn(true);
            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

            // Act
            boolean exists = localFileStorageService.exists(fileId);

            // Assert
            assertTrue(exists);
        }

        @Test
        @DisplayName("Should return false when file not in database")
        void shouldReturnFalseWhenFileNotInDatabase() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            when(fileRepository.existsById(fileId)).thenReturn(false);

            // Act
            boolean exists = localFileStorageService.exists(fileId);

            // Assert
            assertFalse(exists);
        }

        @Test
        @DisplayName("Should return false when file not on disk")
        void shouldReturnFalseWhenFileNotOnDisk() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .storagePath("avatars/2025-11/nonexistent.jpg")
                    .build();

            when(fileRepository.existsById(fileId)).thenReturn(true);
            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

            // Act
            boolean exists = localFileStorageService.exists(fileId);

            // Assert
            assertFalse(exists);
        }
    }

    @Nested
    @DisplayName("Is Owner Tests")
    class IsOwnerTests {

        @Test
        @DisplayName("Should return true when user is owner")
        void shouldReturnTrueWhenUserIsOwner() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            User user = new User();
            user.setId(userId);

            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .uploadedBy(user)
                    .build();

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

            // Act
            boolean isOwner = localFileStorageService.isOwner(fileId, userId);

            // Assert
            assertTrue(isOwner);
        }

        @Test
        @DisplayName("Should return false when user is not owner")
        void shouldReturnFalseWhenUserIsNotOwner() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID differentUserId = UUID.randomUUID();

            User user = new User();
            user.setId(userId);

            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .uploadedBy(user)
                    .build();

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

            // Act
            boolean isOwner = localFileStorageService.isOwner(fileId, differentUserId);

            // Assert
            assertFalse(isOwner);
        }

        @Test
        @DisplayName("Should return false when file has no owner")
        void shouldReturnFalseWhenFileHasNoOwner() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .uploadedBy(null)
                    .build();

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

            // Act
            boolean isOwner = localFileStorageService.isOwner(fileId, userId);

            // Assert
            assertFalse(isOwner);
        }
    }

    @Nested
    @DisplayName("Record Access Tests")
    class RecordAccessTests {

        @Test
        @DisplayName("Should increment access count")
        void shouldIncrementAccessCount() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .accessCount(5)
                    .build();

            when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));
            when(fileRepository.save(any())).thenReturn(fileEntity);

            // Act
            localFileStorageService.recordAccess(fileId);

            // Assert
            assertEquals(6, fileEntity.getAccessCount());
            assertNotNull(fileEntity.getLastAccessedAt());
            verify(fileRepository).save(fileEntity);
        }
    }

    @Nested
    @DisplayName("Get Public URL Tests")
    class GetPublicUrlTests {

        @Test
        @DisplayName("Should return correct download URL")
        void shouldReturnCorrectDownloadUrl() {
            // Arrange
            UUID fileId = UUID.randomUUID();

            // Act
            String url = localFileStorageService.getPublicUrl(fileId);

            // Assert
            assertEquals("/api/v1/files/" + fileId + "/download", url);
        }
    }
}
