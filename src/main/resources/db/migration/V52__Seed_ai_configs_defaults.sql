-- V52__Seed_ai_configs_defaults.sql
-- Seed default AI configuration values

INSERT INTO ai_configs (config_key, config_value, description, is_encrypted, updated_at) VALUES
('global.enabled', 'true', 'Enable/disable AI features globally', false, NOW()),
('global.monthlyBudgetLimit', '100.0', 'Monthly budget limit in USD', false, NOW()),
('global.alertThresholdPercentage', '80', 'Alert when budget reaches this percentage', false, NOW()),
('global.fallbackEnabled', 'false', 'Enable fallback model when primary fails', false, NOW()),
('global.rateLimitEnabled', 'true', 'Enable rate limiting for AI requests', false, NOW()),
('global.costPerInputToken', '0.000001', 'Cost per input token in USD', false, NOW()),
('global.costPerOutputToken', '0.000002', 'Cost per output token in USD', false, NOW()),
('global.defaultDailyLimit', '100', 'Default daily request limit per user', false, NOW()),
('global.defaultMonthlyLimit', '3000', 'Default monthly request limit per user', false, NOW()),
('global.costPerToken', '0.00001', 'Average cost per token in USD', false, NOW())
ON CONFLICT (config_key) DO NOTHING;
