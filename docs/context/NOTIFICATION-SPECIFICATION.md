# LEXIA - Notification System Specification

**Version**: 1.0.0  
**Created**: November 28, 2025  
**Status**: 📝 Planning  
**Target Sprint**: Sprint 5 or 6

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
-- V12__Create_notifications_table.sql
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
-- V13__Create_notification_preferences_table.sql
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
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "https://lexia.app")
                .withSockJS(); // Fallback for browsers without WebSocket
    }
}
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

### 9.1. Phase 1: Core Infrastructure (3 pts)

- [ ] Create database migrations (V12, V13)
- [ ] Create JPA entities (Notification, NotificationPreferences)
- [ ] Create repositories with custom queries
- [ ] Create DTOs and mappers
- [ ] Implement NotificationService

### 9.2. Phase 2: REST API (2 pts)

- [ ] Create NotificationController
- [ ] Add Swagger documentation
- [ ] Add request validation
- [ ] Write controller tests

### 9.3. Phase 3: WebSocket (2.5 pts)

- [ ] Add Spring WebSocket dependencies
- [ ] Configure WebSocket broker
- [ ] Implement WebSocket security
- [ ] Create notification handler
- [ ] Test WebSocket connections

### 9.4. Phase 4: Event Integration (1.5 pts)

- [ ] Create notification events
- [ ] Implement event listeners
- [ ] Integrate with existing services (enrollment, progress)
- [ ] Test event-driven notifications

### 9.5. Phase 5: Frontend Integration (3 pts)

- [ ] Web: Notification store, components, WebSocket client
- [ ] Mobile: Notification hook, components, WebSocket client
- [ ] Admin: Broadcast notification UI

**Total Estimated Points**: 12 pts (1-2 sprints)

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

**Document Owner**: LEXIA Development Team  
**Review Date**: Before Sprint 5 Planning  
**Next Update**: After implementation begins
