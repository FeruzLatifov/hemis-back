-- =====================================================
-- M003 ROLLBACK: Restore original filenames (not recommended)
-- =====================================================
-- Note: This rollback is intentionally empty because:
-- 1. The original state was inconsistent (mixed filenames)
-- 2. logicalFilePath now handles this consistently
-- 3. Reverting would re-introduce the inconsistency bug
-- =====================================================

-- No action needed - logicalFilePath in db.changelog-master.yaml handles this
DO $$
BEGIN
    RAISE NOTICE 'M003 Rollback: No action taken - logicalFilePath handles consistency';
END $$;
