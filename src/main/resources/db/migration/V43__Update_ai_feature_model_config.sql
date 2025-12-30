-- Update existing AI feature model configurations to use the correct model
-- The old model 'gemini-1.5-pro' is not available in the v1beta API
-- Replace with the model configured in application.properties: gemini-2.5-flash-lite
-- This migration is safe even if the table doesn't exist yet (created by app startup)

DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'ai_config') THEN
        UPDATE ai_config 
        SET config_value = 'gemini-2.5-flash-lite' 
        WHERE config_key LIKE 'feature.%.model' 
          AND config_value = 'gemini-1.5-pro';
    END IF;
END $$;
