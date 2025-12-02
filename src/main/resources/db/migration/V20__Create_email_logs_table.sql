-- V20: Create email_logs table for email delivery tracking and analytics
-- This table stores historical email delivery data and engagement metrics

CREATE TABLE email_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    queue_id UUID REFERENCES email_queue(id) ON DELETE SET NULL,

    -- Recipient info
    recipient_id UUID REFERENCES users(id) ON DELETE SET NULL,
    recipient_email VARCHAR(255) NOT NULL,

    -- Email info
    email_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,

    -- Provider info
    provider VARCHAR(50) NOT NULL,
    provider_message_id VARCHAR(255),

    -- Status
    status VARCHAR(20) NOT NULL,
    error_message TEXT,

    -- Tracking
    opened_at TIMESTAMPTZ,
    clicked_at TIMESTAMPTZ,
    unsubscribed_at TIMESTAMPTZ,
    bounced_at TIMESTAMPTZ,
    complained_at TIMESTAMPTZ,

    -- Metadata
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),

    -- Constraints
    CONSTRAINT valid_log_status CHECK (status IN ('SENT', 'DELIVERED', 'OPENED', 'CLICKED', 'BOUNCED', 'COMPLAINED', 'FAILED')),
    CONSTRAINT valid_log_provider CHECK (provider IN ('SMTP', 'SES', 'SENDGRID', 'MAILGUN'))
);

-- Indexes for analytics and querying
CREATE INDEX idx_email_logs_recipient ON email_logs(recipient_id, created_at DESC);
CREATE INDEX idx_email_logs_type ON email_logs(email_type, created_at DESC);
CREATE INDEX idx_email_logs_status ON email_logs(status, created_at DESC);
CREATE INDEX idx_email_logs_opened ON email_logs(opened_at) WHERE opened_at IS NOT NULL;
CREATE INDEX idx_email_logs_clicked ON email_logs(clicked_at) WHERE clicked_at IS NOT NULL;
CREATE INDEX idx_email_logs_queue ON email_logs(queue_id);
CREATE INDEX idx_email_logs_created ON email_logs(created_at DESC);

-- Comments
COMMENT ON TABLE email_logs IS 'Email delivery and engagement tracking logs for analytics';
COMMENT ON COLUMN email_logs.queue_id IS 'Reference to the original email queue entry';
COMMENT ON COLUMN email_logs.provider IS 'Email provider used: SMTP, SES, SENDGRID, MAILGUN';
COMMENT ON COLUMN email_logs.provider_message_id IS 'Message ID returned by the email provider';
COMMENT ON COLUMN email_logs.opened_at IS 'Timestamp when email was opened (tracked via pixel)';
COMMENT ON COLUMN email_logs.clicked_at IS 'Timestamp of first link click';
COMMENT ON COLUMN email_logs.unsubscribed_at IS 'Timestamp when user unsubscribed via this email';
COMMENT ON COLUMN email_logs.bounced_at IS 'Timestamp when bounce notification received';
COMMENT ON COLUMN email_logs.complained_at IS 'Timestamp when spam complaint received';
COMMENT ON COLUMN email_logs.ip_address IS 'IP address from tracking events';
COMMENT ON COLUMN email_logs.user_agent IS 'User agent from tracking events';
