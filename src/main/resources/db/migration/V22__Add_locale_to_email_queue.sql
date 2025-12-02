-- V22__Add_locale_to_email_queue.sql
-- Add locale column to email_queue table for i18n template rendering support

ALTER TABLE email_queue
ADD COLUMN locale VARCHAR(10) DEFAULT 'en';

-- Add comment for documentation
COMMENT ON COLUMN email_queue.locale IS 'Locale for email template rendering (e.g., en, vi). Defaults to English.';
