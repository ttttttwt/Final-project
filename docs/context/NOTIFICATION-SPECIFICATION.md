# LEXIA - Notification System Specification

**Version**: 1.3.0  
**Created**: November 28, 2025  
**Updated**: November 30, 2025  
**Status**: ✅ Implemented (Backend) - Event Integration Completed  
**Implemented Sprint**: Sprint 5

---

## 1. Overview

### 1.1. Purpose

The Notification System enables real-time and persistent communication with users about important events in their learning journey, including:

- Course updates and new content
- Learning streak reminders
- Achievement unlocks
- Enrollment confirmations
- System announcements

### 1.2. Goals

| Goal               | Description                                                       |
| ------------------ | ----------------------------------------------------------------- |
| **Engagement**     | Keep learners informed and motivated through timely notifications |
| **Real-time**      | Deliver urgent notifications instantly via WebSocket              |
| **Persistence**    | Store notifications for later retrieval                           |
| **Cross-platform** | Support Web, Mobile (iOS/Android), and Admin Panel                |
| **Scalability**    | Handle thousands of concurrent users                              |

### 1.3. Non-Goals (Out of Scope)

- Push notifications to mobile devices (requires Firebase/APNs - defer to later sprint)
- Email notifications (separate Email Service)
- SMS notifications
- Notification scheduling/automation engine

---

## 2. Notification Types

### 2.1. Type Definitions

| Type                   | Category    | Priority | Real-time | Description                                |
| ---------------------- | ----------- | -------- | --------- | ------------------------------------------ |
| `COURSE_PUBLISHED`     | Learning    | Normal   | Yes       | New course available matching user's level |
| `LESSON_ADDED`         | Learning    | Normal   | Yes       | New lesson added to enrolled course        |
| `ENROLLMENT_CONFIRMED` | Learning    | Normal   | Yes       | User successfully enrolled in course       |
| `LESSON_COMPLETED`     | Achievement | Normal   | No        | Lesson completion confirmation             |
| `COURSE_COMPLETED`     | Achievement | High     | Yes       | Course completion celebration              |
| `ACHIEVEMENT_UNLOCKED` | Achievement | High     | Yes       | Badge or milestone achieved                |
| `STREAK_REMINDER`      | Engagement  | Normal   | No        | Daily study streak reminder                |
| `STREAK_LOST`          | Engagement  | High     | Yes       | Streak broken notification                 |
| `STREAK_MILESTONE`     | Achievement | High     | Yes       | 7-day, 30-day, 100-day streaks             |
| `LEVEL_UP`             | Achievement | High     | Yes       | CEFR level progression                     |
| `SYSTEM_ANNOUNCEMENT`  | System      | High     | Yes       | Platform-wide announcements                |
| `MAINTENANCE_NOTICE`   | System      | High     | Yes       | Scheduled maintenance alerts               |

### 2.2. Priority Levels

| Priority   | Delivery            | Persistence | Use Case                  |
| ---------- | ------------------- | ----------- | ------------------------- |
| **High**   | Real-time + Persist | 30 days     | Achievements, Alerts      |
| **Normal** | Persist only        | 14 days     | Course updates, Reminders |
| **Low**    | Persist only        | 7 days      | Tips, Suggestions         |

---

## 3. Database Schema

### 3.1. Notifications Table

```sql
-- V13__Create_notifications_table.sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSONB DEFAULT '{}',
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ,

    CONSTRAINT valid_type CHECK (type IN (
        'COURSE_PUBLISHED', 'LESSON_ADDED', 'ENROLLMENT_CONFIRMED',
        'LESSON_COMPLETED', 'COURSE_COMPLETED', 'ACHIEVEMENT_UNLOCKED',
        'STREAK_REMINDER', 'STREAK_LOST', 'STREAK_MILESTONE',
        'LEVEL_UP', 'SYSTEM_ANNOUNCEMENT', 'MAINTENANCE_NOTICE'
    )),
    CONSTRAINT valid_priority CHECK (priority IN ('HIGH', 'NORMAL', 'LOW'))
);

-- Indexes for performance
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = false;
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_expires ON notifications(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_notifications_type ON notifications(type);

COMMENT ON TABLE notifications IS 'User notifications for learning events and system alerts';
COMMENT ON COLUMN notifications.data IS 'Additional payload - JSON object with type-specific data';
COMMENT ON COLUMN notifications.expires_at IS 'Auto-cleanup: notifications older than this are deleted';
```

### 3.2. Notification Preferences Table

```sql
-- V14__Create_notification_preferences_table.sql
CREATE TABLE notification_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,

    -- Channel preferences
    in_app_enabled BOOLEAN DEFAULT true,
    email_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,

    -- Type preferences (which notifications to receive)
    learning_enabled BOOLEAN DEFAULT true,
    achievements_enabled BOOLEAN DEFAULT true,
    reminders_enabled BOOLEAN DEFAULT true,
    system_enabled BOOLEAN DEFAULT true,

    -- Quiet hours (optional)
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    quiet_hours_timezone VARCHAR(50) DEFAULT 'UTC',

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON TABLE notification_preferences IS 'User preferences for notification delivery';
```

### 3.3. Data Column Schema (JSONB)

Different notification types have different data payloads:

**COURSE_PUBLISHED / ENROLLMENT_CONFIRMED**:

```json
{
  "courseId": "uuid",
  "courseTitle": "Business English Basics",
  "courseThumbnail": "https://...",
  "cefrLevel": "B1"
}
```

**LESSON_ADDED / LESSON_COMPLETED**:

```json
{
  "courseId": "uuid",
  "lessonId": "uuid",
  "lessonTitle": "Greetings and Introductions",
  "lessonType": "READING",
  "sectionTitle": "Getting Started"
}
```

**COURSE_COMPLETED**:

```json
{
  "courseId": "uuid",
  "courseTitle": "Business English Basics",
  "completionTime": 1234,
  "certificateUrl": "https://..."
}
```

**ACHIEVEMENT_UNLOCKED**:

```json
{
  "achievementId": "uuid",
  "achievementName": "Early Bird",
  "achievementIcon": "🌅",
  "description": "Complete 5 lessons before 8 AM"
}
```

**STREAK_MILESTONE**:

```json
{
  "streakDays": 30,
  "milestone": "30-day streak",
  "reward": "Badge: Dedicated Learner"
}
```

---

## 4. API Endpoints

### 4.1. Notification REST APIs

| Method | Endpoint                             | Auth | Role | Description                         |
| ------ | ------------------------------------ | ---- | ---- | ----------------------------------- |
| GET    | `/api/v1/notifications`              | Yes  | Any  | List user notifications (paginated) |
| GET    | `/api/v1/notifications/unread-count` | Yes  | Any  | Get unread notification count       |
| GET    | `/api/v1/notifications/{id}`         | Yes  | Any  | Get notification details            |
| PUT    | `/api/v1/notifications/{id}/read`    | Yes  | Any  | Mark single notification as read    |
| PUT    | `/api/v1/notifications/read-all`     | Yes  | Any  | Mark all notifications as read      |
| DELETE | `/api/v1/notifications/{id}`         | Yes  | Any  | Delete single notification          |
| DELETE | `/api/v1/notifications`              | Yes  | Any  | Delete all read notifications       |

### 4.2. Notification Preferences APIs

| Method | Endpoint                            | Auth | Role | Description             |
| ------ | ----------------------------------- | ---- | ---- | ----------------------- |
| GET    | `/api/v1/notifications/preferences` | Yes  | Any  | Get user preferences    |
| PUT    | `/api/v1/notifications/preferences` | Yes  | Any  | Update user preferences |

### 4.3. Admin Notification APIs

| Method | Endpoint                                | Auth | Role  | Description            |
| ------ | --------------------------------------- | ---- | ----- | ---------------------- |
| POST   | `/api/v1/admin/notifications/broadcast` | Yes  | ADMIN | Send to all users      |
| POST   | `/api/v1/admin/notifications/send`      | Yes  | ADMIN | Send to specific users |

### 4.4. Request/Response Examples

**GET /api/v1/notifications?page=0&size=20**

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "type": "COURSE_COMPLETED",
      "title": "Congratulations! 🎉",
      "message": "You completed Business English Basics",
      "data": {
        "courseId": "123e4567-e89b-12d3-a456-426614174000",
        "courseTitle": "Business English Basics",
        "completionTime": 1234
      },
      "priority": "HIGH",
      "isRead": false,
      "createdAt": "2025-11-28T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 42,
  "totalPages": 3
}
```

**GET /api/v1/notifications/unread-count**

```json
{
  "unreadCount": 5,
  "highPriorityCount": 2
}
```

**PUT /api/v1/notifications/preferences**

```json
{
  "inAppEnabled": true,
  "emailEnabled": true,
  "pushEnabled": false,
  "learningEnabled": true,
  "achievementsEnabled": true,
  "remindersEnabled": true,
  "systemEnabled": true,
  "quietHoursStart": "22:00",
  "quietHoursEnd": "07:00",
  "quietHoursTimezone": "Asia/Ho_Chi_Minh"
}
```

---

## 5. Real-time Delivery (WebSocket)

### 5.1. Technology Choice

**Recommended**: WebSocket with STOMP protocol

| Option                   | Pros                                           | Cons                              | Decision        |
| ------------------------ | ---------------------------------------------- | --------------------------------- | --------------- |
| WebSocket + STOMP        | Bidirectional, Spring support, Mobile friendly | More complex setup                | ✅ **Selected** |
| Server-Sent Events (SSE) | Simple, HTTP-based                             | One-way only, Less mobile support | ❌              |
| Long Polling             | Simple, Fallback friendly                      | Higher latency, More server load  | ❌              |

### 5.2. WebSocket Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Allowed origins configurable via application.properties
    @Value("${lexia.websocket.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOriginsString;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for subscriptions
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages from clients
        config.setApplicationDestinationPrefixes("/app");
        // User-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowedOrigins = allowedOriginsString.split(",");
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS(); // Fallback for browsers without WebSocket
    }
}
```

**Configuration (application.properties)**:

```properties
# WebSocket allowed origins (comma-separated)
lexia.websocket.allowed-origins=http://localhost:3000,http://localhost:5173,http://localhost:19006,https://lexia.app,https://admin.lexia.app
```

### 5.3. STOMP Destinations

| Destination                 | Type  | Description                             |
| --------------------------- | ----- | --------------------------------------- |
| `/user/queue/notifications` | Queue | Personal notifications for current user |
| `/topic/announcements`      | Topic | Broadcast to all connected users        |

### 5.4. WebSocket Security

```java
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/user/**", "/topic/**").authenticated()
            .anyMessage().denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true; // CSRF disabled for WebSocket
    }
}
```

### 5.5. Client Integration

**Web (JavaScript/TypeScript)**:

```typescript
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const client = new Client({
  webSocketFactory: () => new SockJS("http://localhost:8088/ws"),
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`,
  },
  onConnect: () => {
    // Subscribe to personal notifications
    client.subscribe("/user/queue/notifications", (message) => {
      const notification = JSON.parse(message.body);
      showNotification(notification);
    });

    // Subscribe to broadcast announcements
    client.subscribe("/topic/announcements", (message) => {
      const announcement = JSON.parse(message.body);
      showAnnouncement(announcement);
    });
  },
});

client.activate();
```

**Mobile (React Native)**:

```typescript
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

// Same pattern as web, but handle reconnection on app resume
```

---

## 6. Backend Architecture

### 6.1. Component Structure

```
com.lexia.backend/
├── notification/
│   ├── controller/
│   │   └── NotificationController.java
│   ├── service/
│   │   ├── NotificationService.java
│   │   └── impl/
│   │       └── NotificationServiceImpl.java
│   ├── repository/
│   │   ├── NotificationRepository.java
│   │   └── NotificationPreferencesRepository.java
│   ├── entity/
│   │   ├── Notification.java
│   │   └── NotificationPreferences.java
│   ├── dto/
│   │   ├── NotificationDTO.java
│   │   ├── NotificationPreferencesDTO.java
│   │   ├── UnreadCountDTO.java
│   │   └── CreateNotificationRequest.java
│   ├── mapper/
│   │   └── NotificationMapper.java
│   ├── event/
│   │   ├── NotificationEvent.java
│   │   └── NotificationEventListener.java
│   └── websocket/
│       ├── WebSocketConfig.java
│       ├── WebSocketSecurityConfig.java
│       └── NotificationWebSocketHandler.java
```

### 6.2. Service Interface

```java
public interface NotificationService {

    // CRUD operations
    Page<NotificationDTO> getNotifications(UUID userId, Pageable pageable);
    NotificationDTO getNotificationById(UUID userId, UUID notificationId);
    void markAsRead(UUID userId, UUID notificationId);
    void markAllAsRead(UUID userId);
    void deleteNotification(UUID userId, UUID notificationId);
    void deleteAllReadNotifications(UUID userId);

    // Unread count
    UnreadCountDTO getUnreadCount(UUID userId);

    // Create and send
    NotificationDTO createNotification(UUID userId, CreateNotificationRequest request);
    void sendRealTimeNotification(UUID userId, NotificationDTO notification);
    void broadcastAnnouncement(CreateNotificationRequest request);

    // Preferences
    NotificationPreferencesDTO getPreferences(UUID userId);
    NotificationPreferencesDTO updatePreferences(UUID userId, NotificationPreferencesDTO preferences);

    // Cleanup
    void deleteExpiredNotifications();
}
```

### 6.3. Event-Driven Notification Creation

```java
@Component
public class NotificationEventListener {

    @Autowired
    private NotificationService notificationService;

    @EventListener
    public void handleCourseCompleted(CourseCompletedEvent event) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
            .type(NotificationType.COURSE_COMPLETED)
            .title("Congratulations! 🎉")
            .message("You completed " + event.getCourseTitle())
            .priority(NotificationPriority.HIGH)
            .data(Map.of(
                "courseId", event.getCourseId(),
                "courseTitle", event.getCourseTitle(),
                "completionTime", event.getCompletionTime()
            ))
            .build();

        NotificationDTO notification = notificationService.createNotification(
            event.getUserId(), request
        );

        // Send real-time via WebSocket
        notificationService.sendRealTimeNotification(event.getUserId(), notification);
    }

    @EventListener
    public void handleStreakMilestone(StreakMilestoneEvent event) {
        // Similar pattern...
    }

    @EventListener
    public void handleAchievementUnlocked(AchievementUnlockedEvent event) {
        // Similar pattern...
    }
}
```

---

## 7. Frontend Integration

### 7.1. Web (Next.js)

**Notification Store (Zustand)**:

```typescript
interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;

  fetchNotifications: () => Promise<void>;
  fetchUnreadCount: () => Promise<void>;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  addNotification: (notification: Notification) => void;
}
```

**Notification Bell Component**:

```tsx
export function NotificationBell() {
  const { unreadCount, notifications } = useNotificationStore();

  return (
    <Popover>
      <PopoverTrigger>
        <Button variant="ghost" className="relative">
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <Badge className="absolute -top-1 -right-1">
              {unreadCount > 99 ? "99+" : unreadCount}
            </Badge>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-80">
        <NotificationList notifications={notifications} />
      </PopoverContent>
    </Popover>
  );
}
```

### 7.2. Mobile (React Native)

**useNotifications Hook**:

```typescript
export function useNotifications() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    // Connect to WebSocket on mount
    const client = connectWebSocket((notification) => {
      setNotifications((prev) => [notification, ...prev]);
      setUnreadCount((prev) => prev + 1);
    });

    return () => client.deactivate();
  }, []);

  return { notifications, unreadCount };
}
```

---

## 8. Testing Strategy

### 8.1. Unit Tests

- NotificationService: create, read, update, delete operations
- NotificationMapper: entity ↔ DTO conversions
- Event listeners: verify notification creation on events

### 8.2. Integration Tests

- REST API endpoints with MockMvc
- WebSocket connections with StompSession
- Database operations with Testcontainers

### 8.3. Coverage Requirements

| Component              | Target Coverage |
| ---------------------- | --------------- |
| NotificationService    | ≥80%            |
| NotificationController | ≥70%            |
| Event Listeners        | ≥70%            |
| Overall                | ≥70%            |

---

## 9. Implementation Tasks

### 9.1. Phase 1: Core Infrastructure (3 pts) ✅ COMPLETED

- [x] Create database migrations (V13, V14)
- [x] Create JPA entities (Notification, NotificationPreferences)
- [x] Create repositories with custom queries
- [x] Create DTOs and mappers
- [x] Implement NotificationService (95% test coverage)

### 9.2. Phase 2: REST API (2 pts) ✅ COMPLETED

- [x] Create NotificationController (user endpoints)
- [x] Create AdminNotificationController (broadcast/send endpoints)
- [x] Add Swagger documentation
- [x] Add request validation
- [x] Write controller tests (78% coverage)

### 9.3. Phase 3: WebSocket (2.5 pts) ✅ COMPLETED

- [x] Add Spring WebSocket dependencies
- [x] Configure WebSocket broker (WebSocketConfig.java)
- [x] Implement WebSocket security (WebSocketAuthInterceptor.java)
- [x] Create notification handler
- [ ] Test WebSocket connections (manual testing needed)

### 9.4. Phase 4: Event Integration (1.5 pts) ✅ COMPLETED

- [x] Create notification events (7 event classes)
- [x] Implement event listeners (NotificationEventListener.java)
- [x] Integrate with existing services (enrollment, progress)
- [x] Test event-driven notifications

### 9.5. Phase 5: Frontend Integration (3 pts) 🔄 PENDING

- [ ] Web: Notification store, components, WebSocket client
- [ ] Mobile: Notification hook, components, WebSocket client
- [ ] Admin: Broadcast notification UI

**Total Estimated Points**: 12 pts (1-2 sprints)

### Implementation Summary (Backend)

| Component           | Status | Coverage | Files Created                                       |
| ------------------- | ------ | -------- | --------------------------------------------------- |
| Database Migrations | ✅     | N/A      | V13, V14                                            |
| Entities            | ✅     | N/A      | 2 files                                             |
| Repositories        | ✅     | N/A      | 2 files                                             |
| DTOs                | ✅     | N/A      | 6 files                                             |
| Mapper              | ✅     | 92%      | 1 file                                              |
| Service             | ✅     | 95%      | 2 files                                             |
| Controllers         | ✅     | 78%      | 2 files                                             |
| WebSocket           | ✅     | 49%      | 2 files                                             |
| Events              | ✅     | N/A\*    | 8 files                                             |
| Event Integration   | ✅     | N/A      | Modified EnrollmentServiceImpl, ProgressServiceImpl |
| Tests               | ✅     | N/A      | 2 files                                             |

\*Event classes are async listeners with try-catch, covered indirectly via service tests

---

## 10. Dependencies

### 10.1. Backend Dependencies

```gradle
// build.gradle
dependencies {
    // WebSocket
    implementation 'org.springframework.boot:spring-boot-starter-websocket'

    // JSON processing (already included)
    implementation 'com.fasterxml.jackson.core:jackson-databind'
}
```

### 10.2. Frontend Dependencies

**Web (package.json)**:

```json
{
  "dependencies": {
    "@stomp/stompjs": "^7.0.0",
    "sockjs-client": "^1.6.1"
  }
}
```

**Mobile (package.json)**:

```json
{
  "dependencies": {
    "@stomp/stompjs": "^7.0.0",
    "sockjs-client": "^1.6.1"
  }
}
```

---

## 11. Security Considerations

| Concern                        | Mitigation                             |
| ------------------------------ | -------------------------------------- |
| Unauthorized access            | JWT validation on WebSocket connect    |
| Cross-user notification access | Verify user ownership in all endpoints |
| Rate limiting                  | Limit notification creation per user   |
| XSS in notification content    | Sanitize HTML in messages              |
| DoS via WebSocket              | Connection limits per user             |

---

## 12. Performance Considerations

| Metric                           | Target  | Strategy                      |
| -------------------------------- | ------- | ----------------------------- |
| Notification delivery            | <500ms  | WebSocket push                |
| API response time                | <200ms  | Indexed queries               |
| Concurrent WebSocket connections | 10,000+ | Connection pooling            |
| Storage cleanup                  | Daily   | Scheduled job deletes expired |

---

## 13. Future Enhancements

| Enhancement                        | Priority | Sprint     |
| ---------------------------------- | -------- | ---------- |
| Push notifications (Firebase/APNs) | High     | Sprint 6-7 |
| Email notification service         | Medium   | Sprint 7   |
| Notification scheduling            | Medium   | Sprint 7-8 |
| Notification templates             | Low      | Sprint 8   |
| Analytics dashboard                | Low      | Sprint 8   |

---

## 14. Implementation Notes

### 14.1. Files Created (Backend)

```
com.lexia.backend.notification/
├── controller/
│   ├── NotificationController.java      # User notification endpoints
│   └── AdminNotificationController.java  # Admin broadcast/send endpoints
├── service/
│   ├── NotificationService.java         # Service interface
│   └── impl/
│       └── NotificationServiceImpl.java  # Implementation with scheduled cleanup
├── repository/
│   ├── NotificationRepository.java       # Custom queries for notifications
│   └── NotificationPreferencesRepository.java
├── entity/
│   ├── Notification.java                 # With NotificationType, NotificationPriority enums
│   └── NotificationPreferences.java      # With quiet hours support
├── dto/
│   ├── NotificationDTO.java
│   ├── NotificationPreferencesDTO.java
│   ├── UnreadCountDTO.java
│   ├── CreateNotificationRequest.java
│   ├── SendNotificationRequest.java
│   └── BroadcastNotificationRequest.java
├── mapper/
│   └── NotificationMapper.java
├── event/
│   ├── NotificationEvent.java           # Base abstract event
│   ├── CourseCompletedEvent.java
│   ├── LessonCompletedEvent.java
│   ├── EnrollmentConfirmedEvent.java
│   ├── StreakMilestoneEvent.java
│   ├── StreakLostEvent.java
│   ├── AchievementUnlockedEvent.java
│   ├── LevelUpEvent.java
│   └── NotificationEventListener.java   # @Async event handlers
└── websocket/
    ├── WebSocketConfig.java             # STOMP broker configuration
    └── WebSocketAuthInterceptor.java    # JWT validation for WebSocket
```

### 14.2. Database Migrations

- `V13__Create_notifications_table.sql` - Main notifications table with JSONB data
- `V14__Create_notification_preferences_table.sql` - User preferences with quiet hours

### 14.3. Key Decisions Made

1. **UUID for notification IDs** - Consistent with other entities
2. **JSONB for data column** - Flexible schema for different notification types
3. **@Scheduled cleanup** - Daily at 3 AM for expired notifications
4. **@Async event listeners** - Non-blocking notification creation
5. **SecurityContextHolder pattern** - For @AuthenticationPrincipal in controller tests
6. **Single Source of Truth for Category Mapping** - `NotificationType.getCategory()` is the only place for type-to-category mapping (DRY principle)
7. **Batch Processing for Broadcast** - Uses `saveAll()` with batch size of 100 for scalability
8. **Externalized CORS Configuration** - WebSocket allowed origins configurable via `application.properties`

---

## 15. Code Review & Optimizations (v1.2.0)

### 15.1. Issues Fixed

| Issue                                                 | Severity | Fix Applied                                                             |
| ----------------------------------------------------- | -------- | ----------------------------------------------------------------------- |
| `shouldNotify()` not called before real-time delivery | Critical | Integrated into `NotificationEventListener.createAndSendNotification()` |
| Duplicate `getCategory()` logic (DRY violation)       | Medium   | Consolidated into `NotificationType.getCategory()` enum method          |
| O(N) database queries in `broadcastNotification()`    | High     | Replaced with batch processing using `saveAll()`                        |
| Hardcoded CORS origins in `WebSocketConfig`           | Low      | Externalized to `application.properties`                                |

### 15.2. Performance Optimizations

**Before (Broadcast):**

```java
// O(2N+1) queries - User lookup + Save for each user
for (UUID userId : activeUserIds) {
    User user = userRepository.findById(userId); // N queries
    notificationRepository.save(notification);   // N queries
}
```

**After (Broadcast):**

```java
// O(N/100 + 1) queries - Batch processing
int batchSize = 100;
List<Notification> batch = new ArrayList<>(batchSize);

for (UUID userId : activeUserIds) {
    User userRef = new User();
    userRef.setId(userId); // Reference only, no DB lookup
    batch.add(notification);

    if (batch.size() >= batchSize) {
        notificationRepository.saveAll(batch); // Single batch insert
        batch.clear();
    }
}
```

### 15.3. Logic Flow (Updated)

```
Event Triggered
     │
     ▼
NotificationEventListener.handleXxxEvent()
     │
     ▼
createAndSendNotification()
     │
     ├─► notificationService.createNotification()  ──► Always persist to DB
     │
     ├─► Check: event.isRealTime()?
     │        │
     │        No ──► END (notification saved, no WebSocket)
     │        │
     │        Yes
     │         │
     │         ▼
     └─► notificationService.shouldNotify(userId, request)
              │
              ├─► Check: inAppEnabled?
              │        No ──► END (skip real-time)
              │
              ├─► Check: isInQuietHours()?
              │        Yes ──► END (skip real-time)
              │
              ├─► Check: isCategoryEnabled()?
              │        No ──► END (skip real-time)
              │
              └─► All checks passed
                       │
                       ▼
              sendRealTimeNotification() ──► WebSocket Push
```

### 15.4. Scalability Considerations

| Scenario             | Current Capacity           | Future Enhancement                                  |
| -------------------- | -------------------------- | --------------------------------------------------- |
| Concurrent WebSocket | ~10,000 (SimpleBroker)     | External Broker (RabbitMQ/Redis) for multi-instance |
| Broadcast to users   | ~50,000 (batch processing) | Pagination/Stream for millions                      |
| Memory footprint     | Moderate                   | Stream-based user ID fetching                       |

---

## 16. Event Integration (v1.3.0) - November 30, 2025

### 16.1. Services Modified

The following services were updated to publish notification events:

**EnrollmentServiceImpl.java**:

- Added `ApplicationEventPublisher` injection
- Publishes `EnrollmentConfirmedEvent` when user enrolls in a course
- Publishes `CourseCompletedEvent` when enrollment progress reaches 100%

**ProgressServiceImpl.java**:

- Added `ApplicationEventPublisher` injection
- Publishes `LessonCompletedEvent` when user completes a lesson

### 16.2. Event Type Fixes

Fixed courseId type mismatch (UUID → Long) in:

- `CourseCompletedEvent.java` - courseId changed from UUID to Long
- `LessonCompletedEvent.java` - courseId changed from UUID to Long

### 16.3. Test Updates

Updated tests to mock `ApplicationEventPublisher`:

- `EnrollmentServiceTest.java` - Added mock for eventPublisher, verified event publishing
- `ProgressServiceTest.java` - Added mock for eventPublisher

### 16.4. Event Flow

```
User Action                     Service Method                  Event Published
───────────────────────────────────────────────────────────────────────────────
Enroll in course       →  EnrollmentServiceImpl.enroll()  →  EnrollmentConfirmedEvent
Complete a lesson      →  ProgressServiceImpl.completeLesson() → LessonCompletedEvent
Complete all lessons   →  EnrollmentServiceImpl.updateEnrollmentProgress() → CourseCompletedEvent
```

---

**Document Owner**: LEXIA Development Team  
**Review Date**: ~~Before Sprint 5 Planning~~ Completed  
**Last Implementation**: November 30, 2025  
**Code Review**: November 30, 2025 (v1.2.0)  
**Event Integration**: November 30, 2025 (v1.3.0)  
**Next Update**: After frontend integration
