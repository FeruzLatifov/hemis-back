-- =====================================================
-- Rollback V004: DROP employee + employee_job + employee_academic_credential
-- =====================================================
-- Self-contained: only drops own tables. FK constraints owned by other migrations
-- (V005 university_legal/_founder, V006 users) are dropped CASCADE-style by the
-- DROP TABLE below.
-- =====================================================

DROP TABLE IF EXISTS employee_academic_credential CASCADE;
DROP TABLE IF EXISTS employee_job                 CASCADE;
DROP TABLE IF EXISTS employee                     CASCADE;
