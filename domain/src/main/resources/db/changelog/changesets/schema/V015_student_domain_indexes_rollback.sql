-- Rollback V015 — drop performance indexes (no data loss).
DROP INDEX IF EXISTS idx_grade_university_active;
DROP INDEX IF EXISTS idx_grade_student_active;
DROP INDEX IF EXISTS idx_attendance_student_active;
DROP INDEX IF EXISTS idx_attendance_uni_date;
DROP INDEX IF EXISTS uq_student_pinfl_master;
DROP INDEX IF EXISTS idx_student_university_status_createts;
