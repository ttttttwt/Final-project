-- V13: Create notifications table for user notifications
-- This table stores all user notifications for learning events and system alerts

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

-- Comments
COMMENT ON TABLE notifications IS 'User notifications for learning events and system alerts';
COMMENT ON COLUMN notifications.type IS 'Type of notification (COURSE_PUBLISHED, ACHIEVEMENT_UNLOCKED, etc.)';
COMMENT ON COLUMN notifications.data IS 'Additional payload - JSON object with type-specific data';
COMMENT ON COLUMN notifications.priority IS 'Priority level: HIGH, NORMAL, LOW';
COMMENT ON COLUMN notifications.expires_at IS 'Auto-cleanup: notifications older than this are deleted';
