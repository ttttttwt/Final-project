-- V44: Add payment email types to email_queue constraint
-- Adds new email types for payment notifications

-- Drop the existing constraint
ALTER TABLE email_queue DROP CONSTRAINT IF EXISTS valid_email_type;

-- Add the updated constraint with payment email types
ALTER TABLE email_queue ADD CONSTRAINT valid_email_type CHECK (email_type IN (
    -- Authentication & Account
    'EMAIL_VERIFICATION', 'PASSWORD_RESET', 'WELCOME',
    'PASSWORD_CHANGED', 'DEVICE_LOGIN',
    'ACCOUNT_DEACTIVATION', 'SECURITY_ALERT',
    
    -- Learning & Engagement
    'ENROLLMENT_CONFIRMATION', 'COURSE_COMPLETED', 'CERTIFICATE_DELIVERY',
    'STREAK_REMINDER', 'STREAK_LOST', 'STREAK_MILESTONE', 'LEVEL_UP',
    'WEEKLY_PROGRESS',
    
    -- System
    'SYSTEM_ANNOUNCEMENT', 'MAINTENANCE_NOTICE',
    
    -- AI Budget/Quota
    'AI_BUDGET_WARNING', 'AI_BUDGET_EXCEEDED', 
    'AI_QUOTA_WARNING', 'AI_QUOTA_EXCEEDED',
    
    -- Payment (NEW)
    'PAYMENT_SUCCESS', 'PAYMENT_RENEWAL', 
    'PAYMENT_FAILED', 'PAYMENT_REFUND', 
    'SUBSCRIPTION_EXPIRED'
));

COMMENT ON CONSTRAINT valid_email_type ON email_queue IS 'Updated to include payment notification email types';
