-- V21: Add granular email preferences to notification_preferences table
-- Extends the existing notification_preferences with email-specific settings

-- Ensure notification_preferences table exists (safety check)
CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    in_app_enabled BOOLEAN DEFAULT true,
    email_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,
    learning_enabled BOOLEAN DEFAULT true,
    achievements_enabled BOOLEAN DEFAULT true,
    reminders_enabled BOOLEAN DEFAULT true,
    system_enabled BOOLEAN DEFAULT true,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    quiet_hours_timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Add email-specific preference columns
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_verification_enabled') THEN
        ALTER TABLE notification_preferences ADD COLUMN email_verification_enabled BOOLEAN DEFAULT true;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_security_alerts_enabled') THEN
        ALTER TABLE notification_preferences ADD COLUMN email_security_alerts_enabled BOOLEAN DEFAULT true;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_weekly_digest_enabled') THEN
        ALTER TABLE notification_preferences ADD COLUMN email_weekly_digest_enabled BOOLEAN DEFAULT true;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_streak_reminders_enabled') THEN
        ALTER TABLE notification_preferences ADD COLUMN email_streak_reminders_enabled BOOLEAN DEFAULT true;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_achievements_enabled') THEN
        ALTER TABLE notification_preferences ADD COLUMN email_achievements_enabled BOOLEAN DEFAULT true;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_course_updates_enabled') THEN
        ALTER TABLE notification_preferences ADD COLUMN email_course_updates_enabled BOOLEAN DEFAULT true;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_announcements_enabled') THEN
        ALTER TABLE notification_preferences ADD COLUMN email_announcements_enabled BOOLEAN DEFAULT true;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'unsubscribe_token') THEN
        ALTER TABLE notification_preferences ADD COLUMN unsubscribe_token VARCHAR(255) UNIQUE;
    END IF;
END
$$;

-- Comments for new columns (wrapped in DO block to handle missing columns gracefully)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_verification_enabled') THEN
        COMMENT ON COLUMN notification_preferences.email_verification_enabled IS 'Receive email verification emails (required, cannot be disabled)';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_security_alerts_enabled') THEN
        COMMENT ON COLUMN notification_preferences.email_security_alerts_enabled IS 'Receive security alert emails (password changes, new device logins)';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_weekly_digest_enabled') THEN
        COMMENT ON COLUMN notification_preferences.email_weekly_digest_enabled IS 'Receive weekly progress digest emails';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_streak_reminders_enabled') THEN
        COMMENT ON COLUMN notification_preferences.email_streak_reminders_enabled IS 'Receive streak reminder emails when streak is at risk';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_achievements_enabled') THEN
        COMMENT ON COLUMN notification_preferences.email_achievements_enabled IS 'Receive achievement notification emails (level up, milestones)';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_course_updates_enabled') THEN
        COMMENT ON COLUMN notification_preferences.email_course_updates_enabled IS 'Receive course-related emails (enrollment, completion)';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'email_announcements_enabled') THEN
        COMMENT ON COLUMN notification_preferences.email_announcements_enabled IS 'Receive system announcement emails';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'notification_preferences' AND column_name = 'unsubscribe_token') THEN
        COMMENT ON COLUMN notification_preferences.unsubscribe_token IS 'Cryptographically signed token for one-click unsubscribe links';
    END IF;
END
$$;

-- Create index for unsubscribe token lookup
CREATE INDEX IF NOT EXISTS idx_notification_preferences_unsubscribe_token 
ON notification_preferences(unsubscribe_token) 
WHERE unsubscribe_token IS NOT NULL;
