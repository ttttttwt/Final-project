-- Kiểm tra Learning Paths
\echo '========== 1. ALL LEARNING PATHS =========='
SELECT id, name, cefr_level, is_default, created_at 
FROM learning_paths 
ORDER BY cefr_level;

\echo ''
\echo '========== 2. LEARNING PATH COURSES =========='
SELECT 
    lp.name as path_name,
    lp.cefr_level,
    c.title as course_title,
    lpc.order_index
FROM learning_paths lp
JOIN learning_path_courses lpc ON lp.id = lpc.path_id
JOIN courses c ON lpc.course_id = c.id
ORDER BY lp.cefr_level, lpc.order_index;

\echo ''
\echo '========== 3. COURSES PER PATH COUNT =========='
SELECT 
    lp.name as path_name,
    lp.cefr_level,
    COUNT(lpc.course_id) as course_count
FROM learning_paths lp
LEFT JOIN learning_path_courses lpc ON lp.id = lpc.path_id
GROUP BY lp.id, lp.name, lp.cefr_level
ORDER BY lp.cefr_level;

\echo ''
\echo '========== 4. USER LEARNING PATHS (Should be empty) =========='
SELECT COUNT(*) as user_path_count FROM user_learning_paths;

\echo ''
\echo '========== 5. DATABASE TABLES =========='
\dt learning*
