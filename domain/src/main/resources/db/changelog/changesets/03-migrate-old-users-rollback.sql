-- =====================================================
-- V3 ROLLBACK: Remove Migrated Old Users
-- =====================================================

-- Delete migrated user-role mappings
DELETE FROM user_roles 
WHERE user_id IN (
    SELECT id FROM users 
    WHERE id IN (SELECT id FROM sec_user WHERE delete_ts IS NULL)
);

-- Delete migrated users
DELETE FROM users 
WHERE id IN (SELECT id FROM sec_user WHERE delete_ts IS NULL);

-- Delete migrated role-permission mappings
DELETE FROM role_permissions
WHERE role_id IN (SELECT id FROM sec_role WHERE delete_ts IS NULL);

-- Delete migrated roles
DELETE FROM roles
WHERE id IN (SELECT id FROM sec_role WHERE delete_ts IS NULL);
