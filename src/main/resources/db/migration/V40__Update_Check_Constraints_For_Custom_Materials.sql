-- V40: Update check constraints for custom materials
-- Fixes missing enum values in database constraints that cause DataIntegrityViolationException

-- 1. Update valid_type constraint on notifications table
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS valid_type;

ALTER TABLE notifications ADD CONSTRAINT valid_type CHECK (type IN (
    -- Original types
    'COURSE_PUBLISHED', 'LESSON_ADDED', 'ENROLLMENT_CONFIRMED',
    'LESSON_COMPLETED', 'COURSE_COMPLETED', 'ACHIEVEMENT_UNLOCKED',
    'STREAK_REMINDER', 'STREAK_LOST', 'STREAK_MILESTONE',
    'LEVEL_UP', 'SYSTEM_ANNOUNCEMENT', 'MAINTENANCE_NOTICE',
    -- Custom Material types
    'CUSTOM_MATERIAL_READY', 'CUSTOM_MATERIAL_FAILED'
));

-- 2. Update chk_ai_usage_content_type constraint on ai_usage_logs table
ALTER TABLE ai_usage_logs DROP CONSTRAINT IF EXISTS chk_ai_usage_content_type;

ALTER TABLE ai_usage_logs ADD CONSTRAINT chk_ai_usage_content_type 
    CHECK (content_type IN (
        -- Original types
        'roleplay', 'grammar', 'flashcard', 'content_generation', 
        'pronunciation_feedback', 'magic_flashcard', 'grammar_sandbox',
        -- Custom Material types
        'cm_pdf_extraction', 'cm_docx_extraction', 'cm_image_ocr', 
        'cm_youtube_transcript', 'cm_website_extraction', 
        'cm_text_input', 'cm_content_generation'
    ));
