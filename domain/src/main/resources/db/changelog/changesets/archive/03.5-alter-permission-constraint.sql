-- =====================================================
-- V3.5: ALTER Permission Category Constraint
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20
-- Purpose: Add 'REPORTS' category to permission constraint
--
-- WHY: V4 menu permissions use 'REPORTS' category
-- FIX: Alter existing constraint to include 'REPORTS'
-- =====================================================

-- Drop old constraint
ALTER TABLE permissions DROP CONSTRAINT IF EXISTS chk_permission_category;

-- Add new constraint with 'REPORTS'
ALTER TABLE permissions ADD CONSTRAINT chk_permission_category CHECK (
    category IN ('CORE', 'ADMIN', 'CUSTOM', 'REPORTS')
);

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Permission category constraint updated';
    RAISE NOTICE '   Now allows: CORE, ADMIN, CUSTOM, REPORTS';
END $$;
