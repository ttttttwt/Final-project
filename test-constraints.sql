-- Migration Constraint Testing Script for Task A1.3
-- Tests all constraints defined in V5 and V6 migrations

-- ============================================
-- TEST 1: CEFR Level CHECK Constraint
-- ============================================
\echo '=== Test 1: CEFR Level CHECK Constraint ==='

-- Should pass: Valid CEFR levels
INSERT INTO courses (title, description, cefr_level, is_published) 
VALUES ('Course A2 Test', 'Elementary level', 'A2', false);

INSERT INTO courses (title, description, cefr_level, is_published) 
VALUES ('Course C2 Test', 'Proficiency level', 'C2', true);

-- Should FAIL: Invalid CEFR level
\echo 'Trying invalid CEFR level (should fail)...'
INSERT INTO courses (title, description, cefr_level, is_published) 
VALUES ('Course Invalid', 'Bad level', 'D1', false);

-- ============================================
-- TEST 2: Section UNIQUE Constraint
-- ============================================
\echo '\n=== Test 2: Section Order Index UNIQUE Constraint ==='

-- Create sections for testing
INSERT INTO sections (course_id, title, order_index) 
VALUES (3, 'Section 2: Vocabulary', 2);

INSERT INTO sections (course_id, title, order_index) 
VALUES (3, 'Section 3: Grammar', 3);

-- Should FAIL: Duplicate order_index for same course
\echo 'Trying duplicate order_index (should fail)...'
INSERT INTO sections (course_id, title, order_index) 
VALUES (3, 'Section Duplicate', 2);

-- ============================================
-- TEST 3: Lesson Duration CHECK Constraint
-- ============================================
\echo '\n=== Test 3: Lesson Duration CHECK Constraint ==='

-- Should pass: Valid durations
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (1, 'Lesson 2: Valid Short', 'QUIZ', '{"questions": []}', 2, 5);

INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (1, 'Lesson 3: Valid Long', 'LISTENING', '{"audioUrl": "test.mp3"}', 3, 180);

-- Should FAIL: Duration = 0
\echo 'Trying duration = 0 (should fail)...'
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (1, 'Lesson Invalid Duration', 'READING', '{"passages": []}', 4, 0);

-- Should FAIL: Duration > 240
\echo 'Trying duration = 300 (should fail)...'
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (1, 'Lesson Too Long', 'READING', '{"passages": []}', 5, 300);

-- ============================================
-- TEST 4: Lesson Order Index UNIQUE Constraint
-- ============================================
\echo '\n=== Test 4: Lesson Order Index UNIQUE Constraint ==='

-- Should FAIL: Duplicate order_index for same section
\echo 'Trying duplicate order_index (should fail)...'
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (1, 'Lesson Duplicate', 'SPEAKING', '{"scenario": "test"}', 2);

-- ============================================
-- TEST 5: JSONB Content Validation
-- ============================================
\echo '\n=== Test 5: JSONB Content Validation ==='

-- Should pass: Valid JSON for READING type
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (2, 'Reading Lesson', 'READING', 
'{"passages": [{"id": 1, "text": "English is a global language."}], "questions": [{"id": 1, "text": "What is English?", "options": ["Global", "Local"], "correctAnswer": 0}]}', 
1, 30);

-- Should pass: Valid JSON for LISTENING type
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (2, 'Listening Lesson', 'LISTENING',
'{"audioUrl": "https://example.com/audio.mp3", "duration": 120, "transcript": "Hello world", "questions": [{"id": 1, "text": "What did you hear?", "options": ["Hello", "Goodbye"], "correctAnswer": 0}]}',
2, 25);

-- Should pass: Valid JSON for QUIZ type
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (2, 'Quiz Lesson', 'QUIZ',
'{"questions": [{"id": 1, "text": "What is 2+2?", "options": ["3", "4", "5"], "correctAnswer": 1, "points": 10}], "timeLimit": 600, "passingScore": 70}',
3, 15);

-- Should pass: Valid JSON for SPEAKING type
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (2, 'Speaking Lesson', 'SPEAKING',
'{"scenario": "Job Interview", "difficulty": "intermediate", "prompts": ["Tell me about yourself", "Why do you want this job?"], "rolePlaySettings": {"turns": 5, "thinkTime": 30}}',
4, 40);

-- Should FAIL: Invalid JSON syntax
\echo 'Trying invalid JSON (should fail)...'
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (2, 'Bad JSON', 'READING', '{invalid json}', 5, 20);

-- ============================================
-- TEST 6: CASCADE DELETE Behavior
-- ============================================
\echo '\n=== Test 6: CASCADE DELETE Behavior ==='

-- Create a course with sections and lessons
INSERT INTO courses (title, description, cefr_level, is_published) 
VALUES ('Course for Deletion', 'Test cascade', 'B2', false) RETURNING id;

-- Insert sections
INSERT INTO sections (course_id, title, order_index) 
VALUES (4, 'Section 1', 1), (4, 'Section 2', 2) RETURNING id;

-- Insert lessons
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES 
(3, 'Lesson 1', 'READING', '{"passages": []}', 1, 15),
(3, 'Lesson 2', 'QUIZ', '{"questions": []}', 2, 10),
(4, 'Lesson 3', 'LISTENING', '{"audioUrl": "test.mp3"}', 1, 20);

-- Count before deletion
SELECT 
    (SELECT COUNT(*) FROM courses WHERE id = 4) as courses_count,
    (SELECT COUNT(*) FROM sections WHERE course_id = 4) as sections_count,
    (SELECT COUNT(*) FROM lessons WHERE section_id IN (SELECT id FROM sections WHERE course_id = 4)) as lessons_count;

-- Delete the course (should cascade to sections and lessons)
\echo 'Deleting course (should cascade)...'
DELETE FROM courses WHERE id = 4;

-- Count after deletion (should be 0)
SELECT 
    (SELECT COUNT(*) FROM courses WHERE id = 4) as courses_after,
    (SELECT COUNT(*) FROM sections WHERE course_id = 4) as sections_after,
    (SELECT COUNT(*) FROM lessons WHERE section_id IN (3, 4)) as lessons_after;

-- ============================================
-- TEST 7: Enum Type Validation
-- ============================================
\echo '\n=== Test 7: Lesson Type ENUM Validation ==='

-- Should pass: Valid enum values
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (3, 'Test Enum READING', 'READING', '{"passages": []}', 10, 20);

-- Should FAIL: Invalid enum value
\echo 'Trying invalid enum value (should fail)...'
INSERT INTO lessons (section_id, title, lesson_type, content, order_index, duration_minutes)
VALUES (3, 'Test Enum Invalid', 'VIDEO', '{"url": "test.mp4"}', 11, 25);

-- ============================================
-- SUMMARY
-- ============================================
\echo '\n=== Test Summary ==='
SELECT 
    (SELECT COUNT(*) FROM courses) as total_courses,
    (SELECT COUNT(*) FROM sections) as total_sections,
    (SELECT COUNT(*) FROM lessons) as total_lessons;
