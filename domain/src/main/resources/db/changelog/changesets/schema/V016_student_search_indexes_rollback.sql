-- Rollback V016 — drop search indexes (no data loss).
-- pg_trgm extension NOT dropped (other migrations may depend; safe leftover).
DROP INDEX IF EXISTS idx_diploma_number_trgm;
DROP INDEX IF EXISTS uq_student_meta_uid_university_active;
