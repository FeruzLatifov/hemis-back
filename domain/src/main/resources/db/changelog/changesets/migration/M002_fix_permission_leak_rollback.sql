-- =====================================================
-- M002 ROLLBACK: Restore original (buggy) permissions
-- =====================================================
-- WARNING: This rollback restores the BUGGY state with permission leak
-- Only use if absolutely necessary for compatibility testing
-- =====================================================

DO $$
DECLARE
    roles_exists BOOLEAN;
    perms_exists BOOLEAN;
    role_perms_exists BOOLEAN;
BEGIN
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'roles') INTO roles_exists;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'permissions') INTO perms_exists;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permissions') INTO role_perms_exists;

    IF NOT (roles_exists AND perms_exists AND role_perms_exists) THEN
        RAISE NOTICE 'M002 Rollback: Required tables do not exist, skipping';
        RETURN;
    END IF;

    -- Restore system.* permissions to VIEWER (NOT RECOMMENDED)
    INSERT INTO role_permissions (role_id, permission_id, assigned_by)
    SELECT r.id, p.id, 'system-rollback'
    FROM roles r CROSS JOIN permissions p
    WHERE r.code = 'VIEWER' AND p.code LIKE 'system.%'
    ON CONFLICT DO NOTHING;

    -- Restore system.* permissions to REPORT_VIEWER (NOT RECOMMENDED)
    INSERT INTO role_permissions (role_id, permission_id, assigned_by)
    SELECT r.id, p.id, 'system-rollback'
    FROM roles r CROSS JOIN permissions p
    WHERE r.code = 'REPORT_VIEWER' AND p.code LIKE 'system.%'
    ON CONFLICT DO NOTHING;

    -- Restore system.translation.manage to UNIVERSITY_ADMIN
    INSERT INTO role_permissions (role_id, permission_id, assigned_by)
    SELECT r.id, p.id, 'system-rollback'
    FROM roles r CROSS JOIN permissions p
    WHERE r.code = 'UNIVERSITY_ADMIN' AND p.code = 'system.translation.manage'
    ON CONFLICT DO NOTHING;

    RAISE NOTICE 'M002 Rollback: Restored buggy permission assignments';
END $$;
