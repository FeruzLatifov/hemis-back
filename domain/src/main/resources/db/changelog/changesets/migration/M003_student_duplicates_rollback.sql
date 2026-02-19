-- Rollback M003: Remove student duplicates feature
DROP MATERIALIZED VIEW IF EXISTS mv_student_duplicates;
DROP INDEX IF EXISTS idx_student_dup_analysis;
DELETE FROM menus WHERE code = 'student-duplicates';
UPDATE menus SET url = NULL, updated_at = CURRENT_TIMESTAMP WHERE code = 'students';
