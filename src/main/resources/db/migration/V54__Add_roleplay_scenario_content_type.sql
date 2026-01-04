-- V54__Add_roleplay_scenario_content_type.sql
-- Add roleplay_scenario to ai_usage_logs content_type check constraint
-- This tracks scenario generation separately from roleplay sessions

-- Drop the existing constraint
ALTER TABLE ai_usage_logs DROP CONSTRAINT IF EXISTS chk_ai_usage_content_type;

-- Add updated constraint with roleplay_scenario
ALTER TABLE ai_usage_logs ADD CONSTRAINT chk_ai_usage_content_type 
    CHECK (content_type IN ('roleplay', 'grammar', 'flashcard', 'content_generation', 
                            'pronunciation_feedback', 'magic_flashcard', 'grammar_sandbox',
                            'flashcard_image', 'custom_material', 'cm_content_generation',
                            'roleplay_scenario'));
