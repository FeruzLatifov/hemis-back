-- =====================================================
-- Rollback S002: REMOVE BASE PERMISSIONS (30)
-- =====================================================
-- Deletes the 30 base permissions (15 CORE + 15 ADMIN) created by S002.
-- Removes role_permissions FK references first, then the permissions.
-- =====================================================

DO $$
DECLARE
    role_perms_exists BOOLEAN;
    perms_exists BOOLEAN;
    _codes text[] := ARRAY[
        -- CORE (15)
        'dashboard.view',
        'students.view', 'students.create', 'students.edit', 'students.delete', 'students.export',
        'teachers.view', 'teachers.create', 'teachers.edit', 'teachers.delete', 'teachers.export',
        'universities.view',
        'reports.view', 'reports.create', 'reports.export',
        -- ADMIN (15)
        'universities.create', 'universities.edit', 'universities.delete',
        'users.view', 'users.create', 'users.edit', 'users.delete',
        'roles.view', 'roles.create', 'roles.edit', 'roles.delete',
        'permissions.view', 'permissions.manage',
        'settings.view', 'settings.edit'
    ];
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permission'
    ) INTO role_perms_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'permission'
    ) INTO perms_exists;

    -- Remove FK references first
    IF role_perms_exists AND perms_exists THEN
        DELETE FROM role_permission WHERE permission_id IN (
            SELECT id FROM permission WHERE code = ANY(_codes)
        );
        RAISE NOTICE 'S002 Rollback: Deleted role_permissions for 30 base permissions';
    END IF;

    -- Remove the permissions
    IF perms_exists THEN
        DELETE FROM permission WHERE code = ANY(_codes);
        RAISE NOTICE 'S002 Rollback: Deleted 30 base permissions';
    ELSE
        RAISE NOTICE 'S002 Rollback: permissions table does not exist, skipping';
    END IF;
END $$;
