-- V48__Add_flashcard_image_content_type.sql
-- Sprint 6 - Add flashcard_image to ai_usage_logs content_type check constraint

-- First, fix any existing rows that might have invalid content_type values
-- (In case the application already inserted 'flashcard_image' before constraint was updated)
UPDATE ai_usage_logs SET content_type = 'flashcard' WHERE content_type = 'flashcard_image';
UPDATE ai_usage_logs SET content_type = 'flashcard' WHERE content_type = 'custom_material';
UPDATE ai_usage_logs SET content_type = 'flashcard' WHERE content_type NOT IN 
    ('roleplay', 'grammar', 'flashcard', 'content_generation', 
     'pronunciation_feedback', 'magic_flashcard', 'grammar_sandbox');

-- Drop the existing constraint
ALTER TABLE ai_usage_logs DROP CONSTRAINT IF EXISTS chk_ai_usage_content_type;

-- Add updated constraint with flashcard_image and custom_material
ALTER TABLE ai_usage_logs ADD CONSTRAINT chk_ai_usage_content_type 
    CHECK (content_type IN ('roleplay', 'grammar', 'flashcard', 'content_generation', 
                            'pronunciation_feedback', 'magic_flashcard', 'grammar_sandbox',
                            'flashcard_image', 'custom_material'));
