-- V42: Add custom_materials_used to user_ai_quotas
-- This migration adds a field to track usage of the Custom Materials feature

ALTER TABLE user_ai_quotas 
ADD COLUMN IF NOT EXISTS custom_materials_used INTEGER DEFAULT 0 NOT NULL;

COMMENT ON COLUMN user_ai_quotas.custom_materials_used IS 'Number of custom materials generated this month';
