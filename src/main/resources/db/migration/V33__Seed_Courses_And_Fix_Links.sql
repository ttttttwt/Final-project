-- V33: Seed courses and fix learning path links
-- Author: LEXIA Team
-- Date: December 18, 2025

-- ============================================
-- Ensure Courses Exist
-- ============================================

DO $$
DECLARE
    course_a1_id BIGINT;
    course_b1_id BIGINT;
    course_c1_id BIGINT;
    section_id BIGINT;
    path_a1_id BIGINT;
    path_b1_id BIGINT;
    path_c1_id BIGINT;
BEGIN
    -- 1. Create English Basics (A1) if not exists
    SELECT id INTO course_a1_id FROM courses WHERE title = 'English Basics (A1)' LIMIT 1;
    
    IF course_a1_id IS NULL THEN
        INSERT INTO courses (title, description, cefr_level, thumbnail_url, is_published, created_at, updated_at)
        VALUES (
            'English Basics (A1)',
            'Master the fundamentals of English for everyday communication. This course covers basic grammar, vocabulary, and conversational skills.',
            'A1',
            'https://i.imgur.com/5d2tGgU.png',
            true,
            NOW(),
            NOW()
        ) RETURNING id INTO course_a1_id;

        -- Add Section
        INSERT INTO sections (course_id, title, order_index, created_at)
        VALUES (course_a1_id, 'Getting Started', 0, NOW()) RETURNING id INTO section_id;

        -- Add Lessons
        INSERT INTO lessons (section_id, title, order_index, lesson_type, duration_minutes, content, created_at, updated_at)
        VALUES 
        (section_id, 'Alphabet and Greetings', 0, 'READING', 5, '{"content": "Learn the alphabet and basic greetings."}', NOW(), NOW()),
        (section_id, 'Basic Introductions', 1, 'SPEAKING', 10, '{"content": "Practice introducing yourself."}', NOW(), NOW());
    END IF;

    -- 2. Create Intermediate English (B1) if not exists
    SELECT id INTO course_b1_id FROM courses WHERE title = 'Intermediate English (B1)' LIMIT 1;
    
    IF course_b1_id IS NULL THEN
        INSERT INTO courses (title, description, cefr_level, thumbnail_url, is_published, created_at, updated_at)
        VALUES (
            'Intermediate English (B1)',
            'Expand your English skills for work and social situations. Focus on more complex grammar, expressing opinions, and understanding native speakers.',
            'B1',
            'https://i.imgur.com/6y3tHjV.png',
            true,
            NOW(),
            NOW()
        ) RETURNING id INTO course_b1_id;

        -- Add Section
        INSERT INTO sections (course_id, title, order_index, created_at)
        VALUES (course_b1_id, 'Work and Career', 0, NOW()) RETURNING id INTO section_id;

        -- Add Lessons
        INSERT INTO lessons (section_id, title, order_index, lesson_type, duration_minutes, content, created_at, updated_at)
        VALUES 
        (section_id, 'Writing a Professional Email', 0, 'READING', 15, '{"content": "Learn email etiquette."}', NOW(), NOW()),
        (section_id, 'Job Interview Practice', 1, 'SPEAKING', 20, '{"content": "Practice common interview questions."}', NOW(), NOW());
    END IF;

    -- 3. Create Advanced English (C1) if not exists
    SELECT id INTO course_c1_id FROM courses WHERE title = 'Advanced English (C1)' LIMIT 1;
    
    IF course_c1_id IS NULL THEN
        INSERT INTO courses (title, description, cefr_level, thumbnail_url, is_published, created_at, updated_at)
        VALUES (
            'Advanced English (C1)',
            'Master professional and academic English. Understand nuanced language, express yourself with precision, and handle specialized content.',
            'C1',
            'https://i.imgur.com/1t2x3y4.png',
            true,
            NOW(),
            NOW()
        ) RETURNING id INTO course_c1_id;

        -- Add Section
        INSERT INTO sections (course_id, title, order_index, created_at)
        VALUES (course_c1_id, 'Academic Writing', 0, NOW()) RETURNING id INTO section_id;

        -- Add Lessons
        INSERT INTO lessons (section_id, title, order_index, lesson_type, duration_minutes, content, created_at, updated_at)
        VALUES 
        (section_id, 'Structuring an Essay', 0, 'READING', 20, '{"content": "Learn essay structure."}', NOW(), NOW()),
        (section_id, 'Debating Complex Topics', 1, 'SPEAKING', 25, '{"content": "Practice debating skills."}', NOW(), NOW());
    END IF;

    -- ============================================
    -- Link Courses to Learning Paths
    -- ============================================

    -- Get Path IDs
    SELECT id INTO path_a1_id FROM learning_paths WHERE cefr_level = 'A1' AND is_default = true LIMIT 1;
    SELECT id INTO path_b1_id FROM learning_paths WHERE cefr_level = 'B1' AND is_default = true LIMIT 1;
    SELECT id INTO path_c1_id FROM learning_paths WHERE cefr_level = 'C1' AND is_default = true LIMIT 1;

    -- Link A1 Course to A1 Path (if not exists)
    IF path_a1_id IS NOT NULL AND course_a1_id IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM learning_path_courses WHERE path_id = path_a1_id AND course_id = course_a1_id) THEN
            INSERT INTO learning_path_courses (path_id, course_id, order_index) VALUES (path_a1_id, course_a1_id, 0);
        END IF;
    END IF;

    -- Link B1 Course to B1 Path (if not exists)
    IF path_b1_id IS NOT NULL AND course_b1_id IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM learning_path_courses WHERE path_id = path_b1_id AND course_id = course_b1_id) THEN
            INSERT INTO learning_path_courses (path_id, course_id, order_index) VALUES (path_b1_id, course_b1_id, 0);
        END IF;
    END IF;

    -- Link C1 Course to C1 Path (if not exists)
    IF path_c1_id IS NOT NULL AND course_c1_id IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM learning_path_courses WHERE path_id = path_c1_id AND course_id = course_c1_id) THEN
            INSERT INTO learning_path_courses (path_id, course_id, order_index) VALUES (path_c1_id, course_c1_id, 0);
        END IF;
    END IF;

END $$;
