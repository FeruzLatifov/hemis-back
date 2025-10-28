-- =====================================================
-- HEMIS Backend - Add isDuplicate Field
-- =====================================================
-- Version: V3
-- Purpose: Add duplicate detection field to Student table
--
-- Background:
-- Old-HEMIS allows duplicate PINFL entries for students.
-- This is managed via the isDuplicate flag:
-- - TRUE: Master record (active/current student)
-- - FALSE: Duplicate record (historical/transferred student)
--
-- CRITICAL: PINFL is NOT UNIQUE for students!
-- Multiple students can have same PINFL.
--
-- MASTER PROMPT Compliance:
-- ✅ NO-RENAME: No existing columns renamed
-- ✅ NO-DELETE: No data deleted
-- ✅ NO-BREAKING-CHANGES: New column, no API changes
-- ✅ REPLICATION-SAFE: Simple ADD COLUMN operation
-- =====================================================

-- =====================================================
-- Step 1: Add is_duplicate Column to Student Table
-- =====================================================

-- Add column with default value FALSE
ALTER TABLE hemishe_e_student
ADD COLUMN IF NOT EXISTS is_duplicate BOOLEAN DEFAULT FALSE;

-- Comment for documentation
COMMENT ON COLUMN hemishe_e_student.is_duplicate IS
'Duplicate detection flag: TRUE = master record, FALSE = duplicate record. Only one student per PINFL can have isDuplicate=true.';

-- =====================================================
-- Step 2: Initialize Existing Data
-- =====================================================
-- For existing students, mark the FIRST student (by create_ts)
-- per PINFL as master record (isDuplicate = TRUE)
-- =====================================================

-- Create temporary ranking
WITH ranked_students AS (
    SELECT
        id,
        pinfl,
        ROW_NUMBER() OVER (
            PARTITION BY pinfl
            ORDER BY create_ts ASC NULLS LAST, id ASC
        ) as rank_number
    FROM hemishe_e_student
    WHERE pinfl IS NOT NULL
      AND pinfl <> ''
      AND delete_ts IS NULL
)
UPDATE hemishe_e_student s
SET is_duplicate = CASE
    WHEN rs.rank_number = 1 THEN TRUE   -- First student = master
    ELSE FALSE                           -- Others = duplicates
END
FROM ranked_students rs
WHERE s.id = rs.id;

-- =====================================================
-- Step 3: Create Performance Index
-- =====================================================

-- Index for finding master record by PINFL
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_pinfl_duplicate
ON hemishe_e_student (pinfl, is_duplicate)
WHERE delete_ts IS NULL;

-- Index for duplicate queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_duplicate_flag
ON hemishe_e_student (is_duplicate)
WHERE delete_ts IS NULL AND is_duplicate = TRUE;

-- =====================================================
-- Step 4: Validation Query
-- =====================================================
-- Run this to verify migration success
-- =====================================================

-- Count duplicates per PINFL
-- Expected: Each PINFL should have maximum 1 master (isDuplicate=true)
DO $$
DECLARE
    duplicate_count INTEGER;
    violation_count INTEGER;
BEGIN
    -- Count PINFLs with multiple masters (violation!)
    SELECT COUNT(*)
    INTO violation_count
    FROM (
        SELECT pinfl, COUNT(*) as master_count
        FROM hemishe_e_student
        WHERE is_duplicate = TRUE
          AND delete_ts IS NULL
          AND pinfl IS NOT NULL
        GROUP BY pinfl
        HAVING COUNT(*) > 1
    ) violations;

    IF violation_count > 0 THEN
        RAISE WARNING 'Found % PINFLs with multiple master records!', violation_count;
    ELSE
        RAISE NOTICE 'Migration successful: All PINFLs have at most one master record.';
    END IF;

    -- Count total duplicates
    SELECT COUNT(*)
    INTO duplicate_count
    FROM (
        SELECT pinfl, COUNT(*) as total_count
        FROM hemishe_e_student
        WHERE delete_ts IS NULL
          AND pinfl IS NOT NULL
        GROUP BY pinfl
        HAVING COUNT(*) > 1
    ) duplicates;

    RAISE NOTICE 'Found % PINFLs with duplicate students.', duplicate_count;
END $$;

-- =====================================================
-- Step 5: Statistics
-- =====================================================

-- Show duplicate statistics
SELECT
    'Total Students' as metric,
    COUNT(*) as count
FROM hemishe_e_student
WHERE delete_ts IS NULL

UNION ALL

SELECT
    'Students with PINFL' as metric,
    COUNT(*) as count
FROM hemishe_e_student
WHERE delete_ts IS NULL AND pinfl IS NOT NULL

UNION ALL

SELECT
    'Master Records (isDuplicate=true)' as metric,
    COUNT(*) as count
FROM hemishe_e_student
WHERE delete_ts IS NULL AND is_duplicate = TRUE

UNION ALL

SELECT
    'Duplicate Records (isDuplicate=false)' as metric,
    COUNT(*) as count
FROM hemishe_e_student
WHERE delete_ts IS NULL AND is_duplicate = FALSE

UNION ALL

SELECT
    'PINFLs with Duplicates' as metric,
    COUNT(*) as count
FROM (
    SELECT pinfl
    FROM hemishe_e_student
    WHERE delete_ts IS NULL AND pinfl IS NOT NULL
    GROUP BY pinfl
    HAVING COUNT(*) > 1
) dup;

-- =====================================================
-- Step 6: Example Query - Find Duplicate Students
-- =====================================================

-- Find all students with duplicate PINFLs
-- Uncomment to run:

-- SELECT
--     pinfl,
--     COUNT(*) as duplicate_count,
--     STRING_AGG(code, ', ' ORDER BY is_duplicate DESC, create_ts) as student_codes,
--     STRING_AGG(university, ', ' ORDER BY is_duplicate DESC) as universities
-- FROM hemishe_e_student
-- WHERE delete_ts IS NULL
--   AND pinfl IS NOT NULL
-- GROUP BY pinfl
-- HAVING COUNT(*) > 1
-- ORDER BY duplicate_count DESC
-- LIMIT 100;

-- =====================================================
-- Migration Complete
-- =====================================================
-- isDuplicate field added successfully
-- Existing data initialized (first student per PINFL = master)
-- Indexes created for performance
-- =====================================================

-- =====================================================
-- IMPORTANT NOTES
-- =====================================================
-- 1. DO NOT add UNIQUE constraint on PINFL!
--    Old-HEMIS allows duplicate PINFLs by design.
--
-- 2. Only ONE student per PINFL should have isDuplicate=true
--    This is the "master" or "active" record.
--
-- 3. When creating new student with existing PINFL:
--    - Option A: Return existing master student
--    - Option B: Set old master to isDuplicate=false,
--                create new master with isDuplicate=true
--
-- 4. Application layer (StudentService) must enforce
--    the isDuplicate business rules.
-- =====================================================
