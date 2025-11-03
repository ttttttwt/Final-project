-- Verification queries for Learning Paths (Task B1.1 and B1.2)
-- Run these queries in your PostgreSQL client to verify the migration and seed data

-- 1. Verify all 6 learning paths were created
SELECT id, name, cefr_level, is_default, created_at 
FROM learning_paths 
ORDER BY cefr_level;

-- Expected output: 6 rows (A1, A2, B1, B2, C1, C2), all with is_default = true

-- 2. Verify course associations with learning paths
SELECT 
    lp.id as path_id,
    lp.name as path_name,
    lp.cefr_level,
    lpc.order_index,
    c.id as course_id,
    c.title as course_title,
    c.cefr_level as course_level
FROM learning_paths lp
LEFT JOIN learning_path_courses lpc ON lpc.path_id = lp.id
LEFT JOIN courses c ON c.id = lpc.course_id
ORDER BY lp.cefr_level, lpc.order_index;

-- Expected output: Shows all paths with their associated courses

-- 3. Count courses per learning path
SELECT 
    lp.name,
    lp.cefr_level,
    COUNT(lpc.course_id) as course_count
FROM learning_paths lp
LEFT JOIN learning_path_courses lpc ON lpc.path_id = lp.id
GROUP BY lp.id, lp.name, lp.cefr_level
ORDER BY lp.cefr_level;

-- Expected output:
-- A1: 1 course (English Basics A1)
-- A2: 1 course (English Basics A1)
-- B1: 1 course (Intermediate English B1)
-- B2: 2 courses (Intermediate B1 + Advanced C1)
-- C1: 1 course (Advanced English C1)
-- C2: 1 course (Advanced English C1)

-- 4. Verify table structures
\d learning_paths
\d learning_path_courses
\d user_learning_paths

-- 5. Verify indexes
SELECT 
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename IN ('learning_paths', 'learning_path_courses', 'user_learning_paths')
ORDER BY tablename, indexname;

-- 6. Test query: Get recommended path for A1 level
SELECT * FROM learning_paths 
WHERE cefr_level = 'A1' AND is_default = true 
LIMIT 1;

-- 7. Test query: Get all courses in a specific path (with order)
SELECT 
    c.id,
    c.title,
    c.cefr_level,
    lpc.order_index
FROM learning_path_courses lpc
JOIN courses c ON c.id = lpc.course_id
WHERE lpc.path_id = (SELECT id FROM learning_paths WHERE cefr_level = 'B1' LIMIT 1)
ORDER BY lpc.order_index;
