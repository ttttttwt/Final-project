-- V21: Add granular email preferences to notification_preferences table
-- Extends the existing notification_preferences with email-specific settings

-- Add email-specific preference columns
ALTER TABLE notification_preferences
ADD COLUMN email_verification_enabled BOOLEAN DEFAULT true,
ADD COLUMN email_security_alerts_enabled BOOLEAN DEFAULT true,
ADD COLUMN email_weekly_digest_enabled BOOLEAN DEFAULT true,
ADD COLUMN email_streak_reminders_enabled BOOLEAN DEFAULT true,
ADD COLUMN email_achievements_enabled BOOLEAN DEFAULT true,
ADD COLUMN email_course_updates_enabled BOOLEAN DEFAULT true,
ADD COLUMN email_announcements_enabled BOOLEAN DEFAULT true,
ADD COLUMN unsubscribe_token VARCHAR(255) UNIQUE;

-- Comments for new columns
COMMENT ON COLUMN notification_preferences.email_verification_enabled IS 'Receive email verification emails (required, cannot be disabled)';
COMMENT ON COLUMN notification_preferences.email_security_alerts_enabled IS 'Receive security alert emails (password changes, new device logins)';
COMMENT ON COLUMN notification_preferences.email_weekly_digest_enabled IS 'Receive weekly progress digest emails';
COMMENT ON COLUMN notification_preferences.email_streak_reminders_enabled IS 'Receive streak reminder emails when streak is at risk';
COMMENT ON COLUMN notification_preferences.email_achievements_enabled IS 'Receive achievement notification emails (level up, milestones)';
COMMENT ON COLUMN notification_preferences.email_course_updates_enabled IS 'Receive course-related emails (enrollment, completion)';
COMMENT ON COLUMN notification_preferences.email_announcements_enabled IS 'Receive system announcement emails';
COMMENT ON COLUMN notification_preferences.unsubscribe_token IS 'Cryptographically signed token for one-click unsubscribe links';

-- Create index for unsubscribe token lookup
CREATE INDEX idx_notification_preferences_unsubscribe_token 
ON notification_preferences(unsubscribe_token) 
WHERE unsubscribe_token IS NOT NULL;
