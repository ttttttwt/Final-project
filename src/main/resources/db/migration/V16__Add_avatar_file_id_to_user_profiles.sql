-- V16__Add_avatar_file_id_to_user_profiles.sql
-- Add avatar_file_id column to user_profiles table for file upload support

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'user_profiles' AND column_name = 'avatar_file_id'
    ) THEN
        ALTER TABLE user_profiles
        ADD COLUMN avatar_file_id UUID REFERENCES files(id) ON DELETE SET NULL;
    END IF;
END
$$;

-- Keep avatar_url for backward compatibility (external URLs)
-- New uploads will use avatar_file_id

CREATE INDEX IF NOT EXISTS idx_user_profiles_avatar_file ON user_profiles(avatar_file_id);

COMMENT ON COLUMN user_profiles.avatar_file_id IS 'Reference to uploaded avatar file, used when avatar is uploaded directly';
