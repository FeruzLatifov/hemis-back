-- =====================================================
-- Rollback V008: DROP MENUS TABLE
-- =====================================================
-- Self-referencing table (parent_id)
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM menus;

    -- GUARD: Block if more than 500 menus (production customizations)
    IF row_count > 500 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: menus table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    RAISE NOTICE 'V008 Rollback: Dropping menus table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS menus CASCADE;
