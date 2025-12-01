-- V17: Add thumbnail_file_id foreign key to courses table
-- Author: LEXIA Team
-- Date: December 1, 2025
-- Sprint: 5, Phase 4: Course/Lesson Media Upload

-- Add thumbnail_file_id column as FK to files table
-- Keep existing thumbnail_url for backward compatibility with external URLs
ALTER TABLE courses
ADD COLUMN thumbnail_file_id UUID REFERENCES files(id) ON DELETE SET NULL;

-- Create index for efficient joins
CREATE INDEX idx_courses_thumbnail_file ON courses(thumbnail_file_id);

-- Comments for documentation
COMMENT ON COLUMN courses.thumbnail_file_id IS 'Foreign key to files table for uploaded thumbnails. If set, takes precedence over thumbnail_url';
