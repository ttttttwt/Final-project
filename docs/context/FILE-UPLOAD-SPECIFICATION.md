# LEXIA - File Upload System Specification

**Version**: 1.5.0  
**Created**: November 28, 2025  
**Updated**: December 1, 2025  
**Status**: ✅ Implemented (Phase 1-4 Complete + Frontend Integration Complete)  
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
| FileMetadataExtractor    | ✅ Implemented     | Image dimensions, audio duration extraction        |
| FileSecurityService      | ✅ Implemented     | Centralized access control (canAccess, canDelete)  |
| Async Access Recording   | ✅ Implemented     | Non-blocking file access tracking                  |
| Database schema          | ✅ Implemented     | V15 files table, V16 avatar_file_id FK             |
| Web Frontend             | ✅ Implemented     | AvatarUpload component with userService            |
| Mobile Frontend          | ✅ Implemented     | expo-image-picker, fileService, AvatarUpload       |
| Admin Panel              | ✅ Implemented     | coursesApi, lessonsApi with thumbnail/audio upload |
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

### 7.2. Mobile (React Native) ✅ Implemented

**File Service** (`lexia-mobile/services/fileService.ts`):

```typescript
// services/fileService.ts
import * as ImagePicker from "expo-image-picker";
import { api } from "./api";
import { User } from "@/types";

/** Allowed MIME types for avatar upload */
const ALLOWED_IMAGE_TYPES = [
  "image/jpeg",
  "image/jpg",
  "image/png",
  "image/gif",
  "image/webp",
];
const MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB

export interface PickedImage {
  uri: string;
  fileName: string;
  mimeType: string;
  fileSize: number;
  width: number;
  height: number;
}

export class FileValidationError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "FileValidationError";
  }
}

export const fileService = {
  /** Request media library permissions */
  requestMediaLibraryPermission: async (): Promise<boolean> => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    return status === "granted";
  },

  /** Request camera permissions */
  requestCameraPermission: async (): Promise<boolean> => {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    return status === "granted";
  },

  /** Pick an image from the device library */
  pickImage: async (): Promise<PickedImage | null> => {
    const hasPermission = await fileService.requestMediaLibraryPermission();
    if (!hasPermission) {
      throw new FileValidationError("Media library permission is required");
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ["images"],
      allowsEditing: true,
      aspect: [1, 1], // Square crop for avatar
      quality: 0.8,
    });

    if (result.canceled || !result.assets?.length) return null;

    const asset = result.assets[0];
    return {
      uri: asset.uri,
      fileName: asset.fileName || `avatar_${Date.now()}.jpg`,
      mimeType: asset.mimeType || "image/jpeg",
      fileSize: asset.fileSize || 0,
      width: asset.width,
      height: asset.height,
    };
  },

  /** Take a photo with the camera */
  takePhoto: async (): Promise<PickedImage | null> => {
    const hasPermission = await fileService.requestCameraPermission();
    if (!hasPermission) {
      throw new FileValidationError("Camera permission is required");
    }

    const result = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
    });

    if (result.canceled || !result.assets?.length) return null;

    const asset = result.assets[0];
    return {
      uri: asset.uri,
      fileName: asset.fileName || `photo_${Date.now()}.jpg`,
      mimeType: asset.mimeType || "image/jpeg",
      fileSize: asset.fileSize || 0,
      width: asset.width,
      height: asset.height,
    };
  },

  /** Validate image before upload */
  validateImage: (image: PickedImage): void => {
    if (!ALLOWED_IMAGE_TYPES.includes(image.mimeType.toLowerCase())) {
      throw new FileValidationError(
        `Invalid file type. Allowed: JPG, PNG, GIF, WebP`
      );
    }
    if (image.fileSize > MAX_AVATAR_SIZE) {
      throw new FileValidationError(`File size exceeds maximum of 5MB`);
    }
  },

  /** Upload avatar image */
  uploadAvatar: async (image: PickedImage): Promise<User> => {
    fileService.validateImage(image);

    const formData = new FormData();
    formData.append("file", {
      uri: image.uri,
      type: image.mimeType,
      name: image.fileName,
    } as unknown as Blob);

    const response = await api.post<User>("/profile/avatar", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      timeout: 60000,
    });
    return response.data;
  },

  /** Delete current user's avatar */
  deleteAvatar: async (): Promise<User> => {
    const response = await api.delete<User>("/profile/avatar");
    return response.data;
  },

  /** Get file download URL */
  getFileUrl: (fileId: string): string => `/files/${fileId}/download`,
};
```

**AvatarUpload Component** (`lexia-mobile/components/profile/AvatarUpload.tsx`):

```tsx
// Reusable avatar upload component with:
// - Image picker (gallery/camera)
// - Image preview modal
// - Upload progress indicator
// - Delete functionality
// - Validation feedback

interface AvatarUploadProps {
  currentAvatarUrl?: string;
  userName: string;
  size?: number;
  onAvatarUpdate?: (user: User) => void;
  disabled?: boolean;
}

export function AvatarUpload({
  currentAvatarUrl,
  userName,
  size = 120,
  onAvatarUpdate,
  disabled,
}: AvatarUploadProps) {
  // State: avatarUrl, previewImage, isUploading, showOptionsModal, showPreviewModal
  // Features:
  // - Shows Avatar.Image or Avatar.Text with initials
  // - Camera overlay icon
  // - Options modal: Take Photo, Choose from Gallery, Remove Photo
  // - Preview modal with image details and Upload/Cancel buttons
  // - Integrated with fileService for upload/delete operations
}
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

### 11.4. Phase 4: Course/Lesson Media (2 pts) ✅ Complete

- [x] Create database migrations (V17 thumbnail_file_id, V18 audio_file_id)
- [x] Create course thumbnail upload endpoint (POST/DELETE /api/v1/courses/{id}/thumbnail)
- [x] Create lesson audio upload endpoint (POST/DELETE /api/v1/lessons/{id}/audio)
- [x] Update Course entity with thumbnailFile relationship + getEffectiveThumbnailUrl()
- [x] Update Lesson entity with audioFile relationship + getEffectiveAudioUrl()
- [x] Update CourseService with uploadThumbnail/deleteThumbnail methods
- [x] Update LessonService with uploadAudio/deleteAudio methods (LISTENING type validation)
- [x] Update CourseMapper to use effective thumbnail URL
- [x] Update LessonMapper and LessonDTO with audioUrl field
- [x] Update admin panel APIs (coursesApi, lessonsApi)

### 11.5. Phase 5: Cloud Storage (Future - 2 pts) ⏳ Pending

- [ ] Add AWS SDK dependency
- [ ] Implement S3FileStorageService
- [ ] Configure profile-based service selection
- [ ] Add CloudFront integration

### 11.6. Phase 6: Mobile Frontend (1 pt) ✅ Complete

- [x] Install expo-image-picker and expo-file-system dependencies
- [x] Create fileService.ts with image picker, validation, upload/delete
- [x] Create AvatarUpload component with preview modal
- [x] Integrate AvatarUpload into ProfileScreen
- [x] Write unit tests for fileService (19 tests)
- [x] Write component tests for AvatarUpload (11 tests)

**Total Estimated Points**: 8 pts (local + mobile) + 2 pts (cloud) = 10 pts  
**Completed Points**: 8 pts (Phase 1-4 + Mobile Frontend)

---

## 12. Dependencies

### 12.1. Backend Dependencies

```gradle
// build.gradle
dependencies {
    // File upload (already included in spring-boot-starter-web)
    // No additional dependencies for local storage

    // For metadata extraction (image dimensions, audio duration)
    implementation 'com.drewnoakes:metadata-extractor:2.19.0'

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

// Mobile (lexia-mobile/package.json)
{
  "dependencies": {
    "expo-image-picker": "~15.0.7",
    "expo-file-system": "~16.0.10"
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

| Component               | File Path                                                          | Notes                                    |
| ----------------------- | ------------------------------------------------------------------ | ---------------------------------------- |
| Files table migration   | `src/main/resources/db/migration/V15__Create_files_table.sql`      | UUID PK, full metadata support           |
| Avatar FK migration     | `src/main/resources/db/migration/V16__Add_avatar_file_id_*.sql`    | Added avatar_file_id to user_profiles    |
| FileCategory enum       | `src/main/java/.../file/enums/FileCategory.java`                   | 6 categories with size limits & paths    |
| FileEntity              | `src/main/java/.../file/entity/FileEntity.java`                    | JPA entity with metadata, public flag    |
| FileRepository          | `src/main/java/.../file/repository/FileRepository.java`            | Custom queries for user files            |
| FileUploadResponse DTO  | `src/main/java/.../file/dto/FileUploadResponse.java`               | Response with download URL               |
| FileMetadataDTO         | `src/main/java/.../file/dto/FileMetadataDTO.java`                  | Full metadata response                   |
| FileMapper              | `src/main/java/.../file/mapper/FileMapper.java`                    | Entity to DTO conversion                 |
| FileValidationException | `src/main/java/.../file/exception/FileValidationException.java`    | Validation errors                        |
| FileNotFoundException   | `src/main/java/.../file/exception/FileNotFoundException.java`      | File not found                           |
| FileStorageException    | `src/main/java/.../file/exception/FileStorageException.java`       | Storage I/O errors                       |
| FileValidator           | `src/main/java/.../file/validator/FileValidator.java`              | MIME, size, magic byte validation        |
| FileStorageService      | `src/main/java/.../file/service/FileStorageService.java`           | Interface for storage abstraction        |
| LocalFileStorageService | `src/main/java/.../file/service/impl/LocalFileStorageService.java` | Local filesystem implementation          |
| FileMetadataExtractor   | `src/main/java/.../file/service/FileMetadataExtractor.java`        | Extract image dimensions, audio duration |
| FileSecurityService     | `src/main/java/.../file/service/FileSecurityService.java`          | Centralized access control logic         |
| FileController          | `src/main/java/.../file/controller/FileController.java`            | REST endpoints for file operations       |

### 15.2. Test Coverage

| Test Class                   | Tests   | Pass    | Coverage Target | Status          |
| ---------------------------- | ------- | ------- | --------------- | --------------- |
| FileValidatorTest            | 47      | 47      | ≥90%            | ✅ Pass         |
| LocalFileStorageServiceTest  | 18      | 18      | ≥80%            | ✅ Pass         |
| FileControllerTest           | 17      | 17      | ≥70%            | ✅ Pass         |
| FileSecurityServiceTest      | 14      | 14      | ≥80%            | ✅ Pass         |
| FileMetadataExtractorTest    | 13      | 13      | ≥80%            | ✅ Pass         |
| **Backend Total**            | **109** | **109** | -               | ✅ **All Pass** |
| Mobile fileService.test.ts   | 19      | 19      | ≥70%            | ✅ Pass         |
| Mobile AvatarUpload.test.tsx | 11      | 11      | ≥70%            | ✅ Pass         |
| **Mobile Total**             | **30**  | **30**  | -               | ✅ **All Pass** |
| **Grand Total**              | **139** | **139** | -               | ✅ **All Pass** |

### 15.3. Bug Fixes Applied (December 1, 2025)

**Issue**: 13 FileControllerTest tests failing with 400 Bad Request

**Root Cause**: Spring MVC `@WebMvcTest` with disabled security filters couldn't bind UUID path variables when `@PathVariable` didn't have explicit name annotation.

**Solution Applied**:

1. **FileController.java**: Changed `@PathVariable UUID id` to `@PathVariable("id") UUID id` for all endpoints (getMetadata, downloadFile, deleteFile)
2. **GlobalExceptionHandler.java**: Added `MethodArgumentTypeMismatchException` handler for better error messages on type conversion failures

```java
// FileController fix
@GetMapping("/{id}")
public ResponseEntity<FileMetadataDTO> getMetadata(@PathVariable("id") UUID id) { ... }

// GlobalExceptionHandler addition
@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Invalid parameter: " + ex.getName() + " - " + ex.getMessage()
    );
    return ResponseEntity.badRequest().body(error);
}
```

### 15.4. Configuration Applied

```properties
# application.properties
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=60MB
file.upload-dir=./uploads
```

### 15.5. API Endpoints Implemented

| Method   | Endpoint                         | Auth     | Status         |
| -------- | -------------------------------- | -------- | -------------- |
| `POST`   | `/api/v1/files/upload`           | Yes      | ✅ Implemented |
| `GET`    | `/api/v1/files/{id}`             | Yes      | ✅ Implemented |
| `GET`    | `/api/v1/files/{id}/download`    | Yes      | ✅ Implemented |
| `DELETE` | `/api/v1/files/{id}`             | Yes      | ✅ Implemented |
| `GET`    | `/api/v1/files/{id}/exists`      | Yes      | ✅ Implemented |
| `GET`    | `/api/v1/files/my-files`         | Yes      | ✅ Implemented |
| `POST`   | `/api/v1/profile/avatar`         | Yes      | ✅ Implemented |
| `DELETE` | `/api/v1/profile/avatar`         | Yes      | ✅ Implemented |
| `POST`   | `/api/v1/courses/{id}/thumbnail` | Yes (CM) | ✅ Implemented |
| `DELETE` | `/api/v1/courses/{id}/thumbnail` | Yes (CM) | ✅ Implemented |
| `POST`   | `/api/v1/lessons/{id}/audio`     | Yes (CM) | ✅ Implemented |
| `DELETE` | `/api/v1/lessons/{id}/audio`     | Yes (CM) | ✅ Implemented |

### 15.6. Key Design Decisions

1. **Magic Byte Validation**: Implemented for JPEG, PNG, GIF, WebP, PDF, MP3, WAV, OGG, M4A
2. **Date-based Partitioning**: Files stored at `./uploads/{category}/{yyyy-MM}/{uuid}.ext`
3. **Authentication Pattern**: Uses `@AuthenticationPrincipal User user` directly
4. **Interface Abstraction**: `FileStorageService` interface allows easy cloud storage switch
5. **Public Files Support**: `isPublic` flag allows unauthenticated access when true
6. **Explicit @PathVariable Binding**: Required for WebMvcTest compatibility with disabled security filters

---

## 16. Progress Summary

### 16.1. Implementation Status

| Phase     | Description         | Points | Status         | Tests      |
| --------- | ------------------- | ------ | -------------- | ---------- |
| 1         | Core Infrastructure | 2      | ✅ Complete    | -          |
| 2         | File Upload API     | 2      | ✅ Complete    | 16/16 ✅   |
| 3         | Avatar Integration  | 1      | ✅ Complete    | 62/62 ✅   |
| 4         | Course/Lesson Media | 2      | ✅ Complete    | ✅         |
| 5         | Cloud Storage (S3)  | 2      | ⏳ Pending     | -          |
| **Total** |                     | **9**  | **7 pts done** | **All ✅** |

### 16.2. Test Results (Last Run: December 1, 2025)

```
Tests:     109 passed, 0 failed
Duration:  ~17 seconds

Breakdown:
- FileValidatorTest:           47 tests ✅
- LocalFileStorageServiceTest: 18 tests ✅
- FileControllerTest:          17 tests ✅
- FileSecurityServiceTest:     14 tests ✅
- FileMetadataExtractorTest:   13 tests ✅
```

### 16.3. Phase 4 Implementation Details (December 1, 2025)

**Database Migrations Added:**

- `V17__Add_thumbnail_file_id_to_courses.sql` - FK for course thumbnails
- `V18__Add_audio_file_id_to_lessons.sql` - FK for lesson audio files

**Backend Files Modified:**
| File | Changes |
|------|--------|
| `Course.java` | Added `thumbnailFile` ManyToOne + `getEffectiveThumbnailUrl()` |
| `Lesson.java` | Added `audioFile` ManyToOne + `getEffectiveAudioUrl()` |
| `CourseService.java` | Added `uploadThumbnail()`, `deleteThumbnail()` |
| `CourseServiceImpl.java` | Implemented thumbnail upload/delete with FileStorageService |
| `LessonService.java` | Added `uploadAudio()`, `deleteAudio()` |
| `LessonServiceImpl.java` | Implemented audio upload/delete with LISTENING validation |
| `CourseController.java` | Added POST/DELETE `/courses/{id}/thumbnail` endpoints |
| `LessonController.java` | Added POST/DELETE `/lessons/{id}/audio` endpoints |
| `CourseMapper.java` | Uses `getEffectiveThumbnailUrl()` for computed URL |
| `LessonMapper.java` | Added `audioUrl` from file entity |
| `LessonDTO.java` | Added `audioUrl` field |

**Admin Panel Files Modified:**
| File | Changes |
|------|--------|
| `coursesApi.ts` | Added `uploadThumbnail()`, `deleteThumbnail()` |
| `lessonsApi.ts` | Added `uploadAudio()`, `deleteAudio()` |
| `lesson.types.ts` | Added `audioUrl` optional field to Lesson interface |

**Key Design Decisions:**

1. **Effective URL Pattern**: Entities have `getEffectiveXxxUrl()` methods that return file download URL if uploaded, otherwise fallback to external URL
2. **LISTENING Type Validation**: Audio upload only allowed for LISTENING type lessons
3. **Backward Compatibility**: Keep `thumbnailUrl`/`content.audioUrl` for external URLs
4. **File Cleanup**: Old files are deleted when new ones are uploaded

### 16.4. Next Steps (Phase 5)

1. **Cloud Storage (S3)**:

   - Add AWS SDK dependency
   - Implement S3FileStorageService
   - Configure profile-based service selection
   - Add CloudFront integration

2. **Future Enhancements**:
   - Image thumbnail generation
   - Audio transcoding
   - CDN integration

**Note**: All frontend implementations (Web, Mobile, Admin) are now complete. Only cloud storage (Phase 5) remains.

- Image thumbnail generation
- Audio transcoding
- CDN integration

---

## 17. December 1, 2025 Enhancements

### 17.1. New Components Added

| Component                 | Description                                                                       |
| ------------------------- | --------------------------------------------------------------------------------- |
| **FileMetadataExtractor** | Extracts image width/height and audio duration using metadata-extractor library   |
| **FileSecurityService**   | Centralized access control with `canAccess()`, `canDelete()`, `isOwner()` methods |
| **fileAccessExecutor**    | Dedicated async executor for non-blocking access recording                        |

### 17.2. FileMetadataExtractor

Extracts metadata from uploaded files automatically during upload:

```java
@Service
public class FileMetadataExtractor {

    @Data
    @Builder
    public static class ExtractedMetadata {
        private Integer width;        // For images
        private Integer height;       // For images
        private Integer durationSeconds; // For audio
    }

    public ExtractedMetadata extract(MultipartFile file, FileCategory category) {
        return switch (category) {
            case AVATAR, COURSE_THUMBNAIL, LESSON_IMAGE -> extractImageMetadata(file);
            case LESSON_AUDIO -> extractAudioMetadata(file);
            default -> ExtractedMetadata.builder().build();
        };
    }
}
```

**Supported Formats:**

- **Images**: JPEG, PNG, GIF, WebP (extracts width, height via EXIF/format headers)
- **Audio**: MP3, WAV, M4A, OGG (calculates duration from bitrate/sample rate)

### 17.3. FileSecurityService

Centralized access control replacing scattered inline checks:

```java
@Service
public class FileSecurityService {

    /**
     * Check if user can access the file.
     * Access granted if: public file OR owner OR ADMIN role
     */
    public boolean canAccess(FileEntity file, User user) {
        if (Boolean.TRUE.equals(file.getIsPublic())) return true;
        if (user == null) return false;
        if (isOwner(file, user)) return true;
        return hasAdminRole(user);
    }

    /**
     * Check if user can delete the file.
     * Deletion allowed if: owner OR ADMIN role
     */
    public boolean canDelete(FileEntity file, User user) {
        if (file == null || user == null) return false;
        if (isOwner(file, user)) return true;
        return hasAdminRole(user);
    }
}
```

### 17.4. FileCategory with isPublicByDefault

Updated enum to include default access control:

```java
public enum FileCategory {
    AVATAR("avatars", true),                    // Public by default
    COURSE_THUMBNAIL("courses/thumbnails", true), // Public by default
    LESSON_AUDIO("lessons/audio", false),       // Private
    LESSON_IMAGE("lessons/images", false),      // Private
    DOCUMENT("documents", false),               // Private
    CERTIFICATE("documents/certificates", false); // Private

    private final String storagePath;
    private final boolean publicByDefault;

    public boolean isPublicByDefault() {
        return publicByDefault;
    }
}
```

### 17.5. Async Access Recording

Non-blocking file access tracking using dedicated executor:

```java
// AsyncConfig.java
@Bean(name = "fileAccessExecutor")
public Executor fileAccessExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("file-access-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy()); // Non-critical
    executor.initialize();
    return executor;
}

// LocalFileStorageService.java
@Override
@Async("fileAccessExecutor")
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordAccess(UUID fileId) {
    fileRepository.findById(fileId).ifPresent(file -> {
        file.recordAccess();
        fileRepository.save(file);
    });
}
```

### 17.6. Updated Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        FileController                               │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
            ┌─────────────────────┼─────────────────────┐
            │                     │                     │
┌───────────▼───────────┐ ┌───────▼───────┐ ┌──────────▼──────────┐
│ FileSecurityService   │ │FileStorage    │ │ FileValidator       │
│ - canAccess()         │ │Service        │ │ - MIME validation   │
│ - canDelete()         │ │(Interface)    │ │ - Size validation   │
│ - isOwner()           │ └───────┬───────┘ │ - Magic bytes       │
└───────────────────────┘         │         └─────────────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
          ┌─────────▼─────────┐   │   ┌─────────▼─────────┐
          │LocalFileStorage   │   │   │FileMetadata       │
          │Service            │   │   │Extractor          │
          │ - store()         │   │   │ - extractImage()  │
          │ - load()          │   │   │ - extractAudio()  │
          │ - recordAccess()  │   │   └───────────────────┘
          │   (async)         │   │
          └───────────────────┘   │
                                  │
                    ┌─────────────▼─────────────┐
                    │    FileRepository         │
                    │    (JPA)                  │
                    └───────────────────────────┘
```

### 17.7. Performance Improvements

| Improvement          | Before                     | After                              |
| -------------------- | -------------------------- | ---------------------------------- |
| Access Recording     | Synchronous (blocking)     | Async with dedicated executor      |
| Security Checks      | Scattered in Controller    | Centralized in FileSecurityService |
| Metadata Extraction  | Not implemented            | Auto-extracted on upload           |
| Public/Private Logic | Hardcoded in service       | Configured in FileCategory enum    |
| Executor Rejection   | Default (throws exception) | DiscardPolicy (non-critical ops)   |

### 17.8. Files Modified/Created

| File                               | Action   | Description                              |
| ---------------------------------- | -------- | ---------------------------------------- |
| `build.gradle`                     | Modified | Added metadata-extractor:2.19.0          |
| `FileCategory.java`                | Modified | Added isPublicByDefault property         |
| `FileMetadataExtractor.java`       | Created  | New service for metadata extraction      |
| `FileSecurityService.java`         | Created  | Centralized access control               |
| `LocalFileStorageService.java`     | Modified | Integrated extractor, async recordAccess |
| `FileController.java`              | Modified | Uses FileSecurityService                 |
| `AsyncConfig.java`                 | Modified | Added fileAccessExecutor bean            |
| `FileSecurityServiceTest.java`     | Created  | 14 test cases                            |
| `FileMetadataExtractorTest.java`   | Created  | 13 test cases                            |
| `LocalFileStorageServiceTest.java` | Modified | Updated for new components               |
| `FileControllerTest.java`          | Modified | Uses FileSecurityService mock            |

---

## 18. Mobile Frontend Implementation (December 1, 2025)

### 18.1. Implementation Overview

The mobile frontend file upload system has been fully implemented using expo-image-picker for image selection and FormData for upload. The implementation includes:

| Component          | File Path                                                 | Description                                                        |
| ------------------ | --------------------------------------------------------- | ------------------------------------------------------------------ |
| fileService        | `lexia-mobile/services/fileService.ts`                    | Complete file service with image picker, validation, upload/delete |
| AvatarUpload       | `lexia-mobile/components/profile/AvatarUpload.tsx`        | Reusable avatar upload component with preview modal                |
| ProfileScreen      | `lexia-mobile/app/tabs/ProfileScreen.tsx`                 | Integrated AvatarUpload component                                  |
| fileService tests  | `lexia-mobile/__tests__/services/fileService.test.ts`     | 19 unit tests                                                      |
| AvatarUpload tests | `lexia-mobile/__tests__/components/AvatarUpload.test.tsx` | 11 component tests                                                 |

### 18.2. Mobile File Service Features

```typescript
// Key functions in fileService.ts
export const fileService = {
  requestMediaLibraryPermission(): Promise<boolean>  // Request gallery access
  requestCameraPermission(): Promise<boolean>        // Request camera access
  pickImage(): Promise<PickedImage | null>           // Pick from gallery
  takePhoto(): Promise<PickedImage | null>           // Capture with camera
  validateImage(image: PickedImage): void            // Validate type/size
  uploadAvatar(image: PickedImage): Promise<User>    // Upload via FormData
  deleteAvatar(): Promise<User>                       // Delete avatar
  pickAndUploadAvatar(): Promise<User | null>        // Convenience method
  takeAndUploadAvatar(): Promise<User | null>        // Convenience method
  getFileUrl(fileId: string): string                 // Get download URL
};
```

### 18.3. AvatarUpload Component Features

| Feature             | Description                                           |
| ------------------- | ----------------------------------------------------- |
| **Avatar Display**  | Shows Avatar.Image or Avatar.Text with initials       |
| **Camera Overlay**  | Circular camera icon button for changing avatar       |
| **Options Modal**   | Take Photo, Choose from Gallery, Remove Photo options |
| **Preview Modal**   | Shows selected image with dimensions and file size    |
| **Upload Progress** | ActivityIndicator during upload                       |
| **Delete Function** | Confirmation dialog before removal                    |
| **Accessibility**   | Full ARIA labels and hints                            |
| **Validation**      | Client-side MIME type and file size validation        |

### 18.4. Mobile Test Coverage

| Test File                          | Tests  | Status             |
| ---------------------------------- | ------ | ------------------ |
| `fileService.test.ts`              | 19     | ✅ All passing     |
| `AvatarUpload.test.tsx`            | 11     | ✅ All passing     |
| **Total Mobile File Upload Tests** | **30** | ✅ **All passing** |

**Test Categories:**

**fileService tests (19):**

- Permission requests (media library, camera)
- pickImage (success, cancel, permission denied)
- takePhoto (success, cancel)
- validateImage (valid, invalid MIME, size exceeded)
- uploadAvatar (success, validation error)
- deleteAvatar
- getFileUrl

**AvatarUpload tests (11):**

- Render with/without avatar URL
- Options modal display
- Remove Photo option when avatar exists
- Gallery/Camera selection
- Preview modal after image selection
- Upload button functionality
- Disabled state
- Remove button visibility
- validateImage integration

### 18.5. Dependencies Installed

```json
{
  "expo-image-picker": "~15.0.7",
  "expo-file-system": "~16.0.10"
}
```

### 18.6. Integration with ProfileScreen

```tsx
// ProfileScreen.tsx integration
import { AvatarUpload } from "@/components/profile";
import { useAuthStore } from "@/store/authStore";

function ProfileScreen() {
  const { user, setUser } = useAuthStore();

  const handleAvatarUpdate = (updatedUser: User) => {
    setUser(updatedUser); // Update global auth state
  };

  return (
    <AvatarUpload
      currentAvatarUrl={user?.avatarUrl}
      userName={`${user?.firstName} ${user?.lastName}`}
      size={120}
      onAvatarUpdate={handleAvatarUpdate}
    />
  );
}
```

---

## 19. Frontend Implementation Summary

### 19.1. Platform Implementation Status

| Platform                  | Status  | Components                                              | Tests      |
| ------------------------- | ------- | ------------------------------------------------------- | ---------- |
| **Backend**               | ✅ 100% | FileController, FileStorageService, FileValidator, etc. | 109 tests  |
| **Web (Next.js)**         | ✅ 100% | userService.ts, AvatarUpload.tsx                        | Integrated |
| **Mobile (React Native)** | ✅ 100% | fileService.ts, AvatarUpload.tsx                        | 30 tests   |
| **Admin Panel**           | ✅ 100% | coursesApi.ts, lessonsApi.ts                            | Integrated |

### 19.2. Total Test Coverage

| Category                    | Tests                                                                                                          |
| --------------------------- | -------------------------------------------------------------------------------------------------------------- |
| Backend (Java)              | 109 tests (FileValidator, LocalFileStorageService, FileController, FileSecurityService, FileMetadataExtractor) |
| Mobile (TypeScript)         | 30 tests (fileService, AvatarUpload)                                                                           |
| **Total File Upload Tests** | **139+ tests**                                                                                                 |

---

**Document Owner**: LEXIA Development Team  
**Last Updated**: December 1, 2025  
**Next Review**: Before Sprint 6 Planning
