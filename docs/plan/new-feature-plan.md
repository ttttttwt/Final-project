## 🔮 Sprint 5 Preview — Notification & File Upload Systems

**Sprint**: 5 / 8 | **Duration**: Dec 12 – Dec 25, 2025 (14 days)  
**Status**: 📝 Planning | **Estimated Points**: 19-21 pts

### New Features (Backend)

#### Epic E: File Upload System (7 pts)

> **Specification**: `docs/context/FILE-UPLOAD-SPECIFICATION.md`

- [ ] **E1**: Core Infrastructure (2 pts)
  - Create `files` table migration (V14)
  - FileEntity, FileRepository
  - FileStorageService interface
  - LocalFileStorageService implementation
  - FileValidator (MIME type, size validation)

- [ ] **E2**: File Upload API (2 pts)
  - FileController with upload/download endpoints
  - Swagger documentation
  - Unit and integration tests
  - Error handling (FileValidationException)

- [ ] **E3**: Avatar Integration (1 pt)
  - Avatar upload endpoint in UserController
  - Update UserProfile entity with avatar_file_id
  - Frontend integration (Web + Mobile)

- [ ] **E4**: Course/Lesson Media (2 pts)
  - Course thumbnail upload endpoint
  - Lesson audio upload endpoint
  - Admin panel integration

#### Epic F: Notification System (12 pts)

> **Specification**: `docs/context/NOTIFICATION-SPECIFICATION.md`

- [ ] **F1**: Core Infrastructure (3 pts)
  - Create `notifications` table migration (V15)
  - Create `notification_preferences` table migration (V16)
  - NotificationEntity, NotificationPreferencesEntity
  - NotificationRepository, NotificationPreferencesRepository
  - NotificationService implementation

- [ ] **F2**: REST API (2 pts)
  - NotificationController (list, read, delete)
  - NotificationPreferencesController
  - Swagger documentation
  - Controller tests

- [ ] **F3**: WebSocket Real-time (2.5 pts)
  - Add Spring WebSocket dependencies
  - WebSocketConfig (STOMP broker)
  - WebSocketSecurityConfig (JWT auth)
  - NotificationWebSocketHandler
  - WebSocket connection tests

- [ ] **F4**: Event Integration (1.5 pts)
  - NotificationEvent classes
  - NotificationEventListener
  - Integration with CourseService, ProgressService
  - Event-driven notification tests

- [ ] **F5**: Frontend Integration (3 pts)
  - Web: Notification store, bell component, WebSocket client
  - Mobile: Notification hook, components, WebSocket client
  - Admin: Broadcast notification UI

### Priority Order

1. **File Upload (E1-E3)** - Unblocks avatar upload feature
2. **Notification Core (F1-F2)** - REST API first
3. **WebSocket (F3)** - Real-time delivery
4. **Full Integration (E4, F4-F5)** - Complete both systems

### Dependencies

**Backend**:
```gradle
// WebSocket
implementation 'org.springframework.boot:spring-boot-starter-websocket'

// Image processing (optional)
implementation 'org.imgscalr:imgscalr-lib:4.2'
```

**Frontend (Web + Mobile)**:
```json
{
  "@stomp/stompjs": "^7.0.0",
  "sockjs-client": "^1.6.1"
}
```