-- V6: Create sections and lessons tables for course content structure
-- Author: LEXIA Team
-- Date: October 30, 2025
-- Sprint: 2, Task: A1.2

-- Create enum type for lesson types
CREATE TYPE lesson_type_enum AS ENUM ('READING', 'LISTENING', 'QUIZ', 'SPEAKING');

-- Create sections table for organizing lessons within courses
CREATE TABLE sections (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_course_section_order UNIQUE (course_id, order_index)
);

-- Index for efficient section retrieval by course (ordered)
CREATE INDEX idx_sections_course_order ON sections(course_id, order_index);

-- Create lessons table with JSONB content
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    lesson_type lesson_type_enum NOT NULL,
    content JSONB NOT NULL,
    order_index INTEGER NOT NULL,
    duration_minutes INTEGER NOT NULL DEFAULT 15,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_section_lesson_order UNIQUE (section_id, order_index),
    CONSTRAINT valid_duration CHECK (duration_minutes > 0 AND duration_minutes <= 240)
);

-- Index for efficient lesson retrieval by section (ordered)
CREATE INDEX idx_lessons_section_order ON lessons(section_id, order_index);

-- Index for filtering lessons by type
CREATE INDEX idx_lessons_type ON lessons(lesson_type);

-- Comments for documentation
COMMENT ON TABLE sections IS 'Course sections for organizing lessons into logical groups';
COMMENT ON COLUMN sections.id IS 'Primary key, auto-incrementing BIGSERIAL';
COMMENT ON COLUMN sections.course_id IS 'Foreign key to courses table with CASCADE DELETE';
COMMENT ON COLUMN sections.title IS 'Section title displayed to users';
COMMENT ON COLUMN sections.order_index IS 'Order position within the course (0-based)';
COMMENT ON CONSTRAINT unique_course_section_order ON sections IS 'Ensures unique order within each course';

COMMENT ON TABLE lessons IS 'Individual lessons with type-specific JSONB content';
COMMENT ON COLUMN lessons.id IS 'Primary key, auto-incrementing BIGSERIAL';
COMMENT ON COLUMN lessons.section_id IS 'Foreign key to sections table with CASCADE DELETE';
COMMENT ON COLUMN lessons.title IS 'Lesson title displayed to users';
COMMENT ON COLUMN lessons.lesson_type IS 'Type of lesson: READING, LISTENING, QUIZ, or SPEAKING';
COMMENT ON COLUMN lessons.content IS 'JSONB content - schema varies by lesson_type (see DATABASE-SCHEMA.md section 2.3)';
COMMENT ON COLUMN lessons.order_index IS 'Order position within the section (0-based)';
COMMENT ON COLUMN lessons.duration_minutes IS 'Estimated time to complete the lesson (1-240 minutes)';
COMMENT ON CONSTRAINT unique_section_lesson_order ON lessons IS 'Ensures unique order within each section';
COMMENT ON CONSTRAINT valid_duration ON lessons IS 'Duration must be between 1 and 240 minutes (4 hours)';
