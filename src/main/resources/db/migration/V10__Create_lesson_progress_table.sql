-- V10: Create lesson_progress table for detailed lesson completion tracking
-- Author: LEXIA Team
-- Date: November 5, 2025
-- Sprint: 2, Task: C1.2

-- ============================================
-- Table: lesson_progress
-- Purpose: Track individual lesson completion status, scores, and attempts
-- ============================================
CREATE TABLE lesson_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    score INTEGER CHECK (score >= 0 AND score <= 100),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    result_details JSONB,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_lesson_progress UNIQUE (user_id, lesson_id)
);

-- Composite index for retrieving user's progress for a specific lesson (supports UNIQUE constraint queries)
CREATE INDEX idx_lesson_progress_user_lesson ON lesson_progress(user_id, lesson_id);

-- Index for retrieving all progress records for a user
CREATE INDEX idx_lesson_progress_user ON lesson_progress(user_id);

-- Index for retrieving progress for a specific lesson across all users
CREATE INDEX idx_lesson_progress_lesson ON lesson_progress(lesson_id);

-- Index for filtering by status (e.g., finding all completed lessons)
CREATE INDEX idx_lesson_progress_status ON lesson_progress(user_id, status);

-- Index for filtering by completion date (useful for streak calculation)
CREATE INDEX idx_lesson_progress_completed_at ON lesson_progress(user_id, completed_at DESC) WHERE completed_at IS NOT NULL;

-- Index for date range queries (e.g., progress in last 30 days, streak calculation)
CREATE INDEX idx_lesson_progress_date_range ON lesson_progress(user_id, completed_at) WHERE completed_at IS NOT NULL;

COMMENT ON TABLE lesson_progress IS 'Individual lesson completion tracking with scores, attempts, and detailed results';
COMMENT ON COLUMN lesson_progress.id IS 'Primary key, auto-incrementing BIGSERIAL';
COMMENT ON COLUMN lesson_progress.user_id IS 'Foreign key to users table (UUID)';
COMMENT ON COLUMN lesson_progress.lesson_id IS 'Foreign key to lessons table';
COMMENT ON COLUMN lesson_progress.status IS 'Current status: NOT_STARTED (default), IN_PROGRESS, or COMPLETED';
COMMENT ON COLUMN lesson_progress.score IS 'Score achieved (0-100), NULL if not applicable or not completed';
COMMENT ON COLUMN lesson_progress.attempts IS 'Number of times user attempted the lesson (0 on creation)';
COMMENT ON COLUMN lesson_progress.result_details IS 'JSONB field storing detailed lesson results (answers, feedback, timestamps)';
COMMENT ON COLUMN lesson_progress.completed_at IS 'Timestamp when lesson was first completed (NULL if incomplete)';
COMMENT ON COLUMN lesson_progress.created_at IS 'Timestamp when progress record was created';
COMMENT ON COLUMN lesson_progress.updated_at IS 'Timestamp when progress record was last updated';

-- ============================================
-- JSONB Structure Documentation for result_details
-- ============================================
-- The result_details JSONB field stores lesson-specific results:
--
-- For READING lessons:
-- {
--   "questions": [
--     {
--       "questionId": 1,
--       "userAnswer": "answer_text",
--       "isCorrect": true,
--       "timeTaken": 45
--     }
--   ],
--   "totalQuestions": 5,
--   "correctAnswers": 4,
--   "timeSpent": 180
-- }
--
-- For LISTENING lessons:
-- {
--   "questions": [...],  // Similar to READING
--   "audioPlayCount": 3,
--   "totalQuestions": 5,
--   "correctAnswers": 4,
--   "timeSpent": 240
-- }
--
-- For QUIZ lessons:
-- {
--   "answers": [
--     {
--       "questionId": 1,
--       "selectedOption": "B",
--       "isCorrect": true,
--       "timeTaken": 30
--     }
--   ],
--   "totalQuestions": 10,
--   "correctAnswers": 8,
--   "timeSpent": 300
-- }
--
-- For SPEAKING lessons:
-- {
--   "recordings": [
--     {
--       "promptId": 1,
--       "recordingUrl": "https://storage.../recording.mp3",
--       "duration": 45,
--       "aiFeedback": {
--         "pronunciation": 85,
--         "fluency": 78,
--         "suggestions": ["Work on 'th' sounds"]
--       }
--     }
--   ],
--   "totalPrompts": 3,
--   "averagePronunciation": 85,
--   "averageFluency": 78,
--   "timeSpent": 360
-- }
