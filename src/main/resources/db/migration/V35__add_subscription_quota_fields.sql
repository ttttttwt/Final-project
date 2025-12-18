-- V35: Add subscription-based quota fields to user_ai_quotas
-- This migration adds fields to support Free/Pro tier quota limits

-- Add subscription-based quota fields
ALTER TABLE user_ai_quotas 
ADD COLUMN IF NOT EXISTS plan_type VARCHAR(20) DEFAULT 'FREE',
ADD COLUMN IF NOT EXISTS quota_reset_date DATE,
ADD COLUMN IF NOT EXISTS roleplay_sessions_used INTEGER DEFAULT 0 NOT NULL,
ADD COLUMN IF NOT EXISTS flashcard_decks_used INTEGER DEFAULT 0 NOT NULL,
ADD COLUMN IF NOT EXISTS grammar_exercises_used INTEGER DEFAULT 0 NOT NULL;

-- Initialize quota reset date for existing users (1 month from now)
UPDATE user_ai_quotas 
SET quota_reset_date = CURRENT_DATE + INTERVAL '1 month'
WHERE quota_reset_date IS NULL;

-- Sync plan type from subscriptions table for users with active subscriptions
UPDATE user_ai_quotas q
SET plan_type = COALESCE(
    (SELECT s.plan_type FROM subscriptions s 
     WHERE s.user_id = q.user_id AND s.status = 'ACTIVE'),
    'FREE'
);

-- Update is_premium flag to match plan type
UPDATE user_ai_quotas
SET is_premium = (plan_type IN ('MONTHLY', 'YEARLY'))
WHERE plan_type IS NOT NULL;

-- Create index for quota lookups by plan type
CREATE INDEX IF NOT EXISTS idx_user_ai_quotas_plan_type 
ON user_ai_quotas(plan_type);

-- Create index for quota reset date checks
CREATE INDEX IF NOT EXISTS idx_user_ai_quotas_reset_date 
ON user_ai_quotas(quota_reset_date);

COMMENT ON COLUMN user_ai_quotas.plan_type IS 'User subscription plan type: FREE, MONTHLY, YEARLY';
COMMENT ON COLUMN user_ai_quotas.quota_reset_date IS 'Date when monthly quota resets, based on subscription start';
COMMENT ON COLUMN user_ai_quotas.roleplay_sessions_used IS 'Number of roleplay sessions used this month';
COMMENT ON COLUMN user_ai_quotas.flashcard_decks_used IS 'Number of flashcard decks generated this month';
COMMENT ON COLUMN user_ai_quotas.grammar_exercises_used IS 'Number of grammar exercises generated this month';
