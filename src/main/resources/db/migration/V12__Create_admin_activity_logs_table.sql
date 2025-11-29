-- V12: Create admin_activity_logs table for tracking admin/content manager actions
-- This table stores activity logs for dashboard display

CREATE TABLE admin_activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    user_name VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    entity_name VARCHAR(500),
    description TEXT NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_action CHECK (action IN (
        'COURSE_CREATED', 'COURSE_UPDATED', 'COURSE_PUBLISHED', 
        'COURSE_UNPUBLISHED', 'COURSE_DELETED',
        'SECTION_CREATED', 'SECTION_UPDATED', 'SECTION_DELETED',
        'LESSON_CREATED', 'LESSON_UPDATED', 'LESSON_DELETED'
    )),
    CONSTRAINT chk_entity_type CHECK (entity_type IN ('COURSE', 'SECTION', 'LESSON'))
);

-- Create indexes for common queries
CREATE INDEX idx_admin_activity_logs_created_at ON admin_activity_logs(created_at DESC);
CREATE INDEX idx_admin_activity_logs_user_id ON admin_activity_logs(user_id);
CREATE INDEX idx_admin_activity_logs_action ON admin_activity_logs(action);
CREATE INDEX idx_admin_activity_logs_entity_type ON admin_activity_logs(entity_type);

-- Add comment to table
COMMENT ON TABLE admin_activity_logs IS 'Tracks admin/content manager CRUD actions for dashboard display';
COMMENT ON COLUMN admin_activity_logs.action IS 'Type of action performed (COURSE_CREATED, LESSON_UPDATED, etc.)';
COMMENT ON COLUMN admin_activity_logs.entity_type IS 'Type of entity affected (COURSE, SECTION, LESSON)';
COMMENT ON COLUMN admin_activity_logs.entity_id IS 'ID of the affected entity';
COMMENT ON COLUMN admin_activity_logs.details IS 'Optional JSON or text with additional change details';
