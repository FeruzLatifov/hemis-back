-- =====================================================
-- M003: FIX CHANGELOG FILENAMES - Standardize to logicalFilePath
-- =====================================================
-- Author: hemis-team
-- Date: 2025-11-26
-- Purpose: Standardize all databasechangelog filenames to match logicalFilePath
-- Root Cause: Different paths used by Gradle tasks vs Spring Boot
--   - Gradle: src/main/resources/db/changelog/db.changelog-master.yaml
--   - Spring: db/changelog/db.changelog-master.yaml
-- Solution: All changesets now use logicalFilePath property
-- =====================================================

-- Update all existing changelog entries to use the standard logical path
UPDATE databasechangelog
SET filename = 'db/changelog/db.changelog-master.yaml'
WHERE filename IN (
    'src/main/resources/db/changelog/db.changelog-master.yaml',
    'classpath:db/changelog/db.changelog-master.yaml'
);

-- Verification
DO $$
DECLARE
    distinct_filenames INTEGER;
    expected_filename TEXT := 'db/changelog/db.changelog-master.yaml';
BEGIN
    SELECT COUNT(DISTINCT filename) INTO distinct_filenames
    FROM databasechangelog
    WHERE filename != expected_filename;

    IF distinct_filenames > 0 THEN
        RAISE WARNING 'M003: Found % non-standard filename(s) in databasechangelog', distinct_filenames;
    ELSE
        RAISE NOTICE 'M003: All changelog filenames standardized to %', expected_filename;
    END IF;
END $$;
