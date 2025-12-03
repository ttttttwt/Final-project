-- V18: Add audio_file_id foreign key to lessons table
-- Author: LEXIA Team
-- Date: December 1, 2025
-- Sprint: 5, Phase 4: Course/Lesson Media Upload

-- Add audio_file_id column as FK to files table
-- Keep existing content.audioUrl in JSONB for backward compatibility
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'lessons' AND column_name = 'audio_file_id'
    ) THEN
        ALTER TABLE lessons
        ADD COLUMN audio_file_id UUID REFERENCES files(id) ON DELETE SET NULL;
    END IF;
END
$$;

-- Create index for efficient joins
CREATE INDEX IF NOT EXISTS idx_lessons_audio_file ON lessons(audio_file_id);

-- Comments for documentation
COMMENT ON COLUMN lessons.audio_file_id IS 'Foreign key to files table for uploaded audio (LISTENING lessons). If set, takes precedence over content.audioUrl';
