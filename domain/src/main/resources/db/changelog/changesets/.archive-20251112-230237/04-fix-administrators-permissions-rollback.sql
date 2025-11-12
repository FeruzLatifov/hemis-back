-- =====================================================
-- ROLLBACK: Fix ADMINISTRATORS Role Permissions
-- =====================================================

-- Remove all permissions from ADMINISTRATORS and SUPER_ADMIN
DELETE FROM role_permissions
WHERE role_id IN (
    SELECT id FROM roles WHERE code IN ('ADMINISTRATORS', 'SUPER_ADMIN')
);

RAISE NOTICE 'Rolled back V4: ADMINISTRATORS and SUPER_ADMIN permissions removed';
