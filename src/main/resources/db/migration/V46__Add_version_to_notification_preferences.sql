-- V46: Add version column to notification_preferences for optimistic locking
ALTER TABLE notification_preferences ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Ensure all existing rows have version 0
UPDATE notification_preferences SET version = 0 WHERE version IS NULL;
