-- V5: Create courses table for course catalog management
-- Author: LEXIA Team
-- Date: October 30, 2025
-- Sprint: 2, Task: A1.1

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(255),
    cefr_level VARCHAR(2) NOT NULL CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    is_published BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Composite index for filtering by CEFR level and publication status
-- Most common query: published courses by level
CREATE INDEX idx_courses_cefr_published ON courses(cefr_level, is_published);

-- Index for sorting by most recent courses
CREATE INDEX idx_courses_created_at ON courses(created_at DESC);

-- B-tree index for title search (exact match and prefix matching)
CREATE INDEX idx_courses_title ON courses(title);

-- Comments for documentation
COMMENT ON TABLE courses IS 'Course catalog with CEFR-leveled English learning courses';
COMMENT ON COLUMN courses.id IS 'Primary key, auto-incrementing BIGSERIAL';
COMMENT ON COLUMN courses.title IS 'Course title displayed to users';
COMMENT ON COLUMN courses.description IS 'Detailed course description';
COMMENT ON COLUMN courses.thumbnail_url IS 'URL to course thumbnail image';
COMMENT ON COLUMN courses.cefr_level IS 'Common European Framework of Reference level: A1 (beginner) to C2 (proficient)';
COMMENT ON COLUMN courses.is_published IS 'Publication status - only published courses are visible to learners';
COMMENT ON COLUMN courses.created_at IS 'Timestamp when course was created';
COMMENT ON COLUMN courses.updated_at IS 'Timestamp when course was last updated';
