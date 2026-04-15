-- =====================================================
-- Rollback V008: DROP MENU MODULE TABLES
-- =====================================================
-- Drops: user_favorites, menus (CASCADE)
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM menus;
    IF row_count > 500 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: menus table has % rows. Production detected.', row_count;
    END IF;
    RAISE NOTICE 'V008 Rollback: Dropping menu tables (% menus)', row_count;
END $$;

DROP TABLE IF EXISTS user_favorites;
DROP TRIGGER IF EXISTS trigger_menus_updated_at ON menus;
DROP FUNCTION IF EXISTS update_menus_updated_at();
DROP TABLE IF EXISTS menus CASCADE;
