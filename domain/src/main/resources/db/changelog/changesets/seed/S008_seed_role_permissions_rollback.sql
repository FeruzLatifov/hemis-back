-- Rollback S008
DELETE FROM role_permissions WHERE permission_id IN (SELECT id FROM permissions WHERE code = 'roles.manage');
DELETE FROM permissions WHERE code = 'roles.manage';
