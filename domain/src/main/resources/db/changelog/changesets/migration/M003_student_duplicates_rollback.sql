-- Rollback M003: Remove student duplicates feature
DROP MATERIALIZED VIEW IF EXISTS mv_student_duplicates;
-- idx_student_dup_analysis M002b changeset'ida yaratiladi/rollback qilinadi (bu yerda emas).
DELETE FROM menu WHERE code = 'student-duplicates';
UPDATE menu SET url = NULL, updated_at = CURRENT_TIMESTAMP WHERE code = 'students';
