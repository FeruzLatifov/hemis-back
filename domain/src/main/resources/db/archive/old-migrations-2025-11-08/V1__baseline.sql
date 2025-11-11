-- =====================================================
-- V1__baseline.sql - DOCUMENTATION ONLY
-- =====================================================
-- Purpose: Golden baseline snapshot of ministry.sql database
-- Database: test_hemis (PostgreSQL 18.0)
-- Generated: 2025-10-30T05:15:00Z
-- =====================================================

-- CRITICAL: THIS FILE IS NOT EXECUTED BY FLYWAY
-- 
-- This is a DOCUMENTATION-ONLY reference of the existing database schema.
-- The database was migrated from old-hemis (CUBA Platform) and already
-- contains all necessary tables, indexes, constraints, and sequences.
-- 
-- Flyway is configured to:
--   1. baseline-on-migrate: true  -- Accept existing schema
--   2. baseline-version: 1        -- Start from version 1
--   3. enabled: false (in dev)    -- Only enable for new migrations
--
-- =====================================================

-- DATABASE STATISTICS
-- =====================================================
-- Tables:       289
-- Views:        14
-- Sequences:    16
-- Indexes:      815
-- Constraints:  1624
-- Total Objects: 2758
-- =====================================================

-- IMMUTABILITY POLICY
-- =====================================================
-- PROHIBITED:
--   - DROP TABLE / DROP COLUMN / DROP INDEX
--   - ALTER TABLE ... RENAME
--   - TRUNCATE
--   - DELETE (use UPDATE with deleted_ts instead)
--
-- ALLOWED:
--   - CREATE INDEX CONCURRENTLY
--   - CREATE VIEW
--   - ALTER TABLE ADD COLUMN (nullable or with default)
--   - GRANT/REVOKE
--   - COMMENT ON
-- =====================================================

-- KEY ENTITY TABLES (Sample - not complete list)
-- =====================================================

-- Students
-- hemishe_e_student (id UUID PRIMARY KEY, code VARCHAR, ...)
-- hemishe_e_student_gpa
-- hemishe_e_student_diploma
-- hemishe_e_student_scholarship

-- Teachers
-- hemishe_e_teacher (id UUID PRIMARY KEY, code VARCHAR, ...)
-- hemishe_e_employee_job
-- hemishe_e_teacher_qualification

-- Universities
-- hemishe_e_university (id UUID PRIMARY KEY, code VARCHAR, ...)
-- hemishe_e_faculty
-- hemishe_e_cathedry (departments)
-- hemishe_e_university_department
-- hemishe_e_university_group

-- Doctoral
-- hemishe_e_doctorate_student
-- hemishe_e_dissertation_defense
-- hemishe_e_project
-- hemishe_e_publication_scientific

-- Diplomas
-- hemishe_e_diplom_blank
-- hemishe_e_diplom_blank_distribution

-- Social Register
-- hemishe_e_single_social_register
-- hemishe_e_socialregister_checked

-- ~100+ Classifier tables (hemishe_h_*)
-- ~289 Total tables

-- =====================================================

-- AUDIT COLUMNS (Standard Pattern)
-- =====================================================
-- All entity tables include:
--   id UUID PRIMARY KEY DEFAULT gen_random_uuid()
--   code VARCHAR(50)              -- Business key
--   create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
--   created_by UUID
--   update_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
--   updated_by UUID
--   version INTEGER DEFAULT 1
--   delete_ts TIMESTAMP            -- Soft delete (NDG)
--   deleted_by UUID
--   _university UUID               -- Multi-tenancy
-- =====================================================

-- FOREIGN KEY CONSTRAINTS
-- =====================================================
-- Example relationships:
--   hemishe_e_student._university -> hemishe_e_university.id
--   hemishe_e_student.faculty_id -> hemishe_e_faculty.id
--   hemishe_e_student.specialty_id -> hemishe_e_speciality.id
--   hemishe_e_teacher.department_id -> hemishe_e_cathedry.id
-- 
-- CRITICAL: All foreign keys have ON DELETE RESTRICT (NDG enforcement)
-- =====================================================

-- INDEXES
-- =====================================================
-- 815 indexes total, including:
--   - Primary key indexes (auto-created)
--   - Foreign key indexes (performance)
--   - Partial indexes for soft-delete: WHERE delete_ts IS NULL
--   - Unique constraints: code, pinfl, passport numbers
-- =====================================================

-- SEQUENCES
-- =====================================================
-- 16 sequences for code generation:
--   hemishe_e_student_code_seq
--   hemishe_e_teacher_code_seq
--   hemishe_e_university_code_seq
--   hemishe_e_diplom_blank_seq
--   ... (12 more)
-- =====================================================

-- VIEWS
-- =====================================================
-- 14 views for reporting and compatibility:
--   v_student_full
--   v_teacher_full
--   v_university_stats
--   ... (11 more)
-- =====================================================

-- FULL DDL EXTRACTION COMMAND (For Reference)
-- =====================================================
-- To extract complete DDL:
-- 
-- pg_dump -h localhost -U postgres -d test_hemis \
--     --schema-only \
--     --no-owner \
--     --no-privileges \
--     --no-tablespaces \
--     > V1__baseline_reference.sql
-- 
-- NOTE: This file would be 50,000+ lines and too large for VCS.
-- Instead, see docs/DB_SCHEMA_CATALOG.md for structured documentation.
-- =====================================================

-- VERIFICATION QUERY
-- =====================================================
-- Verify baseline is recorded in Flyway history:
-- 
-- SELECT version, description, type, installed_on
-- FROM flyway_schema_history
-- WHERE version = '1';
-- 
-- Expected result:
--   version | description | type     | installed_on
--   --------|-------------|----------|---------------
--   1       | baseline    | BASELINE | 2025-10-30...
-- =====================================================

-- END OF V1__baseline.sql (DOCUMENTATION ONLY)
