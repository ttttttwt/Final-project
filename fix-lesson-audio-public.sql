-- Fix existing LESSON_AUDIO files to be public
-- Run this SQL command in your PostgreSQL database

UPDATE files 
SET is_public = true 
WHERE category = 'LESSON_AUDIO';

-- Verify the update
SELECT id, original_filename, category, is_public 
FROM files 
WHERE category = 'LESSON_AUDIO';
