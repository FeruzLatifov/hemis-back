-- =====================================================
-- Rollback V008: DROP MENUS TABLE
-- =====================================================
-- Note: user_favorites (V008b) has its own rollback script
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

-- Drop trigger first
DROP TRIGGER IF EXISTS trigger_menus_updated_at ON menus;
DROP FUNCTION IF EXISTS update_menus_updated_at();

DROP TABLE IF EXISTS menus CASCADE;
