-- =====================================================
-- V3.5 ROLLBACK: Revert Permission Constraint
-- =====================================================

-- Drop constraint with REPORTS
ALTER TABLE permissions DROP CONSTRAINT IF EXISTS chk_permission_category;

-- Add back old constraint (without REPORTS)
ALTER TABLE permissions ADD CONSTRAINT chk_permission_category CHECK (
    category IN ('CORE', 'ADMIN', 'CUSTOM')
);

DO $$
BEGIN
    RAISE NOTICE '✅ Permission constraint rolled back to: CORE, ADMIN, CUSTOM';
END $$;
