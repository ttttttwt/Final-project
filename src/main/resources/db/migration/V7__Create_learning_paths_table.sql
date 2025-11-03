-- V7: Create learning paths tables for structured learning journeys
-- Author: LEXIA Team
-- Date: November 3, 2025
-- Sprint: 2, Task: B1.1

-- ============================================
-- Table: learning_paths
-- Purpose: Define structured learning journeys organized by CEFR level
-- ============================================
CREATE TABLE learning_paths (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cefr_level VARCHAR(2) NOT NULL CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for filtering default paths by CEFR level
CREATE INDEX idx_learning_paths_cefr_default ON learning_paths(cefr_level, is_default);

-- Index for sorting by creation date
CREATE INDEX idx_learning_paths_created_at ON learning_paths(created_at DESC);

COMMENT ON TABLE learning_paths IS 'Structured learning journeys organized by CEFR level';
COMMENT ON COLUMN learning_paths.id IS 'Primary key, auto-incrementing BIGSERIAL';
COMMENT ON COLUMN learning_paths.name IS 'Display name of the learning path (e.g., "Beginner Path (A1)")';
COMMENT ON COLUMN learning_paths.description IS 'Detailed description of the learning path objectives';
COMMENT ON COLUMN learning_paths.cefr_level IS 'Target CEFR level: A1 (beginner) to C2 (proficient)';
COMMENT ON COLUMN learning_paths.is_default IS 'System-provided default path (true) vs user-created custom path (false)';
COMMENT ON COLUMN learning_paths.created_at IS 'Timestamp when learning path was created';
COMMENT ON COLUMN learning_paths.updated_at IS 'Timestamp when learning path was last updated';

-- ============================================
-- Table: learning_path_courses
-- Purpose: Associate courses with learning paths in specific order
-- ============================================
CREATE TABLE learning_path_courses (
    path_id BIGINT NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL,
    PRIMARY KEY (path_id, course_id),
    CONSTRAINT valid_order_index CHECK (order_index >= 0)
);

-- Index for retrieving courses in order for a given path
CREATE INDEX idx_learning_path_courses_path_order ON learning_path_courses(path_id, order_index);

-- Index for finding which paths contain a specific course
CREATE INDEX idx_learning_path_courses_course ON learning_path_courses(course_id);

COMMENT ON TABLE learning_path_courses IS 'Join table associating courses with learning paths in specific order';
COMMENT ON COLUMN learning_path_courses.path_id IS 'Foreign key to learning_paths table';
COMMENT ON COLUMN learning_path_courses.course_id IS 'Foreign key to courses table';
COMMENT ON COLUMN learning_path_courses.order_index IS 'Sequential position of course in learning path (0-based)';

-- ============================================
-- Table: user_learning_paths
-- Purpose: Track user enrollment and progress in learning paths
-- ============================================
CREATE TABLE user_learning_paths (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    path_id BIGINT NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
    current_course_id BIGINT REFERENCES courses(id) ON DELETE SET NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT unique_user_path UNIQUE (user_id, path_id)
);

-- Index for retrieving a user's learning paths
CREATE INDEX idx_user_learning_paths_user ON user_learning_paths(user_id);

-- Index for finding all users on a specific path
CREATE INDEX idx_user_learning_paths_path ON user_learning_paths(path_id);

-- Composite index for checking user enrollment status
CREATE INDEX idx_user_learning_paths_user_path ON user_learning_paths(user_id, path_id);

COMMENT ON TABLE user_learning_paths IS 'User enrollment and progress tracking for learning paths';
COMMENT ON COLUMN user_learning_paths.id IS 'Primary key, auto-incrementing BIGSERIAL';
COMMENT ON COLUMN user_learning_paths.user_id IS 'Foreign key to users table (UUID)';
COMMENT ON COLUMN user_learning_paths.path_id IS 'Foreign key to learning_paths table';
COMMENT ON COLUMN user_learning_paths.current_course_id IS 'Current course user is working on (NULL if path completed or not started)';
COMMENT ON COLUMN user_learning_paths.started_at IS 'Timestamp when user enrolled in the path';
COMMENT ON COLUMN user_learning_paths.completed_at IS 'Timestamp when user completed all courses in path (NULL if incomplete)';
