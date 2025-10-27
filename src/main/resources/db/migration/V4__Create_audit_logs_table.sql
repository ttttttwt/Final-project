-- V4: Create audit_logs table for tracking user profile changes
-- Author: LEXIA Team
-- Date: October 27, 2025

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    changes TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for efficient querying by user
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

-- Index for efficient querying by entity
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

-- Index for efficient querying by action type
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- Index for efficient querying by timestamp
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- Comments for documentation
COMMENT ON TABLE audit_logs IS 'Audit trail for tracking user profile and system changes';
COMMENT ON COLUMN audit_logs.user_id IS 'User who performed the action';
COMMENT ON COLUMN audit_logs.action IS 'Type of action performed (e.g., PROFILE_UPDATE, AVATAR_UPDATE)';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type of entity modified (e.g., UserProfile, User)';
COMMENT ON COLUMN audit_logs.entity_id IS 'ID of the modified entity';
COMMENT ON COLUMN audit_logs.changes IS 'JSON string describing the changes made';
COMMENT ON COLUMN audit_logs.ip_address IS 'IP address from which the action was performed';
COMMENT ON COLUMN audit_logs.user_agent IS 'Browser/client user agent string';
