-- V36: Create admin enhancement tables for user monitoring and subscription management
-- Created: 2024-12-19

-- =============================================
-- User Sessions Table (for monitoring)
-- =============================================
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    device_type VARCHAR(20) DEFAULT 'UNKNOWN',
    login_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_activity_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    logout_time TIMESTAMP WITH TIME ZONE,
    session_token_hash VARCHAR(64),
    
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for user_sessions
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_is_active ON user_sessions(is_active);
CREATE INDEX IF NOT EXISTS idx_user_sessions_last_activity ON user_sessions(last_activity_time);
CREATE INDEX IF NOT EXISTS idx_user_sessions_ip_address ON user_sessions(ip_address);

-- =============================================
-- Subscription Plans Table (for dynamic pricing)
-- =============================================
CREATE TABLE IF NOT EXISTS subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    stripe_price_id VARCHAR(100),
    billing_interval VARCHAR(20) DEFAULT 'MONTHLY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    features JSONB DEFAULT '{}',
    effective_from TIMESTAMP WITH TIME ZONE,
    effective_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for subscription_plans
CREATE INDEX IF NOT EXISTS idx_subscription_plans_active ON subscription_plans(is_active);
CREATE INDEX IF NOT EXISTS idx_subscription_plans_plan_type ON subscription_plans(plan_type);
CREATE INDEX IF NOT EXISTS idx_subscription_plans_effective ON subscription_plans(effective_from, effective_until);

-- =============================================
-- Promo Codes Table
-- =============================================
CREATE TABLE IF NOT EXISTS promo_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL DEFAULT 'PERCENTAGE',
    discount_value INTEGER NOT NULL,
    max_discount_amount INTEGER,
    applicable_plan_type VARCHAR(20),
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    max_uses INTEGER,
    used_count INTEGER NOT NULL DEFAULT 0,
    max_uses_per_user INTEGER DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    stripe_coupon_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for promo_codes
CREATE INDEX IF NOT EXISTS idx_promo_codes_code ON promo_codes(code);
CREATE INDEX IF NOT EXISTS idx_promo_codes_active ON promo_codes(is_active);
CREATE INDEX IF NOT EXISTS idx_promo_codes_validity ON promo_codes(valid_from, valid_until);

-- =============================================
-- Seed default subscription plans
-- =============================================
INSERT INTO subscription_plans (name, plan_type, description, price, original_price, billing_interval, is_active, is_featured, display_order, features)
VALUES 
    ('Monthly Pro', 'MONTHLY', 'Full access to all Pro features, billed monthly', 9.99, NULL, 'MONTHLY', TRUE, FALSE, 1, 
     '{"roleplaySessionsLimit": 100, "flashcardDecksLimit": 50, "grammarExercisesLimit": 200, "prioritySupport": false}'::jsonb),
    ('Yearly Pro', 'YEARLY', 'Full access to all Pro features, billed yearly. Save 20%!', 95.99, 119.88, 'YEARLY', TRUE, TRUE, 2, 
     '{"roleplaySessionsLimit": -1, "flashcardDecksLimit": -1, "grammarExercisesLimit": -1, "prioritySupport": true}'::jsonb)
ON CONFLICT DO NOTHING;

-- Add comment
COMMENT ON TABLE user_sessions IS 'Tracks user login sessions for monitoring dashboard';
COMMENT ON TABLE subscription_plans IS 'Dynamic subscription plans with configurable pricing and features';
COMMENT ON TABLE promo_codes IS 'Promotional discount codes for subscriptions';
