-- V11__Create_ai_usage_logs_table.sql
-- Create AI usage logs table for tracking AI API usage, costs, and frequency

CREATE TABLE IF NOT EXISTS ai_usage_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    feature_name VARCHAR(100) NOT NULL,
    input_tokens INTEGER DEFAULT 0,
    output_tokens INTEGER DEFAULT 0,
    cost DECIMAL(10, 6) DEFAULT 0.000000,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create index for common queries
CREATE INDEX idx_ai_usage_logs_user_id ON ai_usage_logs(user_id);
CREATE INDEX idx_ai_usage_logs_feature_name ON ai_usage_logs(feature_name);
CREATE INDEX idx_ai_usage_logs_created_at ON ai_usage_logs(created_at);
CREATE INDEX idx_ai_usage_logs_user_feature ON ai_usage_logs(user_id, feature_name);

-- Add comments
COMMENT ON TABLE ai_usage_logs IS 'Track AI API usage cost and frequency';
COMMENT ON COLUMN ai_usage_logs.feature_name IS 'AI feature name: MAGIC_FLASHCARD, ROLEPLAY, GRAMMAR_SANDBOX, PRONUNCIATION_FEEDBACK, CONTENT_GENERATION';
COMMENT ON COLUMN ai_usage_logs.input_tokens IS 'Number of input tokens sent to AI model';
COMMENT ON COLUMN ai_usage_logs.output_tokens IS 'Number of output tokens received from AI model';
COMMENT ON COLUMN ai_usage_logs.cost IS 'Calculated cost of the API call in USD';
