-- Migration: Change lesson_type column from PostgreSQL enum to VARCHAR
-- Reason: H2 test database compatibility and Hibernate 6 simplified enum handling

-- Step 1: Add a temporary VARCHAR column
ALTER TABLE lessons ADD COLUMN lesson_type_new VARCHAR(20);

-- Step 2: Copy data from enum column to VARCHAR column
UPDATE lessons SET lesson_type_new = lesson_type::VARCHAR;

-- Step 3: Drop the old enum column
ALTER TABLE lessons DROP COLUMN lesson_type;

-- Step 4: Rename the new column to the original name
ALTER TABLE lessons RENAME COLUMN lesson_type_new TO lesson_type;

-- Step 5: Add NOT NULL constraint
ALTER TABLE lessons ALTER COLUMN lesson_type SET NOT NULL;

-- Step 6: Add CHECK constraint to ensure valid values
ALTER TABLE lessons ADD CONSTRAINT chk_lesson_type 
    CHECK (lesson_type IN ('READING', 'LISTENING', 'QUIZ', 'SPEAKING'));

-- Step 7: Drop the enum type (optional, as it's no longer needed)
DROP TYPE IF EXISTS lesson_type_enum;
