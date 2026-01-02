-- V47: Create goals table for persistent weekly/monthly learning goals
--
-- This migration creates the goals table to replace mock weekly goals logic.
-- Goals track user objectives (e.g., "Complete 5 lessons this week") with
-- target values, current progress, and automatic status management.
--
-- Author: LEXIA Team
-- Date: 2026-01-02
-- Sprint: 3

CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    goal_type VARCHAR(50) NOT NULL CHECK (goal_type IN ('WEEKLY_LESSONS', 'WEEKLY_STREAK', 'MONTHLY_COURSES', 'DAILY_PRACTICE')),
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    target_value INTEGER NOT NULL CHECK (target_value >= 1),
    current_value INTEGER NOT NULL DEFAULT 0 CHECK (current_value >= 0),
    unit VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'EXPIRED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT check_goal_dates CHECK (end_date >= start_date),
    CONSTRAINT check_goal_progress CHECK (current_value <= target_value OR status = 'COMPLETED'),
    CONSTRAINT unique_user_goal_period UNIQUE (user_id, goal_type, start_date)
);

-- Index for querying active goals by user
CREATE INDEX idx_goals_user_status ON goals(user_id, status);

-- Index for checking duplicate goals and querying by type and date range
CREATE INDEX idx_goals_user_type_date ON goals(user_id, goal_type, start_date);

-- Comments for documentation
COMMENT ON TABLE goals IS 'Stores user learning goals with target values and real-time progress tracking';
COMMENT ON COLUMN goals.goal_type IS 'Type of goal: WEEKLY_LESSONS, WEEKLY_STREAK, MONTHLY_COURSES, DAILY_PRACTICE';
COMMENT ON COLUMN goals.status IS 'Current status: ACTIVE (in progress), COMPLETED (achieved), EXPIRED (past end date without completion)';
COMMENT ON COLUMN goals.current_value IS 'Real-time progress updated from lesson_progress, enrollments, etc.';
COMMENT ON COLUMN goals.start_date IS 'Start of goal period (e.g., Monday for weekly goals)';
COMMENT ON COLUMN goals.end_date IS 'End of goal period (e.g., Sunday for weekly goals)';
