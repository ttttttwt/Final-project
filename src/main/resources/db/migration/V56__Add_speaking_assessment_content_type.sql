-- V56__Add_speaking_assessment_content_type.sql
-- Add speaking_assessment to ai_usage_logs content_type check constraint
-- This tracks AI-powered speaking lesson assessments (speech-to-text, pronunciation scoring, feedback)

-- Drop the existing constraint
ALTER TABLE ai_usage_logs DROP CONSTRAINT IF EXISTS chk_ai_usage_content_type;

-- Add updated constraint with speaking_assessment
ALTER TABLE ai_usage_logs ADD CONSTRAINT chk_ai_usage_content_type 
    CHECK (content_type IN ('roleplay', 'grammar', 'flashcard', 'content_generation', 
                            'pronunciation_feedback', 'magic_flashcard', 'grammar_sandbox',
                            'flashcard_image', 'custom_material', 'cm_content_generation',
                            'roleplay_scenario', 'speaking_assessment'));
