-- ============================================
-- Verification Script for V9 and V10 Migrations
-- Tasks: C1.1 (Enrollments) and C1.2 (Lesson Progress)
-- Date: November 5, 2025
-- ============================================

\echo '=========================================='
\echo 'MIGRATION STATUS CHECK'
\echo '=========================================='

-- Check current Flyway version
SELECT version, description, installed_on, success
FROM flyway_schema_history
WHERE version IN ('9', '10')
ORDER BY version;

\echo ''
\echo '=========================================='
\echo 'TABLE STRUCTURE: enrollments'
\echo '=========================================='

-- Describe enrollments table
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'enrollments'
ORDER BY ordinal_position;

\echo ''
\echo 'Enrollments Table Constraints:'
SELECT
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
LEFT JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.table_name = 'enrollments'
ORDER BY tc.constraint_type, tc.constraint_name;

\echo ''
\echo 'Enrollments Table Indexes:'
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'enrollments'
ORDER BY indexname;

\echo ''
\echo '=========================================='
\echo 'TABLE STRUCTURE: lesson_progress'
\echo '=========================================='

-- Describe lesson_progress table
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'lesson_progress'
ORDER BY ordinal_position;

\echo ''
\echo 'Lesson Progress Table Constraints:'
SELECT
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
LEFT JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.table_name = 'lesson_progress'
ORDER BY tc.constraint_type, tc.constraint_name;

\echo ''
\echo 'Lesson Progress Table Indexes:'
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'lesson_progress'
ORDER BY indexname;

\echo ''
\echo '=========================================='
\echo 'TABLE COMMENTS'
\echo '=========================================='

-- Check table comments
SELECT 
    c.relname AS table_name,
    pg_catalog.obj_description(c.oid) AS table_comment
FROM pg_catalog.pg_class c
WHERE c.relname IN ('enrollments', 'lesson_progress')
    AND c.relkind = 'r'
ORDER BY c.relname;

\echo ''
\echo '=========================================='
\echo 'COLUMN COMMENTS (enrollments)'
\echo '=========================================='

SELECT 
    cols.column_name,
    pg_catalog.col_description(c.oid, cols.ordinal_position::int) AS column_comment
FROM information_schema.columns cols
JOIN pg_catalog.pg_class c ON c.relname = cols.table_name
WHERE cols.table_name = 'enrollments'
ORDER BY cols.ordinal_position;

\echo ''
\echo '=========================================='
\echo 'COLUMN COMMENTS (lesson_progress)'
\echo '=========================================='

SELECT 
    cols.column_name,
    pg_catalog.col_description(c.oid, cols.ordinal_position::int) AS column_comment
FROM information_schema.columns cols
JOIN pg_catalog.pg_class c ON c.relname = cols.table_name
WHERE cols.table_name = 'lesson_progress'
ORDER BY cols.ordinal_position;

\echo ''
\echo '=========================================='
\echo 'DATA VERIFICATION'
\echo '=========================================='

-- Check if tables are empty (expected for fresh migrations)
SELECT 'enrollments' AS table_name, COUNT(*) AS row_count FROM enrollments
UNION ALL
SELECT 'lesson_progress' AS table_name, COUNT(*) AS row_count FROM lesson_progress;

\echo ''
\echo '=========================================='
\echo 'FOREIGN KEY RELATIONSHIPS'
\echo '=========================================='

-- Verify foreign key relationships
SELECT
    tc.table_name, 
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    rc.delete_rule,
    rc.update_rule
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
JOIN information_schema.referential_constraints AS rc
    ON rc.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY' 
    AND tc.table_name IN ('enrollments', 'lesson_progress')
ORDER BY tc.table_name, kcu.column_name;

\echo ''
\echo '=========================================='
\echo 'CHECK CONSTRAINTS'
\echo '=========================================='

-- Verify CHECK constraints
SELECT
    tc.table_name,
    tc.constraint_name,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc
    ON tc.constraint_name = cc.constraint_name
WHERE tc.table_name IN ('enrollments', 'lesson_progress')
    AND tc.constraint_type = 'CHECK'
ORDER BY tc.table_name, tc.constraint_name;

\echo ''
\echo '=========================================='
\echo 'VERIFICATION COMPLETE'
\echo '=========================================='
