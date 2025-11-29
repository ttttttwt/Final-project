-- V14: Create notification_preferences table for user notification settings
-- This table stores user preferences for notification delivery

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

-- Comments
COMMENT ON TABLE notification_preferences IS 'User preferences for notification delivery';
COMMENT ON COLUMN notification_preferences.in_app_enabled IS 'Enable in-app notifications';
COMMENT ON COLUMN notification_preferences.email_enabled IS 'Enable email notifications';
COMMENT ON COLUMN notification_preferences.push_enabled IS 'Enable push notifications';
COMMENT ON COLUMN notification_preferences.learning_enabled IS 'Receive learning-related notifications';
COMMENT ON COLUMN notification_preferences.achievements_enabled IS 'Receive achievement notifications';
COMMENT ON COLUMN notification_preferences.reminders_enabled IS 'Receive reminder notifications';
COMMENT ON COLUMN notification_preferences.system_enabled IS 'Receive system announcements';
COMMENT ON COLUMN notification_preferences.quiet_hours_start IS 'Start time for quiet hours (no notifications)';
COMMENT ON COLUMN notification_preferences.quiet_hours_end IS 'End time for quiet hours';
COMMENT ON COLUMN notification_preferences.quiet_hours_timezone IS 'Timezone for quiet hours';
