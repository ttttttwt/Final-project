package com.lexia.backend.file.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexia.backend.auth.CustomUserDetailsService;
import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.common.GlobalExceptionHandler;
import com.lexia.backend.entity.User;
import com.lexia.backend.file.entity.FileEntity;
import com.lexia.backend.file.enums.FileCategory;
import com.lexia.backend.file.exception.FileNotFoundException;
import com.lexia.backend.file.service.FileSecurityService;
import com.lexia.backend.file.service.FileStorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FileController.
 * Uses @WebMvcTest with security filters disabled.
 */
@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { FileController.class, GlobalExceptionHandler.class })
@DisplayName("FileController Tests")
class FileControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private FileStorageService fileStorageService;

        @MockitoBean
        private FileSecurityService fileSecurityService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        private static final String API_BASE = "/api/v1/files";
        private static final byte[] JPEG_MAGIC_BYTES = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF,
                        (byte) 0xE0 };

        private User testUser;
        private UUID userId;

        @BeforeEach
        void setUp() {
                userId = UUID.randomUUID();
                testUser = User.builder()
                                .id(userId)
                                .email("test@lexia.app")
                                .passwordHash("$2a$12$hashedpassword")
                                .isActive(true)
                                .build();

                // Set up Security Context with authenticated user for @AuthenticationPrincipal
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser,
                                null,
                                Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        @Nested
        @DisplayName("Upload File Tests")
        class UploadFileTests {

                @Test
                @DisplayName("Should upload file successfully")
                void shouldUploadFileSuccessfully() throws Exception {
                        // Arrange
                        MockMultipartFile file = new MockMultipartFile(
                                        "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, JPEG_MAGIC_BYTES);

                        FileEntity fileEntity = FileEntity.builder()
                                        .id(UUID.randomUUID())
                                        .originalFilename("avatar.jpg")
                                        .storagePath("avatars/2025-11/test.jpg")
                                        .mimeType("image/jpeg")
                                        .fileSize((long) JPEG_MAGIC_BYTES.length)
                                        .category(FileCategory.AVATAR)
                                        .isPublic(true)
                                        .build();

                        when(fileStorageService.store(any(), eq(FileCategory.AVATAR), eq(userId)))
                                        .thenReturn(fileEntity);

                        // Act & Assert
                        mockMvc.perform(multipart(API_BASE + "/upload")
                                        .file(file)
                                        .param("category", "AVATAR")
                                        .contentType(MediaType.MULTIPART_FORM_DATA))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").exists())
                                        .andExpect(jsonPath("$.originalFilename").value("avatar.jpg"))
                                        .andExpect(jsonPath("$.mimeType").value("image/jpeg"))
                                        .andExpect(jsonPath("$.category").value("AVATAR"));

                        verify(fileStorageService).store(any(), eq(FileCategory.AVATAR), eq(userId));
                }

                @Test
                @DisplayName("Should upload different file categories")
                void shouldUploadDifferentCategories() throws Exception {
                        // Arrange
                        MockMultipartFile file = new MockMultipartFile(
                                        "file", "thumbnail.png", "image/png", new byte[10]);

                        FileEntity fileEntity = FileEntity.builder()
                                        .id(UUID.randomUUID())
                                        .originalFilename("thumbnail.png")
                                        .storagePath("thumbnails/2025-11/test.png")
                                        .mimeType("image/png")
                                        .fileSize(10L)
                                        .category(FileCategory.COURSE_THUMBNAIL)
                                        .isPublic(true)
                                        .build();

                        when(fileStorageService.store(any(), eq(FileCategory.COURSE_THUMBNAIL), eq(userId)))
                                        .thenReturn(fileEntity);

                        // Act & Assert
                        mockMvc.perform(multipart(API_BASE + "/upload")
                                        .file(file)
                                        .param("category", "COURSE_THUMBNAIL"))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.category").value("COURSE_THUMBNAIL"));
                }
        }

        @Nested
        @DisplayName("Get Metadata Tests")
        class GetMetadataTests {

                @Test
                @DisplayName("Should return file metadata")
                void shouldReturnFileMetadata() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        FileEntity fileEntity = FileEntity.builder()
                                        .id(fileId)
                                        .originalFilename("test.jpg")
                                        .storagePath("avatars/2025-11/test.jpg")
                                        .mimeType("image/jpeg")
                                        .fileSize(1024L)
                                        .category(FileCategory.AVATAR)
                                        .isPublic(true)
                                        .width(800)
                                        .height(600)
                                        .build();

                        when(fileStorageService.getFileById(fileId)).thenReturn(fileEntity);

                        // Act & Assert
                        mockMvc.perform(get(API_BASE + "/{fileId}/metadata", fileId))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(fileId.toString()))
                                        .andExpect(jsonPath("$.originalFilename").value("test.jpg"))
                                        .andExpect(jsonPath("$.mimeType").value("image/jpeg"))
                                        .andExpect(jsonPath("$.width").value(800))
                                        .andExpect(jsonPath("$.height").value(600));
                }

                @Test
                @DisplayName("Should return 404 for non-existent file")
                void shouldReturn404ForNonExistentFile() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        when(fileStorageService.getFileById(fileId))
                                        .thenThrow(new FileNotFoundException("File not found"));

                        // Act & Assert
                        mockMvc.perform(get(API_BASE + "/{fileId}/metadata", fileId))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("Download File Tests")
        class DownloadFileTests {

                @Test
                @DisplayName("Should download public file")
                void shouldDownloadPublicFile() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        FileEntity fileEntity = FileEntity.builder()
                                        .id(fileId)
                                        .originalFilename("test.jpg")
                                        .storagePath("avatars/2025-11/test.jpg")
                                        .mimeType("image/jpeg")
                                        .fileSize((long) JPEG_MAGIC_BYTES.length)
                                        .category(FileCategory.AVATAR)
                                        .isPublic(true)
                                        .build();

                        ByteArrayResource resource = new ByteArrayResource(JPEG_MAGIC_BYTES);

                        when(fileStorageService.getFileById(fileId)).thenReturn(fileEntity);
                        when(fileSecurityService.canAccess(fileEntity, testUser)).thenReturn(true);
                        when(fileStorageService.loadAsResource(fileId)).thenReturn(resource);

                        // Act & Assert
                        MvcResult result = mockMvc.perform(get(API_BASE + "/{fileId}/download", fileId))
                                        .andExpect(status().isOk())
                                        .andExpect(header().string("Content-Type", "image/jpeg"))
                                        .andExpect(header().string("Content-Disposition",
                                                        "inline; filename=\"test.jpg\""))
                                        .andReturn();

                        Assertions.assertArrayEquals(JPEG_MAGIC_BYTES, result.getResponse().getContentAsByteArray());
                        verify(fileStorageService).recordAccess(fileId);
                }

                @Test
                @DisplayName("Should download PDF as attachment")
                void shouldDownloadPdfAsAttachment() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        byte[] pdfContent = new byte[] { 0x25, 0x50, 0x44, 0x46 }; // %PDF

                        FileEntity fileEntity = FileEntity.builder()
                                        .id(fileId)
                                        .originalFilename("document.pdf")
                                        .storagePath("documents/2025-11/document.pdf")
                                        .mimeType("application/pdf")
                                        .fileSize((long) pdfContent.length)
                                        .category(FileCategory.DOCUMENT)
                                        .isPublic(true)
                                        .build();

                        ByteArrayResource resource = new ByteArrayResource(pdfContent);

                        when(fileStorageService.getFileById(fileId)).thenReturn(fileEntity);
                        when(fileSecurityService.canAccess(fileEntity, testUser)).thenReturn(true);
                        when(fileStorageService.loadAsResource(fileId)).thenReturn(resource);

                        // Act & Assert
                        mockMvc.perform(get(API_BASE + "/{fileId}/download", fileId))
                                        .andExpect(status().isOk())
                                        .andExpect(header().string("Content-Disposition",
                                                        "attachment; filename=\"document.pdf\""));
                }

                @Test
                @DisplayName("Should return 403 when user cannot access private file")
                void shouldReturn403WhenUserCannotAccessPrivateFile() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        FileEntity fileEntity = FileEntity.builder()
                                        .id(fileId)
                                        .originalFilename("private.pdf")
                                        .storagePath("documents/2025-11/private.pdf")
                                        .mimeType("application/pdf")
                                        .category(FileCategory.DOCUMENT)
                                        .isPublic(false)
                                        .build();

                        when(fileStorageService.getFileById(fileId)).thenReturn(fileEntity);
                        when(fileSecurityService.canAccess(fileEntity, testUser)).thenReturn(false);

                        // Act & Assert
                        mockMvc.perform(get(API_BASE + "/{fileId}/download", fileId))
                                        .andExpect(status().isForbidden());

                        verify(fileStorageService, never()).loadAsResource(any());
                        verify(fileStorageService, never()).recordAccess(any());
                }

                @Test
                @DisplayName("Should return 404 for non-existent file")
                void shouldReturn404ForNonExistentFileDownload() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        when(fileStorageService.getFileById(fileId))
                                        .thenThrow(new FileNotFoundException("File not found"));

                        // Act & Assert
                        mockMvc.perform(get(API_BASE + "/{fileId}/download", fileId))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("Delete File Tests")
        class DeleteFileTests {

                @Test
                @DisplayName("Should delete file when user is owner")
                void shouldDeleteFileWhenUserIsOwner() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        FileEntity fileEntity = FileEntity.builder()
                                        .id(fileId)
                                        .originalFilename("test.jpg")
                                        .category(FileCategory.AVATAR)
                                        .isPublic(true)
                                        .build();

                        when(fileStorageService.getFileById(fileId)).thenReturn(fileEntity);
                        when(fileSecurityService.canDelete(fileEntity, testUser)).thenReturn(true);
                        doNothing().when(fileStorageService).delete(fileId);

                        // Act & Assert
                        mockMvc.perform(delete(API_BASE + "/{fileId}", fileId))
                                        .andExpect(status().isNoContent());

                        verify(fileStorageService).delete(fileId);
                }

                @Test
                @DisplayName("Should return 403 when user is not owner")
                void shouldReturn403WhenUserIsNotOwner() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        FileEntity fileEntity = FileEntity.builder()
                                        .id(fileId)
                                        .originalFilename("test.jpg")
                                        .category(FileCategory.AVATAR)
                                        .isPublic(false)
                                        .build();

                        when(fileStorageService.getFileById(fileId)).thenReturn(fileEntity);
                        when(fileSecurityService.canDelete(fileEntity, testUser)).thenReturn(false);

                        // Act & Assert
                        mockMvc.perform(delete(API_BASE + "/{fileId}", fileId))
                                        .andExpect(status().isForbidden());

                        verify(fileStorageService, never()).delete(any());
                }
        }

        @Nested
        @DisplayName("Check Exists Tests")
        class CheckExistsTests {

                @Test
                @DisplayName("Should return 200 when file exists")
                void shouldReturn200WhenFileExists() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        when(fileStorageService.exists(fileId)).thenReturn(true);

                        // Act & Assert
                        mockMvc.perform(get(API_BASE + "/{fileId}/exists", fileId))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Should return 404 when file does not exist")
                void shouldReturn404WhenFileDoesNotExist() throws Exception {
                        // Arrange
                        UUID fileId = UUID.randomUUID();
                        when(fileStorageService.exists(fileId)).thenReturn(false);

                        // Act & Assert
                        mockMvc.perform(get(API_BASE + "/{fileId}/exists", fileId))
                                        .andExpect(status().isNotFound());
                }
        }
}
