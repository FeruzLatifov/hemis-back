-- =====================================================
-- Rollback V004: DROP USER_ROLES TABLE
-- =====================================================
-- Junction table - no CASCADE dependencies
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM user_roles;

    -- GUARD: Block if more than 2000 assignments (production scale)
    IF row_count > 2000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: user_roles table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    RAISE NOTICE 'V004 Rollback: Dropping user_roles table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS user_roles CASCADE;
