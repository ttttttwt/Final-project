-- V27: Add consecutive_correct column to user_flashcard_progress
-- This column tracks consecutive correct answers for SM-2 algorithm
-- The SM-2 algorithm uses consecutive correct answers (not total reviews) for interval calculation:
--   1st consecutive correct: 1 day interval
--   2nd consecutive correct: 6 day interval
--   3rd+ consecutive correct: previous_interval * ease_factor

-- Add the column with default 0
ALTER TABLE user_flashcard_progress 
ADD COLUMN IF NOT EXISTS consecutive_correct INTEGER NOT NULL DEFAULT 0;

-- Add comment for documentation
COMMENT ON COLUMN user_flashcard_progress.consecutive_correct IS 
    'Number of consecutive correct responses. Used for SM-2 interval calculation. Resets on incorrect answer.';

-- Initialize consecutive_correct based on existing review pattern
-- For existing records, we estimate consecutive_correct from interval_days and ease_factor
-- If interval > 6 days, they likely had at least 2-3 consecutive correct
UPDATE user_flashcard_progress
SET consecutive_correct = 
    CASE 
        WHEN interval_days >= 6 AND correct_count >= 2 THEN 
            LEAST(correct_count, GREATEST(2, FLOOR(LN(interval_days / 6.0) / LN(GREATEST(ease_factor, 1.3)) + 2)))::INTEGER
        WHEN interval_days >= 1 AND correct_count >= 1 THEN 1
        ELSE 0
    END
WHERE consecutive_correct = 0 AND correct_count > 0;
