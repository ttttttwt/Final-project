-- V50__Add_soft_delete_columns_to_users.sql
-- Add soft delete support to users table

-- Add is_deleted column with default false
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false;

-- Add deleted_at column (nullable, only set when user is deleted)
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Create index for querying non-deleted users
CREATE INDEX IF NOT EXISTS idx_users_is_deleted ON users(is_deleted);
