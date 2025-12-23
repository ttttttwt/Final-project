-- V41: Update valid_category constraint on files table
-- Add CUSTOM_MATERIAL to allowed categories

ALTER TABLE files DROP CONSTRAINT IF EXISTS valid_category;

ALTER TABLE files ADD CONSTRAINT valid_category CHECK (category IN (
    'AVATAR', 'COURSE_THUMBNAIL', 'LESSON_AUDIO',
    'LESSON_IMAGE', 'DOCUMENT', 'CERTIFICATE', 'CUSTOM_MATERIAL'
));
