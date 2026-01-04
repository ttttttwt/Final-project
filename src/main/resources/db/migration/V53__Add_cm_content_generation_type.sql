-- V53__Add_cm_content_generation_type.sql
-- Add cm_content_generation to ai_usage_logs content_type check constraint

-- Drop the existing constraint
ALTER TABLE ai_usage_logs DROP CONSTRAINT IF EXISTS chk_ai_usage_content_type;

-- Add updated constraint with cm_content_generation
ALTER TABLE ai_usage_logs ADD CONSTRAINT chk_ai_usage_content_type 
    CHECK (content_type IN ('roleplay', 'grammar', 'flashcard', 'content_generation', 
                            'pronunciation_feedback', 'magic_flashcard', 'grammar_sandbox',
                            'flashcard_image', 'custom_material', 'cm_content_generation'));
