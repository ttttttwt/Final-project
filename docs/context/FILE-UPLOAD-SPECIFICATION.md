# LEXIA - File Upload System Specification

**Version**: 1.1.0  
**Created**: November 28, 2025  
**Updated**: December 1, 2025  
**Status**: ✅ Implemented  
**Implemented Sprint**: Sprint 5

---

## 1. Overview

### 1.1. Purpose

The File Upload System enables secure file management for the LEXIA platform, supporting:

- User avatar uploads
- Course thumbnail images
- Lesson audio files (for LISTENING lessons)
- Lesson images and attachments
- Future: Certificate PDFs, downloadable resources

### 1.2. Current State

| Component                | Status             | Notes                                              |
| ------------------------ | ------------------ | -------------------------------------------------- |
| `application.properties` | ✅ Configured      | `max-file-size=50MB`, `max-request-size=60MB`      |
| `uploads/` directory     | ✅ Exists          | Category-based structure with date partitioning    |
| Avatar storage           | ✅ Implemented     | Supports both file upload and external URLs        |
| File upload endpoint     | ✅ Implemented     | Full CRUD with multipart handling                  |
| FileStorageService       | ✅ Implemented     | LocalFileStorageService with interface abstraction |
| FileValidator            | ✅ Implemented     | MIME type, size, magic byte validation             |
| Database schema          | ✅ Implemented     | V15 files table, V16 avatar_file_id FK             |
| Cloud storage            | ❌ Not implemented | No S3/Azure Blob integration (future)              |

### 1.3. Goals

| Goal                  | Description                                            |
| --------------------- | ------------------------------------------------------ |
| **Local Development** | Support local file storage for development             |
| **Cloud Ready**       | Abstract storage for easy cloud migration              |
| **Security**          | Validate file types, sizes, and content                |
| **Performance**       | Efficient upload/download with streaming               |
| **Integration**       | Seamless integration with User, Course, Lesson modules |

### 1.4. Non-Goals (Out of Scope)

- Video file uploads (too large, need dedicated video service)
- Real-time file collaboration
- File versioning
- CDN integration (defer to production setup)

---

## 2. Supported File Types

### 2.1. File Categories

| Category     | Allowed Types             | Max Size | Use Case                |
| ------------ | ------------------------- | -------- | ----------------------- |
| **Image**    | JPG, JPEG, PNG, GIF, WebP | 5 MB     | Avatars, thumbnails     |
| **Audio**    | MP3, WAV, OGG, M4A        | 50 MB    | Listening lessons       |
| **Document** | PDF                       | 10 MB    | Certificates, resources |
| **General**  | Any above                 | -        | Mixed content           |

### 2.2. MIME Type Validation

```java
public enum AllowedFileType {
    // Images
    JPEG("image/jpeg", ".jpg", ".jpeg"),
    PNG("image/png", ".png"),
    GIF("image/gif", ".gif"),
    WEBP("image/webp", ".webp"),

    // Audio
    MP3("audio/mpeg", ".mp3"),
    WAV("audio/wav", ".wav"),
    OGG("audio/ogg", ".ogg"),
    M4A("audio/mp4", ".m4a"),

    // Documents
    PDF("application/pdf", ".pdf");

    private final String mimeType;
    private final String[] extensions;
}
```

---

## 3. Storage Architecture

### 3.1. Storage Abstraction

```
┌─────────────────────────┐
│    FileUploadController │
└───────────┬─────────────┘
            │
┌───────────▼─────────────┐
│   FileStorageService    │  ← Interface
└───────────┬─────────────┘
            │
    ┌───────┴───────┐
    │               │
┌───▼───┐     ┌────▼────┐
│ Local │     │  Cloud  │
│Storage│     │ Storage │
└───────┘     └─────────┘
   ↓               ↓
./uploads/     S3/Azure
```

### 3.2. Storage Interface

```java
public interface FileStorageService {

    /**
     * Store a file and return the file metadata
     */
    FileMetadata store(MultipartFile file, FileCategory category, UUID userId);

    /**
     * Load file as resource for download
     */
    Resource loadAsResource(String fileId);

    /**
     * Get file metadata by ID
     */
    FileMetadata getMetadata(String fileId);

    /**
     * Delete a file
     */
    void delete(String fileId);

    /**
     * Check if file exists
     */
    boolean exists(String fileId);

    /**
     * Get public URL for file (for images, audio)
     */
    String getPublicUrl(String fileId);
}
```

### 3.3. Local Storage Implementation

```java
@Service
@Profile("local")
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public FileMetadata store(MultipartFile file, FileCategory category, UUID userId) {
        // 1. Validate file
        validateFile(file, category);

        // 2. Generate unique filename
        String fileId = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String filename = fileId + extension;

        // 3. Determine storage path: uploads/{category}/{yyyy-MM}/{filename}
        String storagePath = buildStoragePath(category, filename);

        // 4. Save file
        Path targetPath = Paths.get(uploadDir, storagePath);
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath);

        // 5. Return metadata
        return FileMetadata.builder()
            .id(fileId)
            .originalFilename(file.getOriginalFilename())
            .storagePath(storagePath)
            .mimeType(file.getContentType())
            .size(file.getSize())
            .category(category)
            .uploadedBy(userId)
            .uploadedAt(Instant.now())
            .build();
    }
}
```

### 3.4. Cloud Storage Implementation (Future)

```java
@Service
@Profile("cloud")
public class S3FileStorageService implements FileStorageService {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public FileMetadata store(MultipartFile file, FileCategory category, UUID userId) {
        // Upload to S3 with same interface
        // Return CloudFront URL as publicUrl
    }
}
```

### 3.5. Directory Structure

```
uploads/
├── avatars/
│   └── 2025-11/
│       ├── 550e8400-e29b-41d4-a716-446655440000.jpg
│       └── 6ba7b810-9dad-11d1-80b4-00c04fd430c8.png
├── courses/
│   └── 2025-11/
│       └── thumbnails/
│           └── 7c9e6679-7425-40de-944b-e07fc1f90ae7.webp
├── lessons/
│   └── 2025-11/
│       ├── audio/
│       │   └── 8f14e45f-ceea-467f-a634-1a8f8e3f5ab6.mp3
│       └── images/
│           └── 9a0c9c1f-2f0a-4e4a-8f3b-7a8b9c0d1e2f.png
└── documents/
    └── 2025-11/
        └── certificates/
            └── a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf
```

---

## 4. Database Schema

### 4.1. Files Table

```sql
-- V14__Create_files_table.sql
CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL UNIQUE,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    uploaded_by UUID REFERENCES users(id) ON DELETE SET NULL,
    uploaded_at TIMESTAMPTZ DEFAULT NOW(),

    -- Optional metadata
    width INTEGER,           -- For images
    height INTEGER,          -- For images
    duration_seconds INTEGER, -- For audio

    -- Access control
    is_public BOOLEAN DEFAULT false,
    access_count INTEGER DEFAULT 0,
    last_accessed_at TIMESTAMPTZ,

    CONSTRAINT valid_category CHECK (category IN (
        'AVATAR', 'COURSE_THUMBNAIL', 'LESSON_AUDIO',
        'LESSON_IMAGE', 'DOCUMENT', 'CERTIFICATE'
    )),
    CONSTRAINT valid_file_size CHECK (file_size > 0 AND file_size <= 52428800) -- 50MB max
);

-- Indexes
CREATE INDEX idx_files_uploaded_by ON files(uploaded_by);
CREATE INDEX idx_files_category ON files(category);
CREATE INDEX idx_files_uploaded_at ON files(uploaded_at DESC);

COMMENT ON TABLE files IS 'Metadata for uploaded files';
COMMENT ON COLUMN files.storage_path IS 'Relative path from upload root directory';
COMMENT ON COLUMN files.is_public IS 'If true, file can be accessed without authentication';
```

### 4.2. Update User Profiles Table

```sql
-- V15__Add_avatar_file_id_to_user_profiles.sql
ALTER TABLE user_profiles
ADD COLUMN avatar_file_id UUID REFERENCES files(id) ON DELETE SET NULL;

-- Keep avatar_url for backward compatibility (external URLs)
-- New uploads will use avatar_file_id

CREATE INDEX idx_user_profiles_avatar_file ON user_profiles(avatar_file_id);
```

---

## 5. API Endpoints

### 5.1. File Upload APIs

| Method | Endpoint                        | Auth        | Role        | Max Size | Description           |
| ------ | ------------------------------- | ----------- | ----------- | -------- | --------------------- |
| POST   | `/api/v1/files/upload`          | Yes         | Any         | 10 MB    | Upload single file    |
| POST   | `/api/v1/files/upload/multiple` | Yes         | CM          | 50 MB    | Upload multiple files |
| GET    | `/api/v1/files/{id}`            | Yes         | Any         | -        | Get file metadata     |
| GET    | `/api/v1/files/{id}/download`   | Conditional | -           | -        | Download file         |
| DELETE | `/api/v1/files/{id}`            | Yes         | Owner/Admin | -        | Delete file           |

### 5.2. Avatar Upload APIs

| Method | Endpoint               | Auth | Description        |
| ------ | ---------------------- | ---- | ------------------ |
| POST   | `/api/v1/users/avatar` | Yes  | Upload user avatar |
| DELETE | `/api/v1/users/avatar` | Yes  | Delete user avatar |

### 5.3. Course Media APIs

| Method | Endpoint                         | Auth | Role | Description             |
| ------ | -------------------------------- | ---- | ---- | ----------------------- |
| POST   | `/api/v1/courses/{id}/thumbnail` | Yes  | CM   | Upload course thumbnail |
| DELETE | `/api/v1/courses/{id}/thumbnail` | Yes  | CM   | Delete course thumbnail |

### 5.4. Lesson Media APIs

| Method | Endpoint                      | Auth | Role | Description          |
| ------ | ----------------------------- | ---- | ---- | -------------------- |
| POST   | `/api/v1/lessons/{id}/audio`  | Yes  | CM   | Upload lesson audio  |
| POST   | `/api/v1/lessons/{id}/images` | Yes  | CM   | Upload lesson images |
| DELETE | `/api/v1/lessons/{id}/audio`  | Yes  | CM   | Delete lesson audio  |

### 5.5. Request/Response Examples

**POST /api/v1/files/upload**

Request (multipart/form-data):

```
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="file"; filename="avatar.jpg"
Content-Type: image/jpeg

[binary data]
------WebKitFormBoundary
Content-Disposition: form-data; name="category"

AVATAR
------WebKitFormBoundary--
```

Response (201 Created):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "avatar.jpg",
  "mimeType": "image/jpeg",
  "size": 245789,
  "category": "AVATAR",
  "url": "/api/v1/files/550e8400-e29b-41d4-a716-446655440000/download",
  "uploadedAt": "2025-11-28T10:30:00Z"
}
```

**POST /api/v1/users/avatar**

Request (multipart/form-data):

```
file: [avatar image file]
```

Response (200 OK):

```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "avatarUrl": "/api/v1/files/550e8400-e29b-41d4-a716-446655440000/download"
}
```

**GET /api/v1/files/{id}/download**

Response:

```
Content-Type: image/jpeg
Content-Disposition: inline; filename="avatar.jpg"
Content-Length: 245789

[binary data]
```

---

## 6. Backend Architecture

### 6.1. Component Structure

```
com.lexia.backend/
├── file/
│   ├── controller/
│   │   └── FileController.java
│   ├── service/
│   │   ├── FileStorageService.java
│   │   └── impl/
│   │       ├── LocalFileStorageService.java
│   │       └── S3FileStorageService.java (future)
│   ├── repository/
│   │   └── FileRepository.java
│   ├── entity/
│   │   └── FileEntity.java
│   ├── dto/
│   │   ├── FileUploadResponse.java
│   │   ├── FileMetadataDTO.java
│   │   └── FileValidationException.java
│   ├── mapper/
│   │   └── FileMapper.java
│   ├── config/
│   │   └── FileStorageConfig.java
│   ├── validator/
│   │   └── FileValidator.java
│   └── util/
│       └── FileUtils.java
```

### 6.2. File Validator

```java
@Component
public class FileValidator {

    private static final Map<FileCategory, Set<String>> ALLOWED_MIME_TYPES = Map.of(
        FileCategory.AVATAR, Set.of("image/jpeg", "image/png", "image/gif", "image/webp"),
        FileCategory.COURSE_THUMBNAIL, Set.of("image/jpeg", "image/png", "image/webp"),
        FileCategory.LESSON_AUDIO, Set.of("audio/mpeg", "audio/wav", "audio/ogg", "audio/mp4"),
        FileCategory.LESSON_IMAGE, Set.of("image/jpeg", "image/png", "image/gif", "image/webp"),
        FileCategory.DOCUMENT, Set.of("application/pdf")
    );

    private static final Map<FileCategory, Long> MAX_FILE_SIZES = Map.of(
        FileCategory.AVATAR, 5L * 1024 * 1024,          // 5 MB
        FileCategory.COURSE_THUMBNAIL, 5L * 1024 * 1024, // 5 MB
        FileCategory.LESSON_AUDIO, 50L * 1024 * 1024,    // 50 MB
        FileCategory.LESSON_IMAGE, 10L * 1024 * 1024,    // 10 MB
        FileCategory.DOCUMENT, 10L * 1024 * 1024         // 10 MB
    );

    public void validate(MultipartFile file, FileCategory category) {
        // 1. Check if file is empty
        if (file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }

        // 2. Check MIME type
        String mimeType = file.getContentType();
        if (!ALLOWED_MIME_TYPES.get(category).contains(mimeType)) {
            throw new FileValidationException(
                "File type not allowed. Allowed types: " + ALLOWED_MIME_TYPES.get(category)
            );
        }

        // 3. Check file size
        long maxSize = MAX_FILE_SIZES.get(category);
        if (file.getSize() > maxSize) {
            throw new FileValidationException(
                "File size exceeds maximum allowed: " + formatSize(maxSize)
            );
        }

        // 4. Validate file content (magic bytes)
        validateFileContent(file, mimeType);

        // 5. Sanitize filename
        sanitizeFilename(file.getOriginalFilename());
    }

    private void validateFileContent(MultipartFile file, String expectedMimeType) {
        // Check magic bytes to prevent MIME type spoofing
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            is.read(header);
        }

        // Verify magic bytes match expected type
        // JPEG: FF D8 FF
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        // GIF: 47 49 46 38
        // PDF: 25 50 44 46
        // MP3: 49 44 33 or FF FB
    }
}
```

### 6.3. Controller

```java
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Upload", description = "File upload and management APIs")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a single file")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") FileCategory category,
            @AuthenticationPrincipal UserDetails user) {

        FileMetadata metadata = fileStorageService.store(file, category, getUserId(user));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FileMapper.toResponse(metadata));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file metadata")
    public ResponseEntity<FileMetadataDTO> getMetadata(@PathVariable UUID id) {
        FileMetadata metadata = fileStorageService.getMetadata(id.toString());
        return ResponseEntity.ok(FileMapper.toDTO(metadata));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download file")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        FileMetadata metadata = fileStorageService.getMetadata(id.toString());
        Resource resource = fileStorageService.loadAsResource(id.toString());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user) {

        // Verify ownership or admin role
        fileStorageService.delete(id.toString());
        return ResponseEntity.noContent().build();
    }
}
```

---

## 7. Frontend Integration

### 7.1. Web (Next.js)

**File Service**:

```typescript
// services/fileService.ts
export const fileService = {
  async uploadFile(
    file: File,
    category: FileCategory
  ): Promise<FileUploadResponse> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("category", category);

    const response = await api.post("/files/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      onUploadProgress: (progressEvent) => {
        const percent = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total!
        );
        // Update progress state
      },
    });

    return response.data;
  },

  async uploadAvatar(file: File): Promise<User> {
    const formData = new FormData();
    formData.append("file", file);

    const response = await api.post("/users/avatar", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    return response.data;
  },

  async deleteAvatar(): Promise<void> {
    await api.delete("/users/avatar");
  },

  getFileUrl(fileId: string): string {
    return `${API_BASE_URL}/files/${fileId}/download`;
  },
};
```

**Avatar Upload Component** (already exists, needs backend integration):

```tsx
// Current: Only accepts URL
// Update: Support file upload via fileService.uploadAvatar()
```

### 7.2. Mobile (React Native)

```typescript
// services/fileService.ts
import * as ImagePicker from "expo-image-picker";

export const fileService = {
  async pickAndUploadAvatar(): Promise<User> {
    // 1. Pick image
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
    });

    if (result.canceled) {
      throw new Error("Image selection cancelled");
    }

    // 2. Upload
    const asset = result.assets[0];
    const formData = new FormData();
    formData.append("file", {
      uri: asset.uri,
      type: asset.mimeType || "image/jpeg",
      name: asset.fileName || "avatar.jpg",
    } as any);

    const response = await api.post("/users/avatar", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    return response.data;
  },
};
```

### 7.3. Admin Panel

**Course Thumbnail Upload**:

```tsx
function ThumbnailUpload({ courseId }: { courseId: string }) {
  const [isUploading, setIsUploading] = useState(false);

  const handleUpload = async (file: File) => {
    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);

      await api.post(`/courses/${courseId}/thumbnail`, formData);
      toast.success("Thumbnail uploaded successfully");
    } catch (error) {
      toast.error("Failed to upload thumbnail");
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <FileDropzone
      accept={{ "image/*": [".jpg", ".png", ".webp"] }}
      maxSize={5 * 1024 * 1024}
      onDrop={handleUpload}
      isLoading={isUploading}
    />
  );
}
```

---

## 8. Security Considerations

### 8.1. File Validation

| Threat                | Mitigation                                     |
| --------------------- | ---------------------------------------------- |
| Malicious file upload | Magic byte validation, antivirus scan (future) |
| File size DoS         | Size limits per category                       |
| Path traversal        | Sanitize filenames, use UUID-based paths       |
| MIME type spoofing    | Validate content, not just extension           |
| Unauthorized access   | Check ownership, role-based access             |

### 8.2. Storage Security

| Concern          | Local Storage                | Cloud Storage             |
| ---------------- | ---------------------------- | ------------------------- |
| File permissions | 0600 (owner read/write only) | Bucket policies           |
| Encryption       | Filesystem encryption        | S3 server-side encryption |
| Backup           | Manual                       | Automated S3 versioning   |
| Access logs      | Application logs             | S3 access logs            |

### 8.3. Access Control

```java
@PreAuthorize("hasRole('ADMIN') or @fileSecurityService.isOwner(#fileId, authentication)")
public void deleteFile(UUID fileId) {
    // Only file owner or admin can delete
}

@Service
public class FileSecurityService {
    public boolean isOwner(UUID fileId, Authentication auth) {
        FileEntity file = fileRepository.findById(fileId).orElseThrow();
        return file.getUploadedBy().equals(getUserId(auth));
    }
}
```

---

## 9. Performance Considerations

### 9.1. Upload Optimization

| Technique         | Benefit                         | Implementation     |
| ----------------- | ------------------------------- | ------------------ |
| Chunked upload    | Large files, resume support     | Future enhancement |
| Image compression | Reduce storage, faster delivery | Server-side resize |
| Async processing  | Non-blocking upload             | Spring @Async      |
| Progress tracking | Better UX                       | Response streaming |

### 9.2. Download Optimization

| Technique            | Benefit                         | Implementation          |
| -------------------- | ------------------------------- | ----------------------- |
| Byte-range requests  | Resume downloads, video seeking | HTTP Range header       |
| Caching              | Reduce server load              | Cache-Control headers   |
| CDN                  | Global delivery                 | CloudFront (production) |
| Thumbnail generation | Fast previews                   | On-upload processing    |

### 9.3. Configuration

```properties
# application.properties

# File upload limits
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=60MB

# File storage
file.upload-dir=./uploads
file.allowed-extensions=jpg,jpeg,png,gif,webp,mp3,wav,ogg,m4a,pdf
file.max-avatar-size=5MB
file.max-audio-size=50MB

# Image processing (future)
file.thumbnail.width=200
file.thumbnail.height=200
file.thumbnail.quality=0.8
```

---

## 10. Testing Strategy

### 10.1. Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class FileValidatorTest {

    @Test
    void shouldRejectOversizedFile() {
        MultipartFile file = new MockMultipartFile(
            "file", "large.jpg", "image/jpeg", new byte[6 * 1024 * 1024]
        );

        assertThrows(FileValidationException.class, () ->
            fileValidator.validate(file, FileCategory.AVATAR)
        );
    }

    @Test
    void shouldRejectInvalidMimeType() {
        MultipartFile file = new MockMultipartFile(
            "file", "virus.exe", "application/octet-stream", new byte[100]
        );

        assertThrows(FileValidationException.class, () ->
            fileValidator.validate(file, FileCategory.AVATAR)
        );
    }
}
```

### 10.2. Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class FileControllerIntegrationTest {

    @Test
    @WithMockUser
    void shouldUploadAvatar() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "avatar.jpg", "image/jpeg", getTestImageBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/avatar")
                .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.avatarUrl").exists());
    }
}
```

### 10.3. Coverage Requirements

| Component          | Target Coverage |
| ------------------ | --------------- |
| FileStorageService | ≥80%            |
| FileValidator      | ≥90%            |
| FileController     | ≥70%            |
| Overall            | ≥70%            |

---

## 11. Implementation Tasks

### 11.1. Phase 1: Core Infrastructure (2 pts) ✅

- [x] Create database migration (V15 - files table)
- [x] Create database migration (V16 - avatar_file_id FK)
- [x] Create FileCategory enum
- [x] Create FileEntity and FileRepository
- [x] Create FileStorageService interface
- [x] Implement LocalFileStorageService
- [x] Create FileValidator with MIME type and magic byte validation
- [x] Create custom exceptions (FileValidationException, FileNotFoundException, FileStorageException)
- [x] Create DTOs (FileUploadResponse, FileMetadataDTO)
- [x] Create FileMapper

### 11.2. Phase 2: File Upload API (2 pts) ✅

- [x] Create FileController with upload endpoint
- [x] Create download endpoint with streaming
- [x] Create metadata and delete endpoints
- [x] Add request validation
- [x] Update GlobalExceptionHandler for file exceptions
- [x] Write unit tests (FileValidatorTest)
- [x] Write integration tests (FileControllerTest)

### 11.3. Phase 3: Avatar Integration (1 pt) ✅

- [x] Update UserProfile entity with avatar_file_id
- [x] Update UserProfileService with avatar file handling
- [x] Update UserProfileController with avatar upload endpoint
- [x] Update UserProfileMapper
- [x] Write service tests (LocalFileStorageServiceTest)

### 11.4. Phase 4: Course/Lesson Media (2 pts) ⏳ Pending

- [ ] Create course thumbnail upload endpoint
- [ ] Create lesson audio upload endpoint
- [ ] Update Course and Lesson entities
- [ ] Update admin panel components

### 11.5. Phase 5: Cloud Storage (Future - 2 pts) ⏳ Pending

- [ ] Add AWS SDK dependency
- [ ] Implement S3FileStorageService
- [ ] Configure profile-based service selection
- [ ] Add CloudFront integration

**Total Estimated Points**: 7 pts (local) + 2 pts (cloud) = 9 pts  
**Completed Points**: 5 pts (Phase 1-3)

---

## 12. Dependencies

### 12.1. Backend Dependencies

```gradle
// build.gradle
dependencies {
    // File upload (already included in spring-boot-starter-web)
    // No additional dependencies for local storage

    // For image processing (optional)
    implementation 'org.imgscalr:imgscalr-lib:4.2'

    // For cloud storage (future)
    // implementation 'software.amazon.awssdk:s3:2.20.0'
}
```

### 12.2. Frontend Dependencies

```json
// Web - already has File API support
// No additional dependencies needed

// Mobile
{
  "dependencies": {
    "expo-image-picker": "~14.3.2",
    "expo-file-system": "~15.4.2"
  }
}
```

---

## 13. Configuration

### 13.1. Development (application-dev.properties)

```properties
# Local file storage
file.upload-dir=./uploads
file.base-url=http://localhost:8088/api/v1/files

# File limits
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=60MB
```

### 13.2. Production (application-prod.properties)

```properties
# Cloud storage (S3)
file.storage-type=s3
aws.s3.bucket=lexia-uploads
aws.s3.region=ap-southeast-1
aws.cloudfront.domain=cdn.lexia.app

# File limits (same as dev)
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=60MB
```

---

## 14. Future Enhancements

| Enhancement       | Priority | Sprint     |
| ----------------- | -------- | ---------- |
| S3 cloud storage  | High     | Sprint 6   |
| CDN integration   | High     | Sprint 6   |
| Image thumbnails  | Medium   | Sprint 6-7 |
| Audio transcoding | Low      | Sprint 7-8 |
| Virus scanning    | Low      | Sprint 8   |
| Chunked upload    | Low      | Sprint 8   |

---

**Document Owner**: LEXIA Development Team  
**Review Date**: Before Sprint 5 Planning  
**Next Update**: After Phase 4-5 implementation

---

## 15. Implementation Notes (Sprint 5)

### 15.1. Completed Components

| Component               | File Path                                                          | Notes                                 |
| ----------------------- | ------------------------------------------------------------------ | ------------------------------------- |
| Files table migration   | `src/main/resources/db/migration/V15__Create_files_table.sql`      | UUID PK, full metadata support        |
| Avatar FK migration     | `src/main/resources/db/migration/V16__Add_avatar_file_id_*.sql`    | Added avatar_file_id to user_profiles |
| FileCategory enum       | `src/main/java/.../file/enums/FileCategory.java`                   | 6 categories with size limits & paths |
| FileEntity              | `src/main/java/.../file/entity/FileEntity.java`                    | JPA entity with metadata, public flag |
| FileRepository          | `src/main/java/.../file/repository/FileRepository.java`            | Custom queries for user files         |
| FileUploadResponse DTO  | `src/main/java/.../file/dto/FileUploadResponse.java`               | Response with download URL            |
| FileMetadataDTO         | `src/main/java/.../file/dto/FileMetadataDTO.java`                  | Full metadata response                |
| FileMapper              | `src/main/java/.../file/mapper/FileMapper.java`                    | Entity to DTO conversion              |
| FileValidationException | `src/main/java/.../file/exception/FileValidationException.java`    | Validation errors                     |
| FileNotFoundException   | `src/main/java/.../file/exception/FileNotFoundException.java`      | File not found                        |
| FileStorageException    | `src/main/java/.../file/exception/FileStorageException.java`       | Storage I/O errors                    |
| FileValidator           | `src/main/java/.../file/validator/FileValidator.java`              | MIME, size, magic byte validation     |
| FileStorageService      | `src/main/java/.../file/service/FileStorageService.java`           | Interface for storage abstraction     |
| LocalFileStorageService | `src/main/java/.../file/service/impl/LocalFileStorageService.java` | Local filesystem implementation       |
| FileController          | `src/main/java/.../file/controller/FileController.java`            | REST endpoints for file operations    |

### 15.2. Test Coverage

| Test Class                  | Coverage Target | Status  |
| --------------------------- | --------------- | ------- |
| FileValidatorTest           | ≥90%            | ✅ Pass |
| LocalFileStorageServiceTest | ≥80%            | ✅ Pass |
| FileControllerTest          | ≥70%            | ✅ Pass |

### 15.3. Configuration Applied

```properties
# application.properties
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=60MB
file.upload-dir=./uploads
```

### 15.4. API Endpoints Implemented

| Method   | Endpoint                      | Auth | Status         |
| -------- | ----------------------------- | ---- | -------------- |
| `POST`   | `/api/v1/files/upload`        | Yes  | ✅ Implemented |
| `GET`    | `/api/v1/files/{id}`          | Yes  | ✅ Implemented |
| `GET`    | `/api/v1/files/{id}/download` | Yes  | ✅ Implemented |
| `DELETE` | `/api/v1/files/{id}`          | Yes  | ✅ Implemented |
| `GET`    | `/api/v1/files/my-files`      | Yes  | ✅ Implemented |
| `POST`   | `/api/v1/profile/avatar`      | Yes  | ✅ Implemented |

### 15.5. Key Design Decisions

1. **Magic Byte Validation**: Implemented for JPEG, PNG, GIF, WebP, PDF, MP3, WAV, OGG, M4A
2. **Date-based Partitioning**: Files stored at `./uploads/{category}/{yyyy-MM}/{uuid}.ext`
3. **Authentication Pattern**: Uses `@AuthenticationPrincipal User user` directly
4. **Interface Abstraction**: `FileStorageService` interface allows easy cloud storage switch
5. **Public Files Support**: `isPublic` flag allows unauthenticated access when true
