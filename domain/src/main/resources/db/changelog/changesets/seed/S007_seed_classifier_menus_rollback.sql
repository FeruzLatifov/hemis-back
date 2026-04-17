-- =====================================================
-- Rollback S007: REMOVE CLASSIFIER MENU ENTRIES
-- =====================================================
-- Deletes the 3 classifier sub-menu entries created by S007.
-- =====================================================

DO $$
DECLARE
    menus_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'menu'
    ) INTO menus_exists;

    IF menus_exists THEN
        DELETE FROM menu WHERE code IN ('cls-financial', 'cls-diploma', 'cls-speciality');
        RAISE NOTICE 'S007 Rollback: Deleted 3 classifier menu entries';
    ELSE
        RAISE NOTICE 'S007 Rollback: menus table does not exist, skipping';
    END IF;
END $$;
