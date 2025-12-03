-- V22__Add_locale_to_email_queue.sql
-- Add locale column to email_queue table for i18n template rendering support

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'email_queue' AND column_name = 'locale'
    ) THEN
        ALTER TABLE email_queue ADD COLUMN locale VARCHAR(10) DEFAULT 'en';
    END IF;
END
$$;

-- Add comment for documentation
COMMENT ON COLUMN email_queue.locale IS 'Locale for email template rendering (e.g., en, vi). Defaults to English.';
