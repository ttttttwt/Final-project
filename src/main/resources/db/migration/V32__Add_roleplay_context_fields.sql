-- V32: Add context fields to roleplay_scenarios table
-- Adds: suggested_prompts, context_details, agenda columns

ALTER TABLE roleplay_scenarios
ADD COLUMN suggested_prompts TEXT,
ADD COLUMN context_details TEXT,
ADD COLUMN agenda TEXT;

COMMENT ON COLUMN roleplay_scenarios.suggested_prompts IS 'JSON array of suggested prompts for the user';
COMMENT ON COLUMN roleplay_scenarios.context_details IS 'JSON object with setting, situation, keyInfo, yourGoal, tips';
COMMENT ON COLUMN roleplay_scenarios.agenda IS 'JSON array of agenda/discussion topics';
