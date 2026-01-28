-- =====================================================
-- Rollback S003b: REMOVE MENU PERMISSIONS
-- =====================================================
-- Safe rollback - checks if tables exist first
-- =====================================================

DO $$
DECLARE
    role_perms_exists BOOLEAN;
    perms_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permissions'
    ) INTO role_perms_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'permissions'
    ) INTO perms_exists;

    IF role_perms_exists AND perms_exists THEN
        DELETE FROM role_permissions WHERE permission_id IN (
            SELECT id FROM permissions WHERE code LIKE 'registry.%'
                OR code LIKE 'rating.%' OR code LIKE 'data.%'
                OR code LIKE 'reports.%' OR code LIKE 'system.%'
        );
        RAISE NOTICE 'S003b Rollback: Deleted menu role_permissions';
    END IF;

    IF perms_exists THEN
        DELETE FROM permissions WHERE code LIKE 'registry.%';
        DELETE FROM permissions WHERE code LIKE 'rating.%';
        DELETE FROM permissions WHERE code LIKE 'data.%';
        DELETE FROM permissions WHERE code LIKE 'reports.%' AND code != 'reports.view';
        DELETE FROM permissions WHERE code LIKE 'system.%' AND code NOT IN ('settings.view', 'settings.edit');
        RAISE NOTICE 'S003b Rollback: Deleted menu permissions';
    ELSE
        RAISE NOTICE 'S003b Rollback: Tables do not exist, skipping';
    END IF;
END $$;
