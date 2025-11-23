-- Rollback S004: REMOVE ROLE-PERMISSION MAPPINGS
DELETE FROM role_permissions WHERE assigned_by = 'system';
