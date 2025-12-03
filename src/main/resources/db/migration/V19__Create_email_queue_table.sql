-- V19: Create email_queue table for async email processing
-- This table manages outgoing emails with retry support and priority queuing

CREATE TABLE IF NOT EXISTS email_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Recipient info
    recipient_id UUID REFERENCES users(id) ON DELETE SET NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255),

    -- Email content
    email_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    template_data JSONB NOT NULL DEFAULT '{}',

    -- Status tracking
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',

    -- Retry handling
    attempts INTEGER DEFAULT 0,
    max_attempts INTEGER DEFAULT 3,
    next_retry_at TIMESTAMPTZ,
    last_error TEXT,

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    sent_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,

    -- Constraints
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED', 'CANCELLED')),
    CONSTRAINT valid_priority CHECK (priority IN ('CRITICAL', 'HIGH', 'NORMAL', 'LOW')),
    CONSTRAINT valid_email_type CHECK (email_type IN (
        'EMAIL_VERIFICATION', 'PASSWORD_RESET', 'WELCOME',
        'ENROLLMENT_CONFIRMATION', 'COURSE_COMPLETED', 'CERTIFICATE_DELIVERY',
        'STREAK_REMINDER', 'STREAK_LOST', 'STREAK_MILESTONE', 'LEVEL_UP',
        'WEEKLY_PROGRESS', 'ACCOUNT_DEACTIVATION',
        'SYSTEM_ANNOUNCEMENT', 'MAINTENANCE_NOTICE', 'SECURITY_ALERT',
        'PASSWORD_CHANGED', 'DEVICE_LOGIN'
    ))
);

-- Indexes for queue processing
CREATE INDEX IF NOT EXISTS idx_email_queue_pending ON email_queue(status, priority, created_at)
    WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_email_queue_retry ON email_queue(next_retry_at)
    WHERE status = 'PENDING' AND next_retry_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_email_queue_recipient ON email_queue(recipient_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_email_queue_type ON email_queue(email_type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_email_queue_status ON email_queue(status, created_at DESC);

-- Comments
COMMENT ON TABLE email_queue IS 'Async email queue with retry support for reliable email delivery';
COMMENT ON COLUMN email_queue.recipient_id IS 'Reference to users table, nullable for system emails';
COMMENT ON COLUMN email_queue.email_type IS 'Type of email for categorization and template selection';
COMMENT ON COLUMN email_queue.template_name IS 'Thymeleaf template path relative to templates/email/';
COMMENT ON COLUMN email_queue.template_data IS 'JSONB payload containing template variables';
COMMENT ON COLUMN email_queue.priority IS 'Processing priority: CRITICAL > HIGH > NORMAL > LOW';
COMMENT ON COLUMN email_queue.attempts IS 'Number of send attempts made';
COMMENT ON COLUMN email_queue.max_attempts IS 'Maximum retry attempts before marking as FAILED';
COMMENT ON COLUMN email_queue.next_retry_at IS 'Scheduled time for next retry attempt';
COMMENT ON COLUMN email_queue.last_error IS 'Error message from the last failed attempt';
