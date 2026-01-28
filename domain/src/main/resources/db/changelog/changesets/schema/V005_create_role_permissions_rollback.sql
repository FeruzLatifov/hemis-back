-- =====================================================
-- Rollback V005: DROP ROLE_PERMISSIONS TABLE
-- =====================================================
-- Junction table - no CASCADE dependencies
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM role_permissions;

    -- GUARD: Block if more than 5000 mappings (production scale)
    IF row_count > 5000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: role_permissions table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    RAISE NOTICE 'V005 Rollback: Dropping role_permissions table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS role_permissions CASCADE;
