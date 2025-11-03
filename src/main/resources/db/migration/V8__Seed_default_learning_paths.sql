-- V8: Seed default learning paths with course associations
-- Author: LEXIA Team
-- Date: November 3, 2025
-- Sprint: 2, Task: B1.2

-- ============================================
-- Insert 6 default learning paths (A1 to C2)
-- ============================================

-- A1: Beginner Path
INSERT INTO learning_paths (name, description, cefr_level, is_default)
VALUES (
    'Beginner Path (A1)',
    'Perfect for complete beginners. Learn basic vocabulary, simple grammar, and everyday phrases to start your English journey.',
    'A1',
    true
);

-- A2: Elementary Path
INSERT INTO learning_paths (name, description, cefr_level, is_default)
VALUES (
    'Elementary Path (A2)',
    'Build on your basics with more vocabulary and grammar. Learn to handle common social situations and simple conversations.',
    'A2',
    true
);

-- B1: Intermediate Path
INSERT INTO learning_paths (name, description, cefr_level, is_default)
VALUES (
    'Intermediate Path (B1)',
    'Develop confidence in work and social settings. Express opinions, understand main points, and navigate real-world situations.',
    'B1',
    true
);

-- B2: Upper Intermediate Path
INSERT INTO learning_paths (name, description, cefr_level, is_default)
VALUES (
    'Upper Intermediate Path (B2)',
    'Achieve fluency in complex conversations. Understand detailed texts, express ideas clearly, and interact with native speakers confidently.',
    'B2',
    true
);

-- C1: Advanced Path
INSERT INTO learning_paths (name, description, cefr_level, is_default)
VALUES (
    'Advanced Path (C1)',
    'Master professional and academic English. Understand nuanced language, express yourself with precision, and handle specialized content.',
    'C1',
    true
);

-- C2: Proficiency Path
INSERT INTO learning_paths (name, description, cefr_level, is_default)
VALUES (
    'Proficiency Path (C2)',
    'Achieve near-native fluency. Master complex expressions, subtle meanings, and sophisticated language for any context.',
    'C2',
    true
);

-- ============================================
-- Link courses to learning paths
-- Note: Uses course IDs from CourseSeeder (assumes courses exist)
-- ============================================

-- Get path IDs for reference
DO $$
DECLARE
    path_a1_id BIGINT;
    path_a2_id BIGINT;
    path_b1_id BIGINT;
    path_b2_id BIGINT;
    path_c1_id BIGINT;
    path_c2_id BIGINT;
    course_a1_id BIGINT;
    course_b1_id BIGINT;
    course_c1_id BIGINT;
BEGIN
    -- Get learning path IDs
    SELECT id INTO path_a1_id FROM learning_paths WHERE cefr_level = 'A1' AND is_default = true LIMIT 1;
    SELECT id INTO path_a2_id FROM learning_paths WHERE cefr_level = 'A2' AND is_default = true LIMIT 1;
    SELECT id INTO path_b1_id FROM learning_paths WHERE cefr_level = 'B1' AND is_default = true LIMIT 1;
    SELECT id INTO path_b2_id FROM learning_paths WHERE cefr_level = 'B2' AND is_default = true LIMIT 1;
    SELECT id INTO path_c1_id FROM learning_paths WHERE cefr_level = 'C1' AND is_default = true LIMIT 1;
    SELECT id INTO path_c2_id FROM learning_paths WHERE cefr_level = 'C2' AND is_default = true LIMIT 1;

    -- Get course IDs (from CourseSeeder)
    SELECT id INTO course_a1_id FROM courses WHERE title = 'English Basics (A1)' LIMIT 1;
    SELECT id INTO course_b1_id FROM courses WHERE title = 'Intermediate English (B1)' LIMIT 1;
    SELECT id INTO course_c1_id FROM courses WHERE title = 'Advanced English (C1)' LIMIT 1;

    -- A1 Path: English Basics (A1)
    IF path_a1_id IS NOT NULL AND course_a1_id IS NOT NULL THEN
        INSERT INTO learning_path_courses (path_id, course_id, order_index)
        VALUES (path_a1_id, course_a1_id, 0);
    END IF;

    -- A2 Path: English Basics (A1) -> Intermediate English (B1)
    -- Note: A2-specific courses will be added in future sprints
    IF path_a2_id IS NOT NULL THEN
        IF course_a1_id IS NOT NULL THEN
            INSERT INTO learning_path_courses (path_id, course_id, order_index)
            VALUES (path_a2_id, course_a1_id, 0);
        END IF;
    END IF;

    -- B1 Path: Intermediate English (B1)
    IF path_b1_id IS NOT NULL AND course_b1_id IS NOT NULL THEN
        INSERT INTO learning_path_courses (path_id, course_id, order_index)
        VALUES (path_b1_id, course_b1_id, 0);
    END IF;

    -- B2 Path: Intermediate English (B1) -> Advanced English (C1)
    -- Note: B2-specific courses will be added in future sprints
    IF path_b2_id IS NOT NULL THEN
        IF course_b1_id IS NOT NULL THEN
            INSERT INTO learning_path_courses (path_id, course_id, order_index)
            VALUES (path_b2_id, course_b1_id, 0);
        END IF;
        IF course_c1_id IS NOT NULL THEN
            INSERT INTO learning_path_courses (path_id, course_id, order_index)
            VALUES (path_b2_id, course_c1_id, 1);
        END IF;
    END IF;

    -- C1 Path: Advanced English (C1)
    IF path_c1_id IS NOT NULL AND course_c1_id IS NOT NULL THEN
        INSERT INTO learning_path_courses (path_id, course_id, order_index)
        VALUES (path_c1_id, course_c1_id, 0);
    END IF;

    -- C2 Path: Advanced English (C1)
    -- Note: C2-specific courses will be added in future sprints
    IF path_c2_id IS NOT NULL AND course_c1_id IS NOT NULL THEN
        INSERT INTO learning_path_courses (path_id, course_id, order_index)
        VALUES (path_c2_id, course_c1_id, 0);
    END IF;

END $$;

-- ============================================
-- Verification queries (run manually to verify seed data)
-- ============================================

-- Verify all 6 paths created
-- SELECT id, name, cefr_level, is_default FROM learning_paths ORDER BY cefr_level;

-- Verify course associations
-- SELECT 
--     lp.name as path_name,
--     lp.cefr_level,
--     lpc.order_index,
--     c.title as course_title,
--     c.cefr_level as course_level
-- FROM learning_paths lp
-- LEFT JOIN learning_path_courses lpc ON lpc.path_id = lp.id
-- LEFT JOIN courses c ON c.id = lpc.course_id
-- ORDER BY lp.cefr_level, lpc.order_index;

-- Count courses per path
-- SELECT 
--     lp.name,
--     lp.cefr_level,
--     COUNT(lpc.course_id) as course_count
-- FROM learning_paths lp
-- LEFT JOIN learning_path_courses lpc ON lpc.path_id = lp.id
-- GROUP BY lp.id, lp.name, lp.cefr_level
-- ORDER BY lp.cefr_level;
