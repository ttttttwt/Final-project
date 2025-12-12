-- V23__add_ai_usage_tracking_enhanced.sql
-- Sprint 5 - Task A4: Enhanced AI Usage Tracking
-- Add comprehensive AI usage tracking with quotas and analytics support

-- ============================================================================
-- SECTION 1: Enhance existing ai_usage_logs table (from V11)
-- ============================================================================

-- Add missing columns to ai_usage_logs for comprehensive tracking
ALTER TABLE ai_usage_logs ADD COLUMN IF NOT EXISTS content_type VARCHAR(50);
ALTER TABLE ai_usage_logs ADD COLUMN IF NOT EXISTS model_id VARCHAR(100);
-- Fix #2: Use COALESCE to handle NULL values in generated column
ALTER TABLE ai_usage_logs ADD COLUMN IF NOT EXISTS total_tokens INTEGER GENERATED ALWAYS AS (COALESCE(input_tokens, 0) + COALESCE(output_tokens, 0)) STORED;
ALTER TABLE ai_usage_logs ADD COLUMN IF NOT EXISTS response_time_ms INTEGER;
ALTER TABLE ai_usage_logs ADD COLUMN IF NOT EXISTS success BOOLEAN DEFAULT true;
ALTER TABLE ai_usage_logs ADD COLUMN IF NOT EXISTS error_message TEXT;
ALTER TABLE ai_usage_logs ADD COLUMN IF NOT EXISTS request_metadata JSONB DEFAULT '{}';
ALTER TABLE ai_usage_logs ADD COLUMN IF NOT EXISTS prompt_version VARCHAR(50);

-- Rename feature_name to content_type if it exists (for consistency with spec)
-- Note: We keep both columns for backward compatibility
UPDATE ai_usage_logs SET content_type = LOWER(feature_name) WHERE content_type IS NULL AND feature_name IS NOT NULL;

-- Add constraint for content_type values (standardized lowercase)
ALTER TABLE ai_usage_logs DROP CONSTRAINT IF EXISTS chk_ai_usage_content_type;
ALTER TABLE ai_usage_logs ADD CONSTRAINT chk_ai_usage_content_type 
    CHECK (content_type IN ('roleplay', 'grammar', 'flashcard', 'content_generation', 
                            'pronunciation_feedback', 'magic_flashcard', 'grammar_sandbox'));

-- Add additional indexes for new columns
CREATE INDEX IF NOT EXISTS idx_ai_usage_logs_content_type ON ai_usage_logs(content_type);
CREATE INDEX IF NOT EXISTS idx_ai_usage_logs_success ON ai_usage_logs(success);
CREATE INDEX IF NOT EXISTS idx_ai_usage_logs_model_id ON ai_usage_logs(model_id);
CREATE INDEX IF NOT EXISTS idx_ai_usage_logs_user_date ON ai_usage_logs(user_id, created_at);
-- Fix #5: Add composite index for date-based aggregations in views
CREATE INDEX IF NOT EXISTS idx_ai_usage_logs_date_type ON ai_usage_logs(created_at, content_type);

-- Fix #1: Safe column rename using DO block
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ai_usage_logs' AND column_name = 'cost'
    ) THEN
        ALTER TABLE ai_usage_logs RENAME COLUMN cost TO estimated_cost_usd;
    END IF;
END $$;

-- Add comments for new columns
COMMENT ON COLUMN ai_usage_logs.content_type IS 'Type of AI content: roleplay, grammar, flashcard';
COMMENT ON COLUMN ai_usage_logs.model_id IS 'AI model identifier (e.g., gemini-1.5-flash, gemini-1.5-pro)';
COMMENT ON COLUMN ai_usage_logs.total_tokens IS 'Auto-calculated: input_tokens + output_tokens';
COMMENT ON COLUMN ai_usage_logs.response_time_ms IS 'API response time in milliseconds';
COMMENT ON COLUMN ai_usage_logs.success IS 'Whether the AI request succeeded';
COMMENT ON COLUMN ai_usage_logs.error_message IS 'Error message if request failed';
COMMENT ON COLUMN ai_usage_logs.request_metadata IS 'JSONB: prompt_version, cefr_level, domain, scenario_id, etc.';
COMMENT ON COLUMN ai_usage_logs.prompt_version IS 'Version of the prompt template used';

-- ============================================================================
-- SECTION 2: Create user_ai_quotas table
-- ============================================================================

CREATE TABLE IF NOT EXISTS user_ai_quotas (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    
    -- Daily limits
    daily_limit INTEGER NOT NULL DEFAULT 50,
    daily_used INTEGER NOT NULL DEFAULT 0,
    last_reset_daily TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Monthly limits  
    monthly_limit INTEGER NOT NULL DEFAULT 500,
    monthly_used INTEGER NOT NULL DEFAULT 0,
    last_reset_monthly TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Premium status
    is_premium BOOLEAN NOT NULL DEFAULT false,
    premium_multiplier DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    
    -- Suspension status
    suspended BOOLEAN NOT NULL DEFAULT false,
    suspension_reason TEXT,
    
    -- Feature-specific limits (JSONB for flexibility)
    feature_limits JSONB DEFAULT '{
        "roleplay": {"daily": 20, "monthly": 200},
        "grammar": {"daily": 30, "monthly": 300},
        "flashcard": {"daily": 50, "monthly": 500}
    }'::jsonb,
    
    -- Feature-specific usage (JSONB for flexibility)
    feature_usage JSONB DEFAULT '{
        "roleplay": {"daily": 0, "monthly": 0},
        "grammar": {"daily": 0, "monthly": 0},
        "flashcard": {"daily": 0, "monthly": 0}
    }'::jsonb,
    
    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT chk_daily_limit_positive CHECK (daily_limit > 0),
    CONSTRAINT chk_monthly_limit_positive CHECK (monthly_limit > 0),
    CONSTRAINT chk_daily_used_non_negative CHECK (daily_used >= 0),
    CONSTRAINT chk_monthly_used_non_negative CHECK (monthly_used >= 0),
    CONSTRAINT chk_premium_multiplier_range CHECK (premium_multiplier >= 1.00 AND premium_multiplier <= 10.00)
);

-- Indexes for user_ai_quotas
CREATE INDEX IF NOT EXISTS idx_user_ai_quotas_suspended ON user_ai_quotas(suspended) WHERE suspended = true;
CREATE INDEX IF NOT EXISTS idx_user_ai_quotas_premium ON user_ai_quotas(is_premium) WHERE is_premium = true;
CREATE INDEX IF NOT EXISTS idx_user_ai_quotas_reset_daily ON user_ai_quotas(last_reset_daily);
CREATE INDEX IF NOT EXISTS idx_user_ai_quotas_reset_monthly ON user_ai_quotas(last_reset_monthly);

-- Add trigger for updated_at
CREATE TRIGGER update_user_ai_quotas_updated_at
    BEFORE UPDATE ON user_ai_quotas
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments for user_ai_quotas
COMMENT ON TABLE user_ai_quotas IS 'Track AI usage quotas per user for rate limiting and cost control';
COMMENT ON COLUMN user_ai_quotas.daily_limit IS 'Maximum AI requests allowed per day';
COMMENT ON COLUMN user_ai_quotas.monthly_limit IS 'Maximum AI requests allowed per month';
COMMENT ON COLUMN user_ai_quotas.is_premium IS 'Premium users get higher limits via premium_multiplier';
COMMENT ON COLUMN user_ai_quotas.premium_multiplier IS 'Multiplier applied to limits for premium users (1.00-10.00)';
COMMENT ON COLUMN user_ai_quotas.suspended IS 'If true, user cannot use AI features';
COMMENT ON COLUMN user_ai_quotas.feature_limits IS 'Per-feature limits: {"roleplay": {"daily": 20, "monthly": 200}, ...}';
COMMENT ON COLUMN user_ai_quotas.feature_usage IS 'Per-feature usage counters: {"roleplay": {"daily": 5, "monthly": 45}, ...}';

-- ============================================================================
-- SECTION 3: Create ai_usage_daily_summary view
-- ============================================================================

CREATE OR REPLACE VIEW ai_usage_daily_summary AS
SELECT 
    DATE(created_at) AS usage_date,
    content_type,
    COUNT(*) AS total_requests,
    COUNT(DISTINCT user_id) AS unique_users,
    SUM(input_tokens) AS total_input_tokens,
    SUM(output_tokens) AS total_output_tokens,
    SUM(COALESCE(input_tokens, 0) + COALESCE(output_tokens, 0)) AS total_tokens,
    SUM(estimated_cost_usd) AS total_cost_usd,
    AVG(response_time_ms) AS avg_response_time_ms,
    COUNT(*) FILTER (WHERE success = true) AS successful_requests,
    COUNT(*) FILTER (WHERE success = false) AS failed_requests,
    ROUND(
        COUNT(*) FILTER (WHERE success = true)::NUMERIC / NULLIF(COUNT(*), 0) * 100, 
        2
    ) AS success_rate_percent
FROM ai_usage_logs
GROUP BY DATE(created_at), content_type
ORDER BY usage_date DESC, content_type;

COMMENT ON VIEW ai_usage_daily_summary IS 'Daily aggregated AI usage statistics for admin dashboard';

-- ============================================================================
-- SECTION 4: Create ai_usage_monthly_summary view
-- ============================================================================

CREATE OR REPLACE VIEW ai_usage_monthly_summary AS
SELECT 
    DATE_TRUNC('month', created_at)::DATE AS usage_month,
    content_type,
    COUNT(*) AS total_requests,
    COUNT(DISTINCT user_id) AS unique_users,
    SUM(input_tokens) AS total_input_tokens,
    SUM(output_tokens) AS total_output_tokens,
    SUM(COALESCE(input_tokens, 0) + COALESCE(output_tokens, 0)) AS total_tokens,
    SUM(estimated_cost_usd) AS total_cost_usd,
    AVG(response_time_ms) AS avg_response_time_ms,
    ROUND(
        COUNT(*) FILTER (WHERE success = true)::NUMERIC / NULLIF(COUNT(*), 0) * 100, 
        2
    ) AS success_rate_percent
FROM ai_usage_logs
GROUP BY DATE_TRUNC('month', created_at), content_type
ORDER BY usage_month DESC, content_type;

COMMENT ON VIEW ai_usage_monthly_summary IS 'Monthly aggregated AI usage statistics for cost tracking';

-- ============================================================================
-- SECTION 5: Create ai_usage_by_user view
-- ============================================================================

CREATE OR REPLACE VIEW ai_usage_by_user AS
SELECT 
    u.id AS user_id,
    up.full_name,
    u.email,
    COUNT(*) AS total_requests,
    SUM(al.input_tokens) AS total_input_tokens,
    SUM(al.output_tokens) AS total_output_tokens,
    SUM(al.estimated_cost_usd) AS total_cost_usd,
    COUNT(*) FILTER (WHERE al.content_type = 'roleplay') AS roleplay_count,
    COUNT(*) FILTER (WHERE al.content_type = 'grammar') AS grammar_count,
    COUNT(*) FILTER (WHERE al.content_type = 'flashcard') AS flashcard_count,
    MAX(al.created_at) AS last_ai_request,
    q.daily_used,
    q.daily_limit,
    q.monthly_used,
    q.monthly_limit,
    q.is_premium,
    q.suspended
FROM users u
LEFT JOIN user_profiles up ON u.id = up.user_id
LEFT JOIN ai_usage_logs al ON u.id = al.user_id
LEFT JOIN user_ai_quotas q ON u.id = q.user_id
GROUP BY u.id, up.full_name, u.email, q.daily_used, q.daily_limit, q.monthly_used, q.monthly_limit, q.is_premium, q.suspended
ORDER BY total_cost_usd DESC NULLS LAST;

COMMENT ON VIEW ai_usage_by_user IS 'Per-user AI usage breakdown for admin monitoring';

-- ============================================================================
-- SECTION 6: Helper function for quota reset
-- ============================================================================

-- Function to reset daily quotas (called by application scheduler)
CREATE OR REPLACE FUNCTION reset_daily_ai_quotas()
RETURNS INTEGER AS $$
DECLARE
    affected_rows INTEGER;
BEGIN
    UPDATE user_ai_quotas
    SET 
        daily_used = 0,
        last_reset_daily = NOW(),
        feature_usage = jsonb_set(
            jsonb_set(
                jsonb_set(feature_usage, '{roleplay,daily}', '0'),
                '{grammar,daily}', '0'
            ),
            '{flashcard,daily}', '0'
        )
    WHERE DATE(last_reset_daily) < CURRENT_DATE;
    
    GET DIAGNOSTICS affected_rows = ROW_COUNT;
    RETURN affected_rows;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION reset_daily_ai_quotas() IS 'Reset daily AI quotas - call from scheduled job';

-- Function to reset monthly quotas (called by application scheduler)
CREATE OR REPLACE FUNCTION reset_monthly_ai_quotas()
RETURNS INTEGER AS $$
DECLARE
    affected_rows INTEGER;
BEGIN
    UPDATE user_ai_quotas
    SET 
        monthly_used = 0,
        last_reset_monthly = NOW(),
        feature_usage = jsonb_set(
            jsonb_set(
                jsonb_set(feature_usage, '{roleplay,monthly}', '0'),
                '{grammar,monthly}', '0'
            ),
            '{flashcard,monthly}', '0'
        )
    WHERE DATE_TRUNC('month', last_reset_monthly) < DATE_TRUNC('month', CURRENT_DATE);
    
    GET DIAGNOSTICS affected_rows = ROW_COUNT;
    RETURN affected_rows;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION reset_monthly_ai_quotas() IS 'Reset monthly AI quotas - call from scheduled job';

-- ============================================================================
-- SECTION 7: Function to increment usage
-- ============================================================================

CREATE OR REPLACE FUNCTION increment_ai_usage(
    p_user_id UUID,
    p_content_type VARCHAR(50)
)
RETURNS TABLE(
    allowed BOOLEAN,
    daily_remaining INTEGER,
    monthly_remaining INTEGER,
    message TEXT
) AS $$
DECLARE
    v_quota user_ai_quotas%ROWTYPE;
    v_effective_daily_limit INTEGER;
    v_effective_monthly_limit INTEGER;
BEGIN
    -- Get or create quota record
    INSERT INTO user_ai_quotas (user_id)
    VALUES (p_user_id)
    ON CONFLICT (user_id) DO NOTHING;
    
    -- Lock the row for update
    SELECT * INTO v_quota FROM user_ai_quotas WHERE user_id = p_user_id FOR UPDATE;
    
    -- Check if suspended
    IF v_quota.suspended THEN
        RETURN QUERY SELECT 
            false::BOOLEAN, 
            0::INTEGER, 
            0::INTEGER, 
            'Account suspended: ' || COALESCE(v_quota.suspension_reason, 'Contact support')::TEXT;
        RETURN;
    END IF;
    
    -- Calculate effective limits (with premium multiplier)
    v_effective_daily_limit := FLOOR(v_quota.daily_limit * v_quota.premium_multiplier);
    v_effective_monthly_limit := FLOOR(v_quota.monthly_limit * v_quota.premium_multiplier);
    
    -- Check daily limit
    IF v_quota.daily_used >= v_effective_daily_limit THEN
        RETURN QUERY SELECT 
            false::BOOLEAN, 
            0::INTEGER, 
            (v_effective_monthly_limit - v_quota.monthly_used)::INTEGER, 
            'Daily limit reached. Resets at midnight UTC.'::TEXT;
        RETURN;
    END IF;
    
    -- Check monthly limit
    IF v_quota.monthly_used >= v_effective_monthly_limit THEN
        RETURN QUERY SELECT 
            false::BOOLEAN, 
            (v_effective_daily_limit - v_quota.daily_used)::INTEGER, 
            0::INTEGER, 
            'Monthly limit reached. Resets on the 1st.'::TEXT;
        RETURN;
    END IF;
    
    -- Increment usage
    UPDATE user_ai_quotas
    SET 
        daily_used = daily_used + 1,
        monthly_used = monthly_used + 1,
        feature_usage = jsonb_set(
            jsonb_set(
                feature_usage,
                ARRAY[p_content_type, 'daily'],
                to_jsonb(COALESCE((feature_usage->p_content_type->>'daily')::INTEGER, 0) + 1)
            ),
            ARRAY[p_content_type, 'monthly'],
            to_jsonb(COALESCE((feature_usage->p_content_type->>'monthly')::INTEGER, 0) + 1)
        ),
        updated_at = NOW()
    WHERE user_id = p_user_id;
    
    RETURN QUERY SELECT 
        true::BOOLEAN, 
        (v_effective_daily_limit - v_quota.daily_used - 1)::INTEGER, 
        (v_effective_monthly_limit - v_quota.monthly_used - 1)::INTEGER, 
        'OK'::TEXT;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION increment_ai_usage(UUID, VARCHAR) IS 'Check and increment AI usage quota. Returns allowed status and remaining quota.';
