-- V51__Add_revoked_column_to_refresh_tokens.sql
-- Add revoked column to refresh_tokens table

-- Add revoked column with default false
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS revoked BOOLEAN NOT NULL DEFAULT false;

-- Create index for querying active tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked ON refresh_tokens(revoked);
