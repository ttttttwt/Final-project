-- V9: Create enrollments table for course enrollment tracking
-- Author: LEXIA Team
-- Date: November 5, 2025
-- Sprint: 2, Task: C1.1

-- ============================================
-- Table: enrollments
-- Purpose: Track user enrollment in courses and overall progress
-- ============================================
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    progress_percentage INTEGER NOT NULL DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    completed_at TIMESTAMP,
    CONSTRAINT unique_user_course_enrollment UNIQUE (user_id, course_id)
);

-- Index for retrieving a user's enrollments
CREATE INDEX idx_enrollments_user ON enrollments(user_id);

-- Index for retrieving enrollments for a specific course
CREATE INDEX idx_enrollments_course ON enrollments(course_id);

-- Composite index for checking user enrollment in specific course (supports UNIQUE constraint queries)
CREATE INDEX idx_enrollments_user_course ON enrollments(user_id, course_id);

-- Index for filtering by enrollment date
CREATE INDEX idx_enrollments_enrolled_at ON enrollments(enrolled_at DESC);

-- Index for filtering completed enrollments
CREATE INDEX idx_enrollments_completed ON enrollments(completed_at DESC) WHERE completed_at IS NOT NULL;

COMMENT ON TABLE enrollments IS 'User enrollment records for courses with progress tracking';
COMMENT ON COLUMN enrollments.id IS 'Primary key, auto-incrementing BIGSERIAL';
COMMENT ON COLUMN enrollments.user_id IS 'Foreign key to users table (UUID)';
COMMENT ON COLUMN enrollments.course_id IS 'Foreign key to courses table';
COMMENT ON COLUMN enrollments.enrolled_at IS 'Timestamp when user enrolled in the course';
COMMENT ON COLUMN enrollments.progress_percentage IS 'Overall course completion percentage (0-100), calculated from completed lessons';
COMMENT ON COLUMN enrollments.completed_at IS 'Timestamp when user completed all lessons in course (NULL if incomplete)';
