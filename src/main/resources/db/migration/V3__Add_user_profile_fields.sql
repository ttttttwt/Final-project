-- V3__Add_user_profile_fields.sql
-- Add additional profile fields for user profile management

-- Add new columns to user_profiles table
ALTER TABLE user_profiles 
    ADD COLUMN first_name VARCHAR(100),
    ADD COLUMN last_name VARCHAR(100),
    ADD COLUMN bio VARCHAR(500),
    ADD COLUMN phone_number VARCHAR(20),
    ADD COLUMN timezone VARCHAR(50) DEFAULT 'UTC',
    ADD COLUMN language VARCHAR(10) DEFAULT 'en',
    ADD COLUMN created_at TIMESTAMPTZ DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMPTZ DEFAULT NOW();

-- Update full_name to nullable (we'll use firstName + lastName instead)
ALTER TABLE user_profiles ALTER COLUMN full_name DROP NOT NULL;

-- Create index for better query performance
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);

-- Add comment for documentation
COMMENT ON COLUMN user_profiles.bio IS 'User bio, max 500 characters';
COMMENT ON COLUMN user_profiles.phone_number IS 'User phone number, optional';
COMMENT ON COLUMN user_profiles.timezone IS 'IANA timezone identifier (e.g., Asia/Ho_Chi_Minh)';
COMMENT ON COLUMN user_profiles.language IS 'ISO 639-1 language code (e.g., en, vi)';
